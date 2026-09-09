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
import java.util.logging.Logger;

public class DigiStartMenuUI extends CustomUIPage {
    private static final Logger LOGGER = Logger.getLogger(DigiStartMenuUI.class.getName());

    public DigiStartMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CantClose);
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store) {
        LOGGER.info("DigiStartMenuUI.open()");

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null || !datos.tieneEquipo) {
            LOGGER.info("Sin equipo - abriendo DigiStartMenuUI");
            DigiStartMenuUI page = new DigiStartMenuUI(playerRef);
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null) {
                player.getPageManager().openCustomPage(ref, store, page);
            }
            return;
        }

        LOGGER.info("Con equipo - abriendo SapotamaTabUI");
        SapotamaTabUI.open(playerRef, ref, store);
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder event,
                      @Nonnull Store<EntityStore> store) {
        String ui = """
            Group {
                LayoutMode: Top;
                Anchor: (Height: 580, Width: 620);
                Background: (Color: #1a1a2e);
                Padding: 15;

                Label {
                    Text: "DIGIMON DigiRise";
                    Style: (FontSize: 24, TextColor: #00d4ff, HorizontalAlignment: Center, RenderBold: true);
                    Anchor: (Height: 30);
                }
                Label {
                    Text: "Elige tu primer companero Digimon!";
                    Style: (FontSize: 18, TextColor: #00d4ff, HorizontalAlignment: Center);
                    Anchor: (Height: 22);
                }
                Label {
                    Text: "Selecciona 2 diferentes";
                    Style: (FontSize: 13, TextColor: #8899aa, HorizontalAlignment: Center);
                    Anchor: (Height: 18);
                }
                Group { Anchor: (Height: 6); }

                Group {
                    LayoutMode: Top;
                    Padding: 12;

                    // Fila 1: Botamon | Kuramon
                    Group {
                        LayoutMode: Left;
                        Padding: (Vertical: 6);

                        Group #CardBotamon {
                            Anchor: (Width: 170, Height: 180);
                            Background: (Color: #16213e);
                            Border: (Size: 2, Color: #00d4ff);
                            LayoutMode: Top;
                            Padding: 8;

                            AssetImage #ImgBotamon {
                                Anchor: (Width: 80, Height: 80);
                                AssetPath: "Common/UI/Custom/png_Digis/botamon.png";
                            }

                            Group { Anchor: (Height: 6); }

                            Label #TxtBotamon {
                                Text: "Botamon";
                                Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 20);
                            }

                            Label #LvlBotamon {
                                Text: "Baby";
                                Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                        }

                        Group { Anchor: (Width: 8); }

                        Group #CardKuramon {
                            Anchor: (Width: 170, Height: 180);
                            Background: (Color: #16213e);
                            Border: (Size: 2, Color: #00d4ff);
                            LayoutMode: Top;
                            Padding: 8;

                            AssetImage #ImgKuramon {
                                Anchor: (Width: 80, Height: 80);
                                AssetPath: "Common/UI/Custom/png_Digis/Kuramon.png";
                            }

                            Group { Anchor: (Height: 6); }

                            Label #TxtKuramon {
                                Text: "Kuramon";
                                Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 20);
                            }

                            Label #LvlKuramon {
                                Text: "Baby";
                                Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                        }
                    }

                    Group { Anchor: (Height: 8); }

                    // Fila 2: Punimon | Chibimon
                    Group {
                        LayoutMode: Left;
                        Padding: (Vertical: 6);

                        Group #CardPunimon {
                            Anchor: (Width: 170, Height: 180);
                            Background: (Color: #16213e);
                            Border: (Size: 2, Color: #00d4ff);
                            LayoutMode: Top;
                            Padding: 8;

                            AssetImage #ImgPunimon {
                                Anchor: (Width: 80, Height: 80);
                                AssetPath: "Common/UI/Custom/png_Digis/Punimon.png";
                            }

                            Group { Anchor: (Height: 6); }

                            Label #TxtPunimon {
                                Text: "Punimon";
                                Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 20);
                            }

                            Label #LvlPunimon {
                                Text: "Baby";
                                Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                        }

                        Group { Anchor: (Width: 8); }

                        Group #CardChibimon {
                            Anchor: (Width: 170, Height: 180);
                            Background: (Color: #16213e);
                            Border: (Size: 2, Color: #00d4ff);
                            LayoutMode: Top;
                            Padding: 8;

                            AssetImage #ImgChibimon {
                                Anchor: (Width: 80, Height: 80);
                                AssetPath: "Common/UI/Custom/png_Digis/Chibimon.png";
                            }

                            Group { Anchor: (Height: 6); }

                            Label #TxtChibimon {
                                Text: "Chibimon";
                                Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 20);
                            }

                            Label #LvlChibimon {
                                Text: "Baby";
                                Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                        }
                    }

                    Group { Anchor: (Height: 8); }

                    // Fila 3: Poyomon | Yuramon
                    Group {
                        LayoutMode: Left;
                        Padding: (Vertical: 6);

                        Group #CardPoyomon {
                            Anchor: (Width: 170, Height: 180);
                            Background: (Color: #16213e);
                            Border: (Size: 2, Color: #00d4ff);
                            LayoutMode: Top;
                            Padding: 8;

                            AssetImage #ImgPoyomon {
                                Anchor: (Width: 80, Height: 80);
                                AssetPath: "Common/UI/Custom/png_Digis/Veemon.png";
                            }

                            Group { Anchor: (Height: 6); }

                            Label #TxtPoyomon {
                                Text: "Poyomon";
                                Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 20);
                            }

                            Label #LvlPoyomon {
                                Text: "Baby";
                                Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                        }

                        Group { Anchor: (Width: 8); }

                        Group #CardYuramon {
                            Anchor: (Width: 170, Height: 180);
                            Background: (Color: #16213e);
                            Border: (Size: 2, Color: #00d4ff);
                            LayoutMode: Top;
                            Padding: 8;

                            AssetImage #ImgYuramon {
                                Anchor: (Width: 80, Height: 80);
                                AssetPath: "Common/UI/Custom/png_Digis/YukimiBotamon.png";
                            }

                            Group { Anchor: (Height: 6); }

                            Label #TxtYuramon {
                                Text: "Yuramon";
                                Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 20);
                            }

                            Label #LvlYuramon {
                                Text: "Baby";
                                Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                        }
                    }

                    Group { Anchor: (Height: 8); }

                    // Fila 4: Pichimon | Nyokimon
                    Group {
                        LayoutMode: Left;
                        Padding: (Vertical: 6);

                        Group #CardPichimon {
                            Anchor: (Width: 170, Height: 180);
                            Background: (Color: #16213e);
                            Border: (Size: 2, Color: #00d4ff);
                            LayoutMode: Top;
                            Padding: 8;

                            AssetImage #ImgPichimon {
                                Anchor: (Width: 80, Height: 80);
                                AssetPath: "Common/UI/Custom/png_Digis/Pichimon.png";
                            }

                            Group { Anchor: (Height: 6); }

                            Label #TxtPichimon {
                                Text: "Pichimon";
                                Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 20);
                            }

                            Label #LvlPichimon {
                                Text: "Baby";
                                Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                        }

                        Group { Anchor: (Width: 8); }

                        Group #CardNyokimon {
                            Anchor: (Width: 170, Height: 180);
                            Background: (Color: #16213e);
                            Border: (Size: 2, Color: #00d4ff);
                            LayoutMode: Top;
                            Padding: 8;

                            AssetImage #ImgNyokimon {
                                Anchor: (Width: 80, Height: 80);
                                AssetPath: "Common/UI/Custom/png_Digis/Nyokimon.png";
                            }

                            Group { Anchor: (Height: 6); }

                            Label #TxtNyokimon {
                                Text: "Nyokimon";
                                Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 20);
                            }

                            Label #LvlNyokimon {
                                Text: "Baby";
                                Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                        }
                    }

                    Group { Anchor: (Height: 12); }

                    Label #MsgInfo {
                        Text: "";
                        Style: (FontSize: 14, TextColor: #ff00ff, HorizontalAlignment: Center);
                        Anchor: (Height: 20);
                    }

                    Group { Anchor: (Height: 8); }

                    Group {
                        LayoutMode: Left;

                        TextButton #BtnConfirmar {
                            Text: "Confirmar";
                            Anchor: (Width: 200, Height: 44);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 16, TextColor: #00d4ff, HorizontalAlignment: Center, RenderBold: true)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 16, TextColor: #ffffff, HorizontalAlignment: Center, RenderBold: true)), Pressed: (Background: (Color: #0f3460), LabelStyle: (FontSize: 16, TextColor: #00d4ff, HorizontalAlignment: Center, RenderBold: true)));
                        }

                        Group { FlexWeight: 1; }

                        TextButton #BtnVolver {
                            Text: "Cancelar";
                            Anchor: (Width: 200, Height: 44);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 14, TextColor: #8899aa, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 14, TextColor: #e0e8f0, HorizontalAlignment: Center)), Pressed: (Background: (Color: #0f3460), LabelStyle: (FontSize: 14, TextColor: #e0e8f0, HorizontalAlignment: Center)));
                        }
                    }
                }
            }
            """;
        cmd.appendInline(null, ui);

        String[] starters = {"Botamon", "Punimon", "Poyomon", "Yuramon", "Pichimon", "Nyokimon", "Kuramon", "Chibimon"};
        for (String s : starters) {
            event.addEventBinding(CustomUIEventBindingType.Activating, "#Txt" + s,
                EventData.of("action", "seleccion").append("value", s), false);
        }
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnConfirmar",
            EventData.of("action", "confirmar"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnVolver",
            EventData.of("action", "cancelar"), false);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull String jsonData) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String action = json.has("action") ? json.get("action").getAsString() : "";
        LOGGER.info("DigiStartMenuUI accion: " + action);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        if ("seleccion".equals(action)) {
            String nombre = json.has("value") ? json.get("value").getAsString() : "";
            LOGGER.info("Seleccionado: " + nombre);

            UICommandBuilder cmd = new UICommandBuilder();
            cmd.set("#Card" + nombre + ".Border", "(Size: 4, Color: #ff00ff)");
            cmd.set("#MsgInfo.Text", nombre + " seleccionado!");
            sendUpdate(cmd);
        } else if ("confirmar".equals(action)) {
            @SuppressWarnings("removal")
            AlmacenJugadores.DatosJugador datos = AlmacenJugadores.obtener(player.getUuid());
            String seleccionA = datos.companeroA != null ? datos.companeroA.especie : "Botamon";
            String seleccionB = datos.companeroB != null ? datos.companeroB.especie : "Punimon";
            if (datos.companeroA == null) {
                datos.companeroA = DatoDigimon.crearInicial(seleccionA);
                datos.companeroA.vivo = true;
            }
            if (datos.companeroB == null) {
                datos.companeroB = DatoDigimon.crearInicial(seleccionB);
                datos.companeroB.vivo = true;
            }
            datos.tieneEquipo = true;
            player.getPageManager().setPage(ref, store,
                com.hypixel.hytale.protocol.packets.interface_.Page.None);
        } else if ("cancelar".equals(action)) {
            player.getPageManager().setPage(ref, store,
                com.hypixel.hytale.protocol.packets.interface_.Page.None);
        }
    }
}
