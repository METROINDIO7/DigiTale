package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
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
                LayoutMode: Top;
                Anchor: (Height: 550, Width: 600);

                Group {
                    LayoutMode: Top;
                    Background: (Color: #0d1117);
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
                    Group { Anchor: (Height: 4); }

                    Label {
                        Text: "=== ENEMIGO ===";
                        Style: (TextColor: #ff4444, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #LblEnemigo {
                        Text: "---";
                        Style: (TextColor: #e0e8f0, FontSize: 13, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #LblEnemHp {
                        Text: "HP: ---";
                        Style: (TextColor: #ff4444, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
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
                        Style: (TextColor: #ffcc00, FontSize: 13, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }

                    Label {
                        Text: "________________________";
                        Style: (TextColor: #00d4ff, HorizontalAlignment: Center);
                        Anchor: (Height: 14);
                    }
                    Group { Anchor: (Height: 4); }

                    Label {
                        Text: "--- COMPANERO A ---";
                        Style: (TextColor: #00d4ff, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #LblNombreA {
                        Text: "---";
                        Style: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #LblHpA {
                        Text: "HP: ---";
                        Style: (TextColor: #00ff88, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #LblBarraA {
                        Text: "";
                        Style: (TextColor: #00ff88, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #LblOpA {
                        Text: "OP: ";
                        Style: (TextColor: #ffcc00, FontSize: 11, HorizontalAlignment: Center);
                        Anchor: (Height: 14);
                    }
                    Label #LblGaugeA {
                        Text: "GAUGE: ";
                        Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                        Anchor: (Height: 14);
                    }

                    Group { Anchor: (Height: 4); }

                    Label {
                        Text: "--- COMPANERO B ---";
                        Style: (TextColor: #ff00ff, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #LblNombreB {
                        Text: "---";
                        Style: (TextColor: #ff00ff, FontSize: 13, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #LblHpB {
                        Text: "HP: ---";
                        Style: (TextColor: #00ff88, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #LblBarraB {
                        Text: "";
                        Style: (TextColor: #00ff88, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #LblOpB {
                        Text: "OP: ";
                        Style: (TextColor: #ffcc00, FontSize: 11, HorizontalAlignment: Center);
                        Anchor: (Height: 14);
                    }
                    Label #LblGaugeB {
                        Text: "GAUGE: ";
                        Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center);
                        Anchor: (Height: 14);
                    }

                    Label {
                        Text: "________________________";
                        Style: (TextColor: #00d4ff, HorizontalAlignment: Center);
                        Anchor: (Height: 14);
                    }
                    Group { Anchor: (Height: 4); }

                    Label #LblCombo {
                        Text: "";
                        Style: (TextColor: #ffcc00, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Label #LblLog {
                        Text: "";
                        Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center, Wrap: true);
                        Anchor: (Height: 40);
                    }
                    Group { Anchor: (Height: 4); }

                    Group {
                        LayoutMode: Left;

                        TextButton #BtnSupport {
                            Text: "Support";
                            Anchor: (Width: 90, Height: 36);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }

                        Group { Anchor: (Width: 6); }

                        TextButton #BtnOrderRing {
                            Text: "Order Ring";
                            Anchor: (Width: 90, Height: 36);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }

                        Group { Anchor: (Width: 6); }

                        TextButton #BtnHuir {
                            Text: "Huir";
                            Anchor: (Width: 90, Height: 36);
                            Style: (Default: (Background: (Color: #3d1a1a), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #5d2a2a), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }
                    }
                    Group { Anchor: (Height: 4); }
                    Group {
                        LayoutMode: Left;

                        TextButton #BtnTacticaA {
                            Text: "Tactica A";
                            Anchor: (Width: 120, Height: 36);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                        }

                        Group { FlexWeight: 1; }

                        TextButton #BtnTacticaB {
                            Text: "Tactica B";
                            Anchor: (Width: 120, Height: 36);
                            Style: (Default: (Background: (Color: #16213e), LabelStyle: (FontSize: 12, TextColor: #e0e8f0, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
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
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnHuir",
            EventData.of("action", "huir"), false);

        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (d != null && d.combate != null) {
            aplicarEstado(cmd, d.combate, d.companeroA, d.companeroB);
        }
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
        if (datos == null) return;

        EstadoCombate c = datos.combate;
        DatoDigimon a = datos.companeroA;
        DatoDigimon b = datos.companeroB;

        if (c == null || !c.activa) {
            if (c == null) {
                datos.combate = null;
                guardarYCerrar(ref, store, player, datos);
            }
            return;
        }

        switch (action) {
            case "support" -> SistemaBatalla.support(c, a, b);
            case "order_ring" -> { c.fase = EstadoCombate.Fase.ORDER_RING; c.logear("Order Ring abierto"); }
            case "huir" -> SistemaBatalla.huir(c, a, b);
            case "tactica_a" -> { c.tacticaA = ciclarTactica(c.tacticaA); c.logear("Tactica A -> " + c.tacticaA); }
            case "tactica_b" -> { c.tacticaB = ciclarTactica(c.tacticaB); c.logear("Tactica B -> " + c.tacticaB); }
        }

        player.getPageManager().setPage(ref, store,
            com.hypixel.hytale.protocol.packets.interface_.Page.None);
        open(playerRef, ref, store);
    }

    private void aplicarEstado(UICommandBuilder cmd, EstadoCombate c, DatoDigimon a, DatoDigimon b) {
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

        cmd.set("#LblEnemigo.Text", c.enemigoNombre + " [" + c.enemigoElemento + "]"
            + (c.enemigoCargando ? "  !!CARGANDO!!" : ""));
        cmd.set("#LblEnemHp.Text", "HP: " + c.enemigoHp + "/" + c.enemigoMaxHp);
        cmd.set("#LblBarraEnem.Text", EstadoCombate.barraHp(c.enemigoHp, c.enemigoMaxHp));
        cmd.set("#LblEnemGauge.Text", "GAUGE: " + EstadoCombate.barraGauge(c.gaugeEnemigo));

        if (c.refuerzoActivo) {
            cmd.set("#LblAlerta.Text", "REFUERZO: " + c.refuerzoNombre + " [" + c.refuerzoElemento + "]");
        } else {
            cmd.set("#LblAlerta.Text", "");
        }

        String nomA = a != null ? a.nombre + " [" + a.elemento + "]" : "---";
        cmd.set("#LblNombreA.Text", nomA);
        cmd.set("#LblHpA.Text", "HP: " + c.hpCombateA + (a != null ? "/" + a.maxHp : ""));
        cmd.set("#LblBarraA.Text", EstadoCombate.barraHp(c.hpCombateA, a != null ? a.maxHp : 1));
        cmd.set("#LblOpA.Text", "OP: " + c.opA + "  " + EstadoCombate.barraOp(c.opA));
        cmd.set("#LblGaugeA.Text", "GAUGE: " + EstadoCombate.barraGauge(c.gaugeA));

        String nomB = b != null ? b.nombre + " [" + b.elemento + "]" : "---";
        cmd.set("#LblNombreB.Text", nomB);
        cmd.set("#LblHpB.Text", "HP: " + c.hpCombateB + (b != null ? "/" + b.maxHp : ""));
        cmd.set("#LblBarraB.Text", EstadoCombate.barraHp(c.hpCombateB, b != null ? b.maxHp : 1));
        cmd.set("#LblOpB.Text", "OP: " + c.opB + "  " + EstadoCombate.barraOp(c.opB));
        cmd.set("#LblGaugeB.Text", "GAUGE: " + EstadoCombate.barraGauge(c.gaugeB));

        cmd.set("#LblLog.Text", c.getLogFormateado());
        cmd.set("#LblCombo.Text", c.comboCount > 1 ? "Combo x" + c.comboCount : "");
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
