package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.sistema.SistemaPaseo;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class DigiCuidarMenuUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(DigiCuidarMenuUI.class.getName());

    /** Slot al que se va a dar de comer (-1 = modo tarjetas). */
    private int slotComida = -1;
    /** Mensaje pendiente para #LblInfo tras rebuild. */
    private String msgPendiente = "";

    /** Una comida distinta en la mochila. */
    public static class ComidaEntry {
        public int seccion;
        public short slot;
        public String id;
        public String nombre;
        public int cantidad;
        public int h;
        public int e;
        public int hpPct;
    }

    /** ¿Es comestible? (consumible o categoría food). */
    private static boolean esComida(com.hypixel.hytale.server.core.asset.type.item.config.Item item) {
        if (item == null) return false;
        try {
            if (item.isConsumable()) return true;
        } catch (Exception ignored) {
        }
        try {
            String[] cats = item.getCategories();
            if (cats != null) {
                for (String c : cats) {
                    if (c != null && c.toLowerCase(java.util.Locale.ROOT).contains("food")) return true;
                }
            }
            String sub = item.getSubCategory();
            if (sub != null && sub.toLowerCase(java.util.Locale.ROOT).contains("food")) return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Nutrición por id (H, E, %HP). Lo no listado: ración normal. */
    private static int[] valorComida(String id) {
        String s = id == null ? "" : id.toLowerCase(java.util.Locale.ROOT);
        if (s.contains("cook")) return new int[]{30, 15, 15};
        if (s.contains("berry") || s.contains("apple") || s.contains("melon") || s.contains("bread") || s.contains("cheese")) {
            return new int[]{20, 10, 10};
        }
        if (s.contains("raw") || s.contains("rotten") || s.contains("poop")) return new int[]{8, 4, 0};
        return new int[]{15, 8, 5};
    }

    private static String nombreBonito(String id) {
        if (id == null) return "?";
        String base = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        return base.replace('_', ' ');
    }

    /** Lista comidas distintas (máx 6) con su posición para gastar. */
    static java.util.List<ComidaEntry> listarComidas(Ref<EntityStore> ref, Store<EntityStore> store) {
        java.util.List<ComidaEntry> out = new java.util.ArrayList<>();
        java.util.Set<String> vistas = new java.util.HashSet<>();
        try {
            java.util.List<com.digitale.util.Inventarios.Hallazgo> hallados =
                com.digitale.util.Inventarios.recorrer(ref, store);
            // Agregar cantidades por id antes de filtrar por comida.
            java.util.Map<String, Integer> cant = new java.util.HashMap<>();
            java.util.Map<String, com.digitale.util.Inventarios.Hallazgo> primero = new java.util.HashMap<>();
            for (com.digitale.util.Inventarios.Hallazgo h : hallados) {
                if (h.id == null) continue;
                int c = 1;
                try {
                    c = Math.max(1, h.stack.getQuantity());
                } catch (Exception ignored) {
                }
                cant.merge(h.id, c, Integer::sum);
                primero.putIfAbsent(h.id, h);
            }
            for (java.util.Map.Entry<String, com.digitale.util.Inventarios.Hallazgo> en : primero.entrySet()) {
                if (out.size() >= 6) break;
                String id = en.getKey();
                com.digitale.util.Inventarios.Hallazgo h = en.getValue();
                boolean comida = false;
                try {
                    comida = esComida(h.stack.getItem());
                } catch (Exception e) {
                    continue;
                }
                if (!comida || vistas.contains(id)) continue;
                vistas.add(id);
                int[] v = valorComida(id);
                ComidaEntry ce = new ComidaEntry();
                ce.seccion = h.seccion;
                ce.slot = h.slot;
                ce.id = id;
                ce.nombre = nombreBonito(id);
                ce.cantidad = cant.getOrDefault(id, 1);
                ce.h = v[0];
                ce.e = v[1];
                ce.hpPct = v[2];
                out.add(ce);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "listarComidas: " + e.getMessage());
        }
        return out;
    }

    public DigiCuidarMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store) {
        LOGGER.log(Level.INFO, "DigiCuidarMenuUI.open()");

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null) {
            LOGGER.log(Level.INFO, "Sin datos - redirigiendo al menú principal");
            DigiMainMenuUI.open(playerRef, ref, store);
            return;
        }

        DigiCuidarMenuUI page = new DigiCuidarMenuUI(playerRef);
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
        if (slotComida >= 0) {
            cmd.appendInline(null, buildComidas());
            java.util.List<ComidaEntry> comidas = listarComidas(ref, store);
            for (int i = 0; i < comidas.size(); i++) {
                event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnDar" + i,
                    EventData.of("action", "dar_comida").append("value", String.valueOf(i)), false);
            }
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnComidaVolver",
                EventData.of("action", "comida_volver"), false);
            actualizarComidas(cmd, ref, store, comidas);
            return;
        }

        String ui = buildPanel();
        cmd.appendInline(null, ui);

        for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnAlim" + i,
                EventData.of("action", "alimentar").append("slot", String.valueOf(i)), false);
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnDorm" + i,
                EventData.of("action", "dormir").append("slot", String.valueOf(i)), false);
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnJugar" + i,
                EventData.of("action", "jugar").append("slot", String.valueOf(i)), false);
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnBanc" + i,
                EventData.of("action", "banco").append("slot", String.valueOf(i)), false);
        }
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnVolver",
            EventData.of("action", "volver"), false);

        actualizarEstado(cmd, store);
    }

    /** Panel de selección de comida para el slot elegido. */
    private String buildComidas() {
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
                        Text: "ELEGIR COMIDA";
                        Style: (TextColor: #ffcc00, FontSize: 18, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 26);
                    }
                    Label #LblComidaSlot {
                        Text: "";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Group { Anchor: (Height: 6); }

            """);
        for (int i = 0; i < 6; i++) {
            sb.append("                    Group {\n");
            sb.append("                        LayoutMode: Left;\n");
            sb.append("                        Anchor: (Height: 36);\n");
            sb.append("                        Label #Comida").append(i).append("Name {\n");
            sb.append("                            Text: \"\";\n");
            sb.append("                            Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);\n");
            sb.append("                            Anchor: (Width: 300, Height: 36);\n");
            sb.append("                        }\n");
            sb.append("                        Group { Anchor: (Width: 8); }\n");
            sb.append("                        TextButton #BtnDar").append(i).append(" {\n");
            sb.append("                            Text: \"Dar\";\n");
            sb.append("                            Anchor: (Width: 100, Height: 34);\n");
            sb.append("                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #ffcc00, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));\n");
            sb.append("                        }\n");
            sb.append("                    }\n");
        }
        sb.append("""
                    Group { Anchor: (Height: 8); }
                    TextButton #BtnComidaVolver {
                        Text: "Volver";
                        Anchor: (Width: 160, Height: 36);
                        Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)));
                    }
                }
            }
            """);
        return sb.toString();
    }

    private void actualizarComidas(UICommandBuilder cmd, Ref<EntityStore> ref,
                                   Store<EntityStore> store,
                                   java.util.List<ComidaEntry> comidas) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        DatoDigimon digi = (d != null) ? d.getSlot(slotComida) : null;
        cmd.set("#LblComidaSlot.Text", digi != null
            ? "Para: " + digi.nombre + " (H:" + digi.hambre + " E:" + digi.energia + ")"
            : "Slot vacio");
        for (int i = 0; i < 6; i++) {
            if (i < comidas.size()) {
                ComidaEntry ce = comidas.get(i);
                cmd.set("#Comida" + i + "Name.Visible", true);
                cmd.set("#BtnDar" + i + ".Visible", true);
                cmd.set("#Comida" + i + "Name.Text",
                    ce.nombre + " x" + ce.cantidad + "  (H+" + ce.h + " E+" + ce.e + ")");
            } else {
                cmd.set("#Comida" + i + "Name.Text", "");
                cmd.set("#Comida" + i + "Name.Visible", false);
                cmd.set("#BtnDar" + i + ".Visible", false);
            }
        }
    }

    private String buildPanel() {
        return """
            Group {
                LayoutMode: Top;
                Anchor: (Height: 560, Width: 560);

                Group {
                    LayoutMode: Top;
                    Background: (Color: #0b1220);
                    Padding: (Full: 15);

                    Label {
                        Text: "CRIANZA DE DIGIMON";
                        Style: (TextColor: #ffcc00, FontSize: 18, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 24);
                    }
                    TextButton #BtnVolver {
                        Text: "Volver";
                        Anchor: (Width: 80, Height: 32);
                        Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                    }
                    Group { Anchor: (Height: 6); }
                    Group { Anchor: (Height: 2); Background: (Color: #ffcc00); }
                    Group { Anchor: (Height: 4); }

                    Label {
                        Text: "CUUDA A TU EQUIPO (6)";
                        Style: (TextColor: #00d4ff, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Group { Anchor: (Height: 6); }

                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 82);

                        Group {
                            Anchor: (Width: 250);
                            Background: (Color: #141d33);
                            Padding: (Full: 8);
                            LayoutMode: Top;

                            Label #Cuidar0Name {
                                Text: "1. -";
                                Style: (TextColor: #e0e8f0, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 18);
                            }
                            Label #Cuidar0Info {
                                Text: "vacio";
                                Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Group { Anchor: (Height: 4); }
                            Group {
                                LayoutMode: Left;
                                Anchor: (Height: 28);

                                TextButton #BtnAlim0 {
                                    Text: "Alimentar";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnDorm0 {
                                    Text: "Dormir";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnJugar0 {
                                    Text: "Pasear";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnBanc0 {
                                    Text: "Banco";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }
                            }
                        }

                        Group { Anchor: (Width: 8); }

                        Group {
                            Anchor: (Width: 250);
                            Background: (Color: #141d33);
                            Padding: (Full: 8);
                            LayoutMode: Top;

                            Label #Cuidar1Name {
                                Text: "2. -";
                                Style: (TextColor: #e0e8f0, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 18);
                            }
                            Label #Cuidar1Info {
                                Text: "vacio";
                                Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Group { Anchor: (Height: 4); }
                            Group {
                                LayoutMode: Left;
                                Anchor: (Height: 28);

                                TextButton #BtnAlim1 {
                                    Text: "Alimentar";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnDorm1 {
                                    Text: "Dormir";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnJugar1 {
                                    Text: "Pasear";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnBanc1 {
                                    Text: "Banco";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }
                            }
                        }
                    }
                    Group { Anchor: (Height: 6); }
                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 82);

                        Group {
                            Anchor: (Width: 250);
                            Background: (Color: #141d33);
                            Padding: (Full: 8);
                            LayoutMode: Top;

                            Label #Cuidar2Name {
                                Text: "3. -";
                                Style: (TextColor: #e0e8f0, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 18);
                            }
                            Label #Cuidar2Info {
                                Text: "vacio";
                                Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Group { Anchor: (Height: 4); }
                            Group {
                                LayoutMode: Left;
                                Anchor: (Height: 28);

                                TextButton #BtnAlim2 {
                                    Text: "Alimentar";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnDorm2 {
                                    Text: "Dormir";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnJugar2 {
                                    Text: "Pasear";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnBanc2 {
                                    Text: "Banco";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }
                            }
                        }

                        Group { Anchor: (Width: 8); }

                        Group {
                            Anchor: (Width: 250);
                            Background: (Color: #141d33);
                            Padding: (Full: 8);
                            LayoutMode: Top;

                            Label #Cuidar3Name {
                                Text: "4. -";
                                Style: (TextColor: #e0e8f0, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 18);
                            }
                            Label #Cuidar3Info {
                                Text: "vacio";
                                Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Group { Anchor: (Height: 4); }
                            Group {
                                LayoutMode: Left;
                                Anchor: (Height: 28);

                                TextButton #BtnAlim3 {
                                    Text: "Alimentar";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnDorm3 {
                                    Text: "Dormir";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnJugar3 {
                                    Text: "Pasear";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnBanc3 {
                                    Text: "Banco";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }
                            }
                        }
                    }
                    Group { Anchor: (Height: 6); }
                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 82);

                        Group {
                            Anchor: (Width: 250);
                            Background: (Color: #141d33);
                            Padding: (Full: 8);
                            LayoutMode: Top;

                            Label #Cuidar4Name {
                                Text: "5. -";
                                Style: (TextColor: #e0e8f0, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 18);
                            }
                            Label #Cuidar4Info {
                                Text: "vacio";
                                Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Group { Anchor: (Height: 4); }
                            Group {
                                LayoutMode: Left;
                                Anchor: (Height: 28);

                                TextButton #BtnAlim4 {
                                    Text: "Alimentar";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnDorm4 {
                                    Text: "Dormir";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnJugar4 {
                                    Text: "Pasear";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnBanc4 {
                                    Text: "Banco";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }
                            }
                        }

                        Group { Anchor: (Width: 8); }

                        Group {
                            Anchor: (Width: 250);
                            Background: (Color: #141d33);
                            Padding: (Full: 8);
                            LayoutMode: Top;

                            Label #Cuidar5Name {
                                Text: "6. -";
                                Style: (TextColor: #e0e8f0, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 18);
                            }
                            Label #Cuidar5Info {
                                Text: "vacio";
                                Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Group { Anchor: (Height: 4); }
                            Group {
                                LayoutMode: Left;
                                Anchor: (Height: 28);

                                TextButton #BtnAlim5 {
                                    Text: "Alimentar";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnDorm5 {
                                    Text: "Dormir";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnJugar5 {
                                    Text: "Pasear";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 6); }

                                TextButton #BtnBanc5 {
                                    Text: "Banco";
                                    Anchor: (Width: 55, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }
                            }
                        }
                    }

                    Group { Anchor: (Height: 8); }
                    Group { Anchor: (Height: 2); Background: (Color: #ff00ff); }
                    Group { Anchor: (Height: 4); }

                    Label {
                        Text: "ULTIMA ACCION";
                        Style: (TextColor: #ff00ff, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Label #LblInfo {
                        Text: "-";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                        Anchor: (Height: 40);
                    }
                }
            }
            """;
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull String jsonData) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String action = json.has("action") ? json.get("action").getAsString() : "";
        LOGGER.log(Level.INFO, "DigiCuidarMenuUI - acción: " + action);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null) return;

        int slot = parseSlot(json);
        DatoDigimon digimon = datos.getSlot(slot);

        switch (action) {
            case "alimentar" -> {
                if (digimon != null && digimon.dormido) {
                    // Regla: dormido no come.
                    UICommandBuilder cmd = new UICommandBuilder();
                    cmd.set("#LblInfo.Text", digimon.nombre + " duerme (Zzz). Despiertalo para alimentarlo.");
                    actualizarEstado(cmd, store);
                    sendUpdate(cmd);
                } else if (digimon != null) {
                    java.util.List<ComidaEntry> comidas = listarComidas(ref, store);
                    if (comidas.isEmpty()) {
                        UICommandBuilder cmd = new UICommandBuilder();
                        cmd.set("#LblInfo.Text", "Sin comida en la mochila para " + digimon.nombre + ".");
                        actualizarEstado(cmd, store);
                        sendUpdate(cmd);
                    } else {
                        slotComida = slot;
                        rebuild();
                    }
                } else {
                    UICommandBuilder cmd = new UICommandBuilder();
                    actualizarEstado(cmd, store);
                    sendUpdate(cmd);
                }
            }
            case "dar_comida" -> {
                int cual = -1;
                try {
                    cual = Integer.parseInt(json.has("value") ? json.get("value").getAsString() : "-1");
                } catch (NumberFormatException ignored) {
                }
                java.util.List<ComidaEntry> comidas = listarComidas(ref, store);
                DatoDigimon hambriento = (datos != null) ? datos.getSlot(slotComida) : null;
                if (hambriento != null && cual >= 0 && cual < comidas.size()) {
                    ComidaEntry ce = comidas.get(cual);
                    boolean gastado = gastarComida(ref, store, ce);
                    double tamAntes = hambriento.crecimiento;
                    // Sobre-alimentar a tope 3 veces seguidas = Burpmon (reinicio).
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
                        guardarProgreso(ref, store, datos);
                        msgPendiente = "¡" + antes + " comio demasiado y se volvio Burpmon! (reinicio)"
                            + (gastado ? "" : " [no se pudo gastar]");
                    } else {
                        hambriento.comer(ce.h, ce.e, ce.hpPct);
                        guardarProgreso(ref, store, datos);
                        msgPendiente = hambriento.nombre + " comio " + ce.nombre
                            + " (H+" + ce.h + " E+" + ce.e + ")" + (gastado ? "" : " [no se pudo gastar]");
                        if ("Burpmon".equals(hambriento.especie) && tamAntes < 3.0 && hambriento.esGrande()) {
                            msgPendiente += " ¡Supero 3x! Ahora es GRANDE (repasealo para verlo).";
                        }
                    }
                } else {
                    msgPendiente = "No se pudo dar de comer.";
                }
                slotComida = -1;
                rebuild();
            }
            case "comida_volver" -> {
                slotComida = -1;
                rebuild();
            }
            case "dormir" -> {
                UICommandBuilder cmd = new UICommandBuilder();
                if (digimon != null) {
                    if (digimon.dormido) {
                        double frac = digimon.despertar();
                        int pct = (int) Math.round(frac * 100);
                        LOGGER.log(Level.INFO, slot + " despertado (" + pct + "%): hp=" + digimon.hp + ", hambre=" + digimon.hambre);
                        cmd.set("#LblInfo.Text", digimon.nombre + " desperto (" + pct + "%): HP " + digimon.hp + "/" + digimon.maxHp + " EN " + digimon.energia + " (5min=full).");
                    } else if (!digimon.puedeDormir()) {
                        cmd.set("#LblInfo.Text", digimon.nombre + " tiene hambre (H:" + digimon.hambre + "). Alimentalo antes de dormir.");
                    } else {
                        digimon.dormir();
                        LOGGER.log(Level.INFO, slot + " a dormir.");
                        cmd.set("#LblInfo.Text", digimon.nombre + " a dormir (Zzz). La cura depende del tiempo (5 min = full).");
                    }
                    guardarProgreso(ref, store, datos);
                }
                actualizarEstado(cmd, store);
                sendUpdate(cmd);
            }
            case "jugar" -> {
                UICommandBuilder cmd = new UICommandBuilder();
                if (digimon != null) {
                    // Jugar = sacarlo a pasear (toggle). Pasear juntos sube el lazo.
                    String msg = SistemaPaseo.alternarPaseo(playerRef, ref, store, slot);
                    guardarProgreso(ref, store, datos);
                    cmd.set("#LblInfo.Text", msg != null
                        ? msg + " (Lazo " + digimon.lazo + "/100)"
                        : digimon.nombre + " (Lazo " + digimon.lazo + "/100)");
                }
                actualizarEstado(cmd, store);
                sendUpdate(cmd);
            }
            case "volver" -> SapotamaTabUI.open(playerRef, ref, store);
            case "banco" -> {
                UICommandBuilder cmd = new UICommandBuilder();
                DatoDigimon sale = (datos != null) ? datos.getSlot(slot) : null;
                if (sale == null) {
                    cmd.set("#LblInfo.Text", "Slot vacio.");
                } else if (datos.banco.size() >= AlmacenJugadores.DatosJugador.BANCO_MAX) {
                    cmd.set("#LblInfo.Text", "Banco lleno (100/100).");
                } else {
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
                    guardarProgreso(ref, store, datos);
                    cmd.set("#LblInfo.Text", sale.nombre + " al Banco PC (" + datos.banco.size() + "/100).");
                    LOGGER.log(Level.INFO, "Slot " + slot + " al banco (" + sale.nombre + ")");
                }
                actualizarEstado(cmd, store);
                sendUpdate(cmd);
            }
            default -> {
                UICommandBuilder cmd = new UICommandBuilder();
                sendUpdate(cmd);
            }
        }
    }

    private static int parseSlot(JsonObject json) {
        try {
            int i = json.has("slot") ? Integer.parseInt(json.get("slot").getAsString()) : 0;
            return AlmacenJugadores.DatosJugador.slotValido(i) ? i : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void actualizarEstado(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        if (!msgPendiente.isEmpty()) {
            cmd.set("#LblInfo.Text", msgPendiente);
            msgPendiente = "";
        }
        for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
            DatoDigimon digi = d.getSlot(i);
            boolean ocupado = digi != null;
            if (ocupado) {
                String marca = d.esActivo(i) ? (i == d.activoA ? " [A]" : " [B]") : "";
                if (digi.dormido) marca += " [Zzz]";
                if (d.paseoEn(i)) marca += " (paseando)";
                cmd.set("#Cuidar" + i + "Name.Text", (i + 1) + ". " + digi.nombre + marca);
                cmd.set("#Cuidar" + i + "Info.Text",
                    "H:" + digi.hambre + " E:" + digi.energia + " L:" + digi.lazo + " HP:" + digi.hp + "/" + digi.maxHp);
                cmd.set("#BtnDorm" + i + ".Text", digi.dormido ? "Despert." : "Dormir");
            } else {
                cmd.set("#Cuidar" + i + "Name.Text", (i + 1) + ". -");
                cmd.set("#Cuidar" + i + "Info.Text", "vacio");
            }
            // Solo aparecen acciones en los slots cargados; dormido no come.
            cmd.set("#BtnAlim" + i + ".Visible", ocupado && !digi.dormido);
            cmd.set("#BtnDorm" + i + ".Visible", ocupado);
            cmd.set("#BtnJugar" + i + ".Visible", ocupado);
            cmd.set("#BtnBanc" + i + ".Visible", ocupado);
        }
    }

    /** Gasta 1 unidad de la comida indicada. Retorna false si no pudo. */
    private static boolean gastarComida(Ref<EntityStore> ref, Store<EntityStore> store, ComidaEntry ce) {
        try {
            com.digitale.util.Inventarios.Hallazgo h = new com.digitale.util.Inventarios.Hallazgo();
            h.seccion = ce.seccion;
            h.slot = ce.slot;
            h.id = ce.id;
            com.hypixel.hytale.server.core.inventory.container.ItemContainer cont =
                com.digitale.util.Inventarios.contenedorDe(ref, store, h);
            if (cont == null) return false;
            cont.removeItemStackFromSlot(ce.slot, 1);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "gastarComida: " + e.getMessage());
            return false;
        }
    }

    private void guardarProgreso(Ref<EntityStore> ref, Store<EntityStore> store,
                                 AlmacenJugadores.DatosJugador datos) {
        try {
            PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
            if (progress != null) progress.guardarDesde(datos);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo guardar progreso de crianza: " + e.getMessage());
        }
    }
}
