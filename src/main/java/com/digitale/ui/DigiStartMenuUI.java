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
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store) {
        LOGGER.info("DigiStartMenuUI.open()");
        DigiStartMenuUI page = new DigiStartMenuUI(playerRef);
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
        String ui = """
            Group {
                LayoutMode: Top;
                Anchor: (Height: 500, Width: 600);

                Group {
                    LayoutMode: Top;
                    Background: (Color: #1a1a2e);
                    Padding: (Full: 15);

                    Label { Text: "DIGIMON DigiRise - Elige tu companero"; Style: (TextColor: #00d4ff, FontSize: 20, HorizontalAlignment: Center); Anchor: (Height: 30); }
                    TextButton #BtnBotamon { Text: "Botamon"; Anchor: (Width: 120, Height: 32); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                    TextButton #BtnKuramon { Text: "Kuramon"; Anchor: (Width: 120, Height: 32); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                    TextButton #BtnPunimon { Text: "Punimon"; Anchor: (Width: 120, Height: 32); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                    TextButton #BtnChibimon { Text: "Chibimon"; Anchor: (Width: 120, Height: 32); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                    TextButton #BtnPoyomon { Text: "Poyomon"; Anchor: (Width: 120, Height: 32); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                    TextButton #BtnYuramon { Text: "Yuramon"; Anchor: (Width: 120, Height: 32); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                    TextButton #BtnPichimon { Text: "Pichimon"; Anchor: (Width: 120, Height: 32); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                    TextButton #BtnNyokimon { Text: "Nyokimon"; Anchor: (Width: 120, Height: 32); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 13, HorizontalAlignment: Center))); }
                    Label #MsgInfo { Text: ""; Style: (TextColor: #ff00ff, FontSize: 14, HorizontalAlignment: Center); Anchor: (Height: 18); }
                    TextButton #BtnConfirmar { Text: "Confirmar"; Anchor: (Width: 160, Height: 40); Style: (Default: (Background: (Color: #0f3460), LabelStyle: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #ffffff, FontSize: 14, HorizontalAlignment: Center))); }
                    TextButton #BtnVolver { Text: "Cancelar"; Anchor: (Width: 160, Height: 40); Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #8899aa, FontSize: 14, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #e0e8f0, FontSize: 14, HorizontalAlignment: Center))); }
                }
            }
            """;
        cmd.appendInline(null, ui);

        String[] starters = {"Botamon", "Kuramon", "Punimon", "Chibimon", "Poyomon", "Yuramon", "Pichimon", "Nyokimon"};
        for (String s : starters) {
            event.addEventBinding(CustomUIEventBindingType.Activating, "#Btn" + s,
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
