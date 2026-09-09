package com.digitale;

import com.digitale.comandos.*;
import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.item.DigiMountInputFilter;
import com.digitale.item.SapotamaInputFilter;
import com.digitale.sistema.MountMovementSupport;
import com.digitale.sistema.SistemaPaseo;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
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
        sapotamaFilter = PacketAdapters.registerInbound(new SapotamaInputFilter());
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
        getLogger().at(Level.INFO).log("DigiTale cargado correctamente!");
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
                String msg = datos.tieneEquipo
                        ? "equipo restaurado: A=" + (datos.companeroA != null ? datos.companeroA.especie : "null")
                        + " B=" + (datos.companeroB != null ? datos.companeroB.especie : "null")
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

                MountMovementSupport.clearPlayer(uuid);

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
        if (sapotamaFilter != null) PacketAdapters.deregisterInbound(sapotamaFilter);
        if (mountSupportFilter != null) PacketAdapters.deregisterInbound(mountSupportFilter);
        storeRefCache.clear();

    }
}