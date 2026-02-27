package com.digitale;

import com.digitale.comandos.*;
import com.digitale.item.SapotamaInputFilter;
import com.digitale.ui.DigiBatallaHudManager;
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

import java.util.logging.Level;

public class DigiTale extends JavaPlugin {

    private PacketFilter sapotamaFilter;

    public DigiTale(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();

        // ── Ítem Sapotama — interceptar tecla F en mano ────────────
        sapotamaFilter = PacketAdapters.registerInbound(new SapotamaInputFilter());

        // ── Eventos de jugador ─────────────────────────────────────
        registrarEventosJugador();

        // ── Comandos UI (abren menús gráficos) ─────────────────────
        this.getCommandRegistry().registerCommand(
            new DigiMenuComando("digi_menu",
                "Abre el menu principal de DigiTale"));

        // ── Comandos de texto (backup sin UI) ─────────────────────
        this.getCommandRegistry().registerCommand(
            new DigiStartComando("digi_start",
                "Elige companeros: /digi_start <esp1> <esp2> <nom1> [nom2]"));

        this.getCommandRegistry().registerCommand(
            new DigiStatusComando("digi_status",
                "Ver estado del equipo"));

        this.getCommandRegistry().registerCommand(
            new DigiEntrenarComando("digi_entrenar",
                "Entrenar: /digi_entrenar <a|b|ambos> <atk|def|spd|wis|hp> [sesiones]"));

        this.getCommandRegistry().registerCommand(
            new DigiBatallaComando("digi_batalla",
                "Combate legacy por texto"));

        this.getCommandRegistry().registerCommand(
            new DigiCuidarComando("digi_cuidar",
                "Cuidar: /digi_cuidar <a|b|ambos> [alimentar|mimar|descansar]"));

        this.getCommandRegistry().registerCommand(
            new DigiEvolucionarComando("digi_evolucionar",
                "Evolucionar: /digi_evolucionar [a|b] [forma]"));

        getLogger().at(Level.INFO).log("DigiTale cargado correctamente!");
    }

    /**
     * Registra los eventos de conexión y desconexión de jugadores.
     *
     * - PlayerReadyEvent: jugador completamente cargado en el mundo.
     *   Su getPlayerRef() devuelve Ref<EntityStore> — hay que extraer PlayerRef del store.
     *
     * - PlayerDisconnectEvent: jugador se desconecta.
     *   Su getPlayerRef() devuelve PlayerRef directamente.
     */
    private void registrarEventosJugador() {

        // Al entrar un jugador: podríamos inicializar datos aquí si fuera necesario
        getEventRegistry().registerGlobal(
            PlayerReadyEvent.class,
            event -> {
                Ref<EntityStore> playerStoreRef = event.getPlayerRef();
                Store<EntityStore> store = playerStoreRef.getStore();
                PlayerRef playerRef = store.getComponent(
                    playerStoreRef, PlayerRef.getComponentType());
                if (playerRef != null) {
                    getLogger().at(Level.INFO).log(
                        "DigiTale: jugador listo → " + playerRef.getUsername());
                }
            }
        );

        // Al salir un jugador: limpiar HUD de batalla si lo tenía activo
        getEventRegistry().registerGlobal(
            PlayerDisconnectEvent.class,
            event -> {
                // PlayerDisconnectEvent.getPlayerRef() devuelve PlayerRef directamente
                PlayerRef playerRef = event.getPlayerRef();
                if (playerRef != null) {
                    DigiBatallaHudManager.onPlayerDisconnect(playerRef);
                }
            }
        );

        getLogger().at(Level.INFO).log("Eventos Ready/Disconnect registrados.");
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        if (sapotamaFilter != null) {
            PacketAdapters.deregisterInbound(sapotamaFilter);
        }
        DigiBatallaHudManager.shutdown();
    }
}
