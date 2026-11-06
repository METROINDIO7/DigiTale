package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.digitale.datos.EstadoCombate.Actor;
import com.digitale.datos.EstadoCombate.Enemigo;
import com.digitale.datos.TablaTipos;
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
 * HUD de combate estilo Cyber: paneles oscuros, acentos cian/dorado, barras de
 * HP y lista del grupo enemigo (1-5 salvajes). Solo display; los comandos se
 * manejan por teclado/Page desde {@link MenuBatalla}.
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
                Group #PartyPanel {
                    LayoutMode: Top;
                    Anchor: (Top: 12, Left: 12, Width: 350, Height: 244);
                    Background: (Color: #04070d);
                    Padding: (Full: 10);

                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 24);
                        Label {
                            Text: "EQUIPO";
                            Style: (TextColor: #38d9ff, FontSize: 16, RenderBold: true, LetterSpacing: 1);
                            Anchor: (Width: 190);
                        }
                        Label #HudParteEstado {
                            Text: "CP 0/100";
                            Style: (TextColor: #ffc857, FontSize: 11, HorizontalAlignment: End);
                            Anchor: (Width: 130);
                        }
                    }
                    Group { Anchor: (Height: 2); Background: (Color: #38d9ff); }
                    Group { Anchor: (Height: 6); }

                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 62);
                        AssetImage #HudAImg {
                            Anchor: (Width: 46, Height: 46);
                            AssetPath: "UI/Custom/png_Digis/Botamon.png";
                        }
                        Group { Anchor: (Width: 8); }
                        Group {
                            LayoutMode: Top;
                            Anchor: (Width: 246, Height: 58);
                            Label #HudAName {
                                Text: "---";
                                Style: (TextColor: #38d9ff, FontSize: 14, RenderBold: true);
                                Anchor: (Height: 20);
                            }
                            Label #HudABar {
                                Text: "";
                                Style: (TextColor: #5dffb0, FontSize: 12, LetterSpacing: 1);
                                Anchor: (Height: 14);
                            }
                            Label #HudAHp {
                                Text: "HP ---";
                                Style: (TextColor: #5dffb0, FontSize: 11);
                                Anchor: (Height: 16);
                            }
                        }
                    }
                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 62);
                        AssetImage #HudBImg {
                            Anchor: (Width: 46, Height: 46);
                            AssetPath: "UI/Custom/png_Digis/Botamon.png";
                        }
                        Group { Anchor: (Width: 8); }
                        Group {
                            LayoutMode: Top;
                            Anchor: (Width: 246, Height: 58);
                            Label #HudBName {
                                Text: "---";
                                Style: (TextColor: #ff4fa3, FontSize: 14, RenderBold: true);
                                Anchor: (Height: 20);
                            }
                            Label #HudBBar {
                                Text: "";
                                Style: (TextColor: #5dffb0, FontSize: 12, LetterSpacing: 1);
                                Anchor: (Height: 14);
                            }
                            Label #HudBHp {
                                Text: "HP ---";
                                Style: (TextColor: #5dffb0, FontSize: 11);
                                Anchor: (Height: 16);
                            }
                        }
                    }
                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 62);
                        AssetImage #HudCImg {
                            Anchor: (Width: 46, Height: 46);
                            AssetPath: "UI/Custom/png_Digis/Botamon.png";
                        }
                        Group { Anchor: (Width: 8); }
                        Group {
                            LayoutMode: Top;
                            Anchor: (Width: 246, Height: 58);
                            Label #HudCName {
                                Text: "---";
                                Style: (TextColor: #ffc857, FontSize: 14, RenderBold: true);
                                Anchor: (Height: 20);
                            }
                            Label #HudCBar {
                                Text: "";
                                Style: (TextColor: #5dffb0, FontSize: 12, LetterSpacing: 1);
                                Anchor: (Height: 14);
                            }
                            Label #HudCHp {
                                Text: "HP ---";
                                Style: (TextColor: #5dffb0, FontSize: 11);
                                Anchor: (Height: 16);
                            }
                        }
                    }
                }

                Group #EnemyPanel {
                    LayoutMode: Top;
                    Anchor: (Top: 12, Right: 12, Width: 430, Height: 232);
                    Background: (Color: #1a0f1a);
                    Padding: (Full: 10);

                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 24);
                        Label #HudEnemName {
                            Text: "SALVAJES";
                            Style: (TextColor: #ff5f6d, FontSize: 17, RenderBold: true, LetterSpacing: 1);
                            Anchor: (Width: 260);
                        }
                        Label #HudEnemCount {
                            Text: "";
                            Style: (TextColor: #ffc857, FontSize: 13, HorizontalAlignment: End);
                            Anchor: (Width: 140);
                        }
                    }
                    Label #HudEnemElem {
                        Text: "OBJETIVO // ---";
                        Style: (TextColor: #ff9fb0, FontSize: 12, RenderUppercase: true);
                        Anchor: (Height: 18);
                    }
                    Group { Anchor: (Height: 4); }

                    Label #En0 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 22); }
                    Label #En1 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 22); }
                    Label #En2 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 22); }
                    Label #En3 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 22); }
                    Label #En4 { Text: ""; Style: (TextColor: #e0e8f0, FontSize: 12); Anchor: (Height: 22); }

                    Group { Anchor: (Height: 4); }
                    Label #HudEnemWave {
                        Text: "";
                        Style: (TextColor: #ffc857, FontSize: 11, Wrap: true);
                        Anchor: (Height: 32);
                    }
                }

                Group #CmdPanel {
                    LayoutMode: Top;
                    Anchor: (Left: 12, Bottom: 12, Width: 370, Height: 330);
                    Background: (Color: #04070d);
                    Padding: (Full: 10);

                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 24);
                        Label #MenuTitulo {
                            Text: "COMBATE";
                            Style: (TextColor: #ffc857, FontSize: 16, RenderBold: true, LetterSpacing: 1);
                            Anchor: (Width: 240);
                        }
                        Label {
                            Text: "TURNO";
                            Style: (TextColor: #6f8ca4, FontSize: 12, HorizontalAlignment: End);
                            Anchor: (Width: 110);
                        }
                    }
                    Group { Anchor: (Height: 2); Background: (Color: #ffc857); }
                    Group { Anchor: (Height: 6); }

                    Label #Menu0 { Text: ""; Style: (TextColor: #d7ecff, FontSize: 15); Anchor: (Height: 24); }
                    Label #Menu1 { Text: ""; Style: (TextColor: #d7ecff, FontSize: 15); Anchor: (Height: 24); }
                    Label #Menu2 { Text: ""; Style: (TextColor: #d7ecff, FontSize: 15); Anchor: (Height: 24); }
                    Label #Menu3 { Text: ""; Style: (TextColor: #d7ecff, FontSize: 15); Anchor: (Height: 24); }
                    Label #Menu4 { Text: ""; Style: (TextColor: #d7ecff, FontSize: 15); Anchor: (Height: 24); }
                    Label #Menu5 { Text: ""; Style: (TextColor: #d7ecff, FontSize: 15); Anchor: (Height: 24); }
                    Label #Menu6 { Text: ""; Style: (TextColor: #d7ecff, FontSize: 15); Anchor: (Height: 24); }
                    Label #Menu7 { Text: ""; Style: (TextColor: #d7ecff, FontSize: 15); Anchor: (Height: 24); }

                    Group { Anchor: (Height: 8); }
                    Group { Anchor: (Height: 2); Background: (Color: #38d9ff); }
                    Group { Anchor: (Height: 6); }
                    Label #MenuHint {
                        Text: "HAZ CLIC EN UNA OPCION";
                        Style: (TextColor: #6f8ca4, FontSize: 11, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
                }

                Group #LogPanel {
                    LayoutMode: Top;
                    Anchor: (Right: 12, Bottom: 12, Width: 460, Height: 250);
                    Background: (Color: #04070d);
                    Padding: (Full: 10);

                    Group {
                        LayoutMode: Left;
                        Anchor: (Height: 24);
                        Label #HudTurno {
                            Text: "REGISTRO DE COMBATE";
                            Style: (TextColor: #38d9ff, FontSize: 15, RenderBold: true, LetterSpacing: 1);
                            Anchor: (Width: 270);
                        }
                        Label #HudFase {
                            Text: "";
                            Style: (TextColor: #ffc857, FontSize: 11, HorizontalAlignment: End);
                            Anchor: (Width: 160);
                        }
                    }
                    Group { Anchor: (Height: 2); Background: (Color: #38d9ff); }
                    Group { Anchor: (Height: 6); }
                    Label #HudLog {
                        Text: "";
                        Style: (TextColor: #d7ecff, FontSize: 11);
                        Anchor: (Height: 190);
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
            cmd.set("#HudEnemName.Text", "SALVAJES");
            cmd.set("#HudEnemCount.Text", "");
            cmd.set("#HudEnemElem.Text", "OBJETIVO // ---");
            cmd.set("#HudEnemWave.Text", "");
            for (int i = 0; i < 5; i++) {
                cmd.set("#En" + i + ".Text", "");
                cmd.set("#En" + i + ".Visible", false);
            }
            cmd.set("#HudParteEstado.Text", "SIN COMBATE");
            cmd.set("#HudTurno.Text", "REGISTRO DE COMBATE");
            cmd.set("#HudFase.Text", "");
            cmd.set("#HudLog.Text", "");
            for (int i = 0; i < 3; i++) {
                cmd.set(nom(i) + "Name.Text", "---");
                cmd.set(nom(i) + "Hp.Text", "HP ---");
                cmd.set(nom(i) + "Bar.Text", "");
                cmd.set(nom(i) + "Img.Visible", false);
            }
            return;
        }

        List<DatoDigimon> party = (c.party != null && !c.party.isEmpty()) ? c.party
            : (d != null ? d.equipoCombate() : java.util.Collections.emptyList());

        // ── Grupo enemigo ───────────────────────────────────────────────
        int vivos = 0;
        for (Enemigo e : c.enemigos) if (e != null && e.vivo()) vivos++;
        cmd.set("#HudEnemName.Text", "SALVAJES " + vivos + "/" + c.enemigos.size());
        cmd.set("#HudEnemCount.Text", "TURNO " + c.ronda);
        Enemigo obj = c.objetivoActual();
        cmd.set("#HudEnemElem.Text", obj != null
            ? "OBJETIVO // " + obj.nombre + " " + obj.elemento + "/"
              + TablaTipos.textoAtributo(obj.atributo)
            : "SIN OBJETIVO");

        for (int i = 0; i < 5; i++) {
            Enemigo e = i < c.enemigos.size() ? c.enemigos.get(i) : null;
            if (e == null) {
                cmd.set("#En" + i + ".Text", "");
                cmd.set("#En" + i + ".Visible", false);
                continue;
            }
            String marca = !e.vivo() ? "✖ " : (i == c.objetivoIdx ? "▶ " : "· ");
            String extra = (e.cargando ? " ⚠" : "")
                + (e.analizado ? " ★" : "")
                + EstadoCombate.estadosToString(e.estados);
            cmd.set("#En" + i + ".Visible", true);
            cmd.set("#En" + i + ".Text", marca + e.nombre
                + (e.vivo() ? "" : " [KO]")
                + "  " + EstadoCombate.barraHp(e.hp, e.maxHp)
                + "  " + e.hp + "/" + e.maxHp + " " + extra);
        }

        String tipos = obj == null ? ""
            : (obj.analizado
                ? TablaTipos.analisis(obj.elemento, obj.atributo)
                : "APOYO → ANALIZAR para ver debilidades");
        cmd.set("#HudEnemWave.Text", tipos);

        // ── Equipo ──────────────────────────────────────────────────────
        for (int i = 0; i < 3; i++) {
            DatoDigimon digi = i < party.size() ? party.get(i) : null;
            Actor actor = Actor.aliado(i);
            int hp = c.hpDe(actor);
            String estados = EstadoCombate.estadosToString(c.estadosDeAliado(i));
            String marcaEstados = estados.isEmpty() ? "" : " " + estados;
            String marcaGuardia = c.guardiaDe(i) ? " 🛡" : "";
            boolean actuando = c.esperandoJugador && c.actorActual != null
                && c.actorActual.aliado() && c.actorActual.idx() == i;
            String control = actuando ? " ▶ TÚ" : " · TÚ";
            if (digi != null && hp > 0) {
                cmd.set(nom(i) + "Name.Text", digi.nombre + control + marcaGuardia + marcaEstados);
                cmd.set(nom(i) + "Hp.Text", "HP " + hp + " / " + digi.maxHp
                    + "  ·  OP " + c.opDe(i));
                cmd.set(nom(i) + "Bar.Text", EstadoCombate.barraHp(hp, Math.max(1, digi.maxHp)));
                String ruta = DigiImagenes.rutaPara(digi.especie);
                cmd.set(nom(i) + "Img.Visible", ruta != null);
                if (ruta != null) cmd.set(nom(i) + "Img.AssetPath", ruta);
            } else {
                cmd.set(nom(i) + "Name.Text", digi != null ? digi.nombre + " [KO]" : "---");
                cmd.set(nom(i) + "Hp.Text", digi != null
                    ? "HP 0 / " + Math.max(1, digi.maxHp) : "HP ---");
                cmd.set(nom(i) + "Bar.Text", "");
                cmd.set(nom(i) + "Img.Visible", false);
            }
        }

        // ── Turno / CP ──────────────────────────────────────────────────
        cmd.set("#HudParteEstado.Text", "CP " + c.cp + "/" + EstadoCombate.CP_MAX);
        String turnoTxt = MenuBatalla.estadoTurno(c);
        cmd.set("#HudTurno.Text", "REGISTRO DE COMBATE · TURNO " + c.ronda);
        cmd.set("#HudFase.Text", LogCombateUI.recortar(turnoTxt, 24));
        cmd.set("#HudLog.Text", LogCombateUI.texto(c.getLog(), 10, 84));
        dibujarMenu(cmd, d);
    }

    private static String nom(int i) {
        return switch (i) {
            case 0 -> "#HudA";
            case 1 -> "#HudB";
            default -> "#HudC";
        };
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
        List<String> items = MenuBatalla.listaItems(d);
        String titulo = MenuBatalla.titulo(d);
        if (d.combate != null && d.menuNivel == 0 && !d.combate.esperandoJugador
            && d.combate.fase != EstadoCombate.Fase.FIN_COMBATE) {
            titulo = "ESPERA...";
        }
        cmd.set("#MenuTitulo.Text", titulo);
        for (int i = 0; i < 8; i++) {
            if (i < items.size()) {
                cmd.set("#Menu" + i + ".Visible", true);
                cmd.set("#Menu" + i + ".Text", (i == d.menuIdx ? "> " : "   ") + items.get(i));
            } else {
                cmd.set("#Menu" + i + ".Text", "");
                cmd.set("#Menu" + i + ".Visible", false);
            }
        }
        cmd.set("#MenuHint.Text", MenuBatalla.estadoTurno(d.combate));
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
            LOGGER.log(Level.INFO, "HUD combate mostrado (" + playerRef.getUsername() + ")");
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
