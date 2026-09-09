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
            $C = "Common.ui";
            $P = "Digi.ui";

            Group {
                LayoutMode: Top;
                Anchor: (Height: 520, Width: 500);

                $C.@DecoratedContainer {
                    #Title {
                        Group {
                            LayoutMode: Left;
                            Padding: (Horizontal: 16, Vertical: 4);

                            Label {
                                Text: "CRIANZA DE DIGIMON";
                                Style: (TextColor: #ffcc00, FontSize: 18, RenderBold: true);
                                Anchor: (Left: 8);
                            }

                            Group { FlexWeight: 1; }

                            TextButton #BtnVolver {
                                Anchor: (Width: 100, Height: 40);
                                Text: "Volver";
                                Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 14, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 14, TextColor: #ffffff, HorizontalAlignment: Center)), Pressed: (Background: (Color: #0f3460), LabelStyle: (FontSize: 14, TextColor: #e0e8f0, HorizontalAlignment: Center)));
                            }
                        }
                    }

                    #ContentTabs {
                        LayoutMode: Top;
                        Padding: (Full: 16);
                        Anchor: (Top: 6, Bottom: 8, Left: 8, Right: 8);

                        Label {
                            Text: "SELECCIONA UN DIGIMON";
                            Style: (TextColor: #00d4ff, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                            Anchor: (Height: 20);
                        }

                        Group { Anchor: (Height: 12); }

                        Group {
                            LayoutMode: Left;

                            $P.@Panel #SlotA {
                                Anchor: (Width: 200, Height: 130);
                                Background: (TexturePath: "Digi/Digi_Container.png", Border: 8);
                                Padding: (Full: 8);

                                AssetImage #IconA {
                                    Anchor: (Top: 0, Left: 0, Width: 80, Height: 80);
                                    AssetPath: "Icons/ModelsGenerated/DigiPlaceholder.png";
                                }

                                Label #NombreA {
                                    Anchor: (Top: 4, Left: 0, Width: 200);
                                    Text: "-";
                                    Style: (TextColor: #00d4ff, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                                }

                                Label #NivelA {
                                    Anchor: (Top: 22, Left: 0, Width: 200);
                                    Text: "-";
                                    Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                }

                                $C.@ProgressBar #BarA {
                                    Anchor: (Bottom: 4, Left: 0, Width: 200);
                                    Value: 1.0;
                                }

                                Group {
                                    LayoutMode: Left;
                                    Anchor: (Top: 50, Left: 8);

                                    $C.@SecondaryButton #BtnAlimentarA {
                                        Anchor: (Width: 80, Height: 32);
                                        Padding: (Horizontal: 4, Vertical: 4);
                                        Label { Text: "🍖 Alimentar"; Style: (TextColor: #e0e8f0, FontSize: 11, VerticalAlignment: Center); }
                                    }

                                    Group { Anchor: (Width: 4); }

                                    $C.@SecondaryButton #BtnDormirA {
                                        Anchor: (Width: 80, Height: 32);
                                        Padding: (Horizontal: 4, Vertical: 4);
                                        Label { Text: "💤 Dormir"; Style: (TextColor: #e0e8f0, FontSize: 11, VerticalAlignment: Center); }
                                    }

                                    Group { Anchor: (Width: 4); }

                                    $C.@SecondaryButton #BtnJugarA {
                                        Anchor: (Width: 80, Height: 32);
                                        Padding: (Horizontal: 4, Vertical: 4);
                                        Label { Text: "🎮 Jugar"; Style: (TextColor: #e0e8f0, FontSize: 11, VerticalAlignment: Center); }
                                    }
                                }
                            }

                            Group { FlexWeight: 1; }

                            $P.@Panel #SlotB {
                                Anchor: (Width: 200, Height: 130);
                                Background: (TexturePath: "Digi/Digi_Container.png", Border: 8);
                                Padding: (Full: 8);

                                AssetImage #IconB {
                                    Anchor: (Top: 0, Left: 0, Width: 80, Height: 80);
                                    AssetPath: "Icons/ModelsGenerated/DigiPlaceholder.png";
                                }

                                Label #NombreB {
                                    Anchor: (Top: 4, Left: 0, Width: 200);
                                    Text: "-";
                                    Style: (TextColor: #ff00ff, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                                }

                                Label #NivelB {
                                    Anchor: (Top: 22, Left: 0, Width: 200);
                                    Text: "-";
                                    Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                                }

                                $C.@ProgressBar #BarB {
                                    Anchor: (Bottom: 4, Left: 0, Width: 200);
                                    Value: 1.0;
                                }

                                Group {
                                    LayoutMode: Left;
                                    Anchor: (Top: 50, Left: 8);

                                    $C.@SecondaryButton #BtnAlimentarB {
                                        Anchor: (Width: 80, Height: 32);
                                        Padding: (Horizontal: 4, Vertical: 4);
                                        Label { Text: "🍖 Alimentar"; Style: (TextColor: #e0e8f0, FontSize: 11, VerticalAlignment: Center); }
                                    }

                                    Group { Anchor: (Width: 4); }

                                    $C.@SecondaryButton #BtnDormirB {
                                        Anchor: (Width: 80, Height: 32);
                                        Padding: (Horizontal: 4, Vertical: 4);
                                        Label { Text: "💤 Dormir"; Style: (TextColor: #e0e8f0, FontSize: 11, VerticalAlignment: Center); }
                                    }

                                    Group { Anchor: (Width: 4); }

                                    $C.@SecondaryButton #BtnJugarB {
                                        Anchor: (Width: 80, Height: 32);
                                        Padding: (Horizontal: 4, Vertical: 4);
                                        Label { Text: "🎮 Jugar"; Style: (TextColor: #e0e8f0, FontSize: 11, VerticalAlignment: Center); }
                                    }
                                }
                            }
                        }

                        Group { Anchor: (Height: 16); }

                        Group {
                            LayoutMode: Top;

                            Label {
                                Text: "INFORMACION DE CRIANZA";
                                Style: (TextColor: #ff00ff, FontSize: 14, RenderBold: true);
                                Anchor: (Height: 18);
                            }

                            Group { Anchor: (Height: 8); }

                            Label #LblInfo {
                                Text: "-";
                                Style: (TextColor: #e0e8f0, FontSize: 13, Wrap: true);
                                Anchor: (Height: 100);
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
            double vidaRatio = d.companeroA.maxHp > 0 ? (double) d.companeroA.hp / d.companeroA.maxHp : 0;
            cmd.set("#BarA.Value", vidaRatio);
            cmd.set("#IconA.AssetPath", "Icons/ModelsGenerated/" + d.companeroA.especie + ".png");

            String estadoA = String.format(
                "🍖 Hambre: %d/100 | ❤️ Energia: %d/100 | 💖 Lazo: %d/100\n❤️ HP: %d/%d",
                d.companeroA.hambre, d.companeroA.energia, d.companeroA.lazo, d.companeroA.hp, d.companeroA.maxHp);
            cmd.set("#LblInfo.Text", estadoA);
        } else {
            cmd.set("#NombreA.Text", "-");
            cmd.set("#NivelA.Text", "-");
            cmd.set("#BarA.Value", 0);
            cmd.set("#IconA.AssetPath", "Icons/ModelsGenerated/DigiPlaceholder.png");
        }

        // Slot B
        if (d.companeroB != null) {
            cmd.set("#NombreB.Text", d.companeroB.nombre);
            cmd.set("#NivelB.Text", d.companeroB.especie + " - Nivel " + d.companeroB.nivel);
            double vidaRatio = d.companeroB.maxHp > 0 ? (double) d.companeroB.hp / d.companeroB.maxHp : 0;
            cmd.set("#BarB.Value", vidaRatio);
            cmd.set("#IconB.AssetPath", "Icons/ModelsGenerated/" + d.companeroB.especie + ".png");
        } else {
            cmd.set("#NombreB.Text", "-");
            cmd.set("#NivelB.Text", "-");
            cmd.set("#BarB.Value", 0);
            cmd.set("#IconB.AssetPath", "Icons/ModelsGenerated/DigiPlaceholder.png");
        }
    }
}
