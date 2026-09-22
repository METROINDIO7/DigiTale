package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.crianza.DigitamaDef;
import com.digitale.crianza.SistemaIncubacion;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mesa de Crianza: elige un huevo de tu mochila, ve su receta,
 * incúbalo (consume 1) y eclosiónalo cuando cumpla sus ciclos.
 * 1 ciclo = 60 s reales (persiste al salir).
 */
public class DigiMesaUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(DigiMesaUI.class.getName());
    public static final int CICLO_SEG = 60;

    private String huevoSel = "";
    private String msgPendiente = "";

    /** Un tipo de huevo en la mochila (para seleccionar y gastar). */
    public static class HuevoEntry {
        public String id;
        public String nombre;
        public int cantidad;
        public int seccion;
        public short slot;
    }

    public DigiMesaUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store) {
        LOGGER.info("DigiMesaUI.open()");
        DigiMesaUI page = new DigiMesaUI(playerRef);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPageManager().openCustomPage(ref, store, page);
        }
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder event,
                      @Nonnull Store<EntityStore> store) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            Group {
                LayoutMode: Top;
                Anchor: (Height: 560, Width: 560);

                Group {
                    LayoutMode: Top;
                    Background: (Color: #0b1220);
                    Padding: (Full: 15);

                    Label {
                        Text: "MESA DE CRIANZA";
                        Style: (TextColor: #ffcc00, FontSize: 18, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 26);
                    }
                    Label #LblIncuba {
                        Text: "";
                        Style: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center, Wrap: true);
                        Anchor: (Height: 36);
                    }
                    Group { Anchor: (Height: 2); Background: (Color: #ffcc00); }
                    Group { Anchor: (Height: 4); }
                    Label {
                        Text: "TUS HUEVOS";
                        Style: (TextColor: #00d4ff, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
            """);
        for (int i = 0; i < 6; i++) {
            sb.append("                    Group {\n");
            sb.append("                        LayoutMode: Left;\n");
            sb.append("                        Anchor: (Height: 32);\n");
            sb.append("                        Label #Huevo").append(i).append("Name {\n");
            sb.append("                            Text: \"\";\n");
            sb.append("                            Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);\n");
            sb.append("                            Anchor: (Width: 300, Height: 32);\n");
            sb.append("                        }\n");
            sb.append("                        Group { Anchor: (Width: 8); }\n");
            sb.append("                        TextButton #BtnHuevo").append(i).append(" {\n");
            sb.append("                            Text: \"Ver\";\n");
            sb.append("                            Anchor: (Width: 100, Height: 30);\n");
            sb.append("                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #ffcc00, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));\n");
            sb.append("                        }\n");
            sb.append("                    }\n");
        }
        sb.append("""
                    Group { Anchor: (Height: 4); }
                    Label #RecDetalle {
                        Text: "Elige un huevo para ver su receta.";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                        Anchor: (Height: 80);
                    }
                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 40);
                        TextButton #BtnIncubar {
                            Text: "Incubar";
                            Anchor: (Width: 150, Height: 38);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 13, TextColor: #ffcc00, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                        Group { Anchor: (Width: 8); }
                        TextButton #BtnEclosionar {
                            Text: "Eclosionar";
                            Anchor: (Width: 150, Height: 38);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 13, TextColor: #00ff88, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                        Group { Anchor: (Width: 8); }
                        TextButton #BtnCerrar {
                            Text: "Cerrar";
                            Anchor: (Width: 150, Height: 38);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)));
                        }
                    }
                    Label #LblMsg {
                        Text: "";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                        Anchor: (Height: 36);
                    }
                }
            }
            """);
        cmd.appendInline(null, sb.toString());

        List<HuevoEntry> huevos = listarHuevos(ref, store);
        for (int i = 0; i < huevos.size(); i++) {
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnHuevo" + i,
                EventData.of("action", "ver_huevo").append("value", String.valueOf(i)), false);
        }
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnIncubar",
            EventData.of("action", "incubar"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnEclosionar",
            EventData.of("action", "eclosionar"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCerrar",
            EventData.of("action", "cerrar"), false);

        actualizar(cmd, ref, store, huevos);
    }

    private void actualizar(UICommandBuilder cmd, Ref<EntityStore> ref,
                            Store<EntityStore> store, List<HuevoEntry> huevos) {
        DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        long ahora = System.currentTimeMillis() / 1000L;
        if (d != null && d.incubando()) {
            int need = d.incubaCiclos * CICLO_SEG;
            int queda = (int) (d.incubaInicioSeg + need - ahora);
            if (queda <= 0) {
                cmd.set("#LblIncuba.Text", "Incubando " + d.incubaHuevoId + ": ¡LISTO! Dale a Eclosionar.");
            } else {
                cmd.set("#LblIncuba.Text", "Incubando " + d.incubaHuevoId + ": listo en "
                    + (queda / 60) + "m " + (queda % 60) + "s.");
            }
        } else {
            cmd.set("#LblIncuba.Text", "Mesa libre. Elige un huevo e incuba (1 ciclo = 60s).");
        }
        for (int i = 0; i < 6; i++) {
            if (i < huevos.size()) {
                HuevoEntry h = huevos.get(i);
                cmd.set("#Huevo" + i + "Name.Visible", true);
                cmd.set("#BtnHuevo" + i + ".Visible", true);
                cmd.set("#Huevo" + i + "Name.Text", h.nombre + " x" + h.cantidad);
            } else {
                cmd.set("#Huevo" + i + "Name.Text", "");
                cmd.set("#Huevo" + i + "Name.Visible", false);
                cmd.set("#BtnHuevo" + i + ".Visible", false);
            }
        }
        if (!huevoSel.isEmpty()) {
            DigitamaDef def = DigitamaDef.porItem(huevoSel);
            if (def != null) {
                StringBuilder det = new StringBuilder();
                det.append(def.nombre).append(" -> ").append(def.especie).append(" (Nv").append(def.nivelRes).append(")\n");
                det.append(def.familia).append(" | ").append(def.atributo).append(" | ").append(def.tier).append("\n");
                det.append("Ing: ").append(String.join(", ", def.ingredientes)).append("\n");
                det.append(def.entorno).append(" | E").append(def.energia).append("/10 | ").append(def.ciclos).append(" ciclos");
                cmd.set("#RecDetalle.Text", det.toString());
            } else if ("Digitama_Desconocido".equals(huevoSel)) {
                cmd.set("#RecDetalle.Text", "Huevo Desconocido: fuel + entorno + energia + tiempo deciden (Burpmon/Calumon/NEO).");
            } else {
                cmd.set("#RecDetalle.Text", huevoSel);
            }
        }
        if (!msgPendiente.isEmpty()) {
            cmd.set("#LblMsg.Text", msgPendiente);
            msgPendiente = "";
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull String jsonData) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String action = json.has("action") ? json.get("action").getAsString() : "";
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null) return;

        switch (action) {
            case "ver_huevo" -> {
                int cual = parseIdx(json);
                List<HuevoEntry> huevos = listarHuevos(ref, store);
                if (cual >= 0 && cual < huevos.size()) {
                    huevoSel = huevos.get(cual).id;
                }
                rebuild();
            }
            case "incubar" -> {
                if (huevoSel.isEmpty()) {
                    msgPendiente = "Elige un huevo primero.";
                    rebuild();
                } else if (datos.incubando()) {
                    msgPendiente = "Ya hay una incubacion en curso.";
                    rebuild();
                } else if (!datos.tieneEspacio() && !"Digitama_Desconocido".equals(huevoSel)) {
                    // El espacio se exige al eclosionar; avisar temprano igual.
                    msgPendiente = "Te quedaras sin espacio al eclosionar (6/6).";
                    rebuild();
                } else {
                    HuevoEntry target = buscarHuevo(ref, store, huevoSel);
                    if (target == null) {
                        msgPendiente = "Ya no tienes ese huevo.";
                        huevoSel = "";
                        rebuild();
                    } else if (gastarHuevo(ref, store, target)) {
                        DigitamaDef def = DigitamaDef.porItem(huevoSel);
                        datos.incubaHuevoId = huevoSel;
                        datos.incubaInicioSeg = (int) (System.currentTimeMillis() / 1000L);
                        datos.incubaCiclos = (def != null) ? def.ciclos : 3;
                        datos.incubaEnergia = (def != null) ? def.energia : 5;
                        guardar(store, ref, datos);
                        msgPendiente = "Incubando " + huevoSel + " (" + datos.incubaCiclos + " ciclos). Vuelve luego.";
                        huevoSel = "";
                        rebuild();
                    } else {
                        msgPendiente = "No se pudo gastar el huevo.";
                        rebuild();
                    }
                }
            }
            case "eclosionar" -> {
                if (!datos.incubando()) {
                    msgPendiente = "No hay nada incubando.";
                    rebuild();
                    return;
                }
                long ahora = System.currentTimeMillis() / 1000L;
                int need = datos.incubaCiclos * CICLO_SEG;
                int queda = (int) (datos.incubaInicioSeg + need - ahora);
                if (queda > 0) {
                    msgPendiente = "Aun no esta listo: " + (queda / 60) + "m " + (queda % 60) + "s.";
                    rebuild();
                    return;
                }
                String huevoId = datos.incubaHuevoId;
                int slot;
                String quien;
                if ("Digitama_Desconocido".equals(huevoId)) {
                    SistemaIncubacion.ResultadoDesconocido res = SistemaIncubacion.resolverDesconocido(
                        SistemaIncubacion.FuelDesconocido.NADA,
                        SistemaIncubacion.EntornoDesconocido.NORMAL, 5, 3, false, false);
                    DatoDigimon bebe = DatoDigimon.crearInicial(res.especie);
                    slot = datos.agregarAlEquipo(bebe);
                    quien = res.especie;
                } else {
                    DigitamaDef def = DigitamaDef.porItem(huevoId);
                    if (def == null) {
                        msgPendiente = "Huevo corrupto (" + huevoId + "), incubacion cancelada.";
                        datos.limpiarIncubacion();
                        guardar(store, ref, datos);
                        rebuild();
                        return;
                    }
                    slot = SistemaIncubacion.eclosionar(datos, def);
                    quien = def.especie;
                }
                if (slot < 0) {
                    msgPendiente = "Equipo lleno (6/6). Haz espacio y vuelve.";
                    rebuild();
                    return;
                }
                datos.limpiarIncubacion();
                guardar(store, ref, datos);
                msgPendiente = "¡Eclosiono " + quien + "! (slot " + (slot + 1) + ")";
                rebuild();
            }
            case "cerrar" -> player.getPageManager().setPage(ref, store,
                com.hypixel.hytale.protocol.packets.interface_.Page.None);
            default -> {
                UICommandBuilder cmd = new UICommandBuilder();
                sendUpdate(cmd);
            }
        }
    }

    private static int parseIdx(JsonObject json) {
        try {
            return Integer.parseInt(json.has("value") ? json.get("value").getAsString() : "-1");
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Huevos Digitama_* distintos en la mochila (máx 6). */
    static List<HuevoEntry> listarHuevos(Ref<EntityStore> ref, Store<EntityStore> store) {
        List<HuevoEntry> out = new ArrayList<>();
        Map<String, HuevoEntry> vistos = new LinkedHashMap<>();
        try {
            for (int seccion = 0; seccion < 32 && vistos.size() < 6; seccion++) {
                com.hypixel.hytale.server.core.inventory.container.ItemContainer cont;
                try {
                    cont = com.hypixel.hytale.server.core.inventory.InventoryUtils.getSectionById(ref, seccion, store);
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
                for (short i = 0; i < cap && vistos.size() < 6; i++) {
                    com.hypixel.hytale.server.core.inventory.ItemStack stack;
                    try {
                        stack = cont.getItemStack(i);
                    } catch (Exception e) {
                        continue;
                    }
                    if (stack == null) continue;
                    String id;
                    try {
                        id = stack.getItemId();
                    } catch (Exception e) {
                        continue;
                    }
                    if (id == null) continue;
                    String corto = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
                    if (!corto.startsWith("Digitama_")) continue;
                    HuevoEntry he = vistos.get(corto);
                    if (he == null) {
                        he = new HuevoEntry();
                        he.id = corto;
                        he.nombre = corto.replace('_', ' ');
                        he.cantidad = 0;
                        he.seccion = seccion;
                        he.slot = i;
                        vistos.put(corto, he);
                        out.add(he);
                    }
                    try {
                        he.cantidad += Math.max(1, stack.getQuantity());
                    } catch (Exception ignored) {
                        he.cantidad += 1;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "listarHuevos: " + e.getMessage());
        }
        return out;
    }

    private static HuevoEntry buscarHuevo(Ref<EntityStore> ref, Store<EntityStore> store, String id) {
        for (HuevoEntry he : listarHuevos(ref, store)) {
            if (he.id.equals(id)) return he;
        }
        return null;
    }

    private static boolean gastarHuevo(Ref<EntityStore> ref, Store<EntityStore> store, HuevoEntry he) {
        try {
            com.hypixel.hytale.server.core.inventory.container.ItemContainer cont =
                com.hypixel.hytale.server.core.inventory.InventoryUtils.getSectionById(ref, he.seccion, store);
            if (cont == null) return false;
            // Revalidar que sigue siendo el mismo item.
            com.hypixel.hytale.server.core.inventory.ItemStack stack = cont.getItemStack(he.slot);
            if (stack == null || stack.getItemId() == null) return false;
            String corto = stack.getItemId().contains(":")
                ? stack.getItemId().substring(stack.getItemId().indexOf(':') + 1)
                : stack.getItemId();
            if (!corto.equals(he.id)) return false;
            cont.removeItemStackFromSlot(he.slot, 1);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "gastarHuevo: " + e.getMessage());
            return false;
        }
    }

    private void guardar(Store<EntityStore> store, Ref<EntityStore> ref, DatosJugador datos) {
        try {
            PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
            if (progress != null) progress.guardarDesde(datos);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo guardar incubacion: " + e.getMessage());
        }
    }
}
