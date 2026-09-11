package com.digitale.ui;

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
import java.util.logging.Logger;

public class DigiMainMenuUI extends CustomUIPage {
    private static final Logger LOGGER = Logger.getLogger(DigiMainMenuUI.class.getName());

    private String pestanaActiva = "estado";

    public DigiMainMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store) {
        DigiMainMenuUI page = new DigiMainMenuUI(playerRef);
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

                    Label {
                        Text: "DIGIMON - ESTADO";
                        Style: (TextColor: #00d4ff, FontSize: 20, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 26);
                    }
                    Group { Anchor: (Height: 6); }

                    Group {
                        LayoutMode: Left;

                        TextButton #TabInfo {
                            Text: "Info";
                            Anchor: (Width: 80, Height: 32);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 13, TextColor: #00d4ff, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        };
                        TextButton #TabStats {
                            Text: "Stats";
                            Anchor: (Width: 80, Height: 32);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        };
                        TextButton #TabTecnicas {
                            Text: "Tecnicas";
                            Anchor: (Width: 80, Height: 32);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        };
                        TextButton #TabEvolucion {
                            Text: "Evolucion";
                            Anchor: (Width: 80, Height: 32);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        };
                    }
                    Group { Anchor: (Height: 4); }
                    Label {
                        Text: "________________________";
                        Style: (TextColor: #00d4ff, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Group { Anchor: (Height: 4); }

                    Label {
                        Text: "--- COMPANERO A ---";
                        Style: (TextColor: #ff00ff, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Label #NombreA {
                        Text: "---";
                        Style: (TextColor: #e0e8f0, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Label #EspecieA {
                        Text: "---";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #ElementoA {
                        Text: "---";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #HpA {
                        Text: "HP: ---";
                        Style: (TextColor: #00d4ff, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #AtkA {
                        Text: "ATK: ---";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #DefA {
                        Text: "DEF: ---";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #SpdA {
                        Text: "SPD: ---";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #WisA {
                        Text: "WIS: ---";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #LazoA {
                        Text: "Lazo: ---";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #AbiA {
                        Text: "ABI: ---";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #VicA {
                        Text: "Victorias: ---";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #EnergiaA {
                        Text: "Energia: ---";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }

                    Group { Anchor: (Height: 8); }
                    Label {
                        Text: "________________________";
                        Style: (TextColor: #00d4ff, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Group { Anchor: (Height: 4); }

                    Label {
                        Text: "--- COMPANERO B ---";
                        Style: (TextColor: #ff00ff, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Label #NombreB {
                        Text: "---";
                        Style: (TextColor: #e0e8f0, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Label #EspecieB {
                        Text: "---";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #ElementoB {
                        Text: "---";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #HpB {
                        Text: "HP: ---";
                        Style: (TextColor: #00d4ff, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #AtkB {
                        Text: "ATK: ---";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #DefB {
                        Text: "DEF: ---";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #SpdB {
                        Text: "SPD: ---";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #WisB {
                        Text: "WIS: ---";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #LazoB {
                        Text: "Lazo: ---";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #AbiB {
                        Text: "ABI: ---";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #VicB {
                        Text: "Victorias: ---";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #EnergiaB {
                        Text: "Energia: ---";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }

                    Group { Anchor: (Height: 8); }

                    TextButton #BtnBack {
                        Text: "Cerrar";
                        Anchor: (Width: 160, Height: 40);
                        Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 14, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)));
                    }
                }
            }
            """;
        cmd.appendInline(null, ui);

        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabInfo",
            EventData.of("action", "tab").append("value", "info"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabStats",
            EventData.of("action", "tab").append("value", "stats"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabTecnicas",
            EventData.of("action", "tab").append("value", "tecnicas"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabEvolucion",
            EventData.of("action", "tab").append("value", "evolucion"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnBack",
            EventData.of("action", "cerrar"), false);

        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        if (datos != null) {
            setDigimon(cmd, datos.companeroA, "A");
            setDigimon(cmd, datos.companeroB, "B");
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull String jsonData) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String action = json.has("action") ? json.get("action").getAsString() : "";

        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        switch (action) {
            case "tab" -> {
                pestanaActiva = json.has("value") ? json.get("value").getAsString() : "";
                UICommandBuilder cmd = new UICommandBuilder();
                if (datos != null) {
                    switch (pestanaActiva) {
                        case "info", "estado" -> {
                            setDigimon(cmd, datos.companeroA, "A");
                            setDigimon(cmd, datos.companeroB, "B");
                        }
                        case "evolucionar", "evolucion" -> actualizarEvolucion(cmd, datos);
                        case "entrenar", "stats" -> actualizarEnergias(cmd, datos);
                        case "cuidar", "tecnicas" -> actualizarCuidar(cmd, datos);
                    }
                }
                sendUpdate(cmd);
            }
            case "cerrar" -> player.getPageManager().setPage(ref, store,
                com.hypixel.hytale.protocol.packets.interface_.Page.None);
        }
    }

    private void setDigimon(UICommandBuilder cmd, DatoDigimon d, String slot) {
        if (d == null || !d.vivo) {
            cmd.set("#Nombre" + slot + ".Text", "Sin companero");
            cmd.set("#Especie" + slot + ".Text", "---");
            cmd.set("#Elemento" + slot + ".Text", "---");
            cmd.set("#Hp" + slot + ".Text", "HP: ---");
            cmd.set("#Atk" + slot + ".Text", "ATK: ---");
            cmd.set("#Def" + slot + ".Text", "DEF: ---");
            cmd.set("#Spd" + slot + ".Text", "SPD: ---");
            cmd.set("#Wis" + slot + ".Text", "WIS: ---");
            cmd.set("#Lazo" + slot + ".Text", "Lazo: ---");
            cmd.set("#Abi" + slot + ".Text", "ABI: ---");
            cmd.set("#Vic" + slot + ".Text", "Victorias: ---");
            cmd.set("#Energia" + slot + ".Text", "Energia: ---");
            return;
        }
        cmd.set("#Nombre" + slot + ".Text", d.nombre);
        cmd.set("#Especie" + slot + ".Text", d.especie + " - " + d.nombreNivel());
        cmd.set("#Elemento" + slot + ".Text", d.elemento);
        cmd.set("#Hp" + slot + ".Text", "HP: " + d.hp + "/" + d.maxHp);
        cmd.set("#Atk" + slot + ".Text", "ATK: " + d.atk);
        cmd.set("#Def" + slot + ".Text", "DEF: " + d.def);
        cmd.set("#Spd" + slot + ".Text", "SPD: " + d.spd);
        cmd.set("#Wis" + slot + ".Text", "WIS: " + d.wis);
        cmd.set("#Lazo" + slot + ".Text", "Lazo: " + d.lazo + "/100");
        cmd.set("#Abi" + slot + ".Text", "ABI: " + d.abi);
        cmd.set("#Vic" + slot + ".Text", "Victorias: " + d.victorias);
        cmd.set("#Energia" + slot + ".Text", "Energia: " + d.energia + "/100");
    }

    private void actualizarEvolucion(UICommandBuilder cmd, DatosJugador datos) {
    }

    private void actualizarEnergias(UICommandBuilder cmd, DatosJugador datos) {
    }

    private void actualizarCuidar(UICommandBuilder cmd, DatosJugador datos) {
    }
}
