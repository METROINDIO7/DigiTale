package com.digitale.ui;

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

public class DigiPerfilDigimonUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(DigiPerfilDigimonUI.class.getName());

    private String slotActivo = "A"; // A o B
    private String pestana = "stats";

    public DigiPerfilDigimonUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store,
                            String slot) {
        LOGGER.log(Level.INFO, "DigiPerfilDigimonUI.open() - slot: " + slot);

        DigiPerfilDigimonUI page = new DigiPerfilDigimonUI(playerRef);
        page.slotActivo = slot != null ? slot : "A";

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
        String ui = buildPanel();
        cmd.appendInline(null, ui);

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

        // Eventos de movimientos
        event.addEventBinding(CustomUIEventBindingType.Activating, "#Move1",
            EventData.of("action", "seleccionar_movimiento").append("value", "1"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#Move2",
            EventData.of("action", "seleccionar_movimiento").append("value", "2"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#Move3",
            EventData.of("action", "seleccionar_movimiento").append("value", "3"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#Move4",
            EventData.of("action", "seleccionar_movimiento").append("value", "4"), false);

        // Eventos de evolución
        event.addEventBinding(CustomUIEventBindingType.Activating, "#Evo1",
            EventData.of("action", "evolucionar").append("value", "1"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#Evo2",
            EventData.of("action", "evolucionar").append("value", "2"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#Evo3",
            EventData.of("action", "evolucionar").append("value", "3"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#Evo4",
            EventData.of("action", "evolucionar").append("value", "4"), false);

        // Eventos de de-evolución
        event.addEventBinding(CustomUIEventBindingType.Activating, "#DeEvo1",
            EventData.of("action", "de_evolucionar").append("value", "1"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#DeEvo2",
            EventData.of("action", "de_evolucionar").append("value", "2"), false);

        // Eventos de equipamiento
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

        // Inicializar primera pestaña
        applyTab(store, cmd);
    }

    private String buildPanel() {
        return """
            $C = "Common.ui";
            $P = "Digi.ui";

            Group {
                LayoutMode: Top;
                Anchor: (Height: 580, Width: 520);

                $C.@DecoratedContainer {
                    #Title {
                        Group {
                            LayoutMode: Left;
                            Padding: (Horizontal: 16, Vertical: 4);

                            Label {
                                Text: "PERFIL DEL DIGIMON";
                                Style: (TextColor: #ff00ff, FontSize: 18, RenderBold: true);
                                Anchor: (Left: 8);
                            }

                            Group { FlexWeight: 1; }

                            TextButton #BtnVolver {
                                Anchor: (Width: 100, Height: 40);
                                Text: "Volver";
                                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 14, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)));
                            }
                        }
                    }

                    $C.@HeaderTabsStyle #TabStats {
                        Anchor: (Left: 2, Right: 2, Bottom: 0);
                        SelectedTab: "stats";

                        TabButton #TabStats {
                            Icon: "Digi/Digi_Tab_Stats.png";
                            TooltipText: "Stats";
                            Id: "stats";
                        }
                        TabButton #TabMovimientos {
                            Icon: "Digi/Digi_Tab_Movimientos.png";
                            TooltipText: "Movimientos";
                            Id: "movimientos";
                        }
                        TabButton #TabEvolucion {
                            Icon: "Digi/Digi_Tab_Evolucion.png";
                            TooltipText: "Evolución";
                            Id: "evolucion";
                        }
                        TabButton #TabDeEvolucion {
                            Icon: "Digi/Digi_Tab_DeEvolucion.png";
                            TooltipText: "De-Evolución";
                            Id: "de_evolucion";
                        }
                        TabButton #TabEquipamiento {
                            Icon: "Digi/Digi_Tab_Equipamiento.png";
                            TooltipText: "Equipamiento";
                            Id: "equipamiento";
                        }
                    }

                    #ContentTabs {
                        LayoutMode: Top;
                        Padding: (Full: 6);
                        Anchor: (Top: 6, Bottom: 8, Left: 8, Right: 8);

                        $P.@Panel #TabStatsPanel {
                            Visible: true;
                            Anchor: (Height: 350, Width: 460);
                            Background: (Color: #12121e);

                            Group {
                                LayoutMode: Top;
                                Padding: (Full: 12);

                                Label {
                                    Text: "ESTADISTICAS";
                                    Style: (TextColor: #00d4ff, FontSize: 16, RenderBold: true, HorizontalAlignment: Center);
                                    Anchor: (Height: 22);
                                }

                                Group { Anchor: (Height: 8); }

                                $C.@SimpleContainer #Portrait {
                                    Anchor: (Width: 120, Height: 120);
                                    Padding: (Horizontal: 10, Vertical: 5);
                                    AssetImage #DigiIcon {
                                        Anchor: (Top: 0, Left: 0, Width: 100, Height: 100);
                                        AssetPath: "Icons/ModelsGenerated/DigiPlaceholder.png";
                                    }
                                }

                                Group { FlexWeight: 1; }

                                Group {
                                    LayoutMode: Top;
                                    Padding: (Vertical: 4);

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "NOMBRE";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblNombre {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 14, RenderBold: true);
                                        }
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "ESPECIE";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblEspecie {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "NIVEL";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblNivel {
                                            Text: "-";
                                            Style: (TextColor: #00ff88, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "ELEMENTO";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblElemento {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 8); }

                                    $C.@ContentSeparator {}

                                    Group { Anchor: (Height: 8); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "HP";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblHp {
                                            Text: "- / -";
                                            Style: (TextColor: #00ff88, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "ATK";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblAtk {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "DEF";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblDef {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "SPD";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblSpd {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "WIS";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblWis {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 8); }

                                    $C.@ContentSeparator {}

                                    Group { Anchor: (Height: 8); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "LAZO";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblLazo {
                                            Text: "- / 100";
                                            Style: (TextColor: #ff00ff, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "DISCIPLINA";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblDisciplina {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "VICTORIAS";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblVictorias {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "DERROTAS";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblDerrotas {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "ENERGIA";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblEnergia {
                                            Text: "- / 100";
                                            Style: (TextColor: #ffcc00, FontSize: 14);
                                        }
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Group {
                                        LayoutMode: Left;
                                        Label {
                                            Text: "HAMBRE";
                                            Style: (TextColor: #00d4ff, FontSize: 12);
                                            Anchor: (Width: 80);
                                        }
                                        Label #LblHambre {
                                            Text: "- / 100";
                                            Style: (TextColor: #ff6600, FontSize: 14);
                                        }
                                    }
                                }
                            }
                        }

                        $P.@Panel #TabMovimientosPanel {
                            Visible: false;
                            Anchor: (Height: 350, Width: 460);
                            Background: (Color: #12121e);

                            Group {
                                LayoutMode: Top;
                                Padding: (Full: 12);

                                Label {
                                    Text: "MOVIMIENTOS / TECNICAS";
                                    Style: (TextColor: #00d4ff, FontSize: 16, RenderBold: true, HorizontalAlignment: Center);
                                    Anchor: (Height: 22);
                                }

                                Group { Anchor: (Height: 8); }

                                Label #LblInfoMovimiento {
                                    Text: "Selecciona un movimiento";
                                    Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                    Anchor: (Height: 18);
                                }

                                Group { Anchor: (Height: 8); }

                                Group {
                                    LayoutMode: Left;

                                    $C.@SecondaryButton #Move1 {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        LayoutMode: Top;

                                        Group {
                                            LayoutMode: Left;
                                            AssetImage #MoveIcon1 {
                                                Anchor: (Width: 48, Height: 48);
                                                AssetPath: "UI/Textures/Icons/Types/FireType.png";
                                            }

                                            Group { FlexWeight: 1; }

                                            Label #MoveName1 {
                                                Text: "Movimiento 1";
                                                Style: (TextColor: #e0e8f0, FontSize: 14, VerticalAlignment: Center);
                                                Anchor: (Left: 8);
                                            }
                                        }
                                    }

                                    Group { FlexWeight: 1; }

                                    $C.@SecondaryButton #Move2 {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        LayoutMode: Top;

                                        Group {
                                            LayoutMode: Left;
                                            AssetImage #MoveIcon2 {
                                                Anchor: (Width: 48, Height: 48);
                                                AssetPath: "UI/Textures/Icons/Types/WaterType.png";
                                            }

                                            Group { FlexWeight: 1; }

                                            Label #MoveName2 {
                                                Text: "Movimiento 2";
                                                Style: (TextColor: #e0e8f0, FontSize: 14, VerticalAlignment: Center);
                                                Anchor: (Left: 8);
                                            }
                                        }
                                    }
                                }

                                Group { Anchor: (Height: 8); }

                                Group {
                                    LayoutMode: Left;

                                    $C.@SecondaryButton #Move3 {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        LayoutMode: Top;

                                        Group {
                                            LayoutMode: Left;
                                            AssetImage #MoveIcon3 {
                                                Anchor: (Width: 48, Height: 48);
                                                AssetPath: "UI/Textures/Icons/Types/PlantType.png";
                                            }

                                            Group { FlexWeight: 1; }

                                            Label #MoveName3 {
                                                Text: "Movimiento 3";
                                                Style: (TextColor: #e0e8f0, FontSize: 14, VerticalAlignment: Center);
                                                Anchor: (Left: 8);
                                            }
                                        }
                                    }

                                    Group { FlexWeight: 1; }

                                    $C.@SecondaryButton #Move4 {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        LayoutMode: Top;

                                        Group {
                                            LayoutMode: Left;
                                            AssetImage #MoveIcon4 {
                                                Anchor: (Width: 48, Height: 48);
                                                AssetPath: "UI/Textures/Icons/Types/ElectricType.png";
                                            }

                                            Group { FlexWeight: 1; }

                                            Label #MoveName4 {
                                                Text: "Movimiento 4";
                                                Style: (TextColor: #e0e8f0, FontSize: 14, VerticalAlignment: Center);
                                                Anchor: (Left: 8);
                                            }
                                        }
                                    }
                                }

                                Group { Anchor: (Height: 8); }

                                Group {
                                    LayoutMode: Top;
                                    Anchor: (Height: 120);

                                    Label {
                                        Text: "INFO MOVIMIENTO";
                                        Style: (TextColor: #ff00ff, FontSize: 13, RenderBold: true);
                                        Anchor: (Height: 18);
                                    }

                                    Group { Anchor: (Height: 4); }

                                    Label #LblMovePower {
                                        Text: "Poder: -";
                                        Style: (TextColor: #e0e8f0, FontSize: 12);
                                        Anchor: (Height: 16);
                                    }

                                    Label #LblMoveHits {
                                        Text: "Golpes: -";
                                        Style: (TextColor: #e0e8f0, FontSize: 12);
                                        Anchor: (Height: 16);
                                    }

                                    Label #LblMoveCrit {
                                        Text: "Critico: -";
                                        Style: (TextColor: #e0e8f0, FontSize: 12);
                                        Anchor: (Height: 16);
                                    }

                                    Label #LblMoveRange {
                                        Text: "Alcance: -";
                                        Style: (TextColor: #e0e8f0, FontSize: 12);
                                        Anchor: (Height: 16);
                                    }

                                    Group { Anchor: (Height: 4); }

                                    $C.@ContentSeparator {}

                                    Group { Anchor: (Height: 4); }

                                    Label #LblMoveDesc {
                                        Text: "-";
                                        Style: (TextColor: #8899aa, FontSize: 12, Wrap: true);
                                        Anchor: (Height: 60);
                                    }
                                }
                            }
                        }

                        $P.@Panel #TabEvolucionPanel {
                            Visible: false;
                            Anchor: (Height: 350, Width: 460);
                            Background: (Color: #12121e);

                            Group {
                                LayoutMode: Top;
                                Padding: (Full: 12);

                                Label {
                                    Text: "EVOLUCION";
                                    Style: (TextColor: #ff00ff, FontSize: 16, RenderBold: true, HorizontalAlignment: Center);
                                    Anchor: (Height: 22);
                                }

                                Group { Anchor: (Height: 8); }

                                Label #LblEvolucionInfo {
                                    Text: "Digimon actual: -";
                                    Style: (TextColor: #e0e8f0, FontSize: 13, HorizontalAlignment: Center);
                                    Anchor: (Height: 18);
                                }

                                Group { Anchor: (Height: 8); }

                                Label {
                                    Text: "EVOLUCIONES DISPONIBLES";
                                    Style: (TextColor: #00d4ff, FontSize: 13, RenderBold: true);
                                    Anchor: (Height: 18);
                                }

                                Group { Anchor: (Height: 6); }

                                Group {
                                    LayoutMode: Left;

                                    $C.@SecondaryButton #Evo1 {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        Label {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center);
                                        }
                                    }

                                    Group { FlexWeight: 1; }

                                    $C.@SecondaryButton #Evo2 {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        Label {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center);
                                        }
                                    }
                                }

                                Group { Anchor: (Height: 6); }

                                Group {
                                    LayoutMode: Left;

                                    $C.@SecondaryButton #Evo3 {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        Label {
                                            Text: "-";
                                            Style: (TextColor: #8899aa, FontSize: 13, VerticalAlignment: Center);
                                        }
                                    }

                                    Group { FlexWeight: 1; }

                                    $C.@SecondaryButton #Evo4 {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        Label {
                                            Text: "-";
                                            Style: (TextColor: #8899aa, FontSize: 13, VerticalAlignment: Center);
                                        }
                                    }
                                }

                                Group { Anchor: (Height: 8); }

                                Group {
                                    LayoutMode: Top;

                                    Label {
                                        Text: "CONDICIONES DE EVOLUCION";
                                        Style: (TextColor: #00d4ff, FontSize: 12, RenderBold: true);
                                        Anchor: (Height: 18);
                                    }

                                    Label #LblEvolCondiciones {
                                        Text: "-";
                                        Style: (TextColor: #8899aa, FontSize: 12, Wrap: true);
                                        Anchor: (Height: 100);
                                    }
                                }
                            }
                        }

                        $P.@Panel #TabDeEvolucionPanel {
                            Visible: false;
                            Anchor: (Height: 350, Width: 460);
                            Background: (Color: #12121e);

                            Group {
                                LayoutMode: Top;
                                Padding: (Full: 12);

                                Label {
                                    Text: "DE-EVOLUCION";
                                    Style: (TextColor: #ff6600, FontSize: 16, RenderBold: true, HorizontalAlignment: Center);
                                    Anchor: (Height: 22);
                                }

                                Group { Anchor: (Height: 8); }

                                Label {
                                    Text: "La de-evolucion devuelve a tu Digimon a una forma anterior.";
                                    Style: (TextColor: #8899aa, FontSize: 12, Wrap: true);
                                    Anchor: (Height: 40);
                                }

                                Group { Anchor: (Height: 8); }

                                $C.@ContentSeparator {}

                                Group { Anchor: (Height: 8); }

                                Label #LblDeEvolucionInfo {
                                    Text: "Digimon actual: -";
                                    Style: (TextColor: #e0e8f0, FontSize: 13, HorizontalAlignment: Center);
                                    Anchor: (Height: 18);
                                }

                                Group { Anchor: (Height: 6); }

                                Label {
                                    Text: "DE-EVOLUCIONES DISPONIBLES";
                                    Style: (TextColor: #ff6600, FontSize: 13, RenderBold: true);
                                    Anchor: (Height: 18);
                                }

                                Group { Anchor: (Height: 6); }

                                Group {
                                    LayoutMode: Left;

                                    $C.@SecondaryButton #DeEvo1 {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        Label {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center);
                                        }
                                    }

                                    Group { FlexWeight: 1; }

                                    $C.@SecondaryButton #DeEvo2 {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        Label {
                                            Text: "-";
                                            Style: (TextColor: #e0e8f0, FontSize: 13, VerticalAlignment: Center);
                                        }
                                    }
                                }
                            }
                        }

                        $P.@Panel #TabEquipamientoPanel {
                            Visible: false;
                            Anchor: (Height: 350, Width: 460);
                            Background: (Color: #12121e);

                            Group {
                                LayoutMode: Top;
                                Padding: (Full: 12);

                                Label {
                                    Text: "EQUIPAMIENTO";
                                    Style: (TextColor: #00d4ff, FontSize: 16, RenderBold: true, HorizontalAlignment: Center);
                                    Anchor: (Height: 22);
                                }

                                Group { Anchor: (Height: 8); }

                                Label {
                                    Text: "Equipamiento del Digimon";
                                    Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                    Anchor: (Height: 18);
                                }

                                Group { Anchor: (Height: 8); }

                                $C.@ContentSeparator {}

                                Group { Anchor: (Height: 8); }

                                Group {
                                    LayoutMode: Left;

                                    Label {
                                        Text: "EQUIPO ACTUAL";
                                        Style: (TextColor: #ff00ff, FontSize: 13, RenderBold: true);
                                        Anchor: (Width: 200);
                                    }

                                    Group { FlexWeight: 1; }

                                    $C.@SecondaryButton #ItemEquipo {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        Label {
                                            Text: "Sin equipo";
                                            Style: (TextColor: #8899aa, FontSize: 13, VerticalAlignment: Center);
                                        }
                                    }
                                }

                                Group { Anchor: (Height: 8); }

                                $C.@ContentSeparator {}

                                Group { Anchor: (Height: 8); }

                                Group {
                                    LayoutMode: Left;

                                    Label {
                                        Text: "ARMA";
                                        Style: (TextColor: #00d4ff, FontSize: 13, RenderBold: true);
                                        Anchor: (Width: 200);
                                    }

                                    Group { FlexWeight: 1; }

                                    $C.@SecondaryButton #ItemArma {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        Label {
                                            Text: "Sin arma";
                                            Style: (TextColor: #8899aa, FontSize: 13, VerticalAlignment: Center);
                                        }
                                    }
                                }

                                Group { Anchor: (Height: 8); }

                                $C.@ContentSeparator {}

                                Group { Anchor: (Height: 8); }

                                Group {
                                    LayoutMode: Left;

                                    Label {
                                        Text: "ARMADURA";
                                        Style: (TextColor: #00d4ff, FontSize: 13, RenderBold: true);
                                        Anchor: (Width: 200);
                                    }

                                    Group { FlexWeight: 1; }

                                    $C.@SecondaryButton #ItemArmadura {
                                        Anchor: (Width: 200, Height: 50);
                                        Padding: (Horizontal: 8, Vertical: 8);
                                        Label {
                                            Text: "Sin armadura";
                                            Style: (TextColor: #8899aa, FontSize: 13, VerticalAlignment: Center);
                                        }
                                    }
                                }

                                Group { Anchor: (Height: 8); }

                                $C.@ContentSeparator {}

                                Group { Anchor: (Height: 8); }

                                Label {
                                    Text: "COSMETICOS";
                                    Style: (TextColor: #ff00ff, FontSize: 13, RenderBold: true);
                                    Anchor: (Height: 18);
                                }

                                Group { Anchor: (Height: 4); }

                                $C.@SecondaryButton #Cosmetico1 {
                                    Anchor: (Width: 200, Height: 40);
                                    Padding: (Horizontal: 8, Vertical: 4);
                                    Label {
                                        Text: "Aspecto Normal";
                                        Style: (TextColor: #e0e8f0, FontSize: 12, VerticalAlignment: Center);
                                    }
                                }

                                Group { Anchor: (Height: 4); }

                                $C.@SecondaryButton #Cosmetico2 {
                                    Anchor: (Width: 200, Height: 40);
                                    Padding: (Horizontal: 8, Vertical: 4);
                                    Label {
                                        Text: "Aspecto Especial";
                                        Style: (TextColor: #8899aa, FontSize: 12, VerticalAlignment: Center);
                                    }
                                }
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
                pestana = json.has("value") ? json.get("value").getAsString() : "stats";
                applyTab(store, new UICommandBuilder());
            }
            case "volver" -> onVolver(ref, store, player);
            case "seleccionar_movimiento" -> onSeleccionarMovimiento(store, json);
            case "evolucionar" -> onEvolucion(store, json);
            case "de_evolucionar" -> onDeEvolucion(store, json);
            case "equipamiento" -> onEquipamiento(store, json);
        }
    }

    private void applyTab(Store<EntityStore> store, UICommandBuilder cmd) {
        cmd.set("#TabStatsPanel.Visible", "stats".equals(pestana));
        cmd.set("#TabMovimientosPanel.Visible", "movimientos".equals(pestana));
        cmd.set("#TabEvolucionPanel.Visible", "evolucion".equals(pestana));
        cmd.set("#TabDeEvolucionPanel.Visible", "de_evolucion".equals(pestana));
        cmd.set("#TabEquipamientoPanel.Visible", "equipamiento".equals(pestana));

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

        sendUpdate(cmd);
    }

    private void actualizarStats(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        DatoDigimon digimon = "A".equals(slotActivo) ? d.companeroA : d.companeroB;
        if (digimon == null) {
            cmd.set("#LblNombre.Text", "-");
            cmd.set("#LblEspecie.Text", "-");
            cmd.set("#LblNivel.Text", "-");
            cmd.set("#LblElemento.Text", "-");
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
            cmd.set("#DigiIcon.AssetPath", "Icons/ModelsGenerated/DigiPlaceholder.png");
            return;
        }

        cmd.set("#LblNombre.Text", digimon.nombre);
        cmd.set("#LblEspecie.Text", digimon.especie);
        cmd.set("#LblNivel.Text", digimon.nivel + " (" + digimon.nombreNivel() + ")");
        cmd.set("#LblElemento.Text", digimon.elemento);
        cmd.set("#LblHp.Text", digimon.hp + " / " + digimon.maxHp);
        cmd.set("#LblAtk.Text", digimon.atk + " (+" + digimon.entAtk + ")");
        cmd.set("#LblDef.Text", digimon.def + " (+" + digimon.entDef + ")");
        cmd.set("#LblSpd.Text", digimon.spd + " (+" + digimon.entSpd + ")");
        cmd.set("#LblWis.Text", digimon.wis + " (+" + digimon.entWis + ")");
        cmd.set("#LblLazo.Text", digimon.lazo + " / 100");
        cmd.set("#LblDisciplina.Text", digimon.disciplina);
        cmd.set("#LblVictorias.Text", digimon.victorias);
        cmd.set("#LblDerrotas.Text", digimon.derrotas);
        cmd.set("#LblEnergia.Text", digimon.energia + " / 100");
        cmd.set("#LblHambre.Text", digimon.hambre + " / 100");
        cmd.set("#DigiIcon.AssetPath", "Icons/ModelsGenerated/" + digimon.especie + ".png");
    }

    private void actualizarMovimientos(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        DatoDigimon digimon = "A".equals(slotActivo) ? d.companeroA : d.companeroB;
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

        DatoDigimon digimon = "A".equals(slotActivo) ? d.companeroA : d.companeroB;
        if (digimon == null) return;

        cmd.set("#LblEvolucionInfo.Text", "Digimon actual: " + digimon.nombre + " (" + digimon.especie + ")");
        cmd.set("#Evo1.Label.Text", "Siguiente evolución (si aplica)");
        cmd.set("#Evo2.Label.Text", "Siguiente evolución (si aplica)");

        // Verificar evoluciones disponibles
        boolean tieneProxima = false;
        String condiciones = "";

        switch (digimon.especie) {
            case "Botamon" -> {
                if (digimon.puedeEvolucionar("Koromon")) {
                    cmd.set("#Evo1.Label.Text", "Koromon (Baby → In-Training)");
                    tieneProxima = true;
                    condiciones = "Lazo >= 30 o Victorias >= 1";
                } else {
                    cmd.set("#Evo1.Label.Text", "Koromon (Necesitas: Lazo 30 o 1 victoria)");
                }
            }
            case "Punimon" -> {
                if (digimon.puedeEvolucionar("Tsunomon")) {
                    cmd.set("#Evo1.Label.Text", "Tsunomon (Baby → In-Training)");
                    tieneProxima = true;
                    condiciones = "Lazo >= 30 o Victorias >= 1";
                } else {
                    cmd.set("#Evo1.Label.Text", "Tsunomon (Necesitas: Lazo 30 o 1 victoria)");
                }
            }
            case "Poyomon" -> {
                if (digimon.puedeEvolucionar("Tokomon")) {
                    cmd.set("#Evo1.Label.Text", "Tokomon (Baby → In-Training)");
                    tieneProxima = true;
                    condiciones = "Lazo >= 30 o Victorias >= 1";
                } else {
                    cmd.set("#Evo1.Label.Text", "Tokomon (Necesitas: Lazo 30 o 1 victoria)");
                }
            }
            case "Yuramon" -> {
                if (digimon.puedeEvolucionar("Tanemon")) {
                    cmd.set("#Evo1.Label.Text", "Tanemon (Baby → In-Training)");
                    tieneProxima = true;
                    condiciones = "Lazo >= 30 o Victorias >= 1";
                } else {
                    cmd.set("#Evo1.Label.Text", "Tanemon (Necesitas: Lazo 30 o 1 victoria)");
                }
            }
            case "Pichimon" -> {
                if (digimon.puedeEvolucionar("Bukamon")) {
                    cmd.set("#Evo1.Label.Text", "Bukamon (Baby → In-Training)");
                    tieneProxima = true;
                    condiciones = "Lazo >= 30 o Victorias >= 1";
                } else {
                    cmd.set("#Evo1.Label.Text", "Bukamon (Necesitas: Lazo 30 o 1 victoria)");
                }
            }
            case "Nyokimon" -> {
                if (digimon.puedeEvolucionar("Yokomon")) {
                    cmd.set("#Evo1.Label.Text", "Yokomon (Baby → In-Training)");
                    tieneProxima = true;
                    condiciones = "Lazo >= 30 o Victorias >= 1";
                } else {
                    cmd.set("#Evo1.Label.Text", "Yokomon (Necesitas: Lazo 30 o 1 victoria)");
                }
            }
            case "Koromon" -> {
                if (digimon.puedeEvolucionar("Agumon")) {
                    cmd.set("#Evo1.Label.Text", "Agumon (In-Training → Rookie)");
                    tieneProxima = true;
                    condiciones = "Victorias >= 3, Lazo >= 40";
                } else {
                    cmd.set("#Evo1.Label.Text", "Agumon (Necesitas: 3 victorias, Lazo 40)");
                }
            }
            case "Tsunomon" -> {
                if (digimon.puedeEvolucionar("Gabumon")) {
                    cmd.set("#Evo1.Label.Text", "Gabumon (In-Training → Rookie)");
                    tieneProxima = true;
                    condiciones = "Victorias >= 3, Def >= 12";
                } else {
                    cmd.set("#Evo1.Label.Text", "Gabumon (Necesitas: 3 victorias, Def 12)");
                }
            }
            case "Tokomon" -> {
                if (digimon.puedeEvolucionar("Patamon")) {
                    cmd.set("#Evo1.Label.Text", "Patamon (In-Training → Rookie)");
                    tieneProxima = true;
                    condiciones = "Victorias >= 3, Wis >= 12";
                } else {
                    cmd.set("#Evo1.Label.Text", "Patamon (Necesitas: 3 victorias, Wis 12)");
                }
            }
            case "Tanemon" -> {
                if (digimon.puedeEvolucionar("Palmon")) {
                    cmd.set("#Evo1.Label.Text", "Palmon (In-Training → Rookie)");
                    tieneProxima = true;
                    condiciones = "Victorias >= 3, Wis >= 11";
                } else {
                    cmd.set("#Evo1.Label.Text", "Palmon (Necesitas: 3 victorias, Wis 11)");
                }
            }
            case "Bukamon" -> {
                if (digimon.puedeEvolucionar("Gomamon")) {
                    cmd.set("#Evo1.Label.Text", "Gomamon (In-Training → Rookie)");
                    tieneProxima = true;
                    condiciones = "Victorias >= 3, Spd >= 14";
                } else {
                    cmd.set("#Evo1.Label.Text", "Gomamon (Necesitas: 3 victorias, Spd 14)");
                }
            }
            case "Yokomon" -> {
                if (digimon.puedeEvolucionar("Biyomon")) {
                    cmd.set("#Evo1.Label.Text", "Biyomon (In-Training → Rookie)");
                    tieneProxima = true;
                    condiciones = "Victorias >= 3, Spd >= 15";
                } else {
                    cmd.set("#Evo1.Label.Text", "Biyomon (Necesitas: 3 victorias, Spd 15)");
                }
            }
            default -> {
                cmd.set("#Evo1.Label.Text", "Evolución no disponible");
                cmd.set("#Evo2.Label.Text", "No hay más evoluciones");
            }
        }

        cmd.set("#LblEvolCondiciones.Text", tieneProxima
            ? "Para evolucionar necesitas: " + condiciones
            : "Tu Digimon no puede evolucionar aún. Sigue entrenando y ganando batallas!");

        // Configurar visibilidad de evoluciones
        cmd.set("#Evo3.Label.Text", "");
        cmd.set("#Evo4.Label.Text", "");
    }

    private void actualizarDeEvolucion(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        DatoDigimon digimon = "A".equals(slotActivo) ? d.companeroA : d.companeroB;
        if (digimon == null) return;

        cmd.set("#LblDeEvolucionInfo.Text", "Digimon actual: " + digimon.nombre + " (" + digimon.especie + ")");

        // Verificar si puede de-evolucionar
        if (digimon.nivel > 1) {
            cmd.set("#DeEvo1.Label.Text", "De-evolución disponible");
            cmd.set("#DeEvo2.Label.Text", "De-evolución disponible");
        } else {
            cmd.set("#DeEvo1.Label.Text", "No se puede de-evolucionar más");
            cmd.set("#DeEvo2.Label.Text", "Forma inicial");
        }
    }

    private void actualizarEquipamiento(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        DatoDigimon digimon = "A".equals(slotActivo) ? d.companeroA : d.companeroB;
        if (digimon == null) return;

        cmd.set("#ItemEquipo.Label.Text", "Sin equipo");
        cmd.set("#ItemArma.Label.Text", "Sin arma");
        cmd.set("#ItemArmadura.Label.Text", "Sin armadura");
        cmd.set("#Cosmetico1.Label.Text", "Aspecto Normal");
        cmd.set("#Cosmetico2.Label.Text", "Aspecto Especial");
    }

    private void onVolver(Ref<EntityStore> ref, Store<EntityStore> store, Player player) {
        LOGGER.log(Level.INFO, "Volver del perfil");
        player.getPageManager().setPage(ref, store,
            com.hypixel.hytale.protocol.packets.interface_.Page.None);

        // Volver al menú principal de tabs
        SapotamaTabUI.open(playerRef, ref, store);
    }

    private void onSeleccionarMovimiento(Store<EntityStore> store, JsonObject json) {
        LOGGER.log(Level.INFO, "Seleccionar movimiento");
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        DatoDigimon digimon = "A".equals(slotActivo) ? d.companeroA : d.companeroB;
        if (digimon == null) return;

        String[] movimientos = digimon.nombresHabilidades();
        int indice = json.has("value") ? Integer.parseInt(json.get("value").getAsString()) : 1;

        if (indice >= 1 && indice <= movimientos.length && !movimientos[indice - 1].equals("-")) {
            // Aquí se podría agregar lógica para mostrar los detalles del movimiento seleccionado
            // Por ahora solo actualizamos la UI
        }

        applyTab(store, new UICommandBuilder());
    }

    private void onEvolucion(Store<EntityStore> store, JsonObject json) {
        LOGGER.log(Level.INFO, "Evolucionar");
        // Lógica de evolución
    }

    private void onDeEvolucion(Store<EntityStore> store, JsonObject json) {
        LOGGER.log(Level.INFO, "De-evolucionar");
        // Lógica de de-evolución
    }

    private void onEquipamiento(Store<EntityStore> store, JsonObject json) {
        LOGGER.log(Level.INFO, "Equipamiento");
        // Lógica de equipamiento
    }
}
