package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UI de combate estilo Cyber Sleuth: tarjetas de Digimon abajo, linea de turnos
 * a la derecha y menu simplificado (boton circular + botones laterales).
 */
public class DigiBatallaComandosUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(DigiBatallaComandosUI.class.getName());
    private static final int MAX_ITEMS = 8;

    private record Turno(String nombre, int gauge, boolean aliado, String icono) {}

    public DigiBatallaComandosUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CantClose);
    }

    public static void abrir(@Nonnull PlayerRef playerRef,
                             @Nonnull Ref<EntityStore> ref,
                             @Nonnull Store<EntityStore> store) {
        try {
            AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
            if (d == null || d.combate == null) return;
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;
            player.getPageManager().openCustomPage(ref, store, new DigiBatallaComandosUI(playerRef));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "DigiBatallaComandosUI abrir: " + e.getMessage());
        }
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder event,
                      @Nonnull Store<EntityStore> store) {
        cmd.appendInline(null, """
            Group {
                Group #PCard0 {
                    Anchor: (Left: 410, Bottom: 12, Width: 210, Height: 125);
                    AssetImage #PCardBg0 { Anchor: (Full: 0); AssetPath: "UI/Custom/Common/Cyber/battle/panel_digimon.png"; }
                    AssetImage #PCardImg0 { Anchor: (Left: 18, Top: 10, Width: 32, Height: 32); AssetPath: "UI/Custom/png_Digis/Botamon.png"; }
                    AssetImage #PCardType0 { Anchor: (Left: 182, Top: 6, Width: 20, Height: 20); AssetPath: "UI/Textures/Icons/Type/Unknown.png"; }
                    Label #PName0 { Anchor: (Left: 62, Top: 4, Width: 116, Height: 20); Text: "---"; Style: (TextColor: #38d9ff, FontSize: 17, RenderBold: true); }
                    Label #PHp0 { Anchor: (Left: 62, Top: 24, Width: 116, Height: 16); Text: "HP ---"; Style: (TextColor: #5dffb0, FontSize: 12); }
                    Label #POp0 { Anchor: (Left: 14, Top: 66, Width: 182, Height: 18); Text: "OP ---"; Style: (TextColor: #d7ecff, FontSize: 12, RenderBold: true); }
                    Label #PTurn0 { Anchor: (Left: 14, Top: 88, Width: 182, Height: 18); Text: "TURNO ---"; Style: (TextColor: #38d9ff, FontSize: 12); }
                }
                Group #PCard1 {
                    Anchor: (Left: 630, Bottom: 12, Width: 210, Height: 125);
                    AssetImage #PCardBg1 { Anchor: (Full: 0); AssetPath: "UI/Custom/Common/Cyber/battle/panel_digimon.png"; }
                    AssetImage #PCardImg1 { Anchor: (Left: 18, Top: 10, Width: 32, Height: 32); AssetPath: "UI/Custom/png_Digis/Botamon.png"; }
                    AssetImage #PCardType1 { Anchor: (Left: 182, Top: 6, Width: 20, Height: 20); AssetPath: "UI/Textures/Icons/Type/Unknown.png"; }
                    Label #PName1 { Anchor: (Left: 62, Top: 8, Width: 140, Height: 18); Text: "---"; Style: (TextColor: #ff4fa3, FontSize: 13, RenderBold: true); }
                    Label #PHp1 { Anchor: (Left: 62, Top: 24, Width: 116, Height: 16); Text: "HP ---"; Style: (TextColor: #5dffb0, FontSize: 12); }
                    Label #POp1 { Anchor: (Left: 14, Top: 66, Width: 182, Height: 18); Text: "OP ---"; Style: (TextColor: #d7ecff, FontSize: 12, RenderBold: true); }
                    Label #PTurn1 { Anchor: (Left: 14, Top: 88, Width: 182, Height: 18); Text: "TURNO ---"; Style: (TextColor: #38d9ff, FontSize: 12); }
                }
                Group #PCard2 {
                    Anchor: (Left: 850, Bottom: 12, Width: 210, Height: 125);
                    AssetImage #PCardBg2 { Anchor: (Full: 0); AssetPath: "UI/Custom/Common/Cyber/battle/panel_digimon.png"; }
                    AssetImage #PCardImg2 { Anchor: (Left: 18, Top: 10, Width: 32, Height: 32); AssetPath: "UI/Custom/png_Digis/Botamon.png"; }
                    AssetImage #PCardType2 { Anchor: (Left: 182, Top: 6, Width: 20, Height: 20); AssetPath: "UI/Textures/Icons/Type/Unknown.png"; }
                    Label #PName2 { Anchor: (Left: 62, Top: 8, Width: 140, Height: 18); Text: "---"; Style: (TextColor: #ffc857, FontSize: 13, RenderBold: true); }
                    Label #PHp2 { Anchor: (Left: 62, Top: 24, Width: 116, Height: 16); Text: "HP ---"; Style: (TextColor: #5dffb0, FontSize: 12); }
                    Label #POp2 { Anchor: (Left: 14, Top: 66, Width: 182, Height: 18); Text: "OP ---"; Style: (TextColor: #d7ecff, FontSize: 12, RenderBold: true); }
                    Label #PTurn2 { Anchor: (Left: 14, Top: 88, Width: 182, Height: 18); Text: "TURNO ---"; Style: (TextColor: #38d9ff, FontSize: 12); }
                }

                Group #TurnPanel {
                    LayoutMode: Top;
                    Anchor: (Top: 12, Right: 12, Width: 260, Height: 380);
                    Background: (Color: #04070d);
                    Padding: (Full: 10);

                    Label {
                        Text: "LINEA DE TURNOS";
                        Style: (TextColor: #ffc857, FontSize: 16, RenderBold: true, LetterSpacing: 1);
                        Anchor: (Height: 22);
                    }
                    Group { Anchor: (Height: 2); Background: (Color: #ffc857); }
                    Group { Anchor: (Height: 4); }

                    Group #Turn0 {
                        Anchor: (Height: 64);
                        AssetImage #TurnBg0 { Anchor: (Full: 0); AssetPath: "UI/Custom/Common/Cyber/battle/panel_turn_ally.png"; }
                        AssetImage #TurnIcon0 { Anchor: (Left: 10, Top: 18, Width: 28, Height: 28); AssetPath: "UI/Textures/Icons/Type/Unknown.png"; }
                        Label #TurnName0 { Anchor: (Left: 44, Top: 10, Width: 184, Height: 20); Text: "---"; Style: (TextColor: #ffffff, FontSize: 15, RenderBold: true); }
                        Label #TurnGauge0 { Anchor: (Left: 44, Top: 32, Width: 184, Height: 18); Text: ""; Style: (TextColor: #38d9ff, FontSize: 12); }
                    }
                    Group #Turn1 {
                        Anchor: (Height: 64);
                        AssetImage #TurnBg1 { Anchor: (Full: 0); AssetPath: "UI/Custom/Common/Cyber/battle/panel_turn_ally.png"; }
                        AssetImage #TurnIcon1 { Anchor: (Left: 10, Top: 18, Width: 28, Height: 28); AssetPath: "UI/Textures/Icons/Type/Unknown.png"; }
                        Label #TurnName1 { Anchor: (Left: 44, Top: 10, Width: 184, Height: 20); Text: "---"; Style: (TextColor: #ffffff, FontSize: 15, RenderBold: true); }
                        Label #TurnGauge1 { Anchor: (Left: 44, Top: 32, Width: 184, Height: 18); Text: ""; Style: (TextColor: #38d9ff, FontSize: 12); }
                    }
                    Group #Turn2 {
                        Anchor: (Height: 64);
                        AssetImage #TurnBg2 { Anchor: (Full: 0); AssetPath: "UI/Custom/Common/Cyber/battle/panel_turn_ally.png"; }
                        AssetImage #TurnIcon2 { Anchor: (Left: 10, Top: 18, Width: 28, Height: 28); AssetPath: "UI/Textures/Icons/Type/Unknown.png"; }
                        Label #TurnName2 { Anchor: (Left: 44, Top: 10, Width: 184, Height: 20); Text: "---"; Style: (TextColor: #ffffff, FontSize: 15, RenderBold: true); }
                        Label #TurnGauge2 { Anchor: (Left: 44, Top: 32, Width: 184, Height: 18); Text: ""; Style: (TextColor: #38d9ff, FontSize: 12); }
                    }
                    Group #Turn3 {
                        Anchor: (Height: 64);
                        AssetImage #TurnBg3 { Anchor: (Full: 0); AssetPath: "UI/Custom/Common/Cyber/battle/panel_turn_ally.png"; }
                        AssetImage #TurnIcon3 { Anchor: (Left: 10, Top: 18, Width: 28, Height: 28); AssetPath: "UI/Textures/Icons/Type/Unknown.png"; }
                        Label #TurnName3 { Anchor: (Left: 44, Top: 10, Width: 184, Height: 20); Text: "---"; Style: (TextColor: #ffffff, FontSize: 15, RenderBold: true); }
                        Label #TurnGauge3 { Anchor: (Left: 44, Top: 32, Width: 184, Height: 18); Text: ""; Style: (TextColor: #38d9ff, FontSize: 12); }
                    }
                    Group #Turn4 {
                        Anchor: (Height: 64);
                        AssetImage #TurnBg4 { Anchor: (Full: 0); AssetPath: "UI/Custom/Common/Cyber/battle/panel_turn_ally.png"; }
                        AssetImage #TurnIcon4 { Anchor: (Left: 10, Top: 18, Width: 28, Height: 28); AssetPath: "UI/Textures/Icons/Type/Unknown.png"; }
                        Label #TurnName4 { Anchor: (Left: 44, Top: 10, Width: 184, Height: 20); Text: "---"; Style: (TextColor: #ffffff, FontSize: 15, RenderBold: true); }
                        Label #TurnGauge4 { Anchor: (Left: 44, Top: 32, Width: 184, Height: 18); Text: ""; Style: (TextColor: #38d9ff, FontSize: 12); }
                    }
                }

                Group #CmdPanel {
                    LayoutMode: Top;
                    Anchor: (Left: 12, Bottom: 12, Width: 390, Height: 488);
                    Background: (Color: #04070d);
                    Padding: (Full: 12);

                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 28);
                        Label #CmdTitulo { Text: "COMBATE"; Style: (TextColor: #ffc857, FontSize: 20, RenderBold: true, LetterSpacing: 1); Anchor: (Width: 250); }
                        Label #CmdFase { Text: ""; Style: (TextColor: #38d9ff, FontSize: 14, HorizontalAlignment: End); Anchor: (Width: 100); }
                    }
                    Group { Anchor: (Height: 2); Background: (Color: #ffc857); }
                    Group { Anchor: (Height: 4); }
                    Label #CmdEnemigo { Text: ""; Style: (TextColor: #ff5f6d, FontSize: 13); Anchor: (Height: 18); }
                    Group { Anchor: (Height: 4); }

                    Group #CmdRootGroup {
                        LayoutMode: Top;
                        Anchor: (Height: 300);
                        Group {
                            LayoutMode: Middle;
                            Anchor: (Height: 110);
                            Group {
                                Anchor: (Width: 110, Height: 110);
                                AssetImage #CmdCentralBg { Anchor: (Left: 0, Top: 0, Width: 110, Height: 110); AssetPath: "UI/Custom/Common/Cyber/battle/boton_central.png"; }
                                TextButton #CmdCentral {
                                    Anchor: (Left: 0, Top: 0, Width: 110, Height: 110);
                                    Text: "ATACAR";
                                    Style: (Default: (Background: (Color: #00000001), LabelStyle: (FontSize: 20, TextColor: #d7ecff, HorizontalAlignment: Center, RenderBold: true)), Hovered: (Background: (Color: #38d9ff22), LabelStyle: (FontSize: 16, TextColor: #ffffff, HorizontalAlignment: Center, RenderBold: true)), Pressed: (Background: (Color: #ffc85733), LabelStyle: (FontSize: 16, TextColor: #ffc857, HorizontalAlignment: Center, RenderBold: true)));
                                }
                            }
                        }
                        Group { Anchor: (Height: 6); }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 78);
                            Group {
                                Anchor: (Width: 150, Height: 78);
                                AssetImage { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); AssetPath: "UI/Custom/Common/Cyber/battle/boton_battle.png"; }
                                TextButton #CmdRoot0 { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); Text: "HABILIDAD"; Style: (Default: (Background: (Color: #00000001), LabelStyle: (FontSize: 16, TextColor: #d7ecff, HorizontalAlignment: Center, RenderBold: true)), Hovered: (Background: (Color: #38d9ff22), LabelStyle: (FontSize: 16, TextColor: #ffffff, HorizontalAlignment: Center, RenderBold: true)), Pressed: (Background: (Color: #ffc85733), LabelStyle: (FontSize: 16, TextColor: #ffc857, HorizontalAlignment: Center, RenderBold: true))); }
                            }
                            Group { Anchor: (Width: 8); }
                            Group {
                                Anchor: (Width: 150, Height: 78);
                                AssetImage { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); AssetPath: "UI/Custom/Common/Cyber/battle/boton_battle.png"; }
                                TextButton #CmdRoot1 { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); Text: "APOYO"; Style: (Default: (Background: (Color: #00000001), LabelStyle: (FontSize: 16, TextColor: #d7ecff, HorizontalAlignment: Center, RenderBold: true)), Hovered: (Background: (Color: #38d9ff22), LabelStyle: (FontSize: 16, TextColor: #ffffff, HorizontalAlignment: Center, RenderBold: true)), Pressed: (Background: (Color: #ffc85733), LabelStyle: (FontSize: 16, TextColor: #ffc857, HorizontalAlignment: Center, RenderBold: true))); }
                            }
                        }
                        Group { Anchor: (Height: 6); }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 78);
                            Group {
                                Anchor: (Width: 150, Height: 78);
                                AssetImage { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); AssetPath: "UI/Custom/Common/Cyber/battle/boton_battle.png"; }
                                TextButton #CmdRoot2 { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); Text: "CAMBIAR"; Style: (Default: (Background: (Color: #00000001), LabelStyle: (FontSize: 16, TextColor: #d7ecff, HorizontalAlignment: Center, RenderBold: true)), Hovered: (Background: (Color: #38d9ff22), LabelStyle: (FontSize: 16, TextColor: #ffffff, HorizontalAlignment: Center, RenderBold: true)), Pressed: (Background: (Color: #ffc85733), LabelStyle: (FontSize: 16, TextColor: #ffc857, HorizontalAlignment: Center, RenderBold: true))); }
                            }
                            Group { Anchor: (Width: 8); }
                            Group {
                                Anchor: (Width: 150, Height: 78);
                                AssetImage { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); AssetPath: "UI/Custom/Common/Cyber/battle/boton_battle.png"; }
                                TextButton #CmdRoot3 { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); Text: "HUIR"; Style: (Default: (Background: (Color: #00000001), LabelStyle: (FontSize: 16, TextColor: #d7ecff, HorizontalAlignment: Center, RenderBold: true)), Hovered: (Background: (Color: #38d9ff22), LabelStyle: (FontSize: 16, TextColor: #ffffff, HorizontalAlignment: Center, RenderBold: true)), Pressed: (Background: (Color: #ffc85733), LabelStyle: (FontSize: 16, TextColor: #ffc857, HorizontalAlignment: Center, RenderBold: true))); }
                            }
                        }
                    }

                    Group #CmdListGroup {
                        LayoutMode: Top;
                        Anchor: (Height: 300);
                        TextButton #Cmd0 { Anchor: (Height: 32); Style: (Default: (Background: (Color: #0a1826), LabelStyle: (FontSize: 14, TextColor: #d7ecff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #13314a), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)), Pressed: (Background: (Color: #1b4a6b), LabelStyle: (FontSize: 14, TextColor: #ffc857, HorizontalAlignment: Center))); }
                        Group { Anchor: (Height: 3); }
                        TextButton #Cmd1 { Anchor: (Height: 32); Style: (Default: (Background: (Color: #0a1826), LabelStyle: (FontSize: 14, TextColor: #d7ecff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #13314a), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)), Pressed: (Background: (Color: #1b4a6b), LabelStyle: (FontSize: 14, TextColor: #ffc857, HorizontalAlignment: Center))); }
                        Group { Anchor: (Height: 3); }
                        TextButton #Cmd2 { Anchor: (Height: 32); Style: (Default: (Background: (Color: #0a1826), LabelStyle: (FontSize: 14, TextColor: #d7ecff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #13314a), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)), Pressed: (Background: (Color: #1b4a6b), LabelStyle: (FontSize: 14, TextColor: #ffc857, HorizontalAlignment: Center))); }
                        Group { Anchor: (Height: 3); }
                        TextButton #Cmd3 { Anchor: (Height: 32); Style: (Default: (Background: (Color: #0a1826), LabelStyle: (FontSize: 14, TextColor: #d7ecff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #13314a), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)), Pressed: (Background: (Color: #1b4a6b), LabelStyle: (FontSize: 14, TextColor: #ffc857, HorizontalAlignment: Center))); }
                        Group { Anchor: (Height: 3); }
                        TextButton #Cmd4 { Anchor: (Height: 32); Style: (Default: (Background: (Color: #0a1826), LabelStyle: (FontSize: 14, TextColor: #d7ecff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #13314a), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)), Pressed: (Background: (Color: #1b4a6b), LabelStyle: (FontSize: 14, TextColor: #ffc857, HorizontalAlignment: Center))); }
                        Group { Anchor: (Height: 3); }
                        TextButton #Cmd5 { Anchor: (Height: 32); Style: (Default: (Background: (Color: #0a1826), LabelStyle: (FontSize: 14, TextColor: #d7ecff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #13314a), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)), Pressed: (Background: (Color: #1b4a6b), LabelStyle: (FontSize: 14, TextColor: #ffc857, HorizontalAlignment: Center))); }
                        Group { Anchor: (Height: 3); }
                        TextButton #Cmd6 { Anchor: (Height: 32); Style: (Default: (Background: (Color: #0a1826), LabelStyle: (FontSize: 14, TextColor: #d7ecff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #13314a), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)), Pressed: (Background: (Color: #1b4a6b), LabelStyle: (FontSize: 14, TextColor: #ffc857, HorizontalAlignment: Center))); }
                        Group { Anchor: (Height: 3); }
                        TextButton #Cmd7 { Anchor: (Height: 32); Style: (Default: (Background: (Color: #0a1826), LabelStyle: (FontSize: 14, TextColor: #d7ecff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #13314a), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)), Pressed: (Background: (Color: #1b4a6b), LabelStyle: (FontSize: 14, TextColor: #ffc857, HorizontalAlignment: Center))); }
                    }

                    Group { Anchor: (Height: 6); }
                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 36);
                        TextButton #CmdVolver { Text: "VOLVER"; Anchor: (Width: 172, Height: 34); Style: (Default: (Background: (Color: #0a1826), LabelStyle: (FontSize: 14, TextColor: #6f8ca4, HorizontalAlignment: Center)), Hovered: (Background: (Color: #13314a), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)), Pressed: (Background: (Color: #1b4a6b), LabelStyle: (FontSize: 14, TextColor: #ffc857, HorizontalAlignment: Center))); }
                        Group { Anchor: (Width: 8); }
                        TextButton #CmdCerrar { Text: "CERRAR"; Anchor: (Width: 172, Height: 34); Style: (Default: (Background: (Color: #3d1a1a), LabelStyle: (FontSize: 14, TextColor: #ff9fb0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #5d2a2a), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)), Pressed: (Background: (Color: #7a3a3a), LabelStyle: (FontSize: 14, TextColor: #ffc857, HorizontalAlignment: Center))); }
                    }
                    Group { Anchor: (Height: 4); }
                    Label #CmdLog { Text: ""; Style: (TextColor: #d7ecff, FontSize: 11, Wrap: true); Anchor: (Height: 40); }
                    Group { Anchor: (Height: 4); }
                    Label #CmdHint { Text: "Haz clic en una opcion"; Style: (TextColor: #6f8ca4, FontSize: 11, HorizontalAlignment: Center); Anchor: (Height: 16); }
                }
            }
            """);

        event.addEventBinding(CustomUIEventBindingType.Activating, "#CmdCentral",
            EventData.of("action", "elegir").append("value", "0"), false);
        for (int i = 0; i < 4; i++) {
            event.addEventBinding(CustomUIEventBindingType.Activating, "#CmdRoot" + i,
                EventData.of("action", "elegir").append("value", String.valueOf(i + 1)), false);
        }
        for (int i = 0; i < MAX_ITEMS; i++) {
            event.addEventBinding(CustomUIEventBindingType.Activating, "#Cmd" + i,
                EventData.of("action", "elegir").append("value", String.valueOf(i)), false);
        }
        event.addEventBinding(CustomUIEventBindingType.Activating, "#CmdVolver",
            EventData.of("action", "volver"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#CmdCerrar",
            EventData.of("action", "cerrar"), false);

        refrescar(cmd);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull String jsonData) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        String action = "";
        int value = 0;
        try {
            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
            action = json.has("action") ? json.get("action").getAsString() : "";
            value = json.has("value") && !json.get("value").isJsonNull()
                ? Integer.parseInt(json.get("value").getAsString()) : 0;
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Comandos combate JSON: " + e.getMessage());
            return;
        }

        switch (action) {
            case "elegir" -> {
                d.menuIdx = value;
                MenuBatalla.aceptar(playerRef, ref, store);
            }
            case "volver" -> MenuBatalla.atras(playerRef, ref, store);
            case "cerrar" -> {
                MenuBatalla.cerrar(playerRef, ref, store);
                return;
            }
            default -> {
                return;
            }
        }

        if (!d.menuAbierto) return;

        DigiBattleHud.actualizar(playerRef, ref, store);
        UICommandBuilder cmd = new UICommandBuilder();
        refrescar(cmd);
        sendUpdate(cmd);
    }

    private void refrescar(UICommandBuilder cmd) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        EstadoCombate c = d != null ? d.combate : null;
        if (d == null || c == null) return;

        cmd.set("#CmdTitulo.Text", MenuBatalla.titulo(d));
        cmd.set("#CmdFase.Text", c.fase == EstadoCombate.Fase.FIN_COMBATE
            ? (c.victoria ? "VICTORIA" : "DERROTA")
            : "RONDA " + c.ronda);
        cmd.set("#CmdEnemigo.Text", "ENEMIGO // " + c.enemigoNombre
            + "  " + c.enemigoHp + "/" + c.enemigoMaxHp
            + "  " + EstadoCombate.barraHp(c.enemigoHp, Math.max(1, c.enemigoMaxHp)));

        java.util.List<DatoDigimon> party =
            (c.party != null && !c.party.isEmpty()) ? c.party : d.equipoCombate();
        int[] hps = {c.hpCombateA, c.hpCombateB, c.hpCombateC};
        int[] ops = {c.opA, c.opB, c.opC};
        int[] gauges = {c.gaugeA, c.gaugeB, c.gaugeC};

        for (int i = 0; i < 3; i++) {
            DatoDigimon digi = i < party.size() ? party.get(i) : null;
            boolean vivo = digi != null && hps[i] > 0;
            cmd.set("#PName" + i + ".Text", digi != null
                ? digi.nombre + (vivo ? "" : " [KO]") : "---");
            cmd.set("#PHp" + i + ".Text", digi != null
                ? "HP " + hps[i] + "/" + Math.max(1, digi.maxHp)
                    + "  " + EstadoCombate.barraHp(hps[i], Math.max(1, digi.maxHp))
                : "HP ---");
            cmd.set("#POp" + i + ".Text", digi != null ? "OP " + ops[i] : "OP ---");
            cmd.set("#PTurn" + i + ".Text", digi != null
                ? "TURNO " + EstadoCombate.barraGauge(gauges[i]) : "TURNO ---");
            String ruta = digi != null ? DigiImagenes.rutaPara(digi.especie) : null;
            cmd.set("#PCardImg" + i + ".Visible", ruta != null);
            if (ruta != null) cmd.set("#PCardImg" + i + ".AssetPath", ruta);
            cmd.set("#PCardType" + i + ".AssetPath",
                DigiImagenes.iconoAtributo(digi != null ? digi.atributo : "UNKNOWN"));
        }

        List<String> items = MenuBatalla.listaItems(d);
        boolean raiz = d.menuNivel == 0;
        cmd.set("#CmdRootGroup.Visible", raiz);
        cmd.set("#CmdListGroup.Visible", !raiz);
        if (raiz) {
            cmd.set("#CmdCentral.Text", itemOr(items, 0, "ATACAR"));
            cmd.set("#CmdRoot0.Text", itemOr(items, 1, ""));
            cmd.set("#CmdRoot1.Text", itemOr(items, 2, ""));
            cmd.set("#CmdRoot2.Text", itemOr(items, 3, ""));
            cmd.set("#CmdRoot3.Text", itemOr(items, 4, ""));
        } else {
            for (int i = 0; i < MAX_ITEMS; i++) {
                boolean visible = i < items.size();
                cmd.set("#Cmd" + i + ".Visible", visible);
                cmd.set("#Cmd" + i + ".Text", visible ? items.get(i) : "");
            }
        }
        cmd.set("#CmdVolver.Visible", d.menuNivel == 1);

        List<Turno> turnos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            DatoDigimon digi = i < party.size() ? party.get(i) : null;
            if (digi != null && hps[i] > 0) {
                turnos.add(new Turno(digi.nombre, gauges[i], true,
                    DigiImagenes.iconoAtributo(digi.atributo)));
            }
        }
        if (c.enemigoVivo()) turnos.add(new Turno(c.enemigoNombre, c.gaugeEnemigo, false,
                DigiImagenes.iconoAtributo(atributoEnemigo(c))));
        if (c.refuerzoVivo()) turnos.add(new Turno(c.refuerzoNombre, c.gaugeRefuerzo, false,
                DigiImagenes.iconoAtributo("UNKNOWN")));
        turnos.sort((a, b) -> Integer.compare(b.gauge(), a.gauge()));
        for (int i = 0; i < 5; i++) {
            if (i < turnos.size()) {
                Turno t = turnos.get(i);
                cmd.set("#Turn" + i + ".Visible", true);
                cmd.set("#TurnBg" + i + ".AssetPath", t.aliado()
                    ? "UI/Custom/Common/Cyber/battle/panel_turn_ally.png"
                    : "UI/Custom/Common/Cyber/battle/panel_turn_enemy.png");
                cmd.set("#TurnIcon" + i + ".AssetPath", t.icono());
                cmd.set("#TurnIcon" + i + ".Visible", true);
                cmd.set("#TurnName" + i + ".Text",
                    (i == 0 ? ">> " : "   ") + t.nombre());
                cmd.set("#TurnGauge" + i + ".Text",
                    (t.aliado() ? "ALIADO  " : "ENEMIGO  ")
                        + EstadoCombate.barraGauge(t.gauge()));
            } else {
                cmd.set("#Turn" + i + ".Visible", false);
            }
        }

        cmd.set("#CmdCerrar.Visible", c.fase == EstadoCombate.Fase.FIN_COMBATE);
        cmd.set("#CmdLog.Text", c.getLogFormateado());
        cmd.set("#CmdHint.Text", c.fase == EstadoCombate.Fase.FIN_COMBATE
            ? "Cierra para volver al mundo"
            : "Haz clic en una opcion");
    }

    private static String itemOr(List<String> items, int i, String def) {
        return i >= 0 && i < items.size() ? items.get(i) : def;
    }

    private static String atributoEnemigo(EstadoCombate c) {
        try {
            String n = c.enemigoNombre;
            if (n == null) return "UNKNOWN";
            String base = n.replace(" Salvaje", "").replace(" Salvaje", "").trim();
            return DatoDigimon.crearInicial(base).atributo;
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref,
                          @Nonnull Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d != null && d.menuAbierto) {
            MenuBatalla.cerrar(playerRef, ref, store);
        }
    }
}
