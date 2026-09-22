package com.digitale.ui;

import com.digitale.crianza.DigitamaDef;
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

/**
 * Receta de incubación revelada por un Dato Incubación.
 * Muestra huevo, ingredientes, entorno, energía, ciclos y resultado.
 */
public class DigiRecetaUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(DigiRecetaUI.class.getName());

    private final DigitamaDef def;

    public DigiRecetaUI(@Nonnull PlayerRef playerRef, @Nonnull DigitamaDef def) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.def = def;
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store,
                            @Nonnull DigitamaDef def) {
        LOGGER.info("DigiRecetaUI.open() " + def.idItem);
        DigiRecetaUI page = new DigiRecetaUI(playerRef, def);
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
                Anchor: (Height: 500, Width: 560);

                Group {
                    LayoutMode: Top;
                    Background: (Color: #0b1220);
                    Padding: (Full: 15);

                    Label {
                        Text: "DATO INCUBACION";
                        Style: (TextColor: #ffcc00, FontSize: 18, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 26);
                    }
                    Label #RecHuevo {
                        Text: "";
                        Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 22);
                    }
                    Label #RecFam {
                        Text: "";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Group { Anchor: (Height: 4); }
                    Group { Anchor: (Height: 2); Background: (Color: #ffcc00); }
                    Group { Anchor: (Height: 4); }
                    Label {
                        Text: "INGREDIENTES";
                        Style: (TextColor: #00d4ff, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
            """);
        for (int i = 0; i < 8; i++) {
            sb.append("                    Label #RecIng").append(i).append(" {\n");
            sb.append("                        Text: \"\";\n");
            sb.append("                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);\n");
            sb.append("                        Anchor: (Height: 16);\n");
            sb.append("                    }\n");
        }
        sb.append("""
                    Group { Anchor: (Height: 4); }
                    Label #RecEntorno {
                        Text: "";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                        Anchor: (Height: 32);
                    }
                    Label #RecEnergia {
                        Text: "";
                        Style: (TextColor: #ffcc00, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #RecResultado {
                        Text: "";
                        Style: (TextColor: #00ff88, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 20);
                    }
                    Group { Anchor: (Height: 8); }
                    TextButton #BtnCerrar {
                        Text: "Cerrar";
                        Anchor: (Width: 160, Height: 36);
                        Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #8899aa, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #e0e8f0, FontSize: 13, HorizontalAlignment: Center)));
                    }
                }
            }
            """);
        cmd.appendInline(null, sb.toString());

        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCerrar",
            EventData.of("action", "cerrar"), false);

        cmd.set("#RecHuevo.Text", def.nombre + " (" + def.idItem + ")");
        cmd.set("#RecFam.Text", def.familia + " | " + def.atributo + " | " + def.tier);
        for (int i = 0; i < 8; i++) {
            if (i < def.ingredientes.length) {
                cmd.set("#RecIng" + i + ".Visible", true);
                cmd.set("#RecIng" + i + ".Text", "- " + def.ingredientes[i]);
            } else {
                cmd.set("#RecIng" + i + ".Text", "");
                cmd.set("#RecIng" + i + ".Visible", false);
            }
        }
        cmd.set("#RecEntorno.Text", "Entorno: " + def.entorno);
        cmd.set("#RecEnergia.Text", "Energia " + def.energia + "/10 | Ciclos: " + def.ciclos);
        cmd.set("#RecResultado.Text", "Nace: " + def.especie + " (Nv" + def.nivelRes + ")");
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull String jsonData) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String action = json.has("action") ? json.get("action").getAsString() : "";
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        if ("cerrar".equals(action)) {
            player.getPageManager().setPage(ref, store,
                com.hypixel.hytale.protocol.packets.interface_.Page.None);
        } else {
            UICommandBuilder cmd = new UICommandBuilder();
            sendUpdate(cmd);
        }
    }
}
