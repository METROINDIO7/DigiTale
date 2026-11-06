package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.digitale.datos.EstadoCombate.Actor;
import com.digitale.datos.EstadoCombate.Enemigo;
import com.digitale.datos.TablaTipos;
import com.digitale.sistema.SistemaBatalla;
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
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UI de combate estilo Cyber Sleuth: tarjetas de Digimon abajo, linea de turnos
 * a la derecha y menu simplificado (boton circular + botones laterales).
 */
public class DigiBatallaComandosUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(DigiBatallaComandosUI.class.getName());
    private static final int MAX_ITEMS = 8;

    private record Turno(String nombre, int gauge, boolean aliado, String imagen,
                         String estado, Actor actor) {}

    // ── Animación de la línea de turnos ─────────────────────────────
    private static final ScheduledExecutorService ANIM =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "digitale-ui-turnos");
            t.setDaemon(true);
            return t;
        });
    private ScheduledFuture<?> animTask;
    private volatile boolean abierta = false;
    private List<Actor> ordenAnterior = new ArrayList<>();
    /** Valor de barra que se está mostrando (para interpolar). */
    private final Map<Actor, Integer> gaugeMostrado = new HashMap<>();
    /** Valor de barra al que hay que llegar. */
    private final Map<Actor, Integer> gaugeObjetivo = new HashMap<>();
    /** Para refrescar la página entera cuando termina una cinemática. */
    private boolean cineAnterior = false;
    /** El fin de combate ya se pintó. */
    private boolean finMostrado = false;
    /** Último turno pintado (para refrescar cuando cambia el actor). */
    private boolean esperaAnterior = false;
    private int actorAnterior = -1;

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
        String markup = """
            Group {
                {{CARDS}}

                {{TURNPANEL}}

                {{LOGPANEL}}

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
                                Label #CmdCentralLabel { Anchor: (Full: 0); Text: "ATACAR"; Style: (TextColor: #d7ecff, FontSize: 20, HorizontalAlignment: Center, VerticalAlignment: Center, RenderBold: true); }
                                TextButton #CmdCentral {
                                    Anchor: (Left: 0, Top: 0, Width: 110, Height: 110);
                                    Text: "";
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
                                Label #CmdRoot0Label { Anchor: (Full: 0); Text: "HABILIDAD"; Style: (TextColor: #d7ecff, FontSize: 16, HorizontalAlignment: Center, VerticalAlignment: Center, RenderBold: true); }
                                TextButton #CmdRoot0 { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); Text: ""; Style: (Default: (Background: (Color: #00000001), LabelStyle: (FontSize: 16, TextColor: #d7ecff, HorizontalAlignment: Center, RenderBold: true)), Hovered: (Background: (Color: #38d9ff22), LabelStyle: (FontSize: 16, TextColor: #ffffff, HorizontalAlignment: Center, RenderBold: true)), Pressed: (Background: (Color: #ffc85733), LabelStyle: (FontSize: 16, TextColor: #ffc857, HorizontalAlignment: Center, RenderBold: true))); }
                            }
                            Group { Anchor: (Width: 8); }
                            Group {
                                Anchor: (Width: 150, Height: 78);
                                AssetImage { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); AssetPath: "UI/Custom/Common/Cyber/battle/boton_battle.png"; }
                                Label #CmdRoot1Label { Anchor: (Full: 0); Text: "APOYO"; Style: (TextColor: #d7ecff, FontSize: 16, HorizontalAlignment: Center, VerticalAlignment: Center, RenderBold: true); }
                                TextButton #CmdRoot1 { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); Text: ""; Style: (Default: (Background: (Color: #00000001), LabelStyle: (FontSize: 16, TextColor: #d7ecff, HorizontalAlignment: Center, RenderBold: true)), Hovered: (Background: (Color: #38d9ff22), LabelStyle: (FontSize: 16, TextColor: #ffffff, HorizontalAlignment: Center, RenderBold: true)), Pressed: (Background: (Color: #ffc85733), LabelStyle: (FontSize: 16, TextColor: #ffc857, HorizontalAlignment: Center, RenderBold: true))); }
                            }
                        }
                        Group { Anchor: (Height: 6); }
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 78);
                            Group {
                                Anchor: (Width: 150, Height: 78);
                                AssetImage { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); AssetPath: "UI/Custom/Common/Cyber/battle/boton_battle.png"; }
                                Label #CmdRoot2Label { Anchor: (Full: 0); Text: "CAMBIAR"; Style: (TextColor: #d7ecff, FontSize: 16, HorizontalAlignment: Center, VerticalAlignment: Center, RenderBold: true); }
                                TextButton #CmdRoot2 { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); Text: ""; Style: (Default: (Background: (Color: #00000001), LabelStyle: (FontSize: 16, TextColor: #d7ecff, HorizontalAlignment: Center, RenderBold: true)), Hovered: (Background: (Color: #38d9ff22), LabelStyle: (FontSize: 16, TextColor: #ffffff, HorizontalAlignment: Center, RenderBold: true)), Pressed: (Background: (Color: #ffc85733), LabelStyle: (FontSize: 16, TextColor: #ffc857, HorizontalAlignment: Center, RenderBold: true))); }
                            }
                            Group { Anchor: (Width: 8); }
                            Group {
                                Anchor: (Width: 150, Height: 78);
                                AssetImage { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); AssetPath: "UI/Custom/Common/Cyber/battle/boton_battle.png"; }
                                Label #CmdRoot3Label { Anchor: (Full: 0); Text: "HUIR"; Style: (TextColor: #d7ecff, FontSize: 16, HorizontalAlignment: Center, VerticalAlignment: Center, RenderBold: true); }
                                TextButton #CmdRoot3 { Anchor: (Left: 0, Top: 0, Width: 150, Height: 78); Text: ""; Style: (Default: (Background: (Color: #00000001), LabelStyle: (FontSize: 16, TextColor: #d7ecff, HorizontalAlignment: Center, RenderBold: true)), Hovered: (Background: (Color: #38d9ff22), LabelStyle: (FontSize: 16, TextColor: #ffffff, HorizontalAlignment: Center, RenderBold: true)), Pressed: (Background: (Color: #ffc85733), LabelStyle: (FontSize: 16, TextColor: #ffc857, HorizontalAlignment: Center, RenderBold: true))); }
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
                    Group { Anchor: (Height: 8); }
                    Label #CmdHint { Text: "Haz clic en una opcion"; Style: (TextColor: #6f8ca4, FontSize: 11, HorizontalAlignment: Center, Wrap: true); Anchor: (Height: 30); }
                }
            }
            """;
        markup = markup.replace("{{CARDS}}", cardsMarkup())
            .replace("{{TURNPANEL}}", turnPanelMarkup())
            .replace("{{LOGPANEL}}", logPanelMarkup());
        cmd.appendInline(null, markup);

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
        abierta = true;
        iniciarAnimacion();
        LOGGER.log(Level.INFO, "UI combate construida (" + playerRef.getUsername() + ")");
    }

    private static String logPanelMarkup() {
        StringBuilder b = new StringBuilder();
        b.append("Group #LogPanel {\n");
        b.append("    LayoutMode: Top;\n");
        b.append("    Anchor: (Right: 12, Bottom: 12, Width: 460, Height: 250);\n");
        b.append("    Background: (Color: #04070d);\n");
        b.append("    Padding: (Full: 10);\n");
        b.append("    Group {\n");
        b.append("        LayoutMode: Left;\n");
        b.append("        Anchor: (Height: 24);\n");
        b.append("        Label { Text: \"REGISTRO DE COMBATE\"; Style: (TextColor: #ffc857, FontSize: 15, RenderBold: true, LetterSpacing: 1); Anchor: (Width: 270); }\n");
        b.append("        Label #LogTurno { Text: \"\"; Style: (TextColor: #38d9ff, FontSize: 11, HorizontalAlignment: End); Anchor: (Width: 160); }\n");
        b.append("    }\n");
        b.append("    Group { Anchor: (Height: 2); Background: (Color: #ffc857); }\n");
        b.append("    Group { Anchor: (Height: 6); }\n");
        b.append("    Label #PLog { Text: \"\"; Style: (TextColor: #d7ecff, FontSize: 11); Anchor: (Height: 190); }\n");
        b.append("}\n");
        return b.toString();
    }

    private static String cardsMarkup() {
        StringBuilder b = new StringBuilder();
        String[] cols = {"#38d9ff", "#ff4fa3", "#ffc857"};
        int[] left = {410, 630, 850};
        for (int i = 0; i < 3; i++) {
            b.append("Group #PCard").append(i).append(" {\n");
            b.append("    Anchor: (Left: ").append(left[i]).append(", Bottom: 12, Width: 210, Height: 125);\n");
            b.append("    AssetImage #PCardBg").append(i).append(" { Anchor: (Full: 0); AssetPath: \"UI/Custom/Common/Cyber/battle/panel_digimon.png\"; }\n");
            b.append("    AssetImage #PCardImg").append(i).append(" { Anchor: (Left: 14, Top: 17, Width: 40, Height: 40); AssetPath: \"UI/Custom/png_Digis/Botamon.png\"; }\n");
            b.append("    Label #PName").append(i).append(" { Anchor: (Left: 62, Top: 28, Width: 140, Height: 18); Text: \"---\"; Style: (TextColor: ").append(cols[i]).append(", FontSize: 15, RenderBold: true, VerticalAlignment: Center); }\n");
            b.append("    Label { Text: \"HP\"; Anchor: (Left: 14, Top: 68, Width: 22, Height: 14); Style: (TextColor: #5dffb0, FontSize: 10, VerticalAlignment: Center); }\n");
            b.append("    Label #PHp").append(i).append(" { Anchor: (Left: 124, Top: 66, Width: 70, Height: 14); Text: \"--\"; Style: (TextColor: #d7ecff, FontSize: 10, HorizontalAlignment: End, VerticalAlignment: Center); }\n");
            b.append(bar("PHpSeg" + i, 40, 69, 80, 7, "green"));
            b.append("    Label { Text: \"OP\"; Anchor: (Left: 14, Top: 92, Width: 22, Height: 14); Style: (TextColor: #38d9ff, FontSize: 10, VerticalAlignment: Center); }\n");
            b.append("    Label #POp").append(i).append(" { Anchor: (Left: 124, Top: 90, Width: 70, Height: 14); Text: \"--\"; Style: (TextColor: #d7ecff, FontSize: 10, HorizontalAlignment: End, VerticalAlignment: Center); }\n");
            b.append(bar("POpSeg" + i, 40, 93, 80, 7, "cyan"));
            b.append("}\n");
        }
        return b.toString();
    }

    private static String turnPanelMarkup() {
        StringBuilder b = new StringBuilder();
        b.append("Group #TurnPanel {\n");
        b.append("    LayoutMode: Top;\n");
        b.append("    Anchor: (Top: 12, Right: 12, Width: 260, Height: 380);\n");
        b.append("    Background: (Color: #04070d);\n");
        b.append("    Padding: (Full: 10);\n");
        b.append("    Label { Text: \"LINEA DE TURNOS\"; Style: (TextColor: #ffc857, FontSize: 16, RenderBold: true, LetterSpacing: 1); Anchor: (Height: 22); }\n");
        b.append("    Group { Anchor: (Height: 2); Background: (Color: #ffc857); }\n");
        b.append("    Group { Anchor: (Height: 4); }\n");
        for (int i = 0; i < 5; i++) {
            b.append("    Group #Turn").append(i).append(" {\n");
            b.append("        Anchor: (Height: 64);\n");
            b.append("        AssetImage #TurnBg").append(i).append(" { Anchor: (Full: 0); AssetPath: \"UI/Custom/Common/Cyber/battle/panel_turn_ally.png\"; }\n");
            b.append("        AssetImage #TurnIcon").append(i).append(" { Anchor: (Left: 10, Top: 18, Width: 28, Height: 28); AssetPath: \"UI/Custom/png_Digis/Botamon.png\"; }\n");
            b.append("        Label #TurnName").append(i).append(" { Anchor: (Left: 64, Top: 8, Width: 160, Height: 20); Text: \"---\"; Style: (TextColor: #ffffff, FontSize: 15, RenderBold: true, VerticalAlignment: Center); }\n");
            b.append("        Label #TurnState").append(i).append(" { Anchor: (Left: 64, Top: 30, Width: 120, Height: 14); Text: \"\"; Style: (TextColor: #38d9ff, FontSize: 10, VerticalAlignment: Center); }\n");
            b.append(bar("TurnSeg" + i, 64, 46, 120, 6, "cyan"));
            b.append("    }\n");
        }
        b.append("}\n");
        return b.toString();
    }

    private static String bar(String prefix, int x, int y, int w, int h, String color) {
        StringBuilder b = new StringBuilder();
        b.append("    Group {\n");
        b.append("        Anchor: (Left: ").append(x).append(", Top: ").append(y)
            .append(", Width: ").append(w).append(", Height: ").append(h).append(");\n");
        b.append("        AssetImage { Anchor: (Full: 0); AssetPath: \"UI/Custom/Common/Cyber/bar_bg.png\"; }\n");
        int sw = w / 10;
        for (int j = 0; j < 10; j++) {
            b.append("        AssetImage #").append(prefix).append(j)
                .append(" { Anchor: (Left: ").append(j * sw).append(", Top: 0, Width: ").append(sw)
                .append(", Height: ").append(h).append("); AssetPath: \"UI/Custom/Common/Cyber/bar_")
                .append(color).append(".png\"; }\n");
        }
        b.append("    }\n");
        return b.toString();
    }

    private static void segments(UICommandBuilder cmd, String prefix, int value, int max) {
        for (int j = 0; j < 10; j++) {
            boolean on = max > 0 && (long) value * 10 > (long) j * max;
            cmd.set("#" + prefix + j + ".Visible", on);
        }
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

        String tituloCmd = MenuBatalla.titulo(d);
        if (c.esperandoJugador && c.actorActual != null && c.actorActual.aliado()) {
            tituloCmd += " · " + c.nombreDe(c.actorActual);
        }
        cmd.set("#CmdTitulo.Text", tituloCmd);
        cmd.set("#CmdFase.Text", c.fase == EstadoCombate.Fase.FIN_COMBATE
            ? (c.victoria ? "VICTORIA" : "DERROTA")
            : "TURNO " + c.ronda + " · CP " + c.cp);

        Enemigo obj = c.objetivoActual();
        int vivos = 0;
        for (Enemigo e : c.enemigos) if (e != null && e.vivo()) vivos++;
        String enemTxt = obj != null
            ? "OBJ " + obj.nombre + " [" + obj.elemento + "/" + TablaTipos.textoAtributo(obj.atributo) + "] "
              + obj.hp + "/" + obj.maxHp
              + "  ·  vivos " + vivos + "/" + c.enemigos.size()
              + (obj.cargando ? "  ⚠" : "")
            : "SIN ENEMIGOS";
        cmd.set("#CmdEnemigo.Text", enemTxt);

        java.util.List<DatoDigimon> party =
            (c.party != null && !c.party.isEmpty()) ? c.party : d.equipoCombate();

        for (int i = 0; i < 3; i++) {
            DatoDigimon digi = i < party.size() ? party.get(i) : null;
            Actor actor = Actor.aliado(i);
            int hp = c.hpDe(actor);
            boolean vivo = digi != null && hp > 0;
            String estados = EstadoCombate.estadosToString(c.estadosDeAliado(i));
            String guardia = c.guardiaDe(i) ? " 🛡" : "";
            boolean actuando = c.esperandoJugador && c.actorActual != null
                && c.actorActual.aliado() && c.actorActual.idx() == i;
            String control = actuando ? " ▶TÚ" : " ·TÚ";
            cmd.set("#PName" + i + ".Text", digi != null
                ? digi.nombre + control + guardia + (estados.isEmpty() ? "" : " " + estados)
                  + (vivo ? "" : " [KO]") : "---");
            cmd.set("#PHp" + i + ".Text", digi != null
                ? hp + "/" + Math.max(1, digi.maxHp) : "--");
            cmd.set("#POp" + i + ".Text", digi != null ? String.valueOf(c.opDe(i)) : "--");
            String ruta = digi != null ? DigiImagenes.iconoAtributo(digi.atributo) : null;
            cmd.set("#PCardImg" + i + ".Visible", ruta != null);
            if (ruta != null) cmd.set("#PCardImg" + i + ".AssetPath", ruta);
            segments(cmd, "PHpSeg" + i, hp, Math.max(1, digi != null ? digi.maxHp : 1));
            segments(cmd, "POpSeg" + i, c.opDe(i), EstadoCombate.OP_MAX);
        }

        List<String> items = MenuBatalla.listaItems(d);
        boolean raiz = d.menuNivel == 0;
        cmd.set("#CmdRootGroup.Visible", raiz);
        cmd.set("#CmdListGroup.Visible", !raiz);
        if (raiz) {
            cmd.set("#CmdCentralLabel.Text", itemOr(items, 0, "ATACAR"));
            cmd.set("#CmdRoot0Label.Text", itemOr(items, 1, ""));
            cmd.set("#CmdRoot1Label.Text", itemOr(items, 2, ""));
            cmd.set("#CmdRoot2Label.Text", itemOr(items, 3, ""));
            cmd.set("#CmdRoot3Label.Text", itemOr(items, 4, ""));
        } else {
            for (int i = 0; i < MAX_ITEMS; i++) {
                boolean visible = i < items.size();
                cmd.set("#Cmd" + i + ".Visible", visible);
                cmd.set("#Cmd" + i + ".Text", visible ? items.get(i) : "");
            }
        }
        cmd.set("#CmdVolver.Visible", d.menuNivel == 1);

        // Línea de turnos real. El primero es quien actúa AHORA si te toca.
        List<Turno> turnos = new ArrayList<>();
        List<Actor> cola = SistemaBatalla.predecirTurnos(c, 5);
        for (int k = 0; k < cola.size(); k++) {
            Actor a = cola.get(k);
            DatoDigimon digi = c.digimonDe(a);
            String imagen = a.aliado()
                ? (digi != null ? DigiImagenes.rutaPara(digi.especie) : null)
                : retratoEnemigo(c, a.idx());
            String estado;
            if (a.aliado()) estado = "TÚ";
            else estado = "ENEMIGO";
            if (k == 0 && c.esperandoJugador && a.aliado()) estado = "AHORA";
            turnos.add(new Turno(c.nombreDe(a), c.gaugeDe(a), a.aliado(), imagen, estado, a));
        }

        for (int i = 0; i < 5; i++) {
            if (i < turnos.size()) {
                Turno t = turnos.get(i);
                cmd.set("#Turn" + i + ".Visible", true);
                cmd.set("#TurnBg" + i + ".AssetPath", t.aliado()
                    ? "UI/Custom/Common/Cyber/battle/panel_turn_ally.png"
                    : "UI/Custom/Common/Cyber/battle/panel_turn_enemy.png");
                cmd.set("#TurnIcon" + i + ".Visible", t.imagen() != null);
                if (t.imagen() != null) {
                    cmd.set("#TurnIcon" + i + ".AssetPath", t.imagen());
                }
                cmd.set("#TurnName" + i + ".Text",
                    (i == 0 ? ">> " : "   ") + t.nombre());
                cmd.set("#TurnState" + i + ".Text", t.estado());
                String color = t.aliado() ? "cyan" : "magenta";
                for (int j = 0; j < 10; j++) {
                    cmd.set("#TurnSeg" + i + j + ".AssetPath",
                        "UI/Custom/Common/Cyber/bar_" + color + ".png");
                }
                segments(cmd, "TurnSeg" + i, t.gauge(), EstadoCombate.GAUGE_MAX);
            } else {
                cmd.set("#Turn" + i + ".Visible", false);
            }
        }

        cmd.set("#CmdCerrar.Visible", c.fase == EstadoCombate.Fase.FIN_COMBATE);
        cmd.set("#LogTurno.Text", LogCombateUI.recortar(MenuBatalla.estadoTurno(c), 24));
        cmd.set("#PLog.Text", LogCombateUI.texto(c.getLog(), 10, 84));
        String pista;
        if (c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            pista = "Cierra para volver al mundo";
        } else if (!c.esperandoJugador) {
            pista = c.actorActual != null
                ? "Turno de " + c.nombreDe(c.actorActual) + " (ENEMIGO)..."
                : "Resolviendo...";
        } else if (d.menuNivel == 1 && "objetivoAtaque".equals(d.menuSub)) {
            pista = "Elige objetivo para "
                + (d.menuMovimiento != null ? d.menuMovimiento.nombre : "el ataque");
        } else if (obj != null && obj.analizado) {
            pista = TablaTipos.analisis(obj.elemento, obj.atributo);
        } else {
            pista = "TU TURNO · APOYO → ANALIZAR u OBJETIVO";
        }
        cmd.set("#CmdHint.Text", pista);
    }

    private static String itemOr(List<String> items, int i, String def) {
        return i >= 0 && i < items.size() ? items.get(i) : def;
    }

    private static String retratoEnemigo(EstadoCombate c, int idx) {
        try {
            Enemigo e = c.enemigo(idx);
            if (e == null || e.nombre == null) return null;
            String base = e.nombre.replace(" Salvaje", "").trim();
            return DigiImagenes.rutaPara(base);
        } catch (Exception ex) {
            return null;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // ANIMACIÓN DE LA LÍNEA DE TURNOS
    // ══════════════════════════════════════════════════════════════════

    private void iniciarAnimacion() {
        cancelarAnimacion();
        animTask = ANIM.scheduleAtFixedRate(() -> {
            try {
                if (!abierta) {
                    cancelarAnimacion();
                    return;
                }
                Ref<EntityStore> ref = playerRef.getReference();
                if (ref == null || !ref.isValid()) {
                    cancelarAnimacion();
                    return;
                }
                Store<EntityStore> store = ref.getStore();
                World world = store.getExternalData().getWorld();
                if (world == null) {
                    cancelarAnimacion();
                    return;
                }
                world.execute(this::tickAnimacion);
            } catch (Exception ignored) {
            }
        }, 200L, 200L, TimeUnit.MILLISECONDS);
    }

    private void cancelarAnimacion() {
        if (animTask != null) {
            animTask.cancel(false);
            animTask = null;
        }
    }

    /** Un fotograma de la animación (8 fps, en el hilo del mundo). */
    private void tickAnimacion() {
        if (!abierta) {
            cancelarAnimacion();
            return;
        }
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        EstadoCombate c = d != null ? d.combate : null;
        if (d == null || !d.menuAbierto || c == null) {
            cancelarAnimacion();
            return;
        }

        UICommandBuilder cmd = new UICommandBuilder();
        boolean fin = c.fase == EstadoCombate.Fase.FIN_COMBATE;
        boolean esperando = c.esperandoJugador && c.actorActual != null && c.actorActual.aliado();
        boolean cine = c.cinematicaActiva;

        // Durante la cinemática no se refresca el estado: se avisa y al terminar
        // se pinta todo de golpe (así los números cambian cuando acaba el golpe).
        if (cine) {
            String actor = c.actorActual != null ? c.nombreDe(c.actorActual) : "";
            cmd.set("#CmdTitulo.Text", "ANIMANDO COMBATE");
            cmd.set("#CmdHint.Text", "Animación de " + actor + "...");
            cmd.set("#LogTurno.Text", "ANIMANDO · " + actor);
            // Los botones no se pueden pulsar mientras dura la animación:
            // se ocultan para que no parezca que la UI está colgada.
            cmd.set("#CmdRootGroup.Visible", false);
            cmd.set("#CmdListGroup.Visible", false);
            cmd.set("#CmdVolver.Visible", false);
            // La fila 0 refleja al que está actuando AHORA (no al siguiente).
            if (c.actorActual != null) {
                cmd.set("#TurnName0.Text", ">> " + actor);
                cmd.set("#TurnState0.Text", "AHORA");
            }
        } else {
            int actorClave = c.actorActual == null ? -1
                : (c.actorActual.aliado() ? 1 + c.actorActual.idx() : 10 + c.actorActual.idx());
            boolean cambioTurno = esperando != esperaAnterior || actorClave != actorAnterior;
            if (cineAnterior || cambioTurno || (fin && !finMostrado)) {
                refrescar(cmd);
                esperaAnterior = esperando;
                actorAnterior = actorClave;
                Ref<EntityStore> ref = playerRef.getReference();
                if (ref != null && ref.isValid() && fin && !finMostrado) {
                    finMostrado = true;
                    MenuBatalla.notificarFin(playerRef, ref, ref.getStore());
                }
            }
        }
        cineAnterior = cine;

        // Los botones solo se pueden pulsar cuando es tu turno y no hay cine.
        boolean puedePulsar = esperando && !cine;
        cmd.set("#CmdRootGroup.Visible", d.menuNivel == 0 && puedePulsar);
        cmd.set("#CmdListGroup.Visible", d.menuNivel == 1 && puedePulsar);
        cmd.set("#CmdVolver.Visible", d.menuNivel == 1 && puedePulsar);

        sendUpdate(cmd);
        if (fin && !cine) cancelarAnimacion();
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref,
                          @Nonnull Store<EntityStore> store) {
        abierta = false;
        cancelarAnimacion();
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d != null && d.menuAbierto) {
            MenuBatalla.cerrar(playerRef, ref, store);
        }
    }
}
