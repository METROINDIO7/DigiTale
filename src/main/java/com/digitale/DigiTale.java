package com.digitale;

import com.digitale.comandos.*;
import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.item.DigiMountInputFilter;
import com.digitale.item.BattleInputFilter;
import com.digitale.item.DigiMesaProcesaInteraction;
import com.digitale.item.DigiHuevoIncubandoInteraction;
import com.digitale.item.DigiPcBaseInteraction;
import com.digitale.item.DigiRecetaInteraction;
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

    private PacketFilter battleFilter;
    private PacketFilter mountSupportFilter;

    // Cache de Ref<EntityStore> por UUID para usarlo al desconectar
    private static final ConcurrentHashMap<UUID, Ref<EntityStore>> storeRefCache = new ConcurrentHashMap<>();

    public DigiTale(@NonNullDecl JavaPluginInit init) { super(init); }

    @Override
    protected void setup() {
        super.setup();
        ComponentType<EntityStore, PetProgressComponent> petProgressType =
            this.getEntityStoreRegistry().registerComponent(
                PetProgressComponent.class, "PetProgressComponent", PetProgressComponent.CODEC);
        ComponentRegistry.registerPetProgressType(petProgressType);
        // Interacción custom del Sapotama (clic derecho / F → abre /digi_menu).
        this.getCodecRegistry(Interaction.CODEC).register(
            "DigiTale_Sapotama_Menu", SapotamaMenuInteraction.class, SapotamaMenuInteraction.CODEC);
        // Golpe con el Sapotama (Primary + Selector → retos y combates salvajes).
        this.getCodecRegistry(Interaction.CODEC).register(
            "DigiTale_Sapotama_Hit", SapotamaHitInteraction.class, SapotamaHitInteraction.CODEC);
        // Examinar Dato Incubación (revela receta del bebé).
        this.getCodecRegistry(Interaction.CODEC).register(
            "DigiTale_Dato_Receta", DigiRecetaInteraction.class, DigiRecetaInteraction.CODEC);
        // Recetas de incubación editables (Server/Crianza/Recetas.json).
        try (java.io.InputStream in = getClass().getResourceAsStream("/Server/Crianza/Recetas.json")) {
            if (in != null) {
                int n = com.digitale.crianza.SistemaIncubacion.cargarDesdeJson(in);
                getLogger().at(Level.INFO).log("DigiTale recetas de incubacion: " + n);
            }
        } catch (Exception e) {
            getLogger().at(Level.WARNING).log("No se pudieron cargar recetas: " + e.getMessage());
        }
        // Procesar la Mesa de Crianza (bench nativo): combina inputs del bench.
        this.getCodecRegistry(Interaction.CODEC).register(
            "DigiTale_Mesa_Procesa", DigiMesaProcesaInteraction.class, DigiMesaProcesaInteraction.CODEC);
        // Usar Huevo Incubando (token): abre reclamo si hay crias listas.
        this.getCodecRegistry(Interaction.CODEC).register(
            "DigiTale_Huevo_Incubando", DigiHuevoIncubandoInteraction.class, DigiHuevoIncubandoInteraction.CODEC);
        // PC Digital (base): abre el banco.
        this.getCodecRegistry(Interaction.CODEC).register(
            "DigiTale_PC_Base", DigiPcBaseInteraction.class, DigiPcBaseInteraction.CODEC);
        // Bebe recien eclosionado (item) -> unir al equipo con clic.

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
        this.getCommandRegistry().registerCommand(new DigiGridTestComando("digi_gridtest", "TEST: ItemGrid con arrastre"));
        this.getCommandRegistry().registerCommand(new DigiCamComando("digi_cam", "TEST: camara fija [dist] [lock] [cursor]"));

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
            java.util.List<com.digitale.util.Inventarios.Hallazgo> hallados =
                com.digitale.util.Inventarios.buscar(ref, store,
                    (id, corto) -> corto.equals("Sapotama"));
            return !hallados.isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

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
                // Aviso de incubaciones listas (una sola vez, aunque la mesa esté cerrada).
                for (java.util.Map.Entry<String, AlmacenJugadores.DatosJugador.IncubacionMesa> en
                        : datos.incubaciones.entrySet()) {
                    AlmacenJugadores.DatosJugador.IncubacionMesa im = en.getValue();
                    if (im == null || im.notificado) continue;
                    if (!com.digitale.crianza.Incubacion.listo(im.inicioSeg, im.ciclos, im.segCiclo)) continue;
                    im.notificado = true;
                    try {
                        progress.guardarDesde(datos);
                    } catch (Exception ignored) {
                    }
                    try {
                        playerRef.sendMessage(DigiMsg.parse("§d> ¡Tu " + im.huevoId
                            + " de la mesa " + en.getKey() + " termino de incubar! Reclama tu cria."));
                    } catch (Exception ignored) {
                    }
                }
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

                // Soltar cámara de combate si quedó fija.
                try {
                    com.digitale.sistema.CamaraCombate.salir(playerRef);
                } catch (Exception ignored) {
                }

                // Guardar datos al componente persistente
                PetProgressComponent progress = ComponentRegistry.getProgress(store, storeRef);
                if (progress == null) return;
                progress.guardarDesde(datos);
                getLogger().at(Level.INFO).log("DigiTale [" + playerRef.getUsername() + "] datos guardados al desconectar");
            });
        });

        getLogger().at(Level.INFO).log("Eventos Ready/Disconnect registrados.");

    }

    @Override
    protected void shutdown() {
        super.shutdown();
        if (battleFilter != null) PacketAdapters.deregisterInbound(battleFilter);
        if (mountSupportFilter != null) PacketAdapters.deregisterInbound(mountSupportFilter);
        storeRefCache.clear();
    }
}