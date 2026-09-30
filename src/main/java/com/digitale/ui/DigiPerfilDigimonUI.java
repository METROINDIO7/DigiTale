package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EnemigoCombate;
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

public class DigiPerfilDigimonUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(DigiPerfilDigimonUI.class.getName());

    private int slotActivo = 0; // 0-5
    private String pestana = "stats";

    public DigiPerfilDigimonUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store,
                            int slot) {
        LOGGER.log(Level.INFO, "DigiPerfilDigimonUI.open() - slot: " + slot);

        DigiPerfilDigimonUI page = new DigiPerfilDigimonUI(playerRef);
        page.slotActivo = AlmacenJugadores.DatosJugador.slotValido(slot) ? slot : 0;

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
        // Shell + solo el panel activo (sin Visible: lo que no se adjunta no existe).
        cmd.appendInline(null, buildShell());
        cmd.appendInline("#TabContent", panelPara(pestana));

        // Marca de pestaña activa (botones estáticos, siempre existen).
        cmd.set("#TabStats.Text", "stats".equals(pestana) ? "> Stats" : "Stats");
        cmd.set("#TabMovimientos.Text", "movimientos".equals(pestana) ? "> Movim." : "Movimientos");
        cmd.set("#TabEvolucion.Text", "evolucion".equals(pestana) ? "> Evolucion" : "Evolucion");
        cmd.set("#TabDeEvolucion.Text", "de_evolucion".equals(pestana) ? "> DeEvo." : "DeEvolucion");
        cmd.set("#TabEquipamiento.Text", "equipamiento".equals(pestana) ? "> Equipo" : "Equipamiento");

        // Eventos de pestañas
        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabStats",
            EventData.of("action", "tab").append("value", "stats"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabMovimientos",
            EventData.of("action", "tab").append("value", "movimientos"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabEvolucion",
            EventData.of("action", "tab").append("value", "evolucion"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabDeEvolucion",
            EventData.of("action", "tab").append("value", "de_evolucion"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabEquipamiento",
            EventData.of("action", "tab").append("value", "equipamiento"), false);

        // Evento volver
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnVolver",
            EventData.of("action", "volver"), false);

        // Eventos del contenido activo (existe porque se adjuntó arriba)
        switch (pestana) {
            case "movimientos" -> {
                event.addEventBinding(CustomUIEventBindingType.Activating, "#Move1",
                    EventData.of("action", "seleccionar_movimiento").append("value", "1"), false);
                event.addEventBinding(CustomUIEventBindingType.Activating, "#Move2",
                    EventData.of("action", "seleccionar_movimiento").append("value", "2"), false);
                event.addEventBinding(CustomUIEventBindingType.Activating, "#Move3",
                    EventData.of("action", "seleccionar_movimiento").append("value", "3"), false);
                event.addEventBinding(CustomUIEventBindingType.Activating, "#Move4",
                    EventData.of("action", "seleccionar_movimiento").append("value", "4"), false);
            }
            case "evolucion" -> {
                event.addEventBinding(CustomUIEventBindingType.Activating, "#Evo1",
                    EventData.of("action", "evolucionar").append("value", "1"), false);
                event.addEventBinding(CustomUIEventBindingType.Activating, "#Evo2",
                    EventData.of("action", "evolucionar").append("value", "2"), false);
                event.addEventBinding(CustomUIEventBindingType.Activating, "#Evo3",
                    EventData.of("action", "evolucionar").append("value", "3"), false);
                event.addEventBinding(CustomUIEventBindingType.Activating, "#Evo4",
                    EventData.of("action", "evolucionar").append("value", "4"), false);
            }
            case "de_evolucion" -> {
                event.addEventBinding(CustomUIEventBindingType.Activating, "#DeEvo1",
                    EventData.of("action", "de_evolucionar").append("value", "1"), false);
                event.addEventBinding(CustomUIEventBindingType.Activating, "#DeEvo2",
                    EventData.of("action", "de_evolucionar").append("value", "2"), false);
            }
            case "equipamiento" -> {
                event.addEventBinding(CustomUIEventBindingType.Activating, "#ItemEquipo",
                    EventData.of("action", "equipamiento").append("value", "equipo"), false);
                event.addEventBinding(CustomUIEventBindingType.Activating, "#ItemArma",
                    EventData.of("action", "equipamiento").append("value", "arma"), false);
                event.addEventBinding(CustomUIEventBindingType.Activating, "#ItemArmadura",
                    EventData.of("action", "equipamiento").append("value", "armadura"), false);
                event.addEventBinding(CustomUIEventBindingType.Activating, "#Cosmetico1",
                    EventData.of("action", "equipamiento").append("value", "cosmetico1"), false);
                event.addEventBinding(CustomUIEventBindingType.Activating, "#Cosmetico2",
                    EventData.of("action", "equipamiento").append("value", "cosmetico2"), false);
            }
            default -> {
            }
        }

        // Relleno inicial del tab activo (el framework envía el cmd de build)
        switch (pestana) {
            case "movimientos" -> actualizarMovimientos(cmd, store);
            case "evolucion" -> actualizarEvolucion(cmd, store);
            case "de_evolucion" -> actualizarDeEvolucion(cmd, store);
            case "equipamiento" -> actualizarEquipamiento(cmd, store);
            default -> actualizarStats(cmd, store);
        }
    }

    private String buildShell() {
        return """
            Group {
                LayoutMode: Top;
                Anchor: (Height: 580, Width: 520);

                Group {
                    LayoutMode: Top;
                    Background: (Color: #0b1220);
                    Padding: (Full: 15);

                    Label {
                        Text: "PERFIL DEL DIGIMON";
                        Style: (TextColor: #ff00ff, FontSize: 18, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 24);
                    }
                    Group {
                        LayoutMode: Left;

                        Group { Anchor: (Width: 8); }

                        TextButton #BtnVolver {
                            Text: "Volver";
                            Anchor: (Width: 80, Height: 32);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                    }
                    Group { Anchor: (Height: 6); }

                    Group {
                        LayoutMode: Left;

                        TextButton #TabStats {
                            Text: "Stats";
                            Anchor: (Width: 80, Height: 30);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                        TextButton #TabMovimientos {
                            Text: "Movimientos";
                            Anchor: (Width: 100, Height: 30);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                        TextButton #TabEvolucion {
                            Text: "Evolucion";
                            Anchor: (Width: 90, Height: 30);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                        TextButton #TabDeEvolucion {
                            Text: "DeEvolucion";
                            Anchor: (Width: 100, Height: 30);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                        TextButton #TabEquipamiento {
                            Text: "Equipamiento";
                            Anchor: (Width: 100, Height: 30);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                    }
                    Group { Anchor: (Height: 6); }

                    Group #TabContent {
                        LayoutMode: Top;
                        Background: (Color: #0f1729);
                        Padding: (Full: 12);
                    }
                }
            }
            """;
    }

    private String panelPara(String cual) {
        return switch (cual) {
            case "movimientos" -> panelMovimientos();
            case "evolucion" -> panelEvolucion();
            case "de_evolucion" -> panelDeEvolucion();
            case "equipamiento" -> panelEquipamiento();
            default -> panelStats();
        };
    }

    private String panelStats() {
        return """
            Label {
                Text: "ESTADISTICAS";
                Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                Anchor: (Height: 20);
            }
            Group { Anchor: (Height: 6); }

            Group {
                LayoutMode: Left;

                Group #Portrait {
                    Anchor: (Width: 70, Height: 70);
                    Background: (Color: #16213e);
                    Padding: (Full: 5);
                    AssetImage #DigiPortrait {
                        Anchor: (Width: 60, Height: 60);
                        AssetPath: "UI/Custom/png_Digis/Botamon.png";
                    }
                }

                Group { Anchor: (Width: 8); }

                Group {
                    LayoutMode: Top;
                    Anchor: (Height: 70);

                    Label #DigiIcon {
                        Text: "[Icono]";
                        Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                        Anchor: (Height: 14);
                    }
                    Label #LblNombre {
                        Text: "-";
                        Style: (TextColor: #e0e8f0, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Label #LblEspecie {
                        Text: "-";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                }
            }
            Group { Anchor: (Height: 6); }

            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "NIVEL"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblNivel { Text: "-"; Style: (TextColor: #00ff88, FontSize: 13, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 24);
                Label { Text: "ELEMENTO"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                AssetImage #ImgElemento {
                    Anchor: (Width: 20, Height: 20);
                    AssetPath: "UI/Textures/Icons/Elemental/Neutral.png";
                }
                Group { Anchor: (Width: 6); }
                Label #LblElemento { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 24);
                Label { Text: "ATRIBUTO"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                AssetImage #ImgAtributo {
                    Anchor: (Width: 20, Height: 20);
                    AssetPath: "UI/Textures/Icons/Type/None.png";
                }
                Group { Anchor: (Width: 6); }
                Label #LblAtributo { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "HP"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblHp { Text: "- / -"; Style: (TextColor: #00ff88, FontSize: 13, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "ATK"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblAtk { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "DEF"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblDef { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "SPD"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblSpd { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "WIS"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblWis { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
            }
            Group { Anchor: (Height: 6); }
            Group { Anchor: (Height: 2); Background: (Color: #00d4ff); }
            Group { Anchor: (Height: 4); }

            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "LAZO"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblLazo { Text: "- / 100"; Style: (TextColor: #ff00ff, FontSize: 13, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "DISCIPLINA"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblDisciplina { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "VICTORIAS"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblVictorias { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "DERROTAS"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblDerrotas { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "ENERGIA"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblEnergia { Text: "- / 100"; Style: (TextColor: #ffcc00, FontSize: 13, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "HAMBRE"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblHambre { Text: "- / 100"; Style: (TextColor: #ff6600, FontSize: 13, VerticalAlignment: Center); }
            }
            Group { Anchor: (Height: 6); }
            Group { Anchor: (Height: 2); Background: (Color: #00d4ff); }
            Group { Anchor: (Height: 4); }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "FUERTE VS"; Style: (TextColor: #00ff88, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblFuerte { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 12, VerticalAlignment: Center); }
            }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 16);
                Label { Text: "DEBIL VS"; Style: (TextColor: #ff4444, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                Label #LblDebil { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 12, VerticalAlignment: Center); }
            }
            """;
    }

    private String panelMovimientos() {
        return """
            Label {
                Text: "MOVIMIENTOS / TECNICAS";
                Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                Anchor: (Height: 20);
            }
            Label #LblInfoMovimiento {
                Text: "Selecciona un movimiento";
                Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                Anchor: (Height: 14);
            }
            Group { Anchor: (Height: 4); }

            Group {
                LayoutMode: Left;
                Anchor: (Height: 44);

                TextButton #Move1 {
                    Text: "Movimiento 1";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
                Group { Anchor: (Width: 8); }
                TextButton #Move2 {
                    Text: "Movimiento 2";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
            }
            Group { Anchor: (Height: 4); }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 44);

                TextButton #Move3 {
                    Text: "Movimiento 3";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
                Group { Anchor: (Width: 8); }
                TextButton #Move4 {
                    Text: "Movimiento 4";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
            }
            Group { Anchor: (Height: 6); }

            Label { Text: "TUS MOVIMIENTOS"; Style: (TextColor: #00d4ff, FontSize: 12, RenderBold: true); Anchor: (Height: 16); }
            Label #MoveName1 { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
            Label #MoveName2 { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
            Label #MoveName3 { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
            Label #MoveName4 { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
            Group { Anchor: (Height: 6); }

            Label { Text: "INFO MOVIMIENTO"; Style: (TextColor: #ff00ff, FontSize: 12, RenderBold: true); Anchor: (Height: 16); }
            Label #LblMovePower { Text: "Poder: -"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
            Label #LblMoveHits { Text: "Golpes: -"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
            Label #LblMoveCrit { Text: "Critico: -"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
            Label #LblMoveRange { Text: "Alcance: -"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
            Label #LblMoveDesc { Text: "-"; Style: (TextColor: #8899aa, FontSize: 11, Wrap: true); Anchor: (Height: 32); }
            """;
    }

    private String panelEvolucion() {
        return """
            Label {
                Text: "EVOLUCION";
                Style: (TextColor: #ff00ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                Anchor: (Height: 20);
            }
            Label #LblEvolucionInfo {
                Text: "Digimon actual: -";
                Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                Anchor: (Height: 16);
            }
            Group { Anchor: (Height: 6); }
            Group { Anchor: (Height: 2); Background: (Color: #ff00ff); }
            Group { Anchor: (Height: 4); }

            Label { Text: "EVOLUCIONES DISPONIBLES"; Style: (TextColor: #00d4ff, FontSize: 12, RenderBold: true, HorizontalAlignment: Center); Anchor: (Height: 16); }
            Group { Anchor: (Height: 4); }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 44);

                TextButton #Evo1 {
                    Text: "-";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
                Group { Anchor: (Width: 8); }
                TextButton #Evo2 {
                    Text: "-";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
            }
            Group { Anchor: (Height: 4); }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 44);

                TextButton #Evo3 {
                    Text: "-";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
                Group { Anchor: (Width: 8); }
                TextButton #Evo4 {
                    Text: "-";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
            }
            Group { Anchor: (Height: 6); }
            Group { Anchor: (Height: 2); Background: (Color: #00d4ff); }
            Group { Anchor: (Height: 4); }

            Label { Text: "CONDICIONES DE EVOLUCION"; Style: (TextColor: #00d4ff, FontSize: 12, RenderBold: true, HorizontalAlignment: Center); Anchor: (Height: 16); }
            Label #LblEvolCondiciones {
                Text: "-";
                Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center, Wrap: true);
                Anchor: (Height: 40);
            }
            """;
    }

    private String panelDeEvolucion() {
        return """
            Label {
                Text: "DE-EVOLUCION";
                Style: (TextColor: #ff6600, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                Anchor: (Height: 24);
            }
            Label {
                Text: "La de-evolucion devuelve a tu Digimon a una forma anterior.";
                Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center, Wrap: true);
                Anchor: (Height: 24);
            }
            Group { Anchor: (Height: 4); }
            Group { Anchor: (Height: 2); Background: (Color: #ff6600); }
            Group { Anchor: (Height: 4); }

            Label #LblDeEvolucionInfo {
                Text: "Digimon actual: -";
                Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                Anchor: (Height: 16);
            }
            Group { Anchor: (Height: 4); }
            Label { Text: "DE-EVOLUCIONES DISPONIBLES"; Style: (TextColor: #ff6600, FontSize: 12, RenderBold: true, HorizontalAlignment: Center); Anchor: (Height: 16); }
            Group { Anchor: (Height: 4); }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 44);

                TextButton #DeEvo1 {
                    Text: "-";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
                Group { Anchor: (Width: 8); }
                TextButton #DeEvo2 {
                    Text: "-";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
            }
            """;
    }

    private String panelEquipamiento() {
        return """
            Label {
                Text: "EQUIPAMIENTO";
                Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                Anchor: (Height: 20);
            }
            Label {
                Text: "Equipamiento del Digimon";
                Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                Anchor: (Height: 14);
            }
            Group { Anchor: (Height: 6); }
            Group { Anchor: (Height: 2); Background: (Color: #00d4ff); }
            Group { Anchor: (Height: 4); }

            Group {
                LayoutMode: Left;
                Anchor: (Height: 40);
                Label { Text: "EQUIPO ACTUAL"; Style: (TextColor: #ff00ff, FontSize: 12, RenderBold: true, VerticalAlignment: Center); Anchor: (Width: 200); }
                Group { Anchor: (Width: 8); }
                TextButton #ItemEquipo {
                    Text: "Sin equipo";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
            }
            Group { Anchor: (Height: 4); }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 40);
                Label { Text: "ARMA"; Style: (TextColor: #00d4ff, FontSize: 12, RenderBold: true, VerticalAlignment: Center); Anchor: (Width: 200); }
                Group { Anchor: (Width: 8); }
                TextButton #ItemArma {
                    Text: "Sin arma";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
            }
            Group { Anchor: (Height: 4); }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 40);
                Label { Text: "ARMADURA"; Style: (TextColor: #00d4ff, FontSize: 12, RenderBold: true, VerticalAlignment: Center); Anchor: (Width: 200); }
                Group { Anchor: (Width: 8); }
                TextButton #ItemArmadura {
                    Text: "Sin armadura";
                    Anchor: (Width: 200, Height: 40);
                    Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
            }
            Group { Anchor: (Height: 6); }
            Group { Anchor: (Height: 2); Background: (Color: #ff00ff); }
            Group { Anchor: (Height: 4); }

            Label { Text: "COSMETICOS"; Style: (TextColor: #ff00ff, FontSize: 12, RenderBold: true, HorizontalAlignment: Center); Anchor: (Height: 16); }
            Group { Anchor: (Height: 4); }
            Group {
                LayoutMode: Left;
                Anchor: (Height: 36);

                TextButton #Cosmetico1 {
                    Text: "Aspecto Normal";
                    Anchor: (Width: 200, Height: 36);
                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 11, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 11, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
                Group { Anchor: (Width: 8); }
                TextButton #Cosmetico2 {
                    Text: "Aspecto Especial";
                    Anchor: (Width: 200, Height: 36);
                    Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 11, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 11, TextColor: #ffffff, HorizontalAlignment: Center)));
                }
            }
            """;
    }

    private String buildPanel() {
        return """
            Group {
                LayoutMode: Top;
                Anchor: (Height: 580, Width: 520);

                Group {
                    LayoutMode: Top;
                    Background: (Color: #0b1220);
                    Padding: (Full: 15);

                    Label {
                        Text: "PERFIL DEL DIGIMON";
                        Style: (TextColor: #ff00ff, FontSize: 18, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 24);
                    }
                    Group {
                        LayoutMode: Left;

                        Group { Anchor: (Width: 8); }

                        TextButton #BtnVolver {
                            Text: "Volver";
                            Anchor: (Width: 80, Height: 32);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                    }
                    Group { Anchor: (Height: 6); }

                    Group {
                        LayoutMode: Left;

                        TextButton #TabStats {
                            Text: "Stats";
                            Anchor: (Width: 80, Height: 30);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                        TextButton #TabMovimientos {
                            Text: "Movimientos";
                            Anchor: (Width: 100, Height: 30);
                            Style: (Default: (Background: (Color: #12121e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                        TextButton #TabEvolucion {
                            Text: "Evolucion";
                            Anchor: (Width: 90, Height: 30);
                            Style: (Default: (Background: (Color: #12121e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                        TextButton #TabDeEvolucion {
                            Text: "DeEvolucion";
                            Anchor: (Width: 100, Height: 30);
                            Style: (Default: (Background: (Color: #12121e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                        TextButton #TabEquipamiento {
                            Text: "Equipamiento";
                            Anchor: (Width: 100, Height: 30);
                            Style: (Default: (Background: (Color: #12121e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                    }
                    Group { Anchor: (Height: 6); }

                    Group #TabStatsPanel {
                        Visible: true;
                        Anchor: (Height: 400, Width: 490);
                        Background: (Color: #0f1729);
                        Padding: (Full: 12);

                        Label {
                            Text: "ESTADISTICAS";
                            Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }
                        Group { Anchor: (Height: 6); }

                        Group {
                            LayoutMode: Left;

                            Group #Portrait {
                                Anchor: (Width: 70, Height: 70);
                                Background: (Color: #16213e);
                                Padding: (Full: 5);
                                AssetImage #DigiPortrait {
                                    Anchor: (Width: 60, Height: 60);
                                    AssetPath: "UI/Custom/png_Digis/Botamon.png";
                                }
                            }

                            Group { Anchor: (Width: 8); }

                            Group {
                                LayoutMode: Top;
                                Anchor: (Height: 70);

                                Label #DigiIcon {
                                    Text: "[Icono]";
                                    Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                                    Anchor: (Height: 14);
                                }
                                Group { Anchor: (Width: 8); }
                                Label #LblNombre {
                                    Text: "-";
                                    Style: (TextColor: #e0e8f0, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                                    Anchor: (Height: 18);
                                }
                                Label #LblEspecie {
                                    Text: "-";
                                    Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                                    Anchor: (Height: 16);
                                }
                            }
                        }
                        Group { Anchor: (Height: 6); }

                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "NIVEL"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblNivel { Text: "-"; Style: (TextColor: #00ff88, FontSize: 13, VerticalAlignment: Center); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "ELEMENTO"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblElemento { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "HP"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblHp { Text: "- / -"; Style: (TextColor: #00ff88, FontSize: 13, VerticalAlignment: Center); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "ATK"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblAtk { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "DEF"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblDef { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "SPD"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblSpd { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "WIS"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblWis { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
                        }
                        Group { Anchor: (Height: 6); }
                        Group { Anchor: (Height: 2); Background: (Color: #00d4ff); }
                        Group { Anchor: (Height: 4); }

                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "LAZO"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblLazo { Text: "- / 100"; Style: (TextColor: #ff00ff, FontSize: 13, VerticalAlignment: Center); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "DISCIPLINA"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblDisciplina { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "VICTORIAS"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblVictorias { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "DERROTAS"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblDerrotas { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "ENERGIA"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblEnergia { Text: "- / 100"; Style: (TextColor: #ffcc00, FontSize: 13, VerticalAlignment: Center); }
                        }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 16);
                            Label { Text: "HAMBRE"; Style: (TextColor: #00d4ff, FontSize: 12, VerticalAlignment: Center); Anchor: (Width: 90); }
                            Label #LblHambre { Text: "- / 100"; Style: (TextColor: #ff6600, FontSize: 13, VerticalAlignment: Center); }
                        }
                    }

                    Group #TabMovimientosPanel {
                        Visible: false;
                        Anchor: (Height: 400, Width: 490);
                        Background: (Color: #0f1729);
                        Padding: (Full: 12);

                        Label {
                            Text: "MOVIMIENTOS / TECNICAS";
                            Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }
                        Label #LblInfoMovimiento {
                            Text: "Selecciona un movimiento";
                            Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                            Anchor: (Height: 14);
                        }
                        Group { Anchor: (Height: 4); }

                        Group {
                            LayoutMode: Left;

                            TextButton #Move1 {
                                Text: "Movimiento 1";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                            Group { Anchor: (Width: 8); }
                            TextButton #Move2 {
                                Text: "Movimiento 2";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }
                        Group { Anchor: (Height: 4); }
                        Group {
                            LayoutMode: Left;

                            TextButton #Move3 {
                                Text: "Movimiento 3";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                            Group { Anchor: (Width: 8); }
                            TextButton #Move4 {
                                Text: "Movimiento 4";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }
                        Group { Anchor: (Height: 6); }

                        Label { Text: "TUS MOVIMIENTOS"; Style: (TextColor: #00d4ff, FontSize: 12, RenderBold: true); Anchor: (Height: 16); }
                        Label #MoveName1 { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
                        Label #MoveName2 { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
                        Label #MoveName3 { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
                        Label #MoveName4 { Text: "-"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
                        Group { Anchor: (Height: 6); }

                        Label { Text: "INFO MOVIMIENTO"; Style: (TextColor: #ff00ff, FontSize: 12, RenderBold: true); Anchor: (Height: 16); }
                        Label #LblMovePower { Text: "Poder: -"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
                        Label #LblMoveHits { Text: "Golpes: -"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
                        Label #LblMoveCrit { Text: "Critico: -"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
                        Label #LblMoveRange { Text: "Alcance: -"; Style: (TextColor: #e0e8f0, FontSize: 11); Anchor: (Height: 14); }
                        Label #LblMoveDesc { Text: "-"; Style: (TextColor: #8899aa, FontSize: 11, Wrap: true); Anchor: (Height: 32); }
                    }

                    Group #TabEvolucionPanel {
                        Visible: false;
                        Anchor: (Height: 400, Width: 490);
                        Background: (Color: #0f1729);
                        Padding: (Full: 12);

                        Label {
                            Text: "EVOLUCION";
                            Style: (TextColor: #ff00ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }
                        Label #LblEvolucionInfo {
                            Text: "Digimon actual: -";
                            Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                            Anchor: (Height: 16);
                        }
                        Group { Anchor: (Height: 6); }
                        Group { Anchor: (Height: 2); Background: (Color: #ff00ff); }
                        Group { Anchor: (Height: 4); }

                        Label { Text: "EVOLUCIONES DISPONIBLES"; Style: (TextColor: #00d4ff, FontSize: 12, RenderBold: true, HorizontalAlignment: Center); Anchor: (Height: 16); }
                        Group { Anchor: (Height: 4); }
                        Group {
                            LayoutMode: Left;

                            TextButton #Evo1 {
                                Text: "-";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                            Group { Anchor: (Width: 8); }
                            TextButton #Evo2 {
                                Text: "-";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }
                        Group { Anchor: (Height: 4); }
                        Group {
                            LayoutMode: Left;

                            TextButton #Evo3 {
                                Text: "-";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                            Group { Anchor: (Width: 8); }
                            TextButton #Evo4 {
                                Text: "-";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }
                        Group { Anchor: (Height: 6); }
                        Group { Anchor: (Height: 2); Background: (Color: #00d4ff); }
                        Group { Anchor: (Height: 4); }

                        Label { Text: "CONDICIONES DE EVOLUCION"; Style: (TextColor: #00d4ff, FontSize: 12, RenderBold: true, HorizontalAlignment: Center); Anchor: (Height: 16); }
                        Label #LblEvolCondiciones {
                            Text: "-";
                            Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center, Wrap: true);
                            Anchor: (Height: 40);
                        }
                    }

                    Group #TabDeEvolucionPanel {
                        Visible: false;
                        Anchor: (Height: 400, Width: 490);
                        Background: (Color: #0f1729);
                        Padding: (Full: 12);

                        Label {
                            Text: "DE-EVOLUCION";
                            Style: (TextColor: #ff6600, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                            Anchor: (Height: 24);
                        }
                        Label {
                            Text: "La de-evolucion devuelve a tu Digimon a una forma anterior.";
                            Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center, Wrap: true);
                            Anchor: (Height: 24);
                        }
                        Group { Anchor: (Height: 4); }
                        Group { Anchor: (Height: 2); Background: (Color: #ff6600); }
                        Group { Anchor: (Height: 4); }

                        Label #LblDeEvolucionInfo {
                            Text: "Digimon actual: -";
                            Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                            Anchor: (Height: 16);
                        }
                        Group { Anchor: (Height: 4); }
                        Label { Text: "DE-EVOLUCIONES DISPONIBLES"; Style: (TextColor: #ff6600, FontSize: 12, RenderBold: true, HorizontalAlignment: Center); Anchor: (Height: 16); }
                        Group { Anchor: (Height: 4); }
                        Group {
                            LayoutMode: Left;

                            TextButton #DeEvo1 {
                                Text: "-";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                            Group { Anchor: (Width: 8); }
                            TextButton #DeEvo2 {
                                Text: "-";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }
                    }

                    Group #TabEquipamientoPanel {
                        Visible: false;
                        Anchor: (Height: 400, Width: 490);
                        Background: (Color: #0f1729);
                        Padding: (Full: 12);

                        Label {
                            Text: "EQUIPAMIENTO";
                            Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }
                        Label {
                            Text: "Equipamiento del Digimon";
                            Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                            Anchor: (Height: 14);
                        }
                        Group { Anchor: (Height: 6); }
                        Group { Anchor: (Height: 2); Background: (Color: #00d4ff); }
                        Group { Anchor: (Height: 4); }

                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 40);
                            Label { Text: "EQUIPO ACTUAL"; Style: (TextColor: #ff00ff, FontSize: 12, RenderBold: true, VerticalAlignment: Center); Anchor: (Width: 200); }
                            Group { Anchor: (Width: 8); }
                            TextButton #ItemEquipo {
                                Text: "Sin equipo";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }
                        Group { Anchor: (Height: 4); }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 40);
                            Label { Text: "ARMA"; Style: (TextColor: #00d4ff, FontSize: 12, RenderBold: true, VerticalAlignment: Center); Anchor: (Width: 200); }
                            Group { Anchor: (Width: 8); }
                            TextButton #ItemArma {
                                Text: "Sin arma";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }
                        Group { Anchor: (Height: 4); }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 40);
                            Label { Text: "ARMADURA"; Style: (TextColor: #00d4ff, FontSize: 12, RenderBold: true, VerticalAlignment: Center); Anchor: (Width: 200); }
                            Group { Anchor: (Width: 8); }
                            TextButton #ItemArmadura {
                                Text: "Sin armadura";
                                Anchor: (Width: 200, Height: 40);
                                Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }
                        Group { Anchor: (Height: 6); }
                        Group { Anchor: (Height: 2); Background: (Color: #ff00ff); }
                        Group { Anchor: (Height: 4); }

                        Label { Text: "COSMETICOS"; Style: (TextColor: #ff00ff, FontSize: 12, RenderBold: true, HorizontalAlignment: Center); Anchor: (Height: 16); }
                        Group { Anchor: (Height: 4); }
                        Group {
                            LayoutMode: Left;

                            TextButton #Cosmetico1 {
                                Text: "Aspecto Normal";
                                Anchor: (Width: 200, Height: 36);
                                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 11, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 11, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                            Group { Anchor: (Width: 8); }
                            TextButton #Cosmetico2 {
                                Text: "Aspecto Especial";
                                Anchor: (Width: 200, Height: 36);
                                Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 11, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 11, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }
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
        LOGGER.log(Level.INFO, "DigiPerfilDigimonUI - acción: " + action);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        switch (action) {
            case "tab" -> {
                String v = json.has("value") ? json.get("value").getAsString() : "stats";
                if (!v.equals("stats") && !v.equals("movimientos") && !v.equals("evolucion")
                        && !v.equals("de_evolucion") && !v.equals("equipamiento")) {
                    v = "stats";
                }
                if (!v.equals(pestana)) {
                    pestana = v;
                    rebuild();
                } else {
                    UICommandBuilder cmd = new UICommandBuilder();
                    sendUpdate(cmd);
                }
            }
            case "volver" -> onVolver(ref, store, player);
            case "seleccionar_movimiento" -> onSeleccionarMovimiento(store, json);
            case "evolucionar" -> onEvolucion(store, json);
            case "de_evolucionar" -> onDeEvolucion(store, json);
            case "equipamiento" -> onEquipamiento(store, json);
            default -> {
                UICommandBuilder cmd = new UICommandBuilder();
                sendUpdate(cmd);
            }
        }
    }

    private void applyTab(Store<EntityStore> store, UICommandBuilder cmd) {
        // Solo rellena cmd, NO llama sendUpdate (el llamador decide).
        cmd.set("#TabStatsPanel.Visible", "stats".equals(pestana));
        cmd.set("#TabMovimientosPanel.Visible", "movimientos".equals(pestana));
        cmd.set("#TabEvolucionPanel.Visible", "evolucion".equals(pestana));
        cmd.set("#TabDeEvolucionPanel.Visible", "de_evolucion".equals(pestana));
        cmd.set("#TabEquipamientoPanel.Visible", "equipamiento".equals(pestana));

        // Marca la pestaña activa estilo Cyber Sleuth (prefijo >).
        cmd.set("#TabStats.Text", "stats".equals(pestana) ? "> Stats" : "Stats");
        cmd.set("#TabMovimientos.Text", "movimientos".equals(pestana) ? "> Movim." : "Movimientos");
        cmd.set("#TabEvolucion.Text", "evolucion".equals(pestana) ? "> Evolucion" : "Evolucion");
        cmd.set("#TabDeEvolucion.Text", "de_evolucion".equals(pestana) ? "> DeEvo." : "DeEvolucion");
        cmd.set("#TabEquipamiento.Text", "equipamiento".equals(pestana) ? "> Equipo" : "Equipamiento");

        if ("stats".equals(pestana)) {
            actualizarStats(cmd, store);
        } else if ("movimientos".equals(pestana)) {
            actualizarMovimientos(cmd, store);
        } else if ("evolucion".equals(pestana)) {
            actualizarEvolucion(cmd, store);
        } else if ("de_evolucion".equals(pestana)) {
            actualizarDeEvolucion(cmd, store);
        } else if ("equipamiento".equals(pestana)) {
            actualizarEquipamiento(cmd, store);
        }
    }

    private void actualizarStats(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        DatoDigimon digimon = d.getSlot(slotActivo);
        if (digimon == null) {
            cmd.set("#DigiPortrait.Visible", false);
            cmd.set("#LblNombre.Text", "-");
            cmd.set("#LblEspecie.Text", "-");
            cmd.set("#LblNivel.Text", "-");
            cmd.set("#LblElemento.Text", "-");
            cmd.set("#LblAtributo.Text", "-");
            cmd.set("#ImgElemento.Visible", false);
            cmd.set("#ImgAtributo.Visible", false);
            cmd.set("#LblFuerte.Text", "-");
            cmd.set("#LblDebil.Text", "-");
            cmd.set("#LblHp.Text", "- / -");
            cmd.set("#LblAtk.Text", "-");
            cmd.set("#LblDef.Text", "-");
            cmd.set("#LblSpd.Text", "-");
            cmd.set("#LblWis.Text", "-");
            cmd.set("#LblLazo.Text", "- / 100");
            cmd.set("#LblDisciplina.Text", "-");
            cmd.set("#LblVictorias.Text", "-");
            cmd.set("#LblDerrotas.Text", "-");
            cmd.set("#LblEnergia.Text", "- / 100");
            cmd.set("#LblHambre.Text", "- / 100");
            return;
        }

        cmd.set("#LblNombre.Text", digimon.nombre + (digimon.dormido ? " [Zzz]" : ""));
        cmd.set("#LblEspecie.Text", digimon.especie);
        cmd.set("#LblNivel.Text", digimon.nivel + " (" + digimon.nombreNivel() + ")");
        cmd.set("#LblElemento.Text", digimon.elemento);
        cmd.set("#LblAtributo.Text", digimon.atributo);
        String rutaElem = DigiImagenes.iconoElemento(digimon.elemento);
        cmd.set("#ImgElemento.Visible", rutaElem != null);
        if (rutaElem != null) cmd.set("#ImgElemento.AssetPath", rutaElem);
        String rutaAtr = DigiImagenes.iconoAtributo(digimon.atributo);
        cmd.set("#ImgAtributo.Visible", rutaAtr != null);
        if (rutaAtr != null) cmd.set("#ImgAtributo.AssetPath", rutaAtr);
        cmd.set("#LblHp.Text", digimon.hp + " / " + digimon.maxHp);
        cmd.set("#LblAtk.Text", digimon.atk + " (+" + digimon.entAtk + ")");
        cmd.set("#LblDef.Text", digimon.def + " (+" + digimon.entDef + ")");
        cmd.set("#LblSpd.Text", digimon.spd + " (+" + digimon.entSpd + ")");
        cmd.set("#LblWis.Text", digimon.wis + " (+" + digimon.entWis + ")");
        cmd.set("#LblLazo.Text", digimon.lazo + " / 100");
        cmd.set("#LblDisciplina.Text", String.valueOf(digimon.disciplina));
        cmd.set("#LblVictorias.Text", String.valueOf(digimon.victorias));
        cmd.set("#LblDerrotas.Text", String.valueOf(digimon.derrotas));
        cmd.set("#LblEnergia.Text", digimon.energia + " / 100");
        cmd.set("#LblHambre.Text", digimon.hambre + " / 100");
        cmd.set("#DigiIcon.Text", "[" + digimon.especie + "]");
        String ruta = DigiImagenes.rutaPara(digimon.especie);
        cmd.set("#DigiPortrait.Visible", ruta != null);
        if (ruta != null) cmd.set("#DigiPortrait.AssetPath", ruta);
        java.util.List<String> fuertes = EnemigoCombate.fuerteContra(digimon.elemento);
        java.util.List<String> debiles = EnemigoCombate.debilContra(digimon.elemento);
        cmd.set("#LblFuerte.Text", fuertes.isEmpty() ? "---" : String.join(", ", fuertes));
        cmd.set("#LblDebil.Text", debiles.isEmpty() ? "---" : String.join(", ", debiles));
    }

    private void actualizarMovimientos(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        DatoDigimon digimon = d.getSlot(slotActivo);
        if (digimon == null) return;

        String[] movimientos = digimon.nombresHabilidades();
        cmd.set("#MoveName1.Text", movimientos.length > 0 ? movimientos[0] : "-");
        cmd.set("#MoveName2.Text", movimientos.length > 1 ? movimientos[1] : "-");
        cmd.set("#MoveName3.Text", movimientos.length > 2 ? movimientos[2] : "-");
        cmd.set("#MoveName4.Text", "-");

        cmd.set("#LblInfoMovimiento.Text", "Selecciona un movimiento para ver detalles");
        cmd.set("#LblMovePower.Text", "Poder: -");
        cmd.set("#LblMoveHits.Text", "Golpes: -");
        cmd.set("#LblMoveCrit.Text", "Critico: -");
        cmd.set("#LblMoveRange.Text", "Alcance: -");
        cmd.set("#LblMoveDesc.Text", "-");
    }

    private void actualizarEvolucion(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        DatoDigimon digimon = d.getSlot(slotActivo);
        if (digimon == null) return;

        int bonoLazo = d.bonoLazoCalumon();
        int bonoVic = d.bonoVicCalumon();

        cmd.set("#LblEvolucionInfo.Text", "Digimon actual: " + digimon.nombre + " (" + digimon.especie + ")"
            + (bonoLazo > 0 ? " [Calumon +15/+1]" : ""));
        cmd.set("#Evo1.Text", "Siguiente evolución (si aplica)");
        cmd.set("#Evo2.Text", "Siguiente evolución (si aplica)");

        // Verificar evoluciones disponibles
        boolean tieneProxima = false;
        String condiciones = "";

        switch (digimon.especie) {
            case "Botamon" -> {
                if (digimon.puedeEvolucionar("Koromon", bonoLazo, bonoVic)) {
                    cmd.set("#Evo1.Text", "Koromon (Baby → In-Training)");
                    tieneProxima = true;
                    condiciones = "Lazo >= 30 o Victorias >= 1";
                } else {
                    cmd.set("#Evo1.Text", "Koromon (Necesitas: Lazo 30 o 1 victoria)");
                }
            }
            case "Punimon" -> {
                if (digimon.puedeEvolucionar("Tsunomon", bonoLazo, bonoVic)) {
                    cmd.set("#Evo1.Text", "Tsunomon (Baby → In-Training)");
                    tieneProxima = true;
                    condiciones = "Lazo >= 30 o Victorias >= 1";
                } else {
                    cmd.set("#Evo1.Text", "Tsunomon (Necesitas: Lazo 30 o 1 victoria)");
                }
            }
            case "Poyomon" -> {
                if (digimon.puedeEvolucionar("Tokomon", bonoLazo, bonoVic)) {
                    cmd.set("#Evo1.Text", "Tokomon (Baby → In-Training)");
                    tieneProxima = true;
                    condiciones = "Lazo >= 30 o Victorias >= 1";
                } else {
                    cmd.set("#Evo1.Text", "Tokomon (Necesitas: Lazo 30 o 1 victoria)");
                }
            }
            case "Yuramon" -> {
                if (digimon.puedeEvolucionar("Tanemon", bonoLazo, bonoVic)) {
                    cmd.set("#Evo1.Text", "Tanemon (Baby → In-Training)");
                    tieneProxima = true;
                    condiciones = "Lazo >= 30 o Victorias >= 1";
                } else {
                    cmd.set("#Evo1.Text", "Tanemon (Necesitas: Lazo 30 o 1 victoria)");
                }
            }
            case "Pichimon" -> {
                if (digimon.puedeEvolucionar("Bukamon", bonoLazo, bonoVic)) {
                    cmd.set("#Evo1.Text", "Bukamon (Baby → In-Training)");
                    tieneProxima = true;
                    condiciones = "Lazo >= 30 o Victorias >= 1";
                } else {
                    cmd.set("#Evo1.Text", "Bukamon (Necesitas: Lazo 30 o 1 victoria)");
                }
            }
            case "Nyokimon" -> {
                if (digimon.puedeEvolucionar("Yokomon", bonoLazo, bonoVic)) {
                    cmd.set("#Evo1.Text", "Yokomon (Baby → In-Training)");
                    tieneProxima = true;
                    condiciones = "Lazo >= 30 o Victorias >= 1";
                } else {
                    cmd.set("#Evo1.Text", "Yokomon (Necesitas: Lazo 30 o 1 victoria)");
                }
            }
            case "Koromon" -> {
                if (digimon.puedeEvolucionar("Agumon", bonoLazo, bonoVic)) {
                    cmd.set("#Evo1.Text", "Agumon (In-Training → Rookie)");
                    tieneProxima = true;
                    condiciones = "Victorias >= 3, Lazo >= 40";
                } else {
                    cmd.set("#Evo1.Text", "Agumon (Necesitas: 3 victorias, Lazo 40)");
                }
            }
            case "Tsunomon" -> {
                if (digimon.puedeEvolucionar("Gabumon", bonoLazo, bonoVic)) {
                    cmd.set("#Evo1.Text", "Gabumon (In-Training → Rookie)");
                    tieneProxima = true;
                    condiciones = "Victorias >= 3, Def >= 12";
                } else {
                    cmd.set("#Evo1.Text", "Gabumon (Necesitas: 3 victorias, Def 12)");
                }
            }
            case "Tokomon" -> {
                if (digimon.puedeEvolucionar("Patamon", bonoLazo, bonoVic)) {
                    cmd.set("#Evo1.Text", "Patamon (In-Training → Rookie)");
                    tieneProxima = true;
                    condiciones = "Victorias >= 3, Wis >= 12";
                } else {
                    cmd.set("#Evo1.Text", "Patamon (Necesitas: 3 victorias, Wis 12)");
                }
            }
            case "Tanemon" -> {
                if (digimon.puedeEvolucionar("Palmon", bonoLazo, bonoVic)) {
                    cmd.set("#Evo1.Text", "Palmon (In-Training → Rookie)");
                    tieneProxima = true;
                    condiciones = "Victorias >= 3, Wis >= 11";
                } else {
                    cmd.set("#Evo1.Text", "Palmon (Necesitas: 3 victorias, Wis 11)");
                }
            }
            case "Bukamon" -> {
                if (digimon.puedeEvolucionar("Gomamon", bonoLazo, bonoVic)) {
                    cmd.set("#Evo1.Text", "Gomamon (In-Training → Rookie)");
                    tieneProxima = true;
                    condiciones = "Victorias >= 3, Spd >= 14";
                } else {
                    cmd.set("#Evo1.Text", "Gomamon (Necesitas: 3 victorias, Spd 14)");
                }
            }
            case "Yokomon" -> {
                if (digimon.puedeEvolucionar("Biyomon", bonoLazo, bonoVic)) {
                    cmd.set("#Evo1.Text", "Biyomon (In-Training → Rookie)");
                    tieneProxima = true;
                    condiciones = "Victorias >= 3, Spd >= 15";
                } else {
                    cmd.set("#Evo1.Text", "Biyomon (Necesitas: 3 victorias, Spd 15)");
                }
            }
            default -> {
                cmd.set("#Evo1.Text", "Evolución no disponible");
                cmd.set("#Evo2.Text", "No hay más evoluciones");
            }
        }

        cmd.set("#LblEvolCondiciones.Text", tieneProxima
            ? "Para evolucionar necesitas: " + condiciones
            : "Tu Digimon no puede evolucionar aún. Sigue entrenando y ganando batallas!");

        // Configurar visibilidad de evoluciones
        cmd.set("#Evo3.Text", "");
        cmd.set("#Evo4.Text", "");
    }

    private void actualizarDeEvolucion(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        DatoDigimon digimon = d.getSlot(slotActivo);
        if (digimon == null) return;

        cmd.set("#LblDeEvolucionInfo.Text", "Digimon actual: " + digimon.nombre + " (" + digimon.especie + ")");

        // Verificar si puede de-evolucionar
        if (digimon.nivel > 1) {
            cmd.set("#DeEvo1.Text", "De-evolución disponible");
            cmd.set("#DeEvo2.Text", "De-evolución disponible");
        } else {
            cmd.set("#DeEvo1.Text", "No se puede de-evolucionar más");
            cmd.set("#DeEvo2.Text", "Forma inicial");
        }
    }

    private void actualizarEquipamiento(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        DatoDigimon digimon = d.getSlot(slotActivo);
        if (digimon == null) return;

        cmd.set("#ItemEquipo.Text", "Sin equipo");
        cmd.set("#ItemArma.Text", "Sin arma");
        cmd.set("#ItemArmadura.Text", "Sin armadura");
        cmd.set("#Cosmetico1.Text", "Aspecto Normal");
        cmd.set("#Cosmetico2.Text", "Aspecto Especial");
    }

    private void onVolver(Ref<EntityStore> ref, Store<EntityStore> store, Player player) {
        LOGGER.log(Level.INFO, "Volver del perfil");
        // Abrir directo (sin cerrar antes): el doble cambio de página rompía la UI destino.
        SapotamaTabUI.open(playerRef, ref, store);
    }

    private void onSeleccionarMovimiento(Store<EntityStore> store, JsonObject json) {
        LOGGER.log(Level.INFO, "Seleccionar movimiento");
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) {
            UICommandBuilder empty = new UICommandBuilder();
            sendUpdate(empty);
            return;
        }

        DatoDigimon digimon = d.getSlot(slotActivo);
        if (digimon == null) {
            UICommandBuilder empty = new UICommandBuilder();
            sendUpdate(empty);
            return;
        }

        String[] movimientos = digimon.nombresHabilidades();
        int indice = 1;
        try {
            indice = json.has("value") ? Integer.parseInt(json.get("value").getAsString()) : 1;
        } catch (NumberFormatException ignored) {
        }

        UICommandBuilder cmd = new UICommandBuilder();
        if (indice >= 1 && indice <= movimientos.length && !movimientos[indice - 1].equals("-")) {
            String elegido = movimientos[indice - 1];
            cmd.set("#LblInfoMovimiento.Text", "Seleccionado: " + elegido);
            cmd.set("#LblMoveDesc.Text", elegido + " listo para el combate.");
        } else {
            actualizarMovimientos(cmd, store);
        }
        sendUpdate(cmd);
    }

    private void onEvolucion(Store<EntityStore> store, JsonObject json) {
        LOGGER.log(Level.INFO, "Evolucionar");
        UICommandBuilder cmd = new UICommandBuilder();
        actualizarEvolucion(cmd, store);
        sendUpdate(cmd);
    }

    private void onDeEvolucion(Store<EntityStore> store, JsonObject json) {
        LOGGER.log(Level.INFO, "De-evolucionar");
        UICommandBuilder cmd = new UICommandBuilder();
        actualizarDeEvolucion(cmd, store);
        sendUpdate(cmd);
    }

    private void onEquipamiento(Store<EntityStore> store, JsonObject json) {
        LOGGER.log(Level.INFO, "Equipamiento");
        UICommandBuilder cmd = new UICommandBuilder();
        actualizarEquipamiento(cmd, store);
        sendUpdate(cmd);
    }
}
