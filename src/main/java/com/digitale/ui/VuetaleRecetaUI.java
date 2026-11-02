package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.crianza.DigitamaDef;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import li.kelp.vuetale.app.PlayerUi;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Receta de incubacion en Vuetale (dato Incubacion). Sin fondo ni animacion.
 */
public final class VuetaleRecetaUI {

    private static final Logger LOGGER = Logger.getLogger(VuetaleRecetaUI.class.getName());

    public static final String MODULE = "digitale";
    public static final String PAGE = "RecetaPage";

    private VuetaleRecetaUI() {
    }

    public static boolean abrir(@Nonnull PlayerRef playerRef,
                                @Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull DigitamaDef def) {
        try {
            PlayerUi ui = VuetaleNavegacion.abrir(playerRef, ref, store, MODULE, PAGE);
            new Controlador(playerRef, ref, store, ui, def).iniciar();
            LOGGER.log(Level.INFO, "Receta Vuetale abierta para " + playerRef.getUsername());
            return true;
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING,
                "No se pudo abrir la receta en Vuetale; se usara la nativa: " + t, t);
            return false;
        }
    }

    private static final class Controlador {

        private final PlayerRef playerRef;
        private final Ref<EntityStore> ref;
        private final Store<EntityStore> store;
        private final World world;
        private final PlayerUi ui;
        private final DigitamaDef def;

        private String mensaje = "";

        Controlador(@Nonnull PlayerRef playerRef,
                    @Nonnull Ref<EntityStore> ref,
                    @Nonnull Store<EntityStore> store,
                    @Nonnull PlayerUi ui,
                    @Nonnull DigitamaDef def) {
            this.playerRef = playerRef;
            this.ref = ref;
            this.store = store;
            this.world = store.getExternalData().getWorld();
            this.ui = ui;
            this.def = def;
        }

        void iniciar() {
            ui.setData("onAction", (Consumer<String>) this::onAction);
            empujarTodo();
        }

        private void onAction(String payload) {
            try {
                world.execute(() -> procesar(payload));
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "No se pudo despachar accion de receta: " + t, t);
            }
        }

        private void procesar(String payload) {
            try {
                JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
                String action = json.has("action") ? json.get("action").getAsString() : "";

                if ("cerrar".equals(action)) {
                    VuetaleNavegacion.cerrar(ref, store);
                    return;
                }
                if (!"desbloquear".equals(action)) {
                    empujarTodo();
                    return;
                }

                DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
                if (datos == null) {
                    empujarTodo();
                    return;
                }

                boolean ok = false;
                String usado = "";
                for (String esp : def.pool()) {
                    if (gastarDatoDe(ref, store, esp)) {
                        ok = true;
                        usado = esp;
                        break;
                    }
                }
                if (!ok) {
                    mensaje = "Necesitas un Dato Incubacion de este huevo.";
                } else if (datos.desbloquearReceta(def.especie)) {
                    guardar(datos);
                    mensaje = "Receta de " + def.nombre + " desbloqueada! (gasto " + usado + ")";
                } else {
                    mensaje = "Ya conocias esta receta.";
                }
                empujarTodo(datos);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error procesando receta Vuetale: " + e.getMessage(), e);
            }
        }

        private void empujarTodo() {
            empujarTodo(AlmacenJugadores.getDatos(playerRef.getUuid()));
        }

        private void empujarTodo(DatosJugador datos) {
            List<String> ings = new ArrayList<>();
            if (def.ingredientes != null) {
                for (String ing : def.ingredientes) {
                    ings.add(ing);
                }
            }
            String m = mensaje;
            mensaje = "";

            ui.setData("huevo", def.nombre + " (" + def.idItem + ")");
            ui.setData("familia", def.familia + " | " + def.atributo + " | " + def.tier);
            ui.setData("ingredientes", ings);
            ui.setData("entorno", "Entorno: " + def.entorno);
            ui.setData("energia", "Energia " + def.energia + "/10 | Ciclos: " + def.ciclos);
            ui.setData("nace", "Nace: " + String.join(", ", def.pool()));
            ui.setData("mensaje", m);
        }

        private void guardar(DatosJugador datos) {
            try {
                PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
                if (progress != null) progress.guardarDesde(datos);
            } catch (Exception ignored) {
            }
        }

        /** Gasta 1 Dato_Incubacion_<bebe> de cualquier seccion. */
        private static boolean gastarDatoDe(Ref<EntityStore> ref,
                                            Store<EntityStore> store,
                                            String bebe) {
            String quiero = "Dato_Incubacion_" + bebe;
            try {
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
                        com.hypixel.hytale.server.core.inventory.ItemStack stack;
                        try {
                            stack = cont.getItemStack(i);
                        } catch (Exception e) {
                            continue;
                        }
                        if (stack == null || stack.getItemId() == null) continue;
                        String id = stack.getItemId();
                        String corto = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
                        if (!corto.equals(quiero)) continue;
                        try {
                            cont.removeItemStackFromSlot(i, 1);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            return false;
        }
    }
}
