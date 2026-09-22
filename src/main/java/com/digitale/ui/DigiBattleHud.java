package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.entity.entities.player.hud.HudManager;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HUD de combate (solo display, sin botones): salvaje arriba-derecha,
 * party abajo-izquierda, turno/log abajo-derecha. Los comandos van en la Page.
 */
public class DigiBattleHud extends CustomUIHud {

    public static final String KEY = "digitale_battle";

    private static final Logger LOGGER = Logger.getLogger(DigiBattleHud.class.getName());

    public DigiBattleHud(@Nonnull PlayerRef playerRef) {
        super(playerRef, KEY);
    }

    @Override
    protected void build(UICommandBuilder cmd) {
        cmd.appendInline(null, """
            Group {
                Group {
                    Anchor: (Top: 10, Left: 10, Width: 230);
                    Background: (Color: #0b1220);
                    Padding: (Full: 8);

                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 34);
                        AssetImage #HudAImg {
                            Anchor: (Width: 30, Height: 30);
                            AssetPath: "UI/Custom/png_Digis/botamon.png";
                        }
                        Group { Anchor: (Width: 6); }
                        Group {
                            LayoutMode: Top;
                            Anchor: (Width: 150);
                            Label #HudAName {
                                Text: "---";
                                Style: (TextColor: #00d4ff, FontSize: 13, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Label #HudAHp {
                                Text: "";
                                Style: (TextColor: #00ff88, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 14);
                            }
                        }
                    }
                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 34);
                        AssetImage #HudBImg {
                            Anchor: (Width: 30, Height: 30);
                            AssetPath: "UI/Custom/png_Digis/botamon.png";
                        }
                        Group { Anchor: (Width: 6); }
                        Group {
                            LayoutMode: Top;
                            Anchor: (Width: 150);
                            Label #HudBName {
                                Text: "---";
                                Style: (TextColor: #ff00ff, FontSize: 13, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Label #HudBHp {
                                Text: "";
                                Style: (TextColor: #00ff88, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 14);
                            }
                        }
                    }
                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 34);
                        AssetImage #HudCImg {
                            Anchor: (Width: 30, Height: 30);
                            AssetPath: "UI/Custom/png_Digis/botamon.png";
                        }
                        Group { Anchor: (Width: 6); }
                        Group {
                            LayoutMode: Top;
                            Anchor: (Width: 150);
                            Label #HudCName {
                                Text: "---";
                                Style: (TextColor: #ffcc00, FontSize: 13, HorizontalAlignment: Center);
                                Anchor: (Height: 16);
                            }
                            Label #HudCHp {
                                Text: "";
                                Style: (TextColor: #00ff88, FontSize: 11, HorizontalAlignment: Center);
                                Anchor: (Height: 14);
                            }
                        }
                    }
                }
                Group {
                    Anchor: (Top: 10, Right: 10, Width: 250);
                    Background: (Color: #1a0f1a);
                    Padding: (Full: 8);

                    Label #HudEnemName {
                        Text: "---";
                        Style: (TextColor: #ff4444, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Label #HudEnemHp {
                        Text: "HP: ---";
                        Style: (TextColor: #ff4444, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #HudEnemBar {
                        Text: "";
                        Style: (TextColor: #ff4444, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                }
                Group {
                    Anchor: (Left: 10, Bottom: 10, Width: 250);
                    Background: (Color: #0b1220);
                    Padding: (Full: 8);

                    Label #MenuTitulo {
                        Text: "COMBATE";
                        Style: (TextColor: #00d4ff, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 20);
                    }
                    Label #Menu0 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 16); }
                    Label #Menu1 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 16); }
                    Label #Menu2 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 16); }
                    Label #Menu3 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 16); }
                    Label #Menu4 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 16); }
                    Label #Menu5 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 16); }
                    Label #Menu6 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 16); }
                    Label #Menu7 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 16); }
                    Label {
                        Text: "W/S mover | ESPACIO aceptar | Q atras";
                        Style: (TextColor: #8899aa, FontSize: 10, HorizontalAlignment: Center);
                        Anchor: (Height: 14);
                    }
                }
                Group {
                    Anchor: (Right: 10, Bottom: 10, Width: 300);
                    Background: (Color: #0b1220);
                    Padding: (Full: 8);

                    Label #HudTurno {
                        Text: "";
                        Style: (TextColor: #ffcc00, FontSize: 13, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                    Label #HudLog {
                        Text: "";
                        Style: (TextColor: #8899aa, FontSize: 11, HorizontalAlignment: Center, Wrap: true);
                        Anchor: (Height: 60);
                    }
                }
            }
            """);
        rellenar(cmd);
    }

    /** Relee el combate y empuja los cambios al cliente. */
    public void refrescar() {
        try {
            UICommandBuilder cmd = new UICommandBuilder();
            rellenar(cmd);
            update(false, cmd);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "HUD batalla refrescar: " + e.getMessage());
        }
    }

    private void rellenar(UICommandBuilder cmd) {
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(getPlayerRef().getUuid());
        EstadoCombate c = d != null ? d.combate : null;
        if (c == null) {
            cmd.set("#HudEnemName.Text", "---");
            cmd.set("#HudEnemHp.Text", "HP: ---");
            cmd.set("#HudEnemBar.Text", "");
            cmd.set("#HudTurno.Text", "");
            cmd.set("#HudLog.Text", "");
            return;
        }
        List<DatoDigimon> party = (c.party != null && !c.party.isEmpty()) ? c.party
            : (d != null ? d.equipoCombate() : java.util.Collections.emptyList());

        cmd.set("#HudEnemName.Text", c.enemigoNombre
            + (c.totalOleada > 1 ? " [" + c.oleadaActual + "/" + c.totalOleada + "]" : ""));
        cmd.set("#HudEnemHp.Text", "HP: " + c.enemigoHp + "/" + c.enemigoMaxHp);
        cmd.set("#HudEnemBar.Text", EstadoCombate.barraHp(c.enemigoHp, c.enemigoMaxHp));

        int[] hps = {c.hpCombateA, c.hpCombateB, c.hpCombateC};
        String[] noms = {"#HudA", "#HudB", "#HudC"};
        for (int i = 0; i < 3; i++) {
            DatoDigimon digi = i < party.size() ? party.get(i) : null;
            if (digi != null) {
                cmd.set(noms[i] + "Name.Text", digi.nombre);
                cmd.set(noms[i] + "Hp.Text", "HP: " + hps[i] + "/" + digi.maxHp);
                String ruta = DigiImagenes.rutaPara(digi.especie);
                cmd.set(noms[i] + "Img.Visible", ruta != null);
                if (ruta != null) cmd.set(noms[i] + "Img.AssetPath", ruta);
            } else {
                cmd.set(noms[i] + "Name.Text", "---");
                cmd.set(noms[i] + "Hp.Text", "");
                cmd.set(noms[i] + "Img.Visible", false);
            }
        }

        if (c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            cmd.set("#HudTurno.Text", c.victoria ? "VICTORIA!" : "DERROTA...");
        } else {
            cmd.set("#HudTurno.Text", "Ronda " + c.ronda);
        }
        cmd.set("#HudLog.Text", c.getLogFormateado());
        dibujarMenu(cmd, d);
    }

    /** Caja de menú por teclado (8 slots fijos, cursor "> "). */
    private void dibujarMenu(UICommandBuilder cmd, AlmacenJugadores.DatosJugador d) {
        if (d == null) {
            cmd.set("#MenuTitulo.Text", "COMBATE");
            for (int i = 0; i < 8; i++) {
                cmd.set("#Menu" + i + ".Text", "");
                cmd.set("#Menu" + i + ".Visible", false);
            }
            return;
        }
        java.util.List<String> items = MenuBatalla.listaItems(d);
        cmd.set("#MenuTitulo.Text", MenuBatalla.titulo(d));
        for (int i = 0; i < 8; i++) {
            if (i < items.size()) {
                cmd.set("#Menu" + i + ".Visible", true);
                cmd.set("#Menu" + i + ".Text", (i == d.menuIdx ? "> " : "   ") + items.get(i));
            } else {
                cmd.set("#Menu" + i + ".Text", "");
                cmd.set("#Menu" + i + ".Visible", false);
            }
        }
    }

    public static void mostrar(@Nonnull PlayerRef playerRef,
                               @Nonnull Ref<EntityStore> ref,
                               @Nonnull Store<EntityStore> store) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;
            HudManager hm = player.getHudManager();
            hm.removeCustomHud(playerRef, KEY);
            DigiBattleHud hud = new DigiBattleHud(playerRef);
            hm.addCustomHud(playerRef, hud);
            hud.show();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "HUD batalla mostrar: " + e.getMessage());
        }
    }

    public static void actualizar(@Nonnull PlayerRef playerRef,
                                  @Nonnull Ref<EntityStore> ref,
                                  @Nonnull Store<EntityStore> store) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;
            HudManager hm = player.getHudManager();
            CustomUIHud actual = null;
            try {
                actual = hm.getCustomHud(KEY);
            } catch (Exception ignored) {
            }
            if (actual instanceof DigiBattleHud hud) {
                hud.refrescar();
            } else {
                mostrar(playerRef, ref, store);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "HUD batalla actualizar: " + e.getMessage());
        }
    }

    public static void ocultar(@Nonnull PlayerRef playerRef,
                               @Nonnull Ref<EntityStore> ref,
                               @Nonnull Store<EntityStore> store) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;
            player.getHudManager().removeCustomHud(playerRef, KEY);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "HUD batalla ocultar: " + e.getMessage());
        }
    }
}
