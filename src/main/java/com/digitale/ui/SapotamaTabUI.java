package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class SapotamaTabUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(SapotamaTabUI.class.getName());

    public SapotamaTabUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store) {
        LOGGER.log(Level.INFO, "SapotamaTabUI.open()");

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null || !datos.tieneEquipo) {
            LOGGER.log(Level.INFO, "Sin equipo - abriendo selección inicial");
            DigiStartMenuUI.open(playerRef, ref, store);
            return;
        }

        SapotamaTabUI page = new SapotamaTabUI(playerRef);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPageManager().openCustomPage(ref, store, page);
        }
    }

    private String pestanaActual = "equipo";

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder event,
                      @Nonnull Store<EntityStore> store) {
        // Shell + solo el panel activo (sin Visible: lo que no se adjunta no existe).
        cmd.appendInline(null, buildShell());
        cmd.appendInline("#TabContent", panelPara(pestanaActual));

        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabEquipo",
            EventData.of("action", "tab").append("value", "equipo"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabCrianza",
            EventData.of("action", "tab").append("value", "crianza"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabBatalla",
            EventData.of("action", "tab").append("value", "batalla"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCerrar",
            EventData.of("action", "cerrar"), false);

        // Eventos del contenido activo (existe porque se adjuntó arriba)
        switch (pestanaActual) {
            case "crianza" -> event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCrianza",
                EventData.of("action", "crianza"), false);
            case "batalla" -> {
                event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnDesafio",
                    EventData.of("action", "desafio"), false);
                event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnModo",
                    EventData.of("action", "modo"), false);
            }
            default -> {
                for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
                    event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnVer" + i,
                        EventData.of("action", "ver").append("value", String.valueOf(i)), false);
                    event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnActA" + i,
                        EventData.of("action", "acta").append("value", String.valueOf(i)), false);
                    event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnActB" + i,
                        EventData.of("action", "actb").append("value", String.valueOf(i)), false);
                }
            }
        }

        // Relleno inicial del tab activo (el framework envía el cmd de build)
        if ("crianza".equals(pestanaActual)) {
            actualizarCrianza(cmd, store);
        } else if ("batalla".equals(pestanaActual)) {
            actualizarBatalla(cmd, store);
        } else {
            pestanaActual = "equipo";
            actualizarEquipo(cmd, store);
        }
    }

    private String buildShell() {
        return """
            Group {
                LayoutMode: Top;
                Anchor: (Height: 520, Width: 560);

                Group {
                    LayoutMode: Top;
                    Background: (Color: #0b1220);
                    Padding: (Full: 15);

                    Label {
                        Text: "DIGIMON COMPANEROS";
                        Style: (TextColor: #00d4ff, FontSize: 20, HorizontalAlignment: Center);
                        Anchor: (Height: 26);
                    }
                    Label #LblTab {
                        Text: "Equipo";
                        Style: (TextColor: #8899aa, FontSize: 13, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 34);
                        TextButton #TabEquipo {
                            Text: "Equipo";
                            Anchor: (Width: 165, Height: 32);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center)));
                        }
                        Group { Anchor: (Width: 8); }
                        TextButton #TabCrianza {
                            Text: "Crianza";
                            Anchor: (Width: 165, Height: 32);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center)));
                        }
                        Group { Anchor: (Width: 8); }
                        TextButton #TabBatalla {
                            Text: "Batalla";
                            Anchor: (Width: 165, Height: 32);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center)));
                        }
                    }
                    Group { Anchor: (Height: 4); }
                    Group { Anchor: (Height: 2); Background: (Color: #00d4ff); }
                    Group { Anchor: (Height: 4); }

                    Group #TabContent {
                        LayoutMode: Top;
                        Background: (Color: #0f1729);
                        Padding: (Full: 10);
                    }
                    Group { Anchor: (Height: 8); }
                    TextButton #BtnCerrar {
                        Text: "Cerrar";
                        Anchor: (Width: 160, Height: 40);
                        Style: (Default: (Background: (Color: #3d1a1a), LabelStyle: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center)), Hovered: (Background: (Color: #5d2a2a), LabelStyle: (TextColor: #ffffff, FontSize: 14, HorizontalAlignment: Center)));
                    }
                }
            }
            """;
    }

    private String panelPara(String cual) {
        return switch (cual) {
            case "crianza" -> panelCrianza();
            case "batalla" -> panelBatalla();
            default -> panelEquipo();
        };
    }

    // Layout con tabs reales: 3 paneles (solo uno Visible a la vez) + grid de 6 slots.
    private String panelEquipo() {
        StringBuilder sb = new StringBuilder();
        String btnStyle = "(Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 12, HorizontalAlignment: Center)))";
        String btnAStyle = "(Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center)))";
        String btnBStyle = "(Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #ff00ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center)))";
        for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
            sb.append("Group {\n");
            sb.append("    LayoutMode: Left;\n");
            sb.append("    Anchor: (Height: 40);\n");
            sb.append("    AssetImage #SlotImg").append(i).append(" {\n");
            sb.append("        Anchor: (Width: 36, Height: 36);\n");
            sb.append("        AssetPath: \"UI/Custom/png_Digis/Botamon.png\";\n");
            sb.append("    }\n");
            sb.append("    Group { Anchor: (Width: 6); }\n");
            sb.append("    Group {\n");
            sb.append("        LayoutMode: Top;\n");
            sb.append("        Anchor: (Width: 190);\n");
            sb.append("        Label #Slot").append(i).append("Name { Text: \"").append(i + 1).append(". ---\"; Style: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center); Anchor: (Height: 20); }\n");
            sb.append("        Label #Slot").append(i).append("Info { Text: \"vacio\"; Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center); Anchor: (Height: 16); }\n");
            sb.append("    }\n");
            sb.append("    Group { Anchor: (Width: 8); }\n");
            sb.append("    TextButton #BtnVer").append(i).append(" { Text: \"Ver\"; Anchor: (Width: 70, Height: 36); Style: ").append(btnStyle).append("; }\n");
            sb.append("    Group { Anchor: (Width: 6); }\n");
            sb.append("    TextButton #BtnActA").append(i).append(" { Text: \"A\"; Anchor: (Width: 45, Height: 36); Style: ").append(btnAStyle).append("; }\n");
            sb.append("    Group { Anchor: (Width: 6); }\n");
            sb.append("    TextButton #BtnActB").append(i).append(" { Text: \"B\"; Anchor: (Width: 45, Height: 36); Style: ").append(btnBStyle).append("; }\n");
            sb.append("}\n");
        }
        return sb.toString();
    }

    private String panelCrianza() {
        return """
            Label {
                Text: "CRIANZA";
                Style: (TextColor: #ffcc00, FontSize: 14, HorizontalAlignment: Center);
                Anchor: (Height: 20);
            }
            Label #LblCrianza {
                Text: "";
                Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                Anchor: (Height: 80);
            }
            TextButton #BtnCrianza {
                Text: "Cuidar Digimon";
                Anchor: (Width: 160, Height: 40);
                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #ffcc00, FontSize: 14, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 14, HorizontalAlignment: Center)));
            }
            """;
    }

    private String panelBatalla() {
        return """
            Label {
                Text: "BATALLA PVP";
                Style: (TextColor: #ff00ff, FontSize: 14, HorizontalAlignment: Center);
                Anchor: (Height: 20);
            }
            Label #LblDesafio {
                Text: "Desafio: CERRADO";
                Style: (TextColor: #ffcc00, FontSize: 13, HorizontalAlignment: Center);
                Anchor: (Height: 18);
            }
            Label #LblActivos {
                Text: "";
                Style: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center);
                Anchor: (Height: 18);
            }
            Label #LblBatallaInfo {
                Text: "";
                Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                Anchor: (Height: 60);
            }
            TextButton #BtnDesafio {
                Text: "Abrir desafio";
                Anchor: (Width: 200, Height: 40);
                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #ffcc00, FontSize: 14, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 14, HorizontalAlignment: Center)));
            }
            Group { Anchor: (Height: 4); }
            TextButton #BtnModo {
                Text: "Modo: Amistoso";
                Anchor: (Width: 200, Height: 36);
                Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #e0e8f0, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center)));
            }
            Group { Anchor: (Height: 4); }
            Label {
                Text: "Para retar: golpea con el sapotama al rival con desafio abierto. Salvajes: golpealos para pelear.";
                Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center, Wrap: true);
                Anchor: (Height: 32);
            }
            """;
    }

    private String buildPanel() {
        return """
            Group {
                LayoutMode: Top;
                Anchor: (Height: 520, Width: 560);

                Group {
                    LayoutMode: Top;
                    Background: (Color: #0b1220);
                    Padding: (Full: 15);

                    Label {
                        Text: "DIGIMON COMPANEROS";
                        Style: (TextColor: #00d4ff, FontSize: 20, HorizontalAlignment: Center);
                        Anchor: (Height: 26);
                    }
                    Label #LblTab {
                        Text: "Equipo";
                        Style: (TextColor: #8899aa, FontSize: 13, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 34);
                        TextButton #TabEquipo {
                            Text: "Equipo";
                            Anchor: (Width: 165, Height: 32);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center)));
                        }
                        Group { Anchor: (Width: 8); }
                        TextButton #TabCrianza {
                            Text: "Crianza";
                            Anchor: (Width: 165, Height: 32);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center)));
                        }
                        Group { Anchor: (Width: 8); }
                        TextButton #TabBatalla {
                            Text: "Batalla";
                            Anchor: (Width: 165, Height: 32);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center)));
                        }
                    }
                    Group { Anchor: (Height: 4); }
                    Group { Anchor: (Height: 2); Background: (Color: #00d4ff); }
                    Group { Anchor: (Height: 4); }
                    Group #TabEquipoPanel {
                        Visible: true;
                        LayoutMode: Top;
                        Background: (Color: #0f1729);
                        Padding: (Full: 10);
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 40);
                            AssetImage #SlotImg0 {
                                Anchor: (Width: 36, Height: 36);
                                AssetPath: "UI/Custom/png_Digis/Botamon.png";
                            }
                            Group { Anchor: (Width: 6); }
                            Group {
                                LayoutMode: Top;
                                Anchor: (Width: 190);
                                Label #Slot0Name { Text: "1. ---"; Style: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center); Anchor: (Height: 20); }
                                Label #Slot0Info { Text: "vacio"; Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center); Anchor: (Height: 16); }
                            }
                            Group { Anchor: (Width: 8); }
                            TextButton #BtnVer0 { Text: "Ver"; Anchor: (Width: 70, Height: 36); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 12, HorizontalAlignment: Center))); }
                            Group { Anchor: (Width: 6); }
                            TextButton #BtnActA0 { Text: "A"; Anchor: (Width: 45, Height: 36); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                            Group { Anchor: (Width: 6); }
                            TextButton #BtnActB0 { Text: "B"; Anchor: (Width: 45, Height: 36); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #ff00ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 40);
                            AssetImage #SlotImg1 {
                                Anchor: (Width: 36, Height: 36);
                                AssetPath: "UI/Custom/png_Digis/Botamon.png";
                            }
                            Group { Anchor: (Width: 6); }
                            Group {
                                LayoutMode: Top;
                                Anchor: (Width: 190);
                                Label #Slot1Name { Text: "2. ---"; Style: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center); Anchor: (Height: 20); }
                                Label #Slot1Info { Text: "vacio"; Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center); Anchor: (Height: 16); }
                            }
                            Group { Anchor: (Width: 8); }
                            TextButton #BtnVer1 { Text: "Ver"; Anchor: (Width: 70, Height: 36); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 12, HorizontalAlignment: Center))); }
                            Group { Anchor: (Width: 6); }
                            TextButton #BtnActA1 { Text: "A"; Anchor: (Width: 45, Height: 36); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                            Group { Anchor: (Width: 6); }
                            TextButton #BtnActB1 { Text: "B"; Anchor: (Width: 45, Height: 36); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #ff00ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 40);
                            AssetImage #SlotImg2 {
                                Anchor: (Width: 36, Height: 36);
                                AssetPath: "UI/Custom/png_Digis/Botamon.png";
                            }
                            Group { Anchor: (Width: 6); }
                            Group {
                                LayoutMode: Top;
                                Anchor: (Width: 190);
                                Label #Slot2Name { Text: "3. ---"; Style: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center); Anchor: (Height: 20); }
                                Label #Slot2Info { Text: "vacio"; Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center); Anchor: (Height: 16); }
                            }
                            Group { Anchor: (Width: 8); }
                            TextButton #BtnVer2 { Text: "Ver"; Anchor: (Width: 70, Height: 36); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 12, HorizontalAlignment: Center))); }
                            Group { Anchor: (Width: 6); }
                            TextButton #BtnActA2 { Text: "A"; Anchor: (Width: 45, Height: 36); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                            Group { Anchor: (Width: 6); }
                            TextButton #BtnActB2 { Text: "B"; Anchor: (Width: 45, Height: 36); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #ff00ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 40);
                            AssetImage #SlotImg3 {
                                Anchor: (Width: 36, Height: 36);
                                AssetPath: "UI/Custom/png_Digis/Botamon.png";
                            }
                            Group { Anchor: (Width: 6); }
                            Group {
                                LayoutMode: Top;
                                Anchor: (Width: 190);
                                Label #Slot3Name { Text: "4. ---"; Style: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center); Anchor: (Height: 20); }
                                Label #Slot3Info { Text: "vacio"; Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center); Anchor: (Height: 16); }
                            }
                            Group { Anchor: (Width: 8); }
                            TextButton #BtnVer3 { Text: "Ver"; Anchor: (Width: 70, Height: 36); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 12, HorizontalAlignment: Center))); }
                            Group { Anchor: (Width: 6); }
                            TextButton #BtnActA3 { Text: "A"; Anchor: (Width: 45, Height: 36); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                            Group { Anchor: (Width: 6); }
                            TextButton #BtnActB3 { Text: "B"; Anchor: (Width: 45, Height: 36); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #ff00ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 40);
                            AssetImage #SlotImg4 {
                                Anchor: (Width: 36, Height: 36);
                                AssetPath: "UI/Custom/png_Digis/Botamon.png";
                            }
                            Group { Anchor: (Width: 6); }
                            Group {
                                LayoutMode: Top;
                                Anchor: (Width: 190);
                                Label #Slot4Name { Text: "5. ---"; Style: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center); Anchor: (Height: 20); }
                                Label #Slot4Info { Text: "vacio"; Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center); Anchor: (Height: 16); }
                            }
                            Group { Anchor: (Width: 8); }
                            TextButton #BtnVer4 { Text: "Ver"; Anchor: (Width: 70, Height: 36); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 12, HorizontalAlignment: Center))); }
                            Group { Anchor: (Width: 6); }
                            TextButton #BtnActA4 { Text: "A"; Anchor: (Width: 45, Height: 36); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                            Group { Anchor: (Width: 6); }
                            TextButton #BtnActB4 { Text: "B"; Anchor: (Width: 45, Height: 36); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #ff00ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 40);
                            AssetImage #SlotImg5 {
                                Anchor: (Width: 36, Height: 36);
                                AssetPath: "UI/Custom/png_Digis/Botamon.png";
                            }
                            Group { Anchor: (Width: 6); }
                            Group {
                                LayoutMode: Top;
                                Anchor: (Width: 190);
                                Label #Slot5Name { Text: "6. ---"; Style: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center); Anchor: (Height: 20); }
                                Label #Slot5Info { Text: "vacio"; Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center); Anchor: (Height: 16); }
                            }
                            Group { Anchor: (Width: 8); }
                            TextButton #BtnVer5 { Text: "Ver"; Anchor: (Width: 70, Height: 36); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 12, HorizontalAlignment: Center))); }
                            Group { Anchor: (Width: 6); }
                            TextButton #BtnActA5 { Text: "A"; Anchor: (Width: 45, Height: 36); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                            Group { Anchor: (Width: 6); }
                            TextButton #BtnActB5 { Text: "B"; Anchor: (Width: 45, Height: 36); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #ff00ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                        }
                    }
                    Group #TabCrianzaPanel {
                        Visible: false;
                        LayoutMode: Top;
                        Background: (Color: #0f1729);
                        Padding: (Full: 10);
                        Label {
                            Text: "CRIANZA";
                            Style: (TextColor: #ffcc00, FontSize: 14, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }
                        Label #LblCrianza {
                            Text: "";
                            Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                            Anchor: (Height: 80);
                        }
                        TextButton #BtnCrianza {
                            Text: "Cuidar Digimon";
                            Anchor: (Width: 160, Height: 40);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #ffcc00, FontSize: 14, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 14, HorizontalAlignment: Center)));
                        }
                    }
                    Group #TabBatallaPanel {
                        Visible: false;
                        LayoutMode: Top;
                        Background: (Color: #0f1729);
                        Padding: (Full: 10);
                        Label {
                            Text: "BATALLA PVP";
                            Style: (TextColor: #ff00ff, FontSize: 14, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }
                        Label #LblDesafio {
                            Text: "Desafio: CERRADO";
                            Style: (TextColor: #ffcc00, FontSize: 13, HorizontalAlignment: Center);
                            Anchor: (Height: 18);
                        }
                        Label #LblActivos {
                            Text: "";
                            Style: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center);
                            Anchor: (Height: 18);
                        }
                        Label #LblBatallaInfo {
                            Text: "";
                            Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                            Anchor: (Height: 60);
                        }
                        TextButton #BtnDesafio {
                            Text: "Abrir desafio";
                            Anchor: (Width: 200, Height: 40);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #ffcc00, FontSize: 14, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 14, HorizontalAlignment: Center)));
                        }
                        Group { Anchor: (Height: 4); }
                        TextButton #BtnModo {
                            Text: "Modo: Amistoso";
                            Anchor: (Width: 200, Height: 36);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #e0e8f0, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center)));
                        }
                        Group { Anchor: (Height: 4); }
                        Label {
                            Text: "Para retar: golpea con el sapotama al rival con desafio abierto. Salvajes: golpealos para pelear.";
                            Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center, Wrap: true);
                            Anchor: (Height: 32);
                        }
                    }
                    Group { Anchor: (Height: 8); }
                    TextButton #BtnCerrar {
                        Text: "Cerrar";
                        Anchor: (Width: 160, Height: 40);
                        Style: (Default: (Background: (Color: #3d1a1a), LabelStyle: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center)), Hovered: (Background: (Color: #5d2a2a), LabelStyle: (TextColor: #ffffff, FontSize: 14, HorizontalAlignment: Center)));
                    }
                }
            }
            """;
    }

    private String buildPanelLegacy() {
        return """
            Group {
                LayoutMode: Top;
                Anchor: (Height: 520, Width: 500);

                Group {
                    LayoutMode: Top;
                    Background: (Color: #0b1220);
                    Padding: (Full: 15);

                    Label {
                        Text: "DIGIMON COMPANEROS";
                        Style: (TextColor: #00d4ff, FontSize: 20, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 28);
                    }

                    Group {
                        LayoutMode: Left;

                        TextButton #TabEquipo {
                            Text: "Equipo";
                            Anchor: (Width: 80, Height: 32);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 13, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                        TextButton #TabCrianza {
                            Text: "Crianza";
                            Anchor: (Width: 80, Height: 32);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                        TextButton #TabBatalla {
                            Text: "Batalla";
                            Anchor: (Width: 80, Height: 32);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                    }

                    Group { Anchor: (Height: 6); }

                    Label {
                        Text: "________________________";
                        Style: (TextColor: #00d4ff, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }

                    Group { Anchor: (Height: 4); }

                    Group #TabEquipoPanel {
                        Visible: true;
                        Anchor: (Width: 470, Height: 320);
                        Background: (Color: #0f1729);
                        Padding: (Full: 10);

                        Label {
                            Text: "--- COMPANERO A ---";
                            Style: (TextColor: #00d4ff, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }
                        Label #NombreA {
                            Text: "---";
                            Style: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }
                        Label #NivelA {
                            Text: "---";
                            Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                            Anchor: (Height: 16);
                        }
                        Label #BarA {
                            Text: "";
                            Style: (TextColor: #00ff88, FontSize: 12, HorizontalAlignment: Center);
                            Anchor: (Height: 16);
                        }

                        Group { Anchor: (Height: 6); }

                        Group {
                            LayoutMode: Left;

                            TextButton #BtnIntercambiarA {
                                Text: "Intercambiar";
                                Anchor: (Width: 100, Height: 32);
                                Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }

                            Group { FlexWeight: 1; }

                            TextButton #BtnPerfilA {
                                Text: "Perfil";
                                Anchor: (Width: 80, Height: 32);
                                Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }

                        Group { Anchor: (Height: 10); }

                        Label {
                            Text: "--- COMPANERO B ---";
                            Style: (TextColor: #ff00ff, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }
                        Label #NombreB {
                            Text: "---";
                            Style: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }
                        Label #NivelB {
                            Text: "---";
                            Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                            Anchor: (Height: 16);
                        }
                        Label #BarB {
                            Text: "";
                            Style: (TextColor: #00ff88, FontSize: 12, HorizontalAlignment: Center);
                            Anchor: (Height: 16);
                        }

                        Group { Anchor: (Height: 6); }

                        Group {
                            LayoutMode: Left;

                            TextButton #BtnIntercambiarB {
                                Text: "Intercambiar";
                                Anchor: (Width: 100, Height: 32);
                                Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }

                            Group { FlexWeight: 1; }

                            TextButton #BtnPerfilB {
                                Text: "Perfil";
                                Anchor: (Width: 80, Height: 32);
                                Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }
                    }

                    Group #TabCrianzaPanel {
                        Visible: false;
                        Anchor: (Width: 470, Height: 320);
                        Background: (Color: #0f1729);
                        Padding: (Full: 10);

                        Label {
                            Text: "CRIANZA";
                            Style: (TextColor: #ffcc00, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }

                        Group { Anchor: (Height: 8); }

                        Label #LblCrianza {
                            Text: "A: - | B: -";
                            Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                            Anchor: (Height: 80);
                        }

                        Group { Anchor: (Height: 12); }

                        TextButton #BtnCrianza {
                            Text: "Cuidar Digimon";
                            Anchor: (Width: 160, Height: 40);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 14, TextColor: #ffcc00, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                    }

                    Group #TabBatallaPanel {
                        Visible: false;
                        Anchor: (Width: 470, Height: 320);
                        Background: (Color: #0f1729);
                        Padding: (Full: 10);

                        Label {
                            Text: "BATALLA";
                            Style: (TextColor: #ff00ff, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }

                        Group { Anchor: (Height: 8); }

                        Label #LblBatallaInfo {
                            Text: "A: - | B: -";
                            Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                            Anchor: (Height: 60);
                        }

                        Group { Anchor: (Height: 12); }

                        Group {
                            LayoutMode: Left;

                            TextButton #BtnBatallarA {
                                Text: "Combate A";
                                Anchor: (Width: 120, Height: 40);
                                Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 14, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }

                            Group { FlexWeight: 1; }

                            TextButton #BtnBatallarB {
                                Text: "Combate B";
                                Anchor: (Width: 120, Height: 40);
                                Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 14, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }
                    }

                    Group { Anchor: (Height: 8); }

                    TextButton #BtnCerrar {
                        Text: "Cerrar";
                        Anchor: (Width: 160, Height: 40);
                        Style: (Default: (Background: (Color: #3d1a1a), LabelStyle: (FontSize: 14, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #5d2a2a), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)));
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
        LOGGER.log(Level.INFO, "SapotamaTabUI - acción: " + action);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null) return;

        switch (action) {
            case "tab" -> {
                String v = json.has("value") ? json.get("value").getAsString() : "equipo";
                if (!v.equals("equipo") && !v.equals("crianza") && !v.equals("batalla")) v = "equipo";
                if (!v.equals(pestanaActual)) {
                    pestanaActual = v;
                    rebuild();
                } else {
                    UICommandBuilder cmd = new UICommandBuilder();
                    applyTab(json, cmd, store);
                    sendUpdate(cmd);
                }
            }
            case "intercambiar" -> {
                UICommandBuilder cmd = new UICommandBuilder();
                sendUpdate(cmd);
            }
            case "ver" -> onVer(ref, store, player, json);
            case "acta" -> onElegirActiva(ref, store, json, true);
            case "actb" -> onElegirActiva(ref, store, json, false);
            case "crianza" -> onCrianza(ref, store, player);
            case "desafio" -> onDesafio(new UICommandBuilder(), store);
            case "modo" -> onModo(new UICommandBuilder(), store);
            case "cerrar" -> onCerrar(ref, store, player);
            default -> {
                UICommandBuilder cmd = new UICommandBuilder();
                sendUpdate(cmd);
            }
        }
    }

    private void applyTab(JsonObject json, UICommandBuilder cmd, Store<EntityStore> store) {
        // Rellena según tab (sin Visible: cada tab se adjunta por separado).
        String tab = "equipo";
        if (json != null && json.has("value")) {
            tab = json.get("value").getAsString();
        }

        cmd.set("#LblTab.Text", "equipo".equals(tab) ? "Equipo"
            : "crianza".equals(tab) ? "Crianza" : "batalla".equals(tab) ? "Batalla" : tab);

        if ("equipo".equals(tab)) {
            actualizarEquipo(cmd, store);
        } else if ("crianza".equals(tab)) {
            actualizarCrianza(cmd, store);
        } else if ("batalla".equals(tab)) {
            actualizarBatalla(cmd, store);
        } else {
            actualizarEquipo(cmd, store);
        }
    }

    private void actualizarEquipo(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
            DatoDigimon digi = d.getSlot(i);
            String marca = "";
            if (i == d.activoA) marca = " [A]";
            else if (i == d.activoB) marca = " [B]";
            boolean ocupado = digi != null;
            if (ocupado) {
                cmd.set("#Slot" + i + "Name.Text", (i + 1) + ". " + digi.nombre + marca);
                cmd.set("#Slot" + i + "Info.Text",
                    digi.especie + " Nv" + digi.nivel + " | HP " + digi.hp + "/" + digi.maxHp
                    + (digi.dormido ? " [Zzz]" : ""));
                String ruta = DigiImagenes.rutaPara(digi.especie);
                cmd.set("#SlotImg" + i + ".Visible", ruta != null);
                if (ruta != null) cmd.set("#SlotImg" + i + ".AssetPath", ruta);
            } else {
                cmd.set("#Slot" + i + "Name.Text", (i + 1) + ". ---");
                cmd.set("#Slot" + i + "Info.Text", "vacio");
                cmd.set("#SlotImg" + i + ".Visible", false);
            }
            // Solo aparecen acciones en los slots cargados.
            cmd.set("#BtnVer" + i + ".Visible", ocupado);
            cmd.set("#BtnActA" + i + ".Visible", ocupado);
            cmd.set("#BtnActB" + i + ".Visible", ocupado);
        }
    }

    private void actualizarCrianza(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        StringBuilder sb = new StringBuilder();
        String alerta = null;
        int peorHambre = Integer.MAX_VALUE;
        for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
            DatoDigimon digi = d.getSlot(i);
            if (i > 0) sb.append(" | ");
            if (digi != null) {
                sb.append(i + 1).append(":").append(digi.nombre)
                  .append(" H:").append(digi.hambre)
                  .append(" E:").append(digi.energia)
                  .append(" L:").append(digi.lazo);
                if (digi.hambre < peorHambre) {
                    peorHambre = digi.hambre;
                    alerta = digi.nombre;
                }
            } else {
                sb.append(i + 1).append(":-");
            }
        }
        if (alerta != null && peorHambre < 30) {
            sb.append("  >> ¡").append(alerta).append(" necesita comida!");
        }
        cmd.set("#LblCrianza.Text", sb.toString());
    }

    private void actualizarBatalla(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        DatoDigimon a = d.getA();
        DatoDigimon b = d.getB();
        cmd.set("#LblDesafio.Text", d.desafioAbierto ? "Desafio: ABIERTO (" + d.modoDesafio + ")" : "Desafio: CERRADO");
        cmd.set("#BtnDesafio.Text", d.desafioAbierto ? "Cerrar desafio" : "Abrir desafio");
        cmd.set("#BtnModo.Text", "Modo: " + d.modoDesafio);
        cmd.set("#LblActivos.Text", "Activos: A=" + (a != null ? a.nombre : "-")
            + " B=" + (b != null ? b.nombre : "-"));
        String info = "A: " + (a != null ? a.nombre + " (HP:" + a.hp + "/" + a.maxHp + ")" : "-")
                     + " | B: " + (b != null ? b.nombre + " (HP:" + b.hp + "/" + b.maxHp + ")" : "-");
        if (d.desafioDeNombre != null && !d.desafioDeNombre.isEmpty()) {
            info = "¡RETO de " + d.desafioDeNombre + "! Golpealo para aceptar. | " + info;
        }
        cmd.set("#LblBatallaInfo.Text", info);
    }

    private void onVer(Ref<EntityStore> ref, Store<EntityStore> store, Player player, JsonObject json) {
        int slot = parseSlot(json);
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null || d.getSlot(slot) == null) {
            UICommandBuilder empty = new UICommandBuilder();
            sendUpdate(empty);
            return;
        }
        LOGGER.log(Level.INFO, "Abrir perfil del slot " + slot);
        DigiPerfilDigimonUI.open(playerRef, ref, store, slot);
    }

    private void onElegirActiva(Ref<EntityStore> ref, Store<EntityStore> store, JsonObject json, boolean esA) {
        int slot = parseSlot(json);
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        UICommandBuilder cmd = new UICommandBuilder();
        if (d == null) {
            sendUpdate(cmd);
            return;
        }
        boolean ok = esA ? d.elegirActivoA(slot) : d.elegirActivoB(slot);
        LOGGER.log(Level.INFO, (esA ? "Activo A" : "Activo B") + " -> slot " + slot + (ok ? " OK" : " rechazado"));
        if (ok) {
            try {
                PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
                if (progress != null) progress.guardarDesde(d);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "No se pudo guardar activos: " + e.getMessage());
            }
        }
        rebuild();
    }

    private static int parseSlot(JsonObject json) {
        try {
            int i = json.has("value") ? Integer.parseInt(json.get("value").getAsString()) : 0;
            return AlmacenJugadores.DatosJugador.slotValido(i) ? i : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void onCrianza(Ref<EntityStore> ref, Store<EntityStore> store, Player player) {
        LOGGER.log(Level.INFO, "Crianza - abrir menú de crianza");
        // Abrir directo (sin cerrar antes): el doble cambio de página rompía la UI destino.
        DigiCuidarMenuUI.open(playerRef, ref, store);
    }

    private void onDesafio(UICommandBuilder ignored, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        UICommandBuilder cmd = new UICommandBuilder();
        if (d == null) {
            sendUpdate(cmd);
            return;
        }
        d.desafioAbierto = !d.desafioAbierto;
        LOGGER.log(Level.INFO, "Desafio PvP " + (d.desafioAbierto ? "ABIERTO" : "CERRADO") + " (" + d.modoDesafio + ")");
        rebuild();
    }

    private void onModo(UICommandBuilder ignored, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        UICommandBuilder cmd = new UICommandBuilder();
        if (d == null) {
            sendUpdate(cmd);
            return;
        }
        d.modoDesafio = "Amistoso".equals(d.modoDesafio) ? "Serio" : "Amistoso";
        rebuild();
    }

    private void onCerrar(Ref<EntityStore> ref, Store<EntityStore> store, Player player) {
        player.getPageManager().setPage(ref, store,
            com.hypixel.hytale.protocol.packets.interface_.Page.None);
    }
}
