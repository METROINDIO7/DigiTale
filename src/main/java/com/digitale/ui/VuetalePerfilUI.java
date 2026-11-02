package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EnemigoCombate;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Puente Vuetale para el perfil de un Digimon.
 *
 * <p>La pagina Vue vive en {@code src/ui/lib/pages/PerfilPage.vue}. Si Vuetale no
 * esta disponible, el llamador usa {@link DigiPerfilDigimonUI} como fallback.</p>
 */
public final class VuetalePerfilUI {

    private static final Logger LOGGER = Logger.getLogger(VuetalePerfilUI.class.getName());

    public static final String MODULE = "digitale";
    public static final String PAGE = "PerfilPage";

    /** especie actual -> lista de {destino, etapa, condiciones}. */
    private static final Map<String, List<String[]>> EVOLUCIONES = new HashMap<>();
    /** destino -> especie anterior. */
    private static final Map<String, String> REVERSA = new HashMap<>();

    static {
        evo("Botamon", "Koromon", "In-Training", "Lazo >= 30 o Victorias >= 1");
        evo("Punimon", "Tsunomon", "In-Training", "Lazo >= 30 o Victorias >= 1");
        evo("Poyomon", "Tokomon", "In-Training", "Lazo >= 30 o Victorias >= 1");
        evo("Yuramon", "Tanemon", "In-Training", "Lazo >= 30 o Victorias >= 1");
        evo("Pichimon", "Bukamon", "In-Training", "Lazo >= 30 o Victorias >= 1");
        evo("Nyokimon", "Yokomon", "In-Training", "Lazo >= 30 o Victorias >= 1");

        evo("Koromon", "Agumon", "Rookie", "Victorias >= 3, Lazo >= 40");
        evo("Tsunomon", "Gabumon", "Rookie", "Victorias >= 3, Def >= 12");
        evo("Tokomon", "Patamon", "Rookie", "Victorias >= 3, Wis >= 12");
        evo("Tanemon", "Palmon", "Rookie", "Victorias >= 3, Wis >= 11");
        evo("Bukamon", "Gomamon", "Rookie", "Victorias >= 3, Spd >= 14");
        evo("Yokomon", "Biyomon", "Rookie", "Victorias >= 3, Spd >= 15");

        evo("Agumon", "Greymon", "Champion", "Victorias >= 10, Lazo >= 50");
        evo("Gabumon", "GarurumonA", "Champion", "Victorias >= 10, Def >= 20");
        evo("Patamon", "Angemon", "Champion", "Victorias >= 10, Wis >= 20");
        evo("Palmon", "Togemon", "Champion", "Victorias >= 10, Def >= 18");
        evo("Gomamon", "Ikkakumon", "Champion", "Victorias >= 10, HP >= 160");
        evo("Biyomon", "Birdramon", "Champion", "Victorias >= 10, Spd >= 22");

        evo("Greymon", "MetalGreymon", "Ultimate", "Victorias >= 25, Atk >= 40, Lazo >= 70");
        evo("GarurumonA", "WereGarurumon", "Ultimate", "Victorias >= 25, Def >= 45, Disciplina >= 60");
        evo("Angemon", "MagnaAngemon", "Ultimate", "Victorias >= 25, Wis >= 45");
        evo("Togemon", "Lillymon", "Ultimate", "Victorias >= 25, Wis >= 40");
        evo("Ikkakumon", "Zudomon", "Ultimate", "Victorias >= 25, Def >= 50");
        evo("Birdramon", "Garudamon", "Ultimate", "Victorias >= 25, Spd >= 50");

        evo("MetalGreymon", "WarGreymon", "Mega", "Victorias >= 50, Atk >= 80, Lazo >= 90, Abi >= 20");
        evo("WereGarurumon", "MetalGarurumon", "Mega", "Victorias >= 50, Def >= 80, Abi >= 20");
        evo("MagnaAngemon", "Seraphimon", "Mega", "Victorias >= 50, Wis >= 90, Abi >= 20");
        evo("Lillymon", "Rosemon", "Mega", "Victorias >= 50, Wis >= 85, Lazo >= 85, Abi >= 20");
        evo("Zudomon", "MarineAngemon", "Mega", "Victorias >= 50, HP >= 400, Abi >= 20");
        evo("Garudamon", "Phoenixmon", "Mega", "Victorias >= 50, Spd >= 90, Abi >= 20");

        evo("WarGreymon", "Centaurusmon", "Mega", "Victorias >= 75, Atk >= 100, Lazo >= 100, Abi >= 30");
        evo("WereGarurumon", "Fujinmon", "Mega", "Victorias >= 75, Def >= 100, Abi >= 30");
        evo("MagnaAngemon", "OmniAngemon", "Mega", "Victorias >= 75, Wis >= 100, Abi >= 30");
        evo("Lillymon", "Rosezmemon", "Mega", "Victorias >= 75, Wis >= 100, Lazo >= 100, Abi >= 30");
        evo("Zudomon", "Magnamon", "Mega", "Victorias >= 75, HP >= 500, Abi >= 30");
        evo("Garudamon", "Saturnmon", "Mega", "Victorias >= 75, Spd >= 100, Abi >= 30");

        for (Map.Entry<String, List<String[]>> e : EVOLUCIONES.entrySet()) {
            for (String[] cadena : e.getValue()) {
                REVERSA.put(cadena[0], e.getKey());
            }
        }
    }

    private VuetalePerfilUI() {
    }

    private static void evo(String desde, String hacia, String etapa, String condiciones) {
        EVOLUCIONES.computeIfAbsent(desde, k -> new ArrayList<>())
            .add(new String[]{hacia, etapa, condiciones});
    }

    public static boolean abrir(@Nonnull PlayerRef playerRef,
                                @Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                int slot) {
        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null) return false;
        if (!AlmacenJugadores.DatosJugador.slotValido(slot)) slot = 0;

        try {
            PlayerUi ui = VuetaleNavegacion.abrir(playerRef, ref, store, MODULE, PAGE);
            new Controlador(playerRef, ref, store, ui, slot).iniciar();
            LOGGER.log(Level.INFO, "Perfil Vuetale abierto para " + playerRef.getUsername()
                + " slot " + slot);
            return true;
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING,
                "No se pudo abrir Perfil en Vuetale; se usara la UI nativa: " + t, t);
            return false;
        }
    }

    private static final class Controlador {

        private final PlayerRef playerRef;
        private final Ref<EntityStore> ref;
        private final Store<EntityStore> store;
        private final World world;
        private final PlayerUi ui;

        private final int slotActivo;
        private String pestana = "stats";
        private int movimientoSeleccionado = 0;
        private String aviso = "";

        Controlador(@Nonnull PlayerRef playerRef,
                    @Nonnull Ref<EntityStore> ref,
                    @Nonnull Store<EntityStore> store,
                    @Nonnull PlayerUi ui,
                    int slotActivo) {
            this.playerRef = playerRef;
            this.ref = ref;
            this.store = store;
            this.world = store.getExternalData().getWorld();
            this.ui = ui;
            this.slotActivo = slotActivo;
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
                LOGGER.log(Level.WARNING, "No se pudo despachar la accion de Perfil: " + t, t);
            }
        }

        /** Se ejecuta en el WorldThread. */
        private void procesar(String payload) {
            try {
                JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
                String action = texto(json, "action", "");
                String value = texto(json, "value", "");

                switch (action) {
                    case "tab" -> {
                        if (esPestana(value)) pestana = value;
                        aviso = "";
                    }
                    case "seleccionar_movimiento" -> {
                        movimientoSeleccionado = numero(json, "value", 0);
                        aviso = "";
                    }
                    case "evolucionar" -> {
                        aviso = "La evolucion se aplica automaticamente al cumplir las condiciones.";
                    }
                    case "de_evolucionar" -> {
                        aviso = "La de-evolucion devuelve a tu Digimon a una forma anterior.";
                    }
                    case "equipamiento" -> {
                        aviso = "El equipamiento del Digimon aun no esta disponible.";
                    }
                    case "volver", "ir_equipo" -> {
                        SapotamaTabUI.open(playerRef, ref, store);
                        return;
                    }
                    case "ir_crianza" -> {
                        DigiCuidarMenuUI.open(playerRef, ref, store);
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
                LOGGER.log(Level.WARNING, "Error procesando accion de Perfil: " + e.getMessage(), e);
            }
        }

        private void empujarTodo() {
            AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
            if (datos == null) return;

            DatoDigimon digimon = datos.getSlot(slotActivo);

            ui.setData("titulo", "PERFIL DEL DIGIMON");
            ui.setData("pestana", pestana);
            ui.setData("stats", construirStats(digimon));
            ui.setData("movimientos", construirMovimientos(digimon));
            ui.setData("movimientoSeleccionado", movimientoSeleccionado);
            ui.setData("evolucion", construirEvolucion(datos, digimon));
            ui.setData("deEvolucion", construirDeEvolucion(digimon));
            ui.setData("equipamiento", construirEquipamiento());
        }

        private static Map<String, Object> construirStats(DatoDigimon d) {
            Map<String, Object> out = new LinkedHashMap<>();
            if (d == null) {
                out.put("vacio", true);
                out.put("nombre", "-");
                out.put("especie", "-");
                out.put("nivel", "-");
                out.put("elemento", "-");
                out.put("atributo", "-");
                out.put("hp", "- / -");
                out.put("atk", "-");
                out.put("def", "-");
                out.put("spd", "-");
                out.put("wis", "-");
                out.put("lazo", "- / 100");
                out.put("disciplina", "-");
                out.put("victorias", "-");
                out.put("derrotas", "-");
                out.put("energia", "- / 100");
                out.put("hambre", "- / 100");
                out.put("fuerte", "-");
                out.put("debil", "-");
                out.put("imagen", "");
                out.put("iconoElemento", null);
                out.put("iconoAtributo", null);
                return out;
            }

            List<String> fuertes = EnemigoCombate.fuerteContra(d.elemento);
            List<String> debiles = EnemigoCombate.debilContra(d.elemento);

            out.put("vacio", false);
            out.put("nombre", d.nombre + (d.dormido ? " [Zzz]" : ""));
            out.put("especie", d.especie);
            out.put("nivel", d.nivel + " (" + d.nombreNivel() + ")");
            out.put("elemento", d.elemento);
            out.put("atributo", d.atributo);
            out.put("hp", d.hp + " / " + d.maxHp);
            out.put("atk", d.atk + " (+" + d.entAtk + ")");
            out.put("def", d.def + " (+" + d.entDef + ")");
            out.put("spd", d.spd + " (+" + d.entSpd + ")");
            out.put("wis", d.wis + " (+" + d.entWis + ")");
            out.put("lazo", d.lazo + " / 100");
            out.put("disciplina", String.valueOf(d.disciplina));
            out.put("victorias", String.valueOf(d.victorias));
            out.put("derrotas", String.valueOf(d.derrotas));
            out.put("energia", d.energia + " / 100");
            out.put("hambre", d.hambre + " / 100");
            out.put("fuerte", fuertes.isEmpty() ? "---" : String.join(", ", fuertes));
            out.put("debil", debiles.isEmpty() ? "---" : String.join(", ", debiles));
            out.put("imagen", DigiImagenes.rutaPara(d.especie));
            out.put("iconoElemento", DigiImagenes.iconoElemento(d.elemento));
            out.put("iconoAtributo", DigiImagenes.iconoAtributo(d.atributo));
            return out;
        }

        private static List<Map<String, Object>> construirMovimientos(DatoDigimon d) {
            List<Map<String, Object>> out = new ArrayList<>();
            if (d == null) return out;

            String[] nombres = d.nombresHabilidades();
            for (int i = 0; i < nombres.length; i++) {
                Map<String, Object> mov = new LinkedHashMap<>();
                mov.put("index", i + 1);
                mov.put("nombre", nombres[i]);
                mov.put("categoria", "TECNICA");
                mov.put("poder", d.danoHabilidad(i + 1));
                mov.put("golpes", 1);
                mov.put("critico", d.critico());
                mov.put("alcance", "Media");
                mov.put("coste", 0);
                mov.put("descripcion", "Tecnica de " + d.especie + ".");
                out.add(mov);
            }
            return out;
        }

        private Map<String, Object> construirEvolucion(
            @Nonnull AlmacenJugadores.DatosJugador datos,
            DatoDigimon d) {
            Map<String, Object> out = new LinkedHashMap<>();
            List<Map<String, Object>> opciones = new ArrayList<>();
            out.put("opciones", opciones);

            if (d == null) {
                out.put("actual", "Digimon actual: -");
                out.put("condiciones", "No hay Digimon seleccionado.");
                out.put("info", "");
                return out;
            }

            int bonoLazo = datos.bonoLazoCalumon();
            int bonoVic = datos.bonoVicCalumon();
            List<String[]> cadenas = EVOLUCIONES.get(d.especie);

            StringBuilder actual = new StringBuilder("Digimon actual: ")
                .append(d.nombre).append(" (").append(d.especie).append(')');
            if (bonoLazo > 0 || bonoVic > 0) {
                actual.append(" [Calumon +").append(bonoLazo)
                    .append("/+").append(bonoVic).append(']');
            }
            out.put("actual", actual.toString());

            if (cadenas == null || cadenas.isEmpty()) {
                out.put("condiciones", "Tu Digimon no puede evolucionar aun.");
                out.put("info", aviso);
                return out;
            }

            boolean algunaDisponible = false;
            for (String[] cadena : cadenas) {
                String destino = cadena[0];
                String etapa = cadena[1];
                String condiciones = cadena[2];
                boolean disponible = d.puedeEvolucionar(destino, bonoLazo, bonoVic);
                algunaDisponible |= disponible;

                Map<String, Object> opcion = new LinkedHashMap<>();
                opcion.put("destino", destino);
                opcion.put("etapa", etapa);
                opcion.put("disponible", disponible);
                opcion.put("condiciones", condiciones);
                opciones.add(opcion);
            }

            out.put("condiciones", algunaDisponible
                ? "Hay al menos una evolucion disponible."
                : "Aun no cumples los requisitos de ninguna evolucion.");
            out.put("info", aviso);
            return out;
        }

        private Map<String, Object> construirDeEvolucion(DatoDigimon d) {
            Map<String, Object> out = new LinkedHashMap<>();
            List<Map<String, Object>> opciones = new ArrayList<>();
            out.put("opciones", opciones);

            if (d == null) {
                out.put("actual", "Digimon actual: -");
                out.put("info", "");
                return out;
            }

            out.put("actual", "Digimon actual: " + d.nombre + " (" + d.especie + ")");

            String anterior = REVERSA.get(d.especie);
            if (d.nivel > 1 && anterior != null) {
                Map<String, Object> opcion = new LinkedHashMap<>();
                opcion.put("destino", anterior);
                opcion.put("disponible", true);
                opciones.add(opcion);
                out.put("info", aviso);
            } else {
                out.put("info", "No se puede de-evolucionar mas.");
            }
            return out;
        }

        private static Map<String, Object> construirEquipamiento() {
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("equipo", "Sin equipo");
            out.put("arma", "Sin arma");
            out.put("armadura", "Sin armadura");
            out.put("cosmetico1", "Aspecto Normal");
            out.put("cosmetico2", "Aspecto Especial");
            return out;
        }

        private static boolean esPestana(String v) {
            return "stats".equals(v) || "movimientos".equals(v) || "evolucion".equals(v)
                || "de_evolucion".equals(v) || "equipamiento".equals(v);
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
