package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona el ciclo de vida del HUD de batalla para cada jugador.
 *
 * Patrón correcto (basado en GalleryHudManager de ScarForges):
 *  - Un Map<UUID, DigiBatallaHud> guarda la instancia por jugador
 *  - hudManager.setCustomHud() asigna el HUD (llama a build() internamente)
 *  - Para actualizar: hud.actualizarDatos() → update(false, builder)
 *  - Para ocultar:    hud.ocultar()         → update(true, builderVacio)
 *  - NUNCA setCustomHud(null) → crashea el cliente
 *
 * Uso desde DigiBatallaMenuUI:
 *   DigiBatallaHudManager.mostrar(playerRef, ref, store, datos)
 *   DigiBatallaHudManager.actualizar(playerRef, ref, store, datos)
 *   DigiBatallaHudManager.ocultar(playerRef, ref, store)
 */
public class DigiBatallaHudManager {

    // Un HUD por jugador (reutilizable entre combates)
    private static final Map<UUID, DigiBatallaHud> hudsActivos = new ConcurrentHashMap<>();

    // ── Mostrar HUD (inicio de combate) ─────────────────────────────
    /**
     * Asigna o reutiliza el HUD del jugador.
     * Si ya existe una instancia, la restablece (mostrar()).
     * Si no existe, crea una nueva y la asigna con setCustomHud().
     *
     * IMPORTANTE: debe ejecutarse en el WorldThread.
     */
    public static void mostrar(PlayerRef playerRef,
                                Ref<EntityStore> ref,
                                Store<EntityStore> store,
                                DatosJugador datos) {

        // Asegurar ejecución en el hilo del mundo
        if (!store.getExternalData().getWorld().isInThread()) {
            store.getExternalData().getWorld().execute(
                () -> mostrar(playerRef, ref, store, datos));
            return;
        }

        if (!ref.isValid()) return;
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        UUID uuid = playerRef.getUuid();
        DigiBatallaHud hud = hudsActivos.get(uuid);

        if (hud == null) {
            // Primera vez: crear nueva instancia y asignarla
            hud = new DigiBatallaHud(playerRef);
            hudsActivos.put(uuid, hud);
            // setCustomHud llama a hud.build() internamente → NO llamar build() manualmente
            player.getHudManager().setCustomHud(playerRef, hud);
        } else if (hud.estaOculto()) {
            // Ya existe pero estaba oculto → restaurar
            hud.mostrar();
        }

        // Enviar datos iniciales del combate
        hud.actualizarDatos(datos);
    }

    // ── Actualizar HUD (tras cada ronda) ────────────────────────────
    /**
     * Actualiza los valores del HUD sin reconstruirlo.
     * Llama a update(false, builder) internamente.
     */
    public static void actualizar(PlayerRef playerRef,
                                   Ref<EntityStore> ref,
                                   Store<EntityStore> store,
                                   DatosJugador datos) {

        UUID uuid = playerRef.getUuid();
        DigiBatallaHud hud = hudsActivos.get(uuid);
        if (hud == null || hud.estaOculto()) return;

        // actualizarDatos no necesita WorldThread porque
        // update() de CustomUIHud maneja el envío de paquetes
        hud.actualizarDatos(datos);
    }

    // ── Ocultar HUD (fin de combate o desconexión) ───────────────────
    /**
     * Oculta el HUD enviando un update total con builder vacío.
     * La instancia se conserva para reutilizarla en el próximo combate.
     */
    public static void ocultar(PlayerRef playerRef,
                                Ref<EntityStore> ref,
                                Store<EntityStore> store) {

        UUID uuid = playerRef.getUuid();
        DigiBatallaHud hud = hudsActivos.get(uuid);
        if (hud == null) return;
        hud.ocultar();
    }

    // ── Limpiar al desconectar jugador ───────────────────────────────
    /**
     * Llamar desde el evento PlayerDisconnect.
     * Oculta el HUD y elimina la instancia del mapa.
     */
    public static void onPlayerDisconnect(PlayerRef playerRef) {
        UUID uuid = playerRef.getUuid();
        DigiBatallaHud hud = hudsActivos.remove(uuid);
        if (hud != null && !hud.estaOculto()) {
            hud.ocultar();
        }
    }

    // ── Limpiar todo al apagar el plugin ─────────────────────────────
    public static void shutdown() {
        for (DigiBatallaHud hud : hudsActivos.values()) {
            try {
                if (!hud.estaOculto()) hud.ocultar();
            } catch (Exception ignored) { }
        }
        hudsActivos.clear();
    }
}
