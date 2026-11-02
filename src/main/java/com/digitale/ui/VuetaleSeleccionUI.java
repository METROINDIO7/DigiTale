package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Puente Vuetale para la eleccion del primer Digimon.
 *
 * <p>La pagina Vue vive en {@code src/ui/lib/pages/SeleccionPage.vue}. Si Vuetale
 * no esta disponible, el llamador usa {@link DigiStartMenuUI} como fallback.</p>
 */
public final class VuetaleSeleccionUI {

    private static final Logger LOGGER = Logger.getLogger(VuetaleSeleccionUI.class.getName());

    public static final String MODULE = "digitale";
    public static final String PAGE = "SeleccionPage";

    private static final String[][] STARTERS = {
        {"Botamon", "Neutral", "Una masa de datos compacta. Evoluciona hacia Koromon."},
        {"Kuramon", "Virus", "Datos inestables con un enorme potencial de evolucion."},
        {"Punimon", "Neutral", "Pequeno y adaptable. Su linea evolutiva es muy flexible."},
        {"Chibomon", "Data", "Energia pura en miniatura. Compania leal y resistente."},
        {"Poyomon", "Data", "Un Digimon acuatico con gran afinidad al agua."},
        {"Yuramon", "Data", "Semilla digital. Crecira con los cuidados de su companero."},
        {"Pichimon", "Data", "Pequeno pero veloz. Le encanta explorar la red."},
        {"Nyokimon", "Data", "Una semilla tranquila que florecera con constancia."},
    };

    private VuetaleSeleccionUI() {
    }

    public static boolean abrir(@Nonnull PlayerRef playerRef,
                                @Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store) {
        try {
            PlayerUi ui = VuetaleNavegacion.abrir(playerRef, ref, store, MODULE, PAGE);
            new Controlador(playerRef, ref, store, ui).iniciar();
            LOGGER.log(Level.INFO, "Seleccion Vuetale abierta para " + playerRef.getUsername());
            return true;
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING,
                "No se pudo abrir la seleccion en Vuetale; se usara la UI nativa: " + t, t);
            return false;
        }
    }

    private static final class Controlador {

        private final PlayerRef playerRef;
        private final Ref<EntityStore> ref;
        private final Store<EntityStore> store;
        private final World world;
        private final PlayerUi ui;

        private String seleccionada = "";

        Controlador(@Nonnull PlayerRef playerRef,
                    @Nonnull Ref<EntityStore> ref,
                    @Nonnull Store<EntityStore> store,
                    @Nonnull PlayerUi ui) {
            this.playerRef = playerRef;
            this.ref = ref;
            this.store = store;
            this.world = store.getExternalData().getWorld();
            this.ui = ui;
        }

        void iniciar() {
            ui.setData("onAction", (Consumer<String>) this::onAction);
            empujarTodo();
        }

        /** Se ejecuta en el hilo de V8: nunca tocar la API de Hytale aqui. */
        private void onAction(String payload) {
            try {
                world.execute(() -> procesar(payload));
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "No se pudo despachar la accion de Seleccion: " + t, t);
            }
        }

        /** Se ejecuta en el WorldThread. */
        private void procesar(String payload) {
            try {
                JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
                String action = texto(json, "action", "");
                String value = texto(json, "value", "");

                switch (action) {
                    case "seleccionar" -> {
                        if (esStarter(value)) {
                            seleccionada = value;
                        }
                    }
                    case "confirmar" -> confirmar();
                    case "cancelar", "cerrar" -> {
                        VuetaleNavegacion.cerrar(ref, store);
                        return;
                    }
                    default -> {
                        // accion desconocida: solo refrescar
                    }
                }

                empujarTodo();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error procesando accion de Seleccion: " + e.getMessage(), e);
            }
        }

        @SuppressWarnings("removal")
        private void confirmar() {
            String elegida = seleccionada.isEmpty() ? "Botamon" : seleccionada;
            AlmacenJugadores.DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
            if (datos.getSlot(0) == null) {
                DatoDigimon ini = DatoDigimon.crearInicial(elegida);
                ini.vivo = true;
                datos.setSlot(0, ini);
            }
            datos.activoA = 0;
            datos.activoB = 1;
            datos.tieneEquipo = true;

            try {
                PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
                if (progress != null) progress.guardarDesde(datos);
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "No se pudo guardar el inicial: " + e.getMessage());
            }

            SapotamaTabUI.open(playerRef, ref, store);
        }

        private void empujarTodo() {
            ui.setData("starters", construirStarters());
            ui.setData("seleccionada", seleccionada);
            ui.setData("mensaje", seleccionada.isEmpty()
                ? "SISTEMA ESPERANDO SELECCION"
                : seleccionada.toUpperCase() + " LISTO PARA ENLACE");
        }

        private static List<Map<String, Object>> construirStarters() {
            List<Map<String, Object>> out = new ArrayList<>(STARTERS.length);
            for (String[] starter : STARTERS) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", starter[0]);
                item.put("nombre", starter[0]);
                item.put("imagen", DigiImagenes.rutaPara(starter[0]));
                item.put("elemento", starter[1]);
                item.put("descripcion", starter[2]);
                out.add(item);
            }
            return out;
        }

        private static boolean esStarter(String nombre) {
            for (String[] starter : STARTERS) {
                if (starter[0].equals(nombre)) return true;
            }
            return false;
        }

        private static String texto(JsonObject json, String key, String def) {
            try {
                return json.has(key) && !json.get(key).isJsonNull()
                    ? json.get(key).getAsString()
                    : def;
            } catch (Exception e) {
                return def;
            }
        }
    }
}
