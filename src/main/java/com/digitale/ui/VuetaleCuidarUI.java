package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.sistema.SistemaPaseo;
import com.digitale.util.Inventarios;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
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
 * Puente Vuetale para el menu de crianza.
 *
 * <p>La pagina Vue vive en {@code src/ui/lib/pages/CuidarPage.vue} y se compila a
 * {@code src/main/resources/vuetale/digitale/}. Si Vuetale no esta disponible, el
 * llamador usa {@link DigiCuidarMenuUI} como fallback.</p>
 */
public final class VuetaleCuidarUI {

    private static final Logger LOGGER = Logger.getLogger(VuetaleCuidarUI.class.getName());

    public static final String MODULE = "digitale";
    public static final String PAGE = "CuidarPage";

    private VuetaleCuidarUI() {
    }

    public static boolean abrir(@Nonnull PlayerRef playerRef,
                                @Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null) return false;

        try {
            PlayerUi ui = VuetaleNavegacion.abrir(playerRef, ref, store, MODULE, PAGE);
            new Controlador(playerRef, ref, store, ui).iniciar();
            LOGGER.log(Level.INFO, "Cuidar Vuetale abierto para " + playerRef.getUsername());
            return true;
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING,
                "No se pudo abrir Cuidar en Vuetale; se usara la UI nativa: " + t, t);
            return false;
        }
    }

    private static final class Controlador {

        private final PlayerRef playerRef;
        private final Ref<EntityStore> ref;
        private final Store<EntityStore> store;
        private final World world;
        private final PlayerUi ui;

        private int slotComida = -1;
        private String info = "-";

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
                LOGGER.log(Level.WARNING, "No se pudo despachar la accion de Cuidar: " + t, t);
            }
        }

        /** Se ejecuta en el WorldThread. */
        private void procesar(String payload) {
            try {
                JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
                String action = texto(json, "action", "");
                AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
                if (datos == null) return;

                int slot = numero(json, "value", 0);
                if (!AlmacenJugadores.DatosJugador.slotValido(slot)) slot = 0;
                int cual = numero(json, "value", -1);

                DatoDigimon digimon = datos.getSlot(slot);

                switch (action) {
                    case "alimentar" -> {
                        if (digimon == null) {
                            info = "Slot vacio.";
                        } else if (digimon.dormido) {
                            info = digimon.nombre + " duerme (Zzz). Despiertalo para alimentarlo.";
                        } else {
                            List<DigiCuidarMenuUI.ComidaEntry> comidas =
                                DigiCuidarMenuUI.listarComidas(ref, store);
                            if (comidas.isEmpty()) {
                                info = "Sin comida en la mochila para " + digimon.nombre + ".";
                            } else {
                                slotComida = slot;
                                info = "Elige comida para " + digimon.nombre + ".";
                            }
                        }
                    }
                    case "dar_comida" -> darComida(datos, cual);
                    case "comida_volver" -> {
                        slotComida = -1;
                        info = "-";
                    }
                    case "dormir" -> dormir(datos, digimon, slot);
                    case "jugar" -> jugar(datos, digimon, slot);
                    case "banco" -> banco(datos, slot);
                    case "volver", "ir_equipo" -> {
                        SapotamaTabUI.open(playerRef, ref, store);
                        return;
                    }
                    case "ir_perfil" -> {
                        int activo = AlmacenJugadores.DatosJugador.slotValido(datos.activoA)
                            ? datos.activoA : 0;
                        DigiPerfilDigimonUI.open(playerRef, ref, store, activo);
                        return;
                    }
                    case "ir_batalla" -> {
                        VuetaleSapotamaUI.abrirBatalla(playerRef, ref, store);
                        return;
                    }
                    case "cerrar" -> {
                        VuetaleNavegacion.cerrar(ref, store);
                        return;
                    }
                    default -> {
                        // accion desconocida: solo refrescar
                    }
                }

                empujarTodo();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error procesando accion de Cuidar: " + e.getMessage(), e);
            }
        }

        private void darComida(@Nonnull AlmacenJugadores.DatosJugador datos, int cual) {
            if (slotComida < 0) {
                info = "No se pudo dar de comer.";
                return;
            }

            List<DigiCuidarMenuUI.ComidaEntry> comidas =
                DigiCuidarMenuUI.listarComidas(ref, store);
            DatoDigimon hambriento = datos.getSlot(slotComida);
            if (hambriento == null || cual < 0 || cual >= comidas.size()) {
                info = "No se pudo dar de comer.";
                slotComida = -1;
                return;
            }

            DigiCuidarMenuUI.ComidaEntry ce = comidas.get(cual);
            boolean gastado = gastarComida(ce);
            double tamAntes = hambriento.crecimiento;

            if (hambriento.hambre >= 100) {
                hambriento.vecesLleno++;
            } else {
                hambriento.vecesLleno = 0;
            }

            if (hambriento.vecesLleno >= 3) {
                String antes = hambriento.nombre;
                DatoDigimon burp = DatoDigimon.crearInicial("Burpmon");
                burp.nombre = "Burpmon";
                datos.setSlot(slotComida, burp);
                guardar(datos);
                info = "¡" + antes + " comio demasiado y se volvio Burpmon! (reinicio)"
                    + (gastado ? "" : " [no se pudo gastar]");
            } else {
                hambriento.comer(ce.h, ce.e, ce.hpPct);
                guardar(datos);
                info = hambriento.nombre + " comio " + ce.nombre
                    + " (H+" + ce.h + " E+" + ce.e + ")" + (gastado ? "" : " [no se pudo gastar]");
                if ("Burpmon".equals(hambriento.especie) && tamAntes < 3.0 && hambriento.esGrande()) {
                    info += " ¡Supero 3x! Ahora es GRANDE (repasealo para verlo).";
                }
            }

            slotComida = -1;
        }

        private void dormir(@Nonnull AlmacenJugadores.DatosJugador datos,
                            DatoDigimon digimon,
                            int slot) {
            if (digimon == null) {
                info = "Slot vacio.";
                return;
            }

            if (digimon.dormido) {
                double frac = digimon.despertar();
                int pct = (int) Math.round(frac * 100);
                LOGGER.log(Level.INFO, slot + " despertado (" + pct + "%): hp="
                    + digimon.hp + ", hambre=" + digimon.hambre);
                info = digimon.nombre + " desperto (" + pct + "%): HP " + digimon.hp + "/"
                    + digimon.maxHp + " EN " + digimon.energia + " (5min=full).";
            } else if (!digimon.puedeDormir()) {
                info = digimon.nombre + " tiene hambre (H:" + digimon.hambre
                    + "). Alimentalo antes de dormir.";
                return;
            } else {
                digimon.dormir();
                LOGGER.log(Level.INFO, slot + " a dormir.");
                info = digimon.nombre + " a dormir (Zzz). La cura depende del tiempo (5 min = full).";
            }
            guardar(datos);
        }

        private void jugar(@Nonnull AlmacenJugadores.DatosJugador datos,
                           DatoDigimon digimon,
                           int slot) {
            if (digimon == null) {
                info = "Slot vacio.";
                return;
            }
            String msg = SistemaPaseo.alternarPaseo(playerRef, ref, store, slot);
            guardar(datos);
            info = msg != null
                ? msg + " (Lazo " + digimon.lazo + "/100)"
                : digimon.nombre + " (Lazo " + digimon.lazo + "/100)";
        }

        private void banco(@Nonnull AlmacenJugadores.DatosJugador datos, int slot) {
            DatoDigimon sale = datos.getSlot(slot);
            if (sale == null) {
                info = "Slot vacio.";
                return;
            }
            if (datos.banco.size() >= AlmacenJugadores.DatosJugador.BANCO_MAX) {
                info = "Banco lleno (100/100).";
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
            info = sale.nombre + " al Banco PC (" + datos.banco.size() + "/100).";
            LOGGER.log(Level.INFO, "Slot " + slot + " al banco (" + sale.nombre + ")");
        }

        private void guardar(@Nonnull AlmacenJugadores.DatosJugador datos) {
            try {
                PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
                if (progress != null) progress.guardarDesde(datos);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "No se pudo guardar progreso de crianza: " + e.getMessage());
            }
        }

        private void empujarTodo() {
            AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
            if (datos == null) return;

            ui.setData("titulo", "CRIANZA DE DIGIMON");
            ui.setData("slots", construirSlots(datos));
            ui.setData("comidas", construirComidas());
            ui.setData("slotComida", slotComida);
            ui.setData("comidaPara", construirComidaPara(datos));
            ui.setData("info", info);
        }

        private String construirComidaPara(@Nonnull AlmacenJugadores.DatosJugador datos) {
            if (slotComida < 0) return "";
            DatoDigimon digi = datos.getSlot(slotComida);
            return digi == null
                ? "Slot vacio"
                : "Para: " + digi.nombre + " (H:" + digi.hambre + " E:" + digi.energia + ")";
        }

        private List<Map<String, Object>> construirComidas() {
            List<Map<String, Object>> out = new ArrayList<>();
            if (slotComida < 0) return out;

            int i = 0;
            for (DigiCuidarMenuUI.ComidaEntry ce : DigiCuidarMenuUI.listarComidas(ref, store)) {
                Map<String, Object> comida = new LinkedHashMap<>();
                comida.put("index", i++);
                comida.put("nombre", ce.nombre);
                comida.put("cantidad", ce.cantidad);
                comida.put("h", ce.h);
                comida.put("e", ce.e);
                comida.put("hpPct", ce.hpPct);
                out.add(comida);
            }
            return out;
        }

        private static List<Map<String, Object>> construirSlots(
            @Nonnull AlmacenJugadores.DatosJugador datos) {
            List<Map<String, Object>> out =
                new ArrayList<>(AlmacenJugadores.DatosJugador.MAX_EQUIPO);
            for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
                DatoDigimon digi = datos.getSlot(i);
                boolean ocupado = digi != null;
                boolean esActivo = datos.esActivo(i);

                Map<String, Object> slot = new LinkedHashMap<>();
                slot.put("index", i);
                slot.put("ocupado", ocupado);
                slot.put("esActivo", esActivo);
                if (ocupado) {
                    String marca = esActivo ? (i == datos.activoA ? " [A]" : " [B]") : "";
                    if (digi.dormido) marca += " [Zzz]";
                    if (datos.paseoEn(i)) marca += " (paseando)";
                    slot.put("nombre", (i + 1) + ". " + digi.nombre + marca);
                    slot.put("info", "H:" + digi.hambre + " E:" + digi.energia
                        + " L:" + digi.lazo + " HP:" + digi.hp + "/" + digi.maxHp);
                    slot.put("imagen", DigiImagenes.rutaPara(digi.especie));
                    slot.put("dormido", digi.dormido);
                    slot.put("paseando", datos.paseoEn(i));
                } else {
                    slot.put("nombre", (i + 1) + ". -");
                    slot.put("info", "vacio");
                    slot.put("imagen", null);
                    slot.put("dormido", false);
                    slot.put("paseando", false);
                }
                out.add(slot);
            }
            return out;
        }

        /** Gasta 1 unidad de la comida indicada. Retorna false si no pudo. */
        private boolean gastarComida(@Nonnull DigiCuidarMenuUI.ComidaEntry ce) {
            try {
                Inventarios.Hallazgo h = new Inventarios.Hallazgo();
                h.seccion = ce.seccion;
                h.slot = ce.slot;
                h.id = ce.id;
                ItemContainer cont = Inventarios.contenedorDe(ref, store, h);
                if (cont == null) return false;
                cont.removeItemStackFromSlot(ce.slot, 1);
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "gastarComida: " + e.getMessage());
                return false;
            }
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

        private static int numero(JsonObject json, String key, int def) {
            try {
                return json.has(key) && !json.get(key).isJsonNull()
                    ? Integer.parseInt(json.get(key).getAsString())
                    : def;
            } catch (Exception e) {
                return def;
            }
        }
    }
}
