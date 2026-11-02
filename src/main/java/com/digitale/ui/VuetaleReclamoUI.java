package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.crianza.DigitamaDef;
import com.digitale.crianza.SistemaIncubacion;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.digitale.util.Inventarios;
import com.digitale.util.DigiMsg;
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
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reclamo de crias / banco de incubacion en Vuetale. Sin fondo ni animacion.
 */
public final class VuetaleReclamoUI {

    private static final Logger LOGGER = Logger.getLogger(VuetaleReclamoUI.class.getName());

    public static final String MODULE = "digitale";
    public static final String PAGE = "ReclamoPage";

    private static final Random RNG = new Random();
    private static final int MAX_BANCO = 8;

    private VuetaleReclamoUI() {
    }

    public static boolean abrir(@Nonnull PlayerRef playerRef,
                                @Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                String tokenHuevo,
                                String tokenBebe,
                                boolean tokenVariante) {
        try {
            PlayerUi ui = VuetaleNavegacion.abrir(playerRef, ref, store, MODULE, PAGE);
            new Controlador(playerRef, ref, store, ui, tokenHuevo, tokenBebe, tokenVariante).iniciar();
            LOGGER.log(Level.INFO, "Reclamo Vuetale abierto para " + playerRef.getUsername());
            return true;
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING,
                "No se pudo abrir reclamo en Vuetale; se usara la nativa: " + t, t);
            return false;
        }
    }

    private static final class Controlador {

        private final PlayerRef playerRef;
        private final Ref<EntityStore> ref;
        private final Store<EntityStore> store;
        private final World world;
        private final PlayerUi ui;
        private final String tokenHuevo;
        private final String tokenBebe;
        private final boolean tokenVariante;

        private int tab = 0;
        private String mensaje = "";

        Controlador(@Nonnull PlayerRef playerRef,
                    @Nonnull Ref<EntityStore> ref,
                    @Nonnull Store<EntityStore> store,
                    @Nonnull PlayerUi ui,
                    String tokenHuevo,
                    String tokenBebe,
                    boolean tokenVariante) {
            this.playerRef = playerRef;
            this.ref = ref;
            this.store = store;
            this.world = store.getExternalData().getWorld();
            this.ui = ui;
            this.tokenHuevo = tokenHuevo;
            this.tokenBebe = tokenBebe;
            this.tokenVariante = tokenVariante;
        }

        void iniciar() {
            ui.setData("onAction", (Consumer<String>) this::onAction);
            empujarTodo();
        }

        private void onAction(String payload) {
            try {
                world.execute(() -> procesar(payload));
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "No se pudo despachar accion de reclamo: " + t, t);
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
                    case "tablistas" -> {
                        tab = 0;
                        empujarTodo(datos);
                    }
                    case "tabbanco" -> {
                        tab = 1;
                        empujarTodo(datos);
                    }
                    case "equipo", "banco" -> reclamar(datos, "banco".equals(action));
                    case "liberar" -> liberar(datos);
                    case "bequipo" -> aEquipo(datos, indice);
                    case "bliberar" -> liberarBanco(datos, indice);
                    case "cerrar" -> VuetaleNavegacion.cerrar(ref, store);
                    default -> empujarTodo(datos);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error procesando reclamo Vuetale: " + e.getMessage(), e);
            }
        }

        private void reclamar(DatosJugador datos, boolean aBanco) {
            if (tokenHuevo == null || tokenBebe == null) {
                mensaje = "Usa un Huevo Incubando para reclamar.";
                empujarTodo(datos);
                return;
            }
            if (!aBanco && !datos.tieneEspacio()) {
                mensaje = "Equipo lleno (6/6).";
                empujarTodo(datos);
                return;
            }
            if (aBanco && datos.banco.size() >= DatosJugador.BANCO_MAX) {
                mensaje = "Banco lleno (" + DatosJugador.BANCO_MAX + "/" + DatosJugador.BANCO_MAX + ").";
                empujarTodo(datos);
                return;
            }

            int pct = pctToken(datos);
            String quien = RNG.nextInt(100) < pct ? tokenBebe : "Burpmon";
            DatoDigimon bebe;
            try {
                bebe = DatoDigimon.crearInicial(quien);
            } catch (Exception e) {
                mensaje = "Reclamo fallido.";
                empujarTodo(datos);
                return;
            }
            datos.desbloquearReceta(tokenBebe);
            if ("Calumon".equals(quien)) {
                try {
                    bebe.noCombate = true;
                } catch (Exception ignored) {
                }
            }

            if (aBanco) {
                datos.banco.add(bebe);
                guardar(datos);
                consumirToken();
                playerRef.sendMessage(DigiMsg.parse(
                    "§b¡" + quien + " guardado en el Banco PC! (" + pct + "%)"));
                VuetaleNavegacion.cerrar(ref, store);
                return;
            }

            int slot = datos.agregarAlEquipo(bebe);
            if (slot < 0) {
                mensaje = "Equipo lleno (6/6).";
                empujarTodo(datos);
                return;
            }
            guardar(datos);
            consumirToken();
            playerRef.sendMessage(DigiMsg.parse(
                !quien.equals("Burpmon")
                    ? ("§a¡Eclosiono " + quien + "! (slot " + (slot + 1) + ")")
                    : ("§cLa mezcla fallo... Burpmon al equipo. (slot " + (slot + 1) + ")")));
            VuetaleNavegacion.cerrar(ref, store);
        }

        private void liberar(DatosJugador datos) {
            if (tokenHuevo == null) {
                empujarTodo(datos);
                return;
            }
            consumirToken();
            guardar(datos);
            playerRef.sendMessage(DigiMsg.parse("§7Cria liberada (el token se elimina)."));
            VuetaleNavegacion.cerrar(ref, store);
        }

        private void aEquipo(DatosJugador datos, int cual) {
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
            int slot = datos.agregarAlEquipo(b);
            if (slot < 0) {
                datos.banco.add(cual, b);
                mensaje = "Equipo lleno (6/6).";
                empujarTodo(datos);
                return;
            }
            guardar(datos);
            mensaje = b.nombre + " (" + b.especie + ") al equipo (slot " + (slot + 1) + ").";
            empujarTodo(datos);
        }

        private void liberarBanco(DatosJugador datos, int cual) {
            if (cual < 0 || cual >= datos.banco.size()) {
                empujarTodo(datos);
                return;
            }
            DatoDigimon b = datos.banco.remove(cual);
            guardar(datos);
            mensaje = b.nombre + " liberado del banco.";
            empujarTodo(datos);
        }

        private void empujarTodo() {
            empujarTodo(AlmacenJugadores.getDatos(playerRef.getUuid()));
        }

        private void empujarTodo(DatosJugador datos) {
            if (datos == null) return;

            boolean tieneToken = tokenHuevo != null && tokenBebe != null;
            int pct = pctToken(datos);
            String tokenTexto;
            if (tieneToken) {
                tokenTexto = "Huevo " + tokenHuevo.replace('_', ' ') + " -> " + tokenBebe
                    + " (" + pct + "%)" + (tokenVariante ? " variante (-25%)" : "")
                    + (pct >= 100 ? " receta exacta" : " [fallo = Burpmon]");
            } else {
                tokenTexto = "Usa un Huevo Incubando para reclamar. Banco disponible abajo.";
            }

            String imagen = "";
            if (tieneToken) {
                try {
                    String ruta = DigiImagenes.rutaPara(tokenBebe);
                    imagen = ruta != null ? ruta : "";
                } catch (Exception ignored) {
                }
            }

            List<Map<String, Object>> banco = new ArrayList<>(MAX_BANCO);
            for (int i = 0; i < MAX_BANCO; i++) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("indice", i);
                if (i < datos.banco.size()) {
                    DatoDigimon b = datos.banco.get(i);
                    String extra = (i == MAX_BANCO - 1 && datos.banco.size() > MAX_BANCO)
                        ? "  (+" + (datos.banco.size() - MAX_BANCO) + " mas)" : "";
                    item.put("nombre", b.nombre);
                    item.put("info", b.especie + "  Nv" + b.nivel + extra);
                    item.put("imagen", retrato(b));
                    item.put("ocupado", true);
                } else {
                    item.put("nombre", i == 0 && datos.banco.isEmpty() ? "BANCO VACIO" : "");
                    item.put("info", "");
                    item.put("imagen", "");
                    item.put("ocupado", false);
                }
                banco.add(item);
            }

            String m = mensaje;
            mensaje = "";

            ui.setData("tab", tab);
            ui.setData("tieneToken", tieneToken);
            ui.setData("tokenTexto", tokenTexto);
            ui.setData("imagen", imagen);
            ui.setData("banco", banco);
            ui.setData("conteo", "BANCO " + datos.banco.size() + "/" + DatosJugador.BANCO_MAX);
            ui.setData("mensaje", m);
        }

        private int pctToken(DatosJugador d) {
            if (tokenHuevo == null || tokenBebe == null) return 0;
            DigitamaDef def = SistemaIncubacion.porItem(tokenHuevo);
            boolean conocida = d != null && d.tieneReceta(tokenBebe);
            int pct;
            if (conocida) {
                pct = 100;
            } else {
                int base = 30;
                if (def != null) {
                    String t = def.tier != null ? def.tier.trim().toLowerCase(Locale.ROOT) : "";
                    if (t.startsWith("bas")) base = 60;
                    else if (t.startsWith("med")) base = 40;
                    else if (t.startsWith("avan")) base = 25;
                    else if (t.startsWith("legen")) base = 12;
                }
                pct = Math.min(95, Math.max(5, base));
            }
            if (tokenVariante) pct = Math.max(5, pct - 25);
            return pct;
        }

        private void guardar(DatosJugador datos) {
            try {
                PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
                if (progress != null) progress.guardarDesde(datos);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "No se pudo guardar reclamo: " + e.getMessage());
            }
        }

        private void consumirToken() {
            if (tokenHuevo == null || tokenBebe == null) return;
            try {
                String suf = tokenHuevo.startsWith("Digitama_")
                    ? tokenHuevo.substring("Digitama_".length()) : tokenHuevo;
                String objetivo = "Huevo_Incubando_" + suf + "_" + tokenBebe;
                List<Inventarios.Hallazgo> hallados = Inventarios.buscar(ref, store,
                    (id, corto) -> corto.equals(objetivo) || corto.startsWith(objetivo + "_"));
                if (hallados.isEmpty()) {
                    hallados = Inventarios.buscar(ref, store,
                        (id, corto) -> corto.startsWith("Huevo_Incubando_"));
                }
                if (hallados.isEmpty()) return;
                Inventarios.Hallazgo h = hallados.get(0);
                com.hypixel.hytale.server.core.inventory.container.ItemContainer cont =
                    Inventarios.contenedorDe(ref, store, h);
                if (cont != null) cont.removeItemStackFromSlot(h.slot, 1);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "No se pudo consumir token: " + e.getMessage());
            }
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
