package com.digitale.ui;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Animaci&oacute;n CRT compartida para las pages nativas (PC y Mesa de Reclamo).
 *
 * <p>Los .ui incluyen los overlays fijos {@code #CrtNegro}, {@code #CrtLinea1..3}
 * y {@code #CrtFade1..4}. Solo se alterna su propiedad {@code Visible}, que el
 * cliente acepta sin problemas (a diferencia de {@code Anchor.Width}).</p>
 *
 * <p>Solo se reproduce al abrir; el cierre es inmediato para no dejar
 * overlays negros ni estados raros en Vuetale.</p>
 */
public final class AnimacionUI {

    private static final ScheduledExecutorService SCHED =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "digitale-animacion-ui");
            t.setDaemon(true);
            return t;
        });

    private AnimacionUI() {
    }

    /** Secuencia de encendido: negro -> linea que se expande -> fundido. */
    public static void encender(Consumer<UICommandBuilder> enviar,
                                Ref<EntityStore> ref,
                                Store<EntityStore> store) {
        World world = worldOf(store);
        if (world == null) return;

        step(world, 80, enviar, "CrtLinea1", "1");
        step(world, 160, enviar, "CrtLinea1", "0", "CrtLinea2", "1");
        step(world, 240, enviar, "CrtLinea2", "0", "CrtLinea3", "1");
        step(world, 300, enviar, "CrtLinea3", "0", "CrtNegro", "0", "CrtFade1", "1");
        step(world, 360, enviar, "CrtFade1", "0", "CrtFade2", "1");
        step(world, 420, enviar, "CrtFade2", "0", "CrtFade3", "1");
        step(world, 480, enviar, "CrtFade3", "0", "CrtFade4", "1");
        step(world, 540, enviar, "CrtFade4", "0", "CrtNegro", "0");
    }

    private static void step(World world,
                             long ms,
                             Consumer<UICommandBuilder> enviar,
                             String... idThenVisible) {
        UICommandBuilder cmd = new UICommandBuilder();
        for (int i = 0; i + 1 < idThenVisible.length; i += 2) {
            cmd.set("#" + idThenVisible[i] + ".Visible", "1".equals(idThenVisible[i + 1]));
        }
        try {
            SCHED.schedule(() -> {
                try {
                    world.execute(() -> {
                        try {
                            enviar.accept(cmd);
                        } catch (Exception ignored) {
                        }
                    });
                } catch (Exception ignored) {
                }
            }, ms, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
        }
    }

    private static World worldOf(Store<EntityStore> store) {
        try {
            return store != null ? store.getExternalData().getWorld() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
