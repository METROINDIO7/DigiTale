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

public class DigiCuidarMenuUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(DigiCuidarMenuUI.class.getName());

    public DigiCuidarMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store) {
        LOGGER.log(Level.INFO, "DigiCuidarMenuUI.open()");

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null) {
            LOGGER.log(Level.INFO, "Sin datos - redirigiendo al menú principal");
            DigiMainMenuUI.open(playerRef, ref, store);
            return;
        }

        DigiCuidarMenuUI page = new DigiCuidarMenuUI(playerRef);
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

        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnAlimentarA",
            EventData.of("action", "alimentar").append("slot", "A"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnAlimentarB",
            EventData.of("action", "alimentar").append("slot", "B"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnDormirA",
            EventData.of("action", "dormir").append("slot", "A"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnDormirB",
            EventData.of("action", "dormir").append("slot", "B"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnJugarA",
            EventData.of("action", "jugar").append("slot", "A"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnJugarB",
            EventData.of("action", "jugar").append("slot", "B"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnVolver",
            EventData.of("action", "volver"), false);

        actualizarEstado(cmd, store);
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
                        Text: "CRIANZA DE DIGIMON";
                        Style: (TextColor: #ffcc00, FontSize: 18, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 24);
                    }
                    Group {
                        LayoutMode: Left;

                        Group { FlexWeight: 1; }

                        TextButton #BtnVolver {
                            Text: "Volver";
                            Anchor: (Width: 80, Height: 32);
                            Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                    }
                    Group { Anchor: (Height: 6); }
                    Label {
                        Text: "________________________";
                        Style: (TextColor: #ffcc00, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Group { Anchor: (Height: 4); }

                    Label {
                        Text: "SELECCIONA UN DIGIMON";
                        Style: (TextColor: #00d4ff, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Group { Anchor: (Height: 6); }

                    Group {
                        LayoutMode: Left;

                        Group {
                            Anchor: (Width: 210, Height: 180);
                            Background: (Color: #16213e);
                            Padding: (Full: 8);
                            LayoutMode: Top;

                            Label {
                                Text: "COMPANERO A";
                                Style: (TextColor: #00d4ff, FontSize: 12, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Label #NombreA {
                                Text: "-";
                                Style: (TextColor: #e0e8f0, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 18);
                            }
                            Label #NivelA {
                                Text: "-";
                                Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Label #BarA {
                                Text: "";
                                Style: (TextColor: #00ff88, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 14);
                            }

                            Group { Anchor: (Height: 8); }

                            Group {
                                LayoutMode: Left;

                                TextButton #BtnAlimentarA {
                                    Text: "Alimentar";
                                    Anchor: (Width: 60, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 4); }

                                TextButton #BtnDormirA {
                                    Text: "Dormir";
                                    Anchor: (Width: 60, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 4); }

                                TextButton #BtnJugarA {
                                    Text: "Jugar";
                                    Anchor: (Width: 60, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }
                            }
                        }

                        Group { FlexWeight: 1; }

                        Group {
                            Anchor: (Width: 210, Height: 180);
                            Background: (Color: #16213e);
                            Padding: (Full: 8);
                            LayoutMode: Top;

                            Label {
                                Text: "COMPANERO B";
                                Style: (TextColor: #ff00ff, FontSize: 12, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Label #NombreB {
                                Text: "-";
                                Style: (TextColor: #e0e8f0, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 18);
                            }
                            Label #NivelB {
                                Text: "-";
                                Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Label #BarB {
                                Text: "";
                                Style: (TextColor: #00ff88, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 14);
                            }

                            Group { Anchor: (Height: 8); }

                            Group {
                                LayoutMode: Left;

                                TextButton #BtnAlimentarB {
                                    Text: "Alimentar";
                                    Anchor: (Width: 60, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 4); }

                                TextButton #BtnDormirB {
                                    Text: "Dormir";
                                    Anchor: (Width: 60, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }

                                Group { Anchor: (Width: 4); }

                                TextButton #BtnJugarB {
                                    Text: "Jugar";
                                    Anchor: (Width: 60, Height: 28);
                                    Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 10, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 10, TextColor: #ffffff, HorizontalAlignment: Center)));
                                }
                            }
                        }
                    }

                    Group { Anchor: (Height: 8); }
                    Label {
                        Text: "________________________";
                        Style: (TextColor: #ff00ff, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Group { Anchor: (Height: 4); }

                    Label {
                        Text: "INFORMACION DE CRIANZA";
                        Style: (TextColor: #ff00ff, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Label #LblInfo {
                        Text: "-";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                        Anchor: (Height: 60);
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
        LOGGER.log(Level.INFO, "DigiCuidarMenuUI - acción: " + action);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null) return;

        String slot = json.has("slot") ? json.get("slot").getAsString() : "A";
        DatoDigimon digimon = "A".equals(slot) ? datos.companeroA : datos.companeroB;

        switch (action) {
            case "alimentar" -> {
                if (digimon != null) {
                    digimon.alimentar(20);
                    LOGGER.log(Level.INFO, slot + " alimentado: hambre=" + digimon.hambre + ", energia=" + digimon.energia);
                }
                actualizarEstado(new UICommandBuilder(), store);
            }
            case "dormir" -> {
                if (digimon != null) {
                    digimon.descansar();
                    LOGGER.log(Level.INFO, slot + " durmiendo: energia=" + digimon.energia + ", hp=" + digimon.hp);
                }
                actualizarEstado(new UICommandBuilder(), store);
            }
            case "jugar" -> {
                if (digimon != null) {
                    digimon.interactuar();
                    digimon.energia = Math.min(100, digimon.energia + 5);
                    LOGGER.log(Level.INFO, slot + " jugando: lazo=" + digimon.lazo + ", energia=" + digimon.energia);
                }
                actualizarEstado(new UICommandBuilder(), store);
            }
            case "volver" -> {
                player.getPageManager().setPage(ref, store,
                    com.hypixel.hytale.protocol.packets.interface_.Page.None);
                SapotamaTabUI.open(playerRef, ref, store);
            }
        }
    }

    private void actualizarEstado(UICommandBuilder cmd, Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d == null) return;

        // Slot A
        if (d.companeroA != null) {
            cmd.set("#NombreA.Text", d.companeroA.nombre);
            cmd.set("#NivelA.Text", d.companeroA.especie + " - Nivel " + d.companeroA.nivel);
            int filled = d.companeroA.maxHp > 0 ? (d.companeroA.hp * 20 / d.companeroA.maxHp) : 0;
            cmd.set("#BarA.Text", "=".repeat(Math.max(0, filled)) + " ".repeat(Math.max(0, 20 - filled)));

            String estadoA = String.format(
                "Hambre:%d/100 Energia:%d/100 Lazo:%d/100 HP:%d/%d",
                d.companeroA.hambre, d.companeroA.energia, d.companeroA.lazo, d.companeroA.hp, d.companeroA.maxHp);
            cmd.set("#LblInfo.Text", estadoA);
        } else {
            cmd.set("#NombreA.Text", "-");
            cmd.set("#NivelA.Text", "-");
            cmd.set("#BarA.Text", "");
        }

        // Slot B
        if (d.companeroB != null) {
            cmd.set("#NombreB.Text", d.companeroB.nombre);
            cmd.set("#NivelB.Text", d.companeroB.especie + " - Nivel " + d.companeroB.nivel);
            int filled = d.companeroB.maxHp > 0 ? (d.companeroB.hp * 20 / d.companeroB.maxHp) : 0;
            cmd.set("#BarB.Text", "=".repeat(Math.max(0, filled)) + " ".repeat(Math.max(0, 20 - filled)));
        } else {
            cmd.set("#NombreB.Text", "-");
            cmd.set("#NivelB.Text", "-");
            cmd.set("#BarB.Text", "");
        }
    }
}
