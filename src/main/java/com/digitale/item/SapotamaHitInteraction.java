package com.digitale.item;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.sistema.SistemaBatalla;
import com.digitale.sistema.SistemaPaseo;
import com.digitale.ui.MenuBatalla;
import com.digitale.util.DigiMsg;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Golpe con el Sapotama (Primary + Selector). Se ejecuta por cada entidad
 * alcanzada y decide:
 * - NPC salvaje (server.npcRoles.*_Salvaje.name) → inicia combate por UI.
 * - Jugador con desafío abierto → registra el reto PvP.
 * - Lo demás se ignora.
 */
public class SapotamaHitInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<SapotamaHitInteraction> CODEC = BuilderCodec.builder(
        SapotamaHitInteraction.class, SapotamaHitInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    private static final Logger LOGGER = Logger.getLogger(SapotamaHitInteraction.class.getName());

    /** Anti-doble-disparo del sweep por jugador (ms). */
    private static final ConcurrentHashMap<UUID, Long> ULTIMO_GOLPE = new ConcurrentHashMap<>();
    private static final long VENTANA_MS = 1500L;

    public SapotamaHitInteraction() {
        super();
    }

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType,
                            @Nonnull InteractionContext interactionContext,
                            @Nonnull CooldownHandler cooldownHandler) {
        try {
            Ref<EntityStore> ref = interactionContext.getEntity();
            Ref<EntityStore> target = interactionContext.getTargetEntity();
            if (ref == null || target == null || target.equals(ref)) return;
            var buffer = interactionContext.getCommandBuffer();
            if (buffer == null) return;

            PlayerRef atacante = buffer.getComponent(ref, PlayerRef.getComponentType());
            if (atacante == null) return;

            long ahora = System.currentTimeMillis();
            Long ultimo = ULTIMO_GOLPE.get(atacante.getUuid());
            if (ultimo != null && ahora - ultimo < VENTANA_MS) return;
            ULTIMO_GOLPE.put(atacante.getUuid(), ahora);

            // ¿Jugador rival?
            PlayerRef rival = buffer.getComponent(target, PlayerRef.getComponentType());
            if (rival != null && !rival.getUuid().equals(atacante.getUuid())) {
                manejarGolpeJugador(atacante, rival);
                return;
            }

            // ¿Salvaje? (nombre por clave de traducción del rol)
            String especie = especieSalvaje(buffer, target);
            if (especie != null) {
                manejarGolpeSalvaje(atacante, especie, target);
            }
            // Lo demás (mobs vanilla, compañeros propios, etc.) se ignora.
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "SapotamaHit: " + e.getMessage());
        }
    }

    /** Extrae la especie si el objetivo es un salvaje nuestro. Null si no. */
    private static String especieSalvaje(com.hypixel.hytale.component.ComponentAccessor<EntityStore> accessor,
                                         Ref<EntityStore> target) {
        try {
            DisplayNameComponent nombre = accessor.getComponent(target, DisplayNameComponent.getComponentType());
            if (nombre == null || nombre.getDisplayName() == null) return null;
            String clave = nombre.getDisplayName().getMessageId();
            if (clave == null) return null;
            // Formato nuestro: server.npcRoles.<Especie>_Salvaje.name
            String pre = "server.npcRoles.";
            String suf = "_Salvaje.name";
            if (!clave.startsWith(pre) || !clave.endsWith(suf)) return null;
            String especie = clave.substring(pre.length(), clave.length() - suf.length());
            return especie.isEmpty() ? null : especie;
        } catch (Exception e) {
            return null;
        }
    }

    private static void manejarGolpeSalvaje(PlayerRef atacante, String especie,
                                            Ref<EntityStore> objetivo) {
        DatosJugador datos = AlmacenJugadores.obtener(atacante.getUuid());
        // Ignorar tus propios paseos/formación.
        for (Ref<?> r : datos.refsPaseo) {
            if (r != null && r.equals(objetivo)) return;
        }
        for (Ref<?> r : datos.refsBatalla) {
            if (r != null && r.equals(objetivo)) return;
        }
        if (!datos.tieneEquipo || !datos.puedeCombatir()) {
            atacante.sendMessage(DigiMsg.parse("§cNecesitas un Digimon despierto para combatir."));
            return;
        }
        if (datos.enCombateUI()) {
            // Ya en combate: reabrir la UI.
            abrirBatalla(atacante);
            return;
        }
        int nivelParty = 1;
        for (com.digitale.datos.DatoDigimon d : datos.equipoCombate()) {
            if (d != null && d.nivel > nivelParty) nivelParty = d.nivel;
        }
        var combate = SistemaBatalla.iniciarEncuentroSalvaje(datos, nivelParty, objetivo, especie);
        if (combate == null) {
            atacante.sendMessage(DigiMsg.parse("§cNo pudiste iniciar el combate."));
            return;
        }
        LOGGER.log(Level.INFO, "SapotamaHit: " + atacante.getUsername() + " vs salvaje (" + especie + ")");
        abrirBatalla(atacante);
    }

    private static void manejarGolpeJugador(PlayerRef atacante, PlayerRef rival) {
        DatosJugador datosA = AlmacenJugadores.obtener(atacante.getUuid());
        DatosJugador datosR = AlmacenJugadores.obtener(rival.getUuid());
        if (!datosA.tieneEquipo || !datosA.puedeCombatir()) {
            atacante.sendMessage(DigiMsg.parse("§cNecesitas un Digimon despierto para retar."));
            return;
        }
        if (!datosR.tieneEquipo) return;
        if (!datosR.desafioAbierto) {
            atacante.sendMessage(DigiMsg.parse("§7" + rival.getUsername() + " no tiene desafio abierto."));
            return;
        }
        // Reto mutuo: queda pendiente para el motor PvP.
        if (datosA.desafioDeUuid != null && datosA.desafioDeUuid.equals(rival.getUuid())) {
            atacante.sendMessage(DigiMsg.parse("§a¡Reto mutuo con " + rival.getUsername() + "! El combate PvP llega pronto."));
            rival.sendMessage(DigiMsg.parse("§a¡Reto mutuo con " + atacante.getUsername() + "! El combate PvP llega pronto."));
            return;
        }
        datosR.desafioDeUuid = atacante.getUuid();
        datosR.desafioDeNombre = atacante.getUsername();
        rival.sendMessage(DigiMsg.parse("§e¡" + atacante.getUsername() + " te ha retado (" + datosR.modoDesafio + ")! Golpealo con el sapotama para aceptar."));
        atacante.sendMessage(DigiMsg.parse("§aReto lanzado a " + rival.getUsername() + "."));
        LOGGER.log(Level.INFO, "SapotamaHit: reto " + atacante.getUsername() + " -> " + rival.getUsername());
    }

    private static void abrirBatalla(PlayerRef atacante) {
        try {
            Ref<EntityStore> entityRef = atacante.getReference();
            if (entityRef == null || !entityRef.isValid()) return;
            Store<EntityStore> store = entityRef.getStore();
            store.getExternalData().getWorld().execute(() -> {
                try {
                    // Puesta en escena + menú HUD por teclado (sin Page).
                    SistemaPaseo.prepararBatalla(atacante, entityRef, store);
                    MenuBatalla.abrir(atacante, entityRef, store);
                    com.digitale.sistema.EscenarioBatalla.iniciar(atacante, entityRef, store);
                    com.digitale.ui.DigiBatallaComandosUI.abrir(atacante, entityRef, store);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "No se pudo abrir batalla: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "abrirBatalla: " + e.getMessage());
        }
    }
}
