package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.digitale.sistema.SistemaBatalla;
import com.digitale.sistema.SistemaPaseo;
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

public class DigiBatallaMenuUI extends CustomUIPage {
    private static final Logger LOGGER = Logger.getLogger(DigiBatallaMenuUI.class.getName());

    public DigiBatallaMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store) {
        LOGGER.log(Level.INFO, "DigiBatallaMenuUI.abrir()");

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null || !datos.enCombateUI()) {
            LOGGER.log(Level.WARNING, "Sin combate activo");
            return;
        }

        // Puesta en escena: party en formación + salvaje enfrente.
        SistemaPaseo.prepararBatalla(playerRef, ref, store);

        DigiBatallaMenuUI page = new DigiBatallaMenuUI(playerRef);
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
                LayoutMode: Middle;

                Group {
                    LayoutMode: Top;
                    Anchor: (Height: 580, Width: 560);
                    Background: (Color: #0b1220);
                    Padding: (Full: 12);

                    Label {
                        Text: "BATALLA DIGIMON";
                        Style: (TextColor: #ff00ff, FontSize: 18, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 24);
                    }
                    Label #LblTurno {
                        Text: "Ronda 1";
                        Style: (TextColor: #00d4ff, FontSize: 14, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Label #LblResultado {
                        Text: "";
                        Style: (TextColor: #ffcc00, FontSize: 13, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Label #LblCombo {
                        Text: "";
                        Style: (TextColor: #ffcc00, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Group { Anchor: (Height: 4); }
                    Group {
                        LayoutMode: Top;
                        Anchor: (Height: 110);
                        Background: (Color: #1a0f1a);
                        Padding: (Full: 8);
                        Group {
                            LayoutMode: Left;
                            Anchor: (Height: 40);
                            AssetImage #ImgEnem {
                                Anchor: (Width: 40, Height: 40);
                                AssetPath: "UI/Custom/png_Digis/botamon.png";
                            }
                            Group { Anchor: (Width: 8); }
                            Group {
                                LayoutMode: Top;
                                Anchor: (Width: 300);
                                Label #LblEnemigo {
                                    Text: "---";
                                    Style: (TextColor: #e0e8f0, FontSize: 13, HorizontalAlignment: Center);
                                    Anchor: (Height: 18);
                                }
                                Label #LblEnemHp {
                                    Text: "HP: ---";
                                    Style: (TextColor: #ff4444, FontSize: 12, HorizontalAlignment: Center);
                                    Anchor: (Height: 16);
                                }
                            }
                        }
                        Label #LblBarraEnem {
                            Text: "";
                            Style: (TextColor: #ff4444, FontSize: 12, HorizontalAlignment: Center);
                            Anchor: (Height: 16);
                        }
                        Label #LblEnemGauge {
                            Text: "GAUGE: ";
                            Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                            Anchor: (Height: 14);
                        }
                        Label #LblAlerta {
                            Text: "";
                            Style: (TextColor: #ffcc00, FontSize: 12, HorizontalAlignment: Center);
                            Anchor: (Height: 14);
                        }
                    }
                    Group { Anchor: (Height: 4); }
                    Group {
                        LayoutMode: Left;
                        Group {
                            LayoutMode: Top;
                            Anchor: (Width: 165);
                            Background: (Color: #141d33);
                            Padding: (Full: 8);
                            Label {
                                Text: "--- A ---";
                                Style: (TextColor: #00d4ff, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Group {
                                LayoutMode: Left;
                                Anchor: (Height: 32);
                                AssetImage #ImgA {
                                    Anchor: (Width: 30, Height: 30);
                                    AssetPath: "UI/Custom/png_Digis/botamon.png";
                                }
                                Group { Anchor: (Width: 6); }
                                Label #LblNombreA {
                                    Text: "---";
                                    Style: (TextColor: #00d4ff, FontSize: 12, HorizontalAlignment: Center);
                                    Anchor: (Width: 110, Height: 32);
                                }
                            }
                            Label #LblHpA {
                                Text: "HP: ---";
                                Style: (TextColor: #00ff88, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 14);
                            }
                            Label #LblBarraA {
                                Text: "";
                                Style: (TextColor: #00ff88, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 14);
                            }
                            Label #LblOpA {
                                Text: "OP: ";
                                Style: (TextColor: #ffcc00, FontSize: 10, HorizontalAlignment: Center);
                                Anchor: (Height: 12);
                            }
                            Label #LblGaugeA {
                                Text: "GAUGE: ";
                                Style: (TextColor: #8899aa, FontSize: 10, HorizontalAlignment: Center);
                                Anchor: (Height: 12);
                            }
                        }
                        Group { Anchor: (Width: 8); }
                        Group {
                            LayoutMode: Top;
                            Anchor: (Width: 165);
                            Background: (Color: #1a1533);
                            Padding: (Full: 8);
                            Label {
                                Text: "--- B ---";
                                Style: (TextColor: #ff00ff, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Group {
                                LayoutMode: Left;
                                Anchor: (Height: 32);
                                AssetImage #ImgB {
                                    Anchor: (Width: 30, Height: 30);
                                    AssetPath: "UI/Custom/png_Digis/botamon.png";
                                }
                                Group { Anchor: (Width: 6); }
                                Label #LblNombreB {
                                    Text: "---";
                                    Style: (TextColor: #ff00ff, FontSize: 12, HorizontalAlignment: Center);
                                    Anchor: (Width: 110, Height: 32);
                                }
                            }
                            Label #LblHpB {
                                Text: "HP: ---";
                                Style: (TextColor: #00ff88, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 14);
                            }
                            Label #LblBarraB {
                                Text: "";
                                Style: (TextColor: #00ff88, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 14);
                            }
                            Label #LblOpB {
                                Text: "OP: ";
                                Style: (TextColor: #ffcc00, FontSize: 10, HorizontalAlignment: Center);
                                Anchor: (Height: 12);
                            }
                            Label #LblGaugeB {
                                Text: "GAUGE: ";
                                Style: (TextColor: #8899aa, FontSize: 10, HorizontalAlignment: Center);
                                Anchor: (Height: 12);
                            }
                        }
                        Group { Anchor: (Width: 8); }
                        Group {
                            LayoutMode: Top;
                            Anchor: (Width: 165);
                            Background: (Color: #1a1a0f);
                            Padding: (Full: 8);
                            Label {
                                Text: "--- C ---";
                                Style: (TextColor: #ffcc00, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Group {
                                LayoutMode: Left;
                                Anchor: (Height: 32);
                                AssetImage #ImgC {
                                    Anchor: (Width: 30, Height: 30);
                                    AssetPath: "UI/Custom/png_Digis/botamon.png";
                                }
                                Group { Anchor: (Width: 6); }
                                Label #LblNombreC {
                                    Text: "---";
                                    Style: (TextColor: #ffcc00, FontSize: 12, HorizontalAlignment: Center);
                                    Anchor: (Width: 110, Height: 32);
                                }
                            }
                            Label #LblHpC {
                                Text: "HP: ---";
                                Style: (TextColor: #00ff88, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 14);
                            }
                            Label #LblBarraC {
                                Text: "";
                                Style: (TextColor: #00ff88, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 14);
                            }
                            Label #LblOpC {
                                Text: "OP: ";
                                Style: (TextColor: #ffcc00, FontSize: 10, HorizontalAlignment: Center);
                                Anchor: (Height: 12);
                            }
                            Label #LblGaugeC {
                                Text: "GAUGE: ";
                                Style: (TextColor: #8899aa, FontSize: 10, HorizontalAlignment: Center);
                                Anchor: (Height: 12);
                            }
                        }
                    }
                    Group { Anchor: (Height: 4); }
                    Label #LblLog {
                        Text: "";
                        Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center, Wrap: true);
                        Anchor: (Height: 40);
                    }
                    Group { Anchor: (Height: 4); }
                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 38);

                        TextButton #BtnSupport {
                            Text: "Support";
                            Anchor: (Width: 150, Height: 36);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }

                        Group { Anchor: (Width: 8); }

                        TextButton #BtnOrderRing {
                            Text: "Order Ring";
                            Anchor: (Width: 150, Height: 36);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }

                        Group { Anchor: (Width: 8); }

                        TextButton #BtnHuir {
                            Text: "Huir";
                            Anchor: (Width: 150, Height: 36);
                            Style: (Default: (Background: (Color: #3d1a1a), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #5d2a2a), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                    }
                    Group { Anchor: (Height: 4); }
                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 38);

                        TextButton #BtnTacticaA {
                            Text: "Tactica A";
                            Anchor: (Width: 150, Height: 36);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }

                        Group { Anchor: (Width: 8); }

                        TextButton #BtnTacticaB {
                            Text: "Tactica B";
                            Anchor: (Width: 150, Height: 36);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }

                        Group { Anchor: (Width: 8); }

                        TextButton #BtnTacticaC {
                            Text: "Tactica C";
                            Anchor: (Width: 150, Height: 36);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 13, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 13, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                    }
                }
            }
            """;
        cmd.appendInline(null, ui);

        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnSupport",
            EventData.of("action", "support"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnOrderRing",
            EventData.of("action", "order_ring"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnTacticaA",
            EventData.of("action", "tactica_a"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnTacticaB",
            EventData.of("action", "tactica_b"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnTacticaC",
            EventData.of("action", "tactica_c"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnHuir",
            EventData.of("action", "huir"), false);

        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d != null && d.combate != null) {
            aplicarEstado(cmd, d.combate, partyDe(d.combate, d));
        }
        // Reafirmar formación + HUD en cada construcción.
        SistemaPaseo.prepararBatalla(playerRef, ref, store);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull String jsonData) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String action = json.has("action") ? json.get("action").getAsString() : "";
        LOGGER.log(Level.INFO, "DigiBatallaMenuUI accion: " + action);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null) {
            UICommandBuilder empty = new UICommandBuilder();
            sendUpdate(empty);
            return;
        }

        EstadoCombate c = datos.combate;
        java.util.List<DatoDigimon> party = partyDe(c, datos);

        if (c == null) {
            datos.combate = null;
            guardarYCerrar(ref, store, player, datos);
            return;
        }
        if (!c.activa && c.fase != EstadoCombate.Fase.FIN_COMBATE) {
            UICommandBuilder cmd = new UICommandBuilder();
            aplicarEstado(cmd, c, party);
            sendUpdate(cmd);
                return;
        }

        switch (action) {
            case "support" -> SistemaBatalla.support(c);
            case "order_ring" -> { c.fase = EstadoCombate.Fase.ORDER_RING; c.logear("Order Ring abierto"); }
            case "huir" -> SistemaBatalla.huir(c);
            case "tactica_a" -> { c.tacticaA = ciclarTactica(c.tacticaA); c.logear("Tactica A -> " + c.tacticaA); }
            case "tactica_b" -> { c.tacticaB = ciclarTactica(c.tacticaB); c.logear("Tactica B -> " + c.tacticaB); }
            case "tactica_c" -> { c.tacticaC = ciclarTactica(c.tacticaC); c.logear("Tactica C -> " + c.tacticaC); }
            default -> c.logear("Accion desconocida: " + action);
        }

        // Reafirmar formación (el salvaje deambula) y refrescar todo.
        SistemaPaseo.prepararBatalla(playerRef, ref, store);
        UICommandBuilder cmd = new UICommandBuilder();
        aplicarEstado(cmd, c, party);
        sendUpdate(cmd);

        if (c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            try {
                PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
                if (progress != null) progress.guardarDesde(datos);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "No se pudo guardar fin de combate: " + e.getMessage());
            }
        }
    }

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref,
                          @Nonnull Store<EntityStore> store) {
        // Cerrar la Page recoge la formación y oculta el HUD.
        SistemaPaseo.recogerBatalla(playerRef, ref, store);
    }

    /** Lados del combate: los fijados al iniciar, o el equipo actual si faltan. */
    private static java.util.List<DatoDigimon> partyDe(EstadoCombate c, AlmacenJugadores.DatosJugador d) {
        if (c != null && c.party != null && !c.party.isEmpty()) return c.party;
        return d.equipoCombate();
    }

    private void aplicarEstado(UICommandBuilder cmd, EstadoCombate c, java.util.List<DatoDigimon> party) {
        if (c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            cmd.set("#LblTurno.Text", c.victoria ? "VICTORIA!" : "DERROTA...");
            cmd.set("#LblResultado.Text", c.victoria
                ? "+" + c.expTotal + " XP ganados | Combo max: " + c.comboMax
                : "Tus Digimon necesitan descanso");
        } else {
            String faseLabel = switch (c.fase) {
                case INICIATIVA -> "INICIATIVA";
                case ORDER_RING -> "ORDER RING";
                case RESOLVIENDO -> "RESOLVIENDO";
                default -> "";
            };
            cmd.set("#LblTurno.Text", "Ronda " + c.ronda + "  [" + faseLabel + "]");
            cmd.set("#LblResultado.Text", "");
        }
        cmd.set("#LblCombo.Text", c.comboCount > 1 ? "Combo x" + c.comboCount : "");

        cmd.set("#LblEnemigo.Text", c.enemigoNombre + " [" + c.enemigoElemento + "]"
            + (c.enemigoCargando ? "  !!CARGANDO!!" : "")
            + (c.totalOleada > 1 ? "  [" + c.oleadaActual + "/" + c.totalOleada + "]" : ""));
        String rutaE = DigiImagenes.rutaPara(c.enemigoNombre);
        cmd.set("#ImgEnem.Visible", rutaE != null);
        if (rutaE != null) cmd.set("#ImgEnem.AssetPath", rutaE);
        cmd.set("#LblEnemHp.Text", "HP: " + c.enemigoHp + "/" + c.enemigoMaxHp);
        cmd.set("#LblBarraEnem.Text", EstadoCombate.barraHp(c.enemigoHp, c.enemigoMaxHp));
        cmd.set("#LblEnemGauge.Text", "GAUGE: " + EstadoCombate.barraGauge(c.gaugeEnemigo));

        if (c.refuerzoActivo) {
            cmd.set("#LblAlerta.Text", "REFUERZO: " + c.refuerzoNombre + " [" + c.refuerzoElemento + "]");
        } else {
            cmd.set("#LblAlerta.Text", "");
        }

        DatoDigimon a = party.size() > 0 ? party.get(0) : null;
        DatoDigimon b = party.size() > 1 ? party.get(1) : null;
        DatoDigimon cc = party.size() > 2 ? party.get(2) : null;

        String nomA = a != null ? a.nombre + " [" + a.elemento + "]" : "---";
        cmd.set("#LblNombreA.Text", nomA);
        String rutaA = a != null ? DigiImagenes.rutaPara(a.especie) : null;
        cmd.set("#ImgA.Visible", rutaA != null);
        if (rutaA != null) cmd.set("#ImgA.AssetPath", rutaA);
        cmd.set("#LblHpA.Text", "HP: " + c.hpCombateA + (a != null ? "/" + a.maxHp : ""));
        cmd.set("#LblBarraA.Text", EstadoCombate.barraHp(c.hpCombateA, a != null ? a.maxHp : 1));
        cmd.set("#LblOpA.Text", "OP: " + c.opA + "  " + EstadoCombate.barraOp(c.opA));
        cmd.set("#LblGaugeA.Text", "GAUGE: " + EstadoCombate.barraGauge(c.gaugeA));

        String nomB = b != null ? b.nombre + " [" + b.elemento + "]" : "---";
        cmd.set("#LblNombreB.Text", nomB);
        String rutaB = b != null ? DigiImagenes.rutaPara(b.especie) : null;
        cmd.set("#ImgB.Visible", rutaB != null);
        if (rutaB != null) cmd.set("#ImgB.AssetPath", rutaB);
        cmd.set("#LblHpB.Text", "HP: " + c.hpCombateB + (b != null ? "/" + b.maxHp : ""));
        cmd.set("#LblBarraB.Text", EstadoCombate.barraHp(c.hpCombateB, b != null ? b.maxHp : 1));
        cmd.set("#LblOpB.Text", "OP: " + c.opB + "  " + EstadoCombate.barraOp(c.opB));
        cmd.set("#LblGaugeB.Text", "GAUGE: " + EstadoCombate.barraGauge(c.gaugeB));

        String nomC = cc != null ? cc.nombre + " [" + cc.elemento + "]" : "---";
        cmd.set("#LblNombreC.Text", nomC);
        String rutaC = cc != null ? DigiImagenes.rutaPara(cc.especie) : null;
        cmd.set("#ImgC.Visible", rutaC != null);
        if (rutaC != null) cmd.set("#ImgC.AssetPath", rutaC);
        cmd.set("#LblHpC.Text", "HP: " + c.hpCombateC + (cc != null ? "/" + cc.maxHp : ""));
        cmd.set("#LblBarraC.Text", EstadoCombate.barraHp(c.hpCombateC, cc != null ? cc.maxHp : 1));
        cmd.set("#LblOpC.Text", "OP: " + c.opC + "  " + EstadoCombate.barraOp(c.opC));
        cmd.set("#LblGaugeC.Text", "GAUGE: " + EstadoCombate.barraGauge(c.gaugeC));

        cmd.set("#LblLog.Text", c.getLogFormateado());
    }

    private EstadoCombate.Tactica ciclarTactica(EstadoCombate.Tactica t) {
        return switch (t) {
            case AGRESIVO -> EstadoCombate.Tactica.EQUILIBRADO;
            case EQUILIBRADO -> EstadoCombate.Tactica.DEFENSIVO;
            case DEFENSIVO -> EstadoCombate.Tactica.AGRESIVO;
        };
    }

    private void guardarYCerrar(Ref<EntityStore> ref, Store<EntityStore> store, Player player, AlmacenJugadores.DatosJugador datos) {
        PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
        progress.guardarDesde(datos);
        player.getPageManager().setPage(ref, store,
            com.hypixel.hytale.protocol.packets.interface_.Page.None);
    }
}
