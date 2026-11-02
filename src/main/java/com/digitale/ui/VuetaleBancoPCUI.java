package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.digitale.sistema.SistemaPaseo;
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
 * PC Digital en Vuetale: misma logica que {@link DigiBancoPCUI} (equipo &lt;-&gt;
 * caja) pero con la pagina Cyber {@code BancoPcPage.vue}.
 */
public final class VuetaleBancoPCUI {

    private static final Logger LOGGER = Logger.getLogger(VuetaleBancoPCUI.class.getName());

    public static final String MODULE = "digitale";
    public static final String PAGE = "BancoPcPage";

    private static final int FILAS_BANCO = 8;

    private VuetaleBancoPCUI() {
    }

    /** @return true si abrio la pagina Vuetale; false para usar la nativa. */
    public static boolean abrir(@Nonnull PlayerRef playerRef,
                                @Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store) {
        DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null) return false;

        try {
            PlayerUi ui = VuetaleNavegacion.abrir(playerRef, ref, store, MODULE, PAGE);
            new Controlador(playerRef, ref, store, ui).iniciar();
            LOGGER.log(Level.INFO, "PC Vuetale abierto para " + playerRef.getUsername());
            return true;
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING,
                "No se pudo abrir la PC en Vuetale; se usara la nativa: " + t, t);
            return false;
        }
    }

    private static final class Controlador {

        private final PlayerRef playerRef;
        private final Ref<EntityStore> ref;
        private final Store<EntityStore> store;
        private final World world;
        private final PlayerUi ui;

        private String mensaje = "";

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

        private void onAction(String payload) {
            try {
                world.execute(() -> procesar(payload));
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "No se pudo despachar accion de PC Vuetale: " + t, t);
            }
        }

        private void procesar(String payload) {
            try {
                JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
                String action = json.has("action") ? json.get("action").getAsString() : "";
                int indice = parseIdx(json);

                DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
                if (datos == null) return;

                switch (action) {
                    case "depositar" -> depositar(datos, indice);
                    case "retirar" -> retirar(datos, indice);
                    case "cerrar" -> {
                        VuetaleNavegacion.cerrar(ref, store);
                        return;
                    }
                    default -> empujarTodo(datos);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error procesando accion de PC Vuetale: " + e.getMessage(), e);
            }
        }

        private void depositar(DatosJugador datos, int slot) {
            DatoDigimon sale = datos.getSlot(slot);
            if (sale == null) {
                empujarTodo(datos);
                return;
            }
            if (datos.banco.size() >= DatosJugador.BANCO_MAX) {
                mensaje = "Caja llena (" + DatosJugador.BANCO_MAX + "/" + DatosJugador.BANCO_MAX + ").";
                empujarTodo(datos);
                return;
            }
            if (datos.paseoEn(slot)) {
                SistemaPaseo.despawnearUno(store, datos.refsPaseo[slot]);
                datos.setRefPaseo(slot, null);
                datos.paseoActivo = datos.algunPaseo();
            }
            datos.setSlot(slot, null);
            if (datos.activoA == slot) datos.activoA = 0;
            if (datos.activoB == slot) datos.activoB = 1;
            sale.dormido = false;
            sale.dormidoEnSeg = 0;
            datos.banco.add(sale);
            if (datos.contarEquipo() == 0) datos.tieneEquipo = false;
            guardar(datos);
            mensaje = sale.nombre + " guardado en la Caja (" + datos.banco.size()
                + "/" + DatosJugador.BANCO_MAX + ").";
            empujarTodo(datos);
        }

        private void retirar(DatosJugador datos, int cual) {
            if (cual < 0 || cual >= datos.banco.size()) {
                empujarTodo(datos);
                return;
            }
            if (!datos.tieneEspacio()) {
                mensaje = "Equipo lleno (6/6).";
                empujarTodo(datos);
                return;
            }
            DatoDigimon b = datos.banco.remove(cual);
            int s = datos.agregarAlEquipo(b);
            if (s < 0) {
                datos.banco.add(cual, b);
                mensaje = "Equipo lleno (6/6).";
                empujarTodo(datos);
                return;
            }
            guardar(datos);
            mensaje = b.nombre + " al equipo (slot " + (s + 1) + ").";
            empujarTodo(datos);
        }

        private void guardar(DatosJugador datos) {
            try {
                PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
                if (progress != null) progress.guardarDesde(datos);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "No se pudo guardar PC: " + e.getMessage());
            }
        }

        private void empujarTodo() {
            DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
            empujarTodo(datos);
        }

        private void empujarTodo(DatosJugador datos) {
            if (datos == null) return;

            List<Map<String, Object>> equipo = new ArrayList<>(DatosJugador.MAX_EQUIPO);
            for (int i = 0; i < DatosJugador.MAX_EQUIPO; i++) {
                DatoDigimon digi = datos.getSlot(i);
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("indice", i);
                item.put("nombre", (i + 1) + ". " + (digi != null ? digi.nombre : "SLOT VACIO"));
                item.put("info", digi != null ? digi.especie + "  Nv" + digi.nivel : "SIN DATOS");
                item.put("imagen", digi != null ? retrato(digi) : "");
                item.put("ocupado", digi != null);
                equipo.add(item);
            }

            List<Map<String, Object>> banco = new ArrayList<>(FILAS_BANCO);
            for (int i = 0; i < FILAS_BANCO; i++) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("indice", i);
                if (i < datos.banco.size()) {
                    DatoDigimon b = datos.banco.get(i);
                    String extra = (i == FILAS_BANCO - 1 && datos.banco.size() > FILAS_BANCO)
                        ? "  (+" + (datos.banco.size() - FILAS_BANCO) + " mas)" : "";
                    item.put("nombre", (i + 1) + ". " + b.nombre);
                    item.put("info", b.especie + "  Nv" + b.nivel + extra);
                    item.put("imagen", retrato(b));
                    item.put("ocupado", true);
                } else {
                    item.put("nombre", (i + 1) + ". VACIO");
                    item.put("info", "");
                    item.put("imagen", "");
                    item.put("ocupado", false);
                }
                banco.add(item);
            }

            String m = mensaje;
            mensaje = "";

            ui.setData("equipo", equipo);
            ui.setData("banco", banco);
            ui.setData("conteo", "Equipo " + datos.contarEquipo() + "/6  |  Caja "
                + datos.banco.size() + "/" + DatosJugador.BANCO_MAX);
            ui.setData("mensaje", m);
        }

        private static String retrato(DatoDigimon digi) {
            try {
                String ruta = DigiImagenes.rutaPara(digi.especie);
                return ruta != null ? ruta : "";
            } catch (Exception e) {
                return "";
            }
        }

        private static int parseIdx(JsonObject json) {
            try {
                return json.has("value") && !json.get("value").isJsonNull()
                    ? json.get("value").getAsInt() : -1;
            } catch (Exception e) {
                return -1;
            }
        }
    }
}
