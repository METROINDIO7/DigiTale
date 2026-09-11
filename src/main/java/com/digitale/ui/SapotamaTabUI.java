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

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder event,
                      @Nonnull Store<EntityStore> store) {
        String ui = buildPanel();
        cmd.appendInline(null, ui);

        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabEquipo",
            EventData.of("action", "tab").append("value", "equipo"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabCrianza",
            EventData.of("action", "tab").append("value", "crianza"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#TabBatalla",
            EventData.of("action", "tab").append("value", "batalla"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnIntercambiarA",
            EventData.of("action", "intercambiar"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnIntercambiarB",
            EventData.of("action", "intercambiar"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnPerfilA",
            EventData.of("action", "perfil").append("value", "A"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnPerfilB",
            EventData.of("action", "perfil").append("value", "B"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCrianza",
            EventData.of("action", "crianza"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnBatallarA",
            EventData.of("action", "batallar").append("slot", "A"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnBatallarB",
            EventData.of("action", "batallar").append("slot", "B"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCerrar",
            EventData.of("action", "cerrar"), false);

        applyTab(null, cmd, store);
    }

    private String buildPanel() {
        return """
            Group {
                LayoutMode: Top;
                Anchor: (Height: 520, Width: 500);

                Group {
                    LayoutMode: Top;
                    Background: (Color: #1a1a2e);
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
                        };
                        TextButton #TabCrianza {
                            Text: "Crianza";
                            Anchor: (Width: 80, Height: 32);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        };
                        TextButton #TabBatalla {
                            Text: "Batalla";
                            Anchor: (Width: 80, Height: 32);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        };
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
                        Background: (Color: #12121e);
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
                        Background: (Color: #12121e);
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
                        Background: (Color: #12121e);
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
            case "tab" -> applyTab(json, new UICommandBuilder(), store);
            case "intercambiar" -> onIntercambiar(ref, store, player, json);
            case "perfil" -> onPerfil(ref, store, player, json);
            case "crianza" -> onCrianza(ref, store, player);
            case "batallar" -> onBatallar(ref, store, player, json);
            case "cerrar" -> onCerrar(ref, store, player);
        }
    }

    private void applyTab(JsonObject json, UICommandBuilder cmd, Store<EntityStore> store) {
        String tab = "equipo";
        if (json != null && json.has("value")) {
            tab = json.get("value").getAsString();
        }

        cmd.set("#TabEquipoPanel.Visible", "equipo".equals(tab));
        cmd.set("#TabCrianzaPanel.Visible", "crianza".equals(tab));
        cmd.set("#TabBatallaPanel.Visible", "batalla".equals(tab));

        if ("equipo".equals(tab)) {
            actualizarEquipo(cmd, store);
        } else if ("crianza".equals(tab)) {
            actualizarCrianza(cmd, store);
        } else if ("batalla".equals(tab)) {
            actualizarBatalla(cmd, store);
        }

        sendUpdate(cmd);
    }

    private void actualizarEquipo(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        if (d.companeroA != null) {
            cmd.set("#NombreA.Text", d.companeroA.nombre);
            cmd.set("#NivelA.Text", d.companeroA.especie + " - Nivel " + d.companeroA.nivel + " (" + d.companeroA.nombreNivel() + ")");
            int filled = d.companeroA.maxHp > 0 ? (d.companeroA.hp * 20 / d.companeroA.maxHp) : 0;
            cmd.set("#BarA.Text", "=".repeat(Math.max(0, filled)) + " ".repeat(Math.max(0, 20 - filled)));
        } else {
            cmd.set("#NombreA.Text", "-");
            cmd.set("#NivelA.Text", "-");
            cmd.set("#BarA.Text", "====================");
        }

        if (d.companeroB != null) {
            cmd.set("#NombreB.Text", d.companeroB.nombre);
            cmd.set("#NivelB.Text", d.companeroB.especie + " - Nivel " + d.companeroB.nivel + " (" + d.companeroB.nombreNivel() + ")");
            int filled = d.companeroB.maxHp > 0 ? (d.companeroB.hp * 20 / d.companeroB.maxHp) : 0;
            cmd.set("#BarB.Text", "=".repeat(Math.max(0, filled)) + " ".repeat(Math.max(0, 20 - filled)));
        } else {
            cmd.set("#NombreB.Text", "-");
            cmd.set("#NivelB.Text", "-");
            cmd.set("#BarB.Text", "====================");
        }
    }

    private void actualizarCrianza(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        String info = "A: " + (d.companeroA != null ? d.companeroA.nombre + " [HP:" + d.companeroA.hp + "/" + d.companeroA.maxHp + "] [Hambre:" + d.companeroA.hambre + "] [Energia:" + d.companeroA.energia + "] [Lazo:" + d.companeroA.lazo + "]" : "-")
                     + " | B: " + (d.companeroB != null ? d.companeroB.nombre + " [HP:" + d.companeroB.hp + "/" + d.companeroB.maxHp + "] [Hambre:" + d.companeroB.hambre + "] [Energia:" + d.companeroB.energia + "] [Lazo:" + d.companeroB.lazo + "]" : "-");
        cmd.set("#LblCrianza.Text", info);
    }

    private void actualizarBatalla(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        String info = "A: " + (d.companeroA != null ? d.companeroA.nombre + " (HP:" + d.companeroA.hp + "/" + d.companeroA.maxHp + ")" : "-")
                     + " | B: " + (d.companeroB != null ? d.companeroB.nombre + " (HP:" + d.companeroB.hp + "/" + d.companeroB.maxHp + ")" : "-");
        cmd.set("#LblBatallaInfo.Text", info);
    }

    private void onIntercambiar(Ref<EntityStore> ref, Store<EntityStore> store, Player player, JsonObject json) {
        LOGGER.log(Level.INFO, "Intercambiando compañeros");
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        // Intercambiar A y B
        DatoDigimon temp = d.companeroA;
        d.companeroA = d.companeroB;
        d.companeroB = temp;

        PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
        progress.guardarDesde(d);

        applyTab(null, new UICommandBuilder(), store);
    }

    private void onPerfil(Ref<EntityStore> ref, Store<EntityStore> store, Player player, JsonObject json) {
        LOGGER.log(Level.INFO, "Abrir perfil del digimon");
        String slot = "A";
        if (json.has("value")) {
            String val = json.get("value").getAsString();
            if ("B".equals(val)) slot = "B";
        }
        DigiPerfilDigimonUI.open(playerRef, ref, store, slot);
    }

    private void onCrianza(Ref<EntityStore> ref, Store<EntityStore> store, Player player) {
        LOGGER.log(Level.INFO, "Crianza - abrir menú de crianza");
        player.getPageManager().setPage(ref, store,
            com.hypixel.hytale.protocol.packets.interface_.Page.None);
        DigiCuidarMenuUI.open(playerRef, ref, store);
    }

    private void onBatallar(Ref<EntityStore> ref, Store<EntityStore> store, Player player, JsonObject json) {
        LOGGER.log(Level.INFO, "Batallar - abrir menú de batalla");
        player.getPageManager().setPage(ref, store,
            com.hypixel.hytale.protocol.packets.interface_.Page.None);
        DigiBatallaMenuUI.open(playerRef, ref, store);
    }

    private void onCerrar(Ref<EntityStore> ref, Store<EntityStore> store, Player player) {
        player.getPageManager().setPage(ref, store,
            com.hypixel.hytale.protocol.packets.interface_.Page.None);
    }
}
