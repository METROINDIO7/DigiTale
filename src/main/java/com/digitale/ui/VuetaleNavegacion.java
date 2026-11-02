package com.digitale.ui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import li.kelp.vuetale.app.PlayerUi;
import li.kelp.vuetale.app.PlayerUiManager;

import javax.annotation.Nonnull;

/**
 * Abre paginas de Vuetale reutilizando la app del jugador cuando ya hay una
 * pagina abierta.
 *
 * <p>{@link PlayerUiManager#openPage} crea una pagina nueva y desmonta la app
 * anterior, lo que borra sus datos y callbacks. Al navegar dentro de la misma
 * app con {@link PlayerUi#navigate(String)} se conservan ambos.</p>
 */
final class VuetaleNavegacion {

    private VuetaleNavegacion() {
    }

    @Nonnull
    static PlayerUi abrir(@Nonnull PlayerRef playerRef,
                          @Nonnull Ref<EntityStore> ref,
                          @Nonnull Store<EntityStore> store,
                          @Nonnull String modulo,
                          @Nonnull String pagina) {
        PlayerUi ui = PlayerUiManager.INSTANCE.get(playerRef.getUuid());
        if (ui != null && ui.getPage() != null) {
            try {
                ui.setData("animarEntrada", false);
            } catch (Exception ignored) {
            }
            ui.navigate("@" + modulo + "/pages/" + pagina + ".vue.js");
            return ui;
        }
        PlayerUi abierta = PlayerUiManager.INSTANCE.openPage(
            playerRef, ref, store, modulo, pagina, CustomPageLifetime.CanDismiss);
        try {
            abierta.setData("animarEntrada", true);
        } catch (Exception ignored) {
        }
        return abierta;
    }

    /** Cierra la page Vuetale de forma sincrona y deja que su onDismiss limpie la app. */
    static void cerrar(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().setPage(ref, store, Page.None);
            }
        } catch (Exception ignored) {
        }
    }
}
