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
                Style: (BackgroundColor: #1a1a2e);
                Padding: 15;

                Label {
                    Text: "DIGIMON - ESTADO";
                    Style: (FontSize: 20, TextColor: #00d4ff, Alignment: Center, Padding: (Bottom: 10));
                }
                Group {
                    LayoutMode: Left;
                    Padding: (Bottom: 6);
                    Label #TabInfo { Text: "Info"; Style: (FontSize: 13, TextColor: #00d4ff, Alignment: Center, BackgroundColor: #0f3460); Anchor: (Width: 80, Height: 32); };
                    Label #TabStats { Text: "Stats"; Style: (FontSize: 13, TextColor: #e0e8f0, Alignment: Center, BackgroundColor: #16213e); Anchor: (Width: 80, Height: 32); };
                    Label #TabTecnicas { Text: "Tecnicas"; Style: (FontSize: 13, TextColor: #e0e8f0, Alignment: Center, BackgroundColor: #16213e); Anchor: (Width: 80, Height: 32); };
                    Label #TabEvolucion { Text: "Evolucion"; Style: (FontSize: 13, TextColor: #e0e8f0, Alignment: Center, BackgroundColor: #16213e); Anchor: (Width: 80, Height: 32); };
                }
                Label { Text: "________________________"; Style: (TextColor: #00d4ff, Alignment: Center, Padding: (Bottom: 8)); }

                Label { Text: "--- COMPANERO A ---"; Style: (FontSize: 14, TextColor: #ff00ff, Alignment: Center, Padding: (Bottom: 4)); }
                Label #NombreA { Text: "---"; Style: (FontSize: 14, TextColor: #e0e8f0, Alignment: Center); }
                Label #EspecieA { Text: "---"; Style: (FontSize: 12, TextColor: #8899aa, Alignment: Center); }
                Label #ElementoA { Text: "---"; Style: (FontSize: 12, TextColor: #8899aa, Alignment: Center, Padding: (Bottom: 4)); }
                Label #HpA { Text: "HP: ---"; Style: (FontSize: 12, TextColor: #00d4ff, Alignment: Center); }
                Label #AtkA { Text: "ATK: ---"; Style: (FontSize: 12, TextColor: #e0e8f0, Alignment: Center); }
                Label #DefA { Text: "DEF: ---"; Style: (FontSize: 12, TextColor: #e0e8f0, Alignment: Center); }
                Label #SpdA { Text: "SPD: ---"; Style: (FontSize: 12, TextColor: #e0e8f0, Alignment: Center); }
                Label #WisA { Text: "WIS: ---"; Style: (FontSize: 12, TextColor: #e0e8f0, Alignment: Center); }
                Label #LazoA { Text: "Lazo: ---"; Style: (FontSize: 12, TextColor: #8899aa, Alignment: Center); }
                Label #AbiA { Text: "ABI: ---"; Style: (FontSize: 12, TextColor: #8899aa, Alignment: Center); }
                Label #VicA { Text: "Victorias: ---"; Style: (FontSize: 12, TextColor: #8899aa, Alignment: Center); }
                Label #EnergiaA { Text: "Energia: ---"; Style: (FontSize: 12, TextColor: #8899aa, Alignment: Center, Padding: (Bottom: 8)); }

                Label { Text: "________________________"; Style: (TextColor: #00d4ff, Alignment: Center, Padding: (Bottom: 8)); }

                Label { Text: "--- COMPANERO B ---"; Style: (FontSize: 14, TextColor: #ff00ff, Alignment: Center, Padding: (Bottom: 4)); }
                Label #NombreB { Text: "---"; Style: (FontSize: 14, TextColor: #e0e8f0, Alignment: Center); }
                Label #EspecieB { Text: "---"; Style: (FontSize: 12, TextColor: #8899aa, Alignment: Center); }
                Label #ElementoB { Text: "---"; Style: (FontSize: 12, TextColor: #8899aa, Alignment: Center, Padding: (Bottom: 4)); }
                Label #HpB { Text: "HP: ---"; Style: (FontSize: 12, TextColor: #00d4ff, Alignment: Center); }
                Label #AtkB { Text: "ATK: ---"; Style: (FontSize: 12, TextColor: #e0e8f0, Alignment: Center); }
                Label #DefB { Text: "DEF: ---"; Style: (FontSize: 12, TextColor: #e0e8f0, Alignment: Center); }
                Label #SpdB { Text: "SPD: ---"; Style: (FontSize: 12, TextColor: #e0e8f0, Alignment: Center); }
                Label #WisB { Text: "WIS: ---"; Style: (FontSize: 12, TextColor: #e0e8f0, Alignment: Center); }
                Label #LazoB { Text: "Lazo: ---"; Style: (FontSize: 12, TextColor: #8899aa, Alignment: Center); }
                Label #AbiB { Text: "ABI: ---"; Style: (FontSize: 12, TextColor: #8899aa, Alignment: Center); }
                Label #VicB { Text: "Victorias: ---"; Style: (FontSize: 12, TextColor: #8899aa, Alignment: Center); }
                Label #EnergiaB { Text: "Energia: ---"; Style: (FontSize: 12, TextColor: #8899aa, Alignment: Center, Padding: (Bottom: 8)); }

                Label #BtnBack {
                    Text: "Cerrar";
                    Anchor: (Width: 160, Height: 40);
                    Style: (FontSize: 14, TextColor: #e0e8f0, Alignment: Center, Padding: 8, BackgroundColor: #0f3460);
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
