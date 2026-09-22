package com.digitale;

import com.digitale.comandos.*;
import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.item.DigiMountInputFilter;
import com.digitale.item.BattleInputFilter;
import com.digitale.item.DigiRecetaInteraction;
import com.digitale.ui.DigiMesaUI;
import com.digitale.item.SapotamaHitInteraction;
import com.digitale.item.SapotamaMenuInteraction;
import com.digitale.util.DigiMsg;
import com.digitale.sistema.MountMovementSupport;
import com.digitale.sistema.SistemaPaseo;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.entity.LivingEntityUseBlockEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DigiTale extends JavaPlugin {

    private PacketFilter sapotamaFilter;
    private PacketFilter battleFilter;
    private PacketFilter mountSupportFilter;
    private ComponentType<EntityStore, PetProgressComponent> petProgressType;

    // Cache de Ref<EntityStore> por UUID para usarlo al desconectar
    private static final ConcurrentHashMap<UUID, Ref<EntityStore>> storeRefCache = new ConcurrentHashMap<>();

    public DigiTale(@NonNullDecl JavaPluginInit init) { super(init); }

    @Override
    protected void setup() {
        super.setup();
        this.petProgressType = this.getEntityStoreRegistry().registerComponent(
                PetProgressComponent.class, "PetProgressComponent", PetProgressComponent.CODEC);
        ComponentRegistry.registerPetProgressType(this.petProgressType);
        // Interacción custom del Sapotama (clic derecho / F → abre /digi_menu).
        this.getCodecRegistry(Interaction.CODEC).register(
            "DigiTale_Sapotama_Menu", SapotamaMenuInteraction.class, SapotamaMenuInteraction.CODEC);
        // Golpe con el Sapotama (Primary + Selector → retos y combates salvajes).
        this.getCodecRegistry(Interaction.CODEC).register(
            "DigiTale_Sapotama_Hit", SapotamaHitInteraction.class, SapotamaHitInteraction.CODEC);
        // Examinar Dato Incubación (revela receta del bebé).
        this.getCodecRegistry(Interaction.CODEC).register(
            "DigiTale_Dato_Receta", DigiRecetaInteraction.class, DigiRecetaInteraction.CODEC);
        // NOTA: el filtro de paquetes del Sapotama queda desactivado; el menú lo abre
        // la interacción custom. El filtro se reutilizará para detectar golpes (PvP/salvajes).
        // sapotamaFilter = PacketAdapters.registerInbound(new SapotamaInputFilter());
        // Input del menú de combate por teclado (WASD/espacio/Q, congela al moverse).
        battleFilter = PacketAdapters.registerInbound(new BattleInputFilter());
        mountSupportFilter = PacketAdapters.registerInbound(new DigiMountInputFilter());
        MountMovementSupport.initializeRoleProfiles();
        registrarEventosJugador();
        // ===== Comandos de navegación por UI =====
        this.getCommandRegistry().registerCommand(new DigiMenuComando("digi_menu", "Abre el menu principal de DigiTale"));
        this.getCommandRegistry().registerCommand(new DigiSapotamaTabComando("digi_tab", "Abre el menu de tabs de Sapotama"));
        this.getCommandRegistry().registerCommand(new DigiStartComando("digi_start", "Elige companeros (comando rapido)"));
        this.getCommandRegistry().registerCommand(new DigiStatusComando("digi_status", "Ver estado del equipo (comando rapido)"));
        this.getCommandRegistry().registerCommand(new DigiMountModeComando("digi_mountmode", "Control de montura: default/auto/fly/swim"));

        // ===== Comando de reinicio =====
        this.getCommandRegistry().registerCommand(new DigiResetComando("digi_reset", "Reinicia tu equipo borrando todos los digimons"));

        // ===== Comandos admin =====
        this.getCommandRegistry().registerCommand(new com.digitale.comandos.admin.DigiResetProgressComando("digi_resetprogress", "Reset progreso (admin)"));
        this.getCommandRegistry().registerCommand(new com.digitale.comandos.admin.DigiSetProgressComando("digi_setprogress", "Set progreso (admin)"));
        this.getCommandRegistry().registerCommand(new com.digitale.comandos.admin.DigiIncubarComando("digi_incubar", "Eclosionar huevo (admin/test)"));
        getLogger().at(Level.INFO).log("DigiTale cargado correctamente!");
    }

    /** ¿Tiene el jugador un Sapotama en alguna sección del inventario? */
    private static boolean tieneSapotama(Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            // Secciones por ID numérico (los getters directos están deprecados).
            for (int seccion = 0; seccion < 32; seccion++) {
                com.hypixel.hytale.server.core.inventory.container.ItemContainer cont;
                try {
                    cont = com.hypixel.hytale.server.core.inventory.InventoryUtils
                        .getSectionById(ref, seccion, store);
                } catch (Exception e) {
                    continue;
                }
                if (cont == null) continue;
                short cap;
                try {
                    cap = cont.getCapacity();
                } catch (Exception e) {
                    continue;
                }
                for (short i = 0; i < cap; i++) {
                    try {
                        ItemStack stack = cont.getItemStack(i);
                        if (stack == null) continue;
                        String id = stack.getItemId();
                        if (id != null && (id.equals("Sapotama") || id.endsWith(":Sapotama"))) return true;
                    } catch (Exception ignored) {
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /** Anti-doble-apertura de la mesa (dos eventos pueden disparar a la vez). */
    private static final java.util.concurrent.ConcurrentHashMap<java.util.UUID, Long> MESA_OPEN =
        new java.util.concurrent.ConcurrentHashMap<>();

    private void abrirMesa(PlayerRef playerRef, Ref<EntityStore> eref, Store<EntityStore> store) {
        long ahora = System.currentTimeMillis();
        Long ultimo = MESA_OPEN.get(playerRef.getUuid());
        if (ultimo != null && ahora - ultimo < 1500L) return;
        MESA_OPEN.put(playerRef.getUuid(), ahora);
        try {
            store.getExternalData().getWorld().execute(() -> {
                try {
                    com.digitale.ui.DigiMesaUI.open(playerRef, eref, store);
                } catch (Exception e) {
                    getLogger().at(Level.WARNING).log("No se pudo abrir mesa: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            getLogger().at(Level.WARNING).log("abrirMesa: " + e.getMessage());
        }
    }

    @SuppressWarnings("removal") // LivingEntityUseBlockEvent deprecado pero funcional; UseBlockEvent es el camino futuro.
    private void registrarEventosJugador() {
        // AL ENTRAR: guardar storeRef en cache y restaurar datos a RAM
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
            Ref<EntityStore> storeRef = event.getPlayerRef();
            Store<EntityStore> store = storeRef.getStore();
            store.getExternalData().getWorld().execute(() -> {
                PlayerRef playerRef = store.getComponent(storeRef, PlayerRef.getComponentType());
                if (playerRef == null) return;
                UUID uuid = playerRef.getUuid();
                storeRefCache.put(uuid, storeRef);
                PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, storeRef);
                AlmacenJugadores.DatosJugador datos = AlmacenJugadores.obtener(uuid);
                progress.cargarEn(datos);
                // Sin menú de batalla colgado de otra sesión.
                datos.menuAbierto = false;
                // Sapotama gratis si aún no tiene equipo (primer ingreso o tras reset).
                if (!datos.tieneEquipo && !tieneSapotama(store, storeRef)) {
                    try {
                        Player.giveItem(new ItemStack("Sapotama", 1), storeRef, store);
                        playerRef.sendMessage(DigiMsg.parse("§eRecibiste un §fSapotama§e: usalo con clic derecho o tecla F."));
                        getLogger().at(Level.INFO).log("DigiTale [" + playerRef.getUsername() + "] sapotama entregado");
                    } catch (Exception e) {
                        getLogger().at(Level.WARNING).log("No se pudo dar sapotama: " + e.getMessage());
                    }
                }
                String msg = datos.tieneEquipo
                        ? "equipo restaurado (" + datos.contarEquipo() + "/6): A=" + (datos.getA() != null ? datos.getA().especie : "null")
                        + " B=" + (datos.getB() != null ? datos.getB().especie : "null")
                        : "sin equipo aun";
                getLogger().at(Level.INFO).log("DigiTale [" + playerRef.getUsername() + "] " + msg);
            });
        });

        // AL SALIR: despawnear NPCs si el paseo estaba activo, luego guardar datos
        getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, event -> {
            PlayerRef playerRef = event.getPlayerRef();
            if (playerRef == null) return;

            UUID uuid = playerRef.getUuid();
            Ref<EntityStore> storeRef = storeRefCache.remove(uuid);
            if (storeRef == null || !storeRef.isValid()) {
                getLogger().at(Level.WARNING).log("DigiTale [" + playerRef.getUsername() + "] storeRef no disponible al desconectar");
                return;
            }
            Store<EntityStore> store = storeRef.getStore();
            store.getExternalData().getWorld().execute(() -> {
                AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(uuid);
                if (datos == null) return;

                // Despawnear NPCs si el paseo estaba activo para evitar duplicados
                if (datos.paseoActivo) {
                    getLogger().at(Level.INFO).log("DigiTale [" + playerRef.getUsername() + "] desconexion con paseo activo - despawneando NPCs");
                    SistemaPaseo.recogerCompaneros(playerRef, storeRef, store);
                    datos.paseoActivo = false;
                }
                // Recoger formación de combate si quedó abierta.
                try {
                    com.digitale.sistema.SistemaPaseo.recogerBatalla(playerRef, storeRef, store);
                } catch (Exception e) {
                    // Sin datos de batalla, nada que hacer.
                }

                MountMovementSupport.clearPlayer(uuid);

                // Guardar datos al componente persistente
                PetProgressComponent progress = ComponentRegistry.getProgress(store, storeRef);
                if (progress == null) return;
                progress.guardarDesde(datos);
                getLogger().at(Level.INFO).log("DigiTale [" + playerRef.getUsername() + "] datos guardados al desconectar");
            });
        });

        getLogger().at(Level.INFO).log("Eventos Ready/Disconnect registrados.");

        // Mesa de Crianza: abrir nuestra UI al usar el bloque.
        // Se escuchan ambos eventos (ecs = API vigente, entity = deprecado pero fiable).
        getEventRegistry().registerGlobal(UseBlockEvent.class, event -> {
            try {
                var blockType = event.getBlockType();
                if (blockType == null || blockType.getId() == null) return;
                String id = blockType.getId();
                if (!id.equals("Mesa_De_Crianza") && !id.endsWith(":Mesa_De_Crianza")) return;
                var ctx = event.getContext();
                if (ctx == null) return;
                Ref<EntityStore> eref = ctx.getEntity();
                if (eref == null || !eref.isValid()) return;
                Store<EntityStore> store = eref.getStore();
                PlayerRef pref = store.getComponent(eref, PlayerRef.getComponentType());
                if (pref == null) return;
                abrirMesa(pref, eref, store);
            } catch (Exception e) {
                getLogger().at(Level.WARNING).log("UseBlock mesa: " + e.getMessage());
            }
        });
        // Mesa de Crianza: abrir nuestra UI al usar el bloque (evento concreto).
        getEventRegistry().registerGlobal(LivingEntityUseBlockEvent.class, event -> {
            try {
                String id = event.getBlockType();
                if (id == null || (!id.equals("Mesa_De_Crianza") && !id.endsWith(":Mesa_De_Crianza"))) return;
                Ref<EntityStore> eref = event.getRef();
                if (eref == null || !eref.isValid()) return;
                Store<EntityStore> store = eref.getStore();
                PlayerRef pref = store.getComponent(eref, PlayerRef.getComponentType());
                if (pref == null) return;
                abrirMesa(pref, eref, store);
            } catch (Exception e) {
                getLogger().at(Level.WARNING).log("UseBlock mesa: " + e.getMessage());
            }
        });
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        if (sapotamaFilter != null) PacketAdapters.deregisterInbound(sapotamaFilter);
        if (battleFilter != null) PacketAdapters.deregisterInbound(battleFilter);
        if (mountSupportFilter != null) PacketAdapters.deregisterInbound(mountSupportFilter);
        storeRefCache.clear();

    }
}