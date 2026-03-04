package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.digitale.sistema.SistemaBatalla;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UI interactiva del combate — overlay transparente.
 * Ocupa pantalla completa con fondo invisible; la info se
 * distribuye en los bordes para no tapar el campo de batalla.
 *
 * TOP CENTER   : estado del enemigo
 * CENTER       : alerta de refuerzo (solo cuando aparece)
 * BOTTOM LEFT  : equipo del jugador (HP / OP)
 * BOTTOM CENTER: log de combate + resultado
 * BOTTOM RIGHT : botones de accion
 */
public class DigiBatallaMenuUI extends InteractiveCustomUIPage<DigiBatallaMenuUI.Data> {

    private static final Logger LOGGER = Logger.getLogger(DigiBatallaMenuUI.class.getName());

    // ── Codec ──────────────────────────────────────────────────────
    public static class Data {
        public String accion = "";
        public static final BuilderCodec<Data> CODEC = BuilderCodec
                .builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Accion", Codec.STRING), (d, v) -> d.accion = v, d -> d.accion).add()
                .build();
    }

    private final PlayerRef playerRef;

    public DigiBatallaMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
    }

    // ── Build inicial ──────────────────────────────────────────────
    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder ui,
                      @Nonnull UIEventBuilder ev,
                      @Nonnull Store<EntityStore> store) {

        ui.append("Pages/DigiBatallaMenu.ui");

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null || !datos.enCombateUI()) {
            ui.set("#LblTurno.Text", "Sin combate activo");
            return;
        }

        EstadoCombate c = datos.combate;
        DatoDigimon a = datos.companeroA;
        DatoDigimon b = datos.companeroB;

        aplicarEstado(ui, c, a, b);

        if (c.fase == EstadoCombate.Fase.ORDER_RING) {
            aplicarTextosBotonesOrderRing(ui, c);
            bindOrderRing(ev);
        } else if (c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            aplicarTextosBotonesFin(ui);
            DigiUIHelper.bindClick(ev, "#BtnSupport", "Accion", "cerrar");
        } else {
            aplicarTextosBotonesNormal(ui, c);
            bindNormal(ev, c);
        }
    }

    // ── Poblar labels con el estado actual ─────────────────────────
    private void aplicarEstado(UICommandBuilder ui, EstadoCombate c, DatoDigimon a, DatoDigimon b) {

        // Turno / resultado
        if (c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            ui.set("#LblTurno.Text",     c.victoria ? "VICTORIA!" : "DERROTA...");
            ui.set("#LblResultado.Text", c.victoria
                    ? "+" + c.expTotal + " XP ganados"
                    : "Tus Digimon necesitan descanso");
        } else {
            ui.set("#LblTurno.Text",     "Ronda " + c.ronda
                    + (c.fase == EstadoCombate.Fase.ORDER_RING ? "  [ORDER RING]" : ""));
            ui.set("#LblResultado.Text", "");
        }

        // Enemigo principal
        String nomEnem = c.enemigoNombre + " [" + c.enemigoElemento + "]"
                + (c.enemigoCargando ? "  !!CARGANDO!!" : "");
        ui.set("#LblEnemigo.Text",   nomEnem);
        ui.set("#LblEnemHp.Text",    "HP: " + c.enemigoHp + "/" + c.enemigoMaxHp);
        ui.set("#LblBarraEnem.Text", EstadoCombate.barraHp(c.enemigoHp, c.enemigoMaxHp));

        // Alerta central — refuerzo
        if (c.refuerzoActivo) {
            String nomRef = c.refuerzoNombre + " [" + c.refuerzoElemento + "]"
                    + (c.refuerzoCargando ? "  !!CARGANDO!!" : "");
            ui.set("#LblAlerta.Text", "REFUERZO: " + nomRef
                    + "  HP:" + c.refuerzoHp + "/" + c.refuerzoMaxHp
                    + "  " + EstadoCombate.barraHp(c.refuerzoHp, c.refuerzoMaxHp));
        } else {
            ui.set("#LblAlerta.Text", "");
        }

        // Digimon A
        String nomA = a != null ? a.nombre + " [" + a.elemento + "]" : "---";
        ui.set("#LblNombreA.Text", nomA);
        ui.set("#LblBarraA.Text",  EstadoCombate.barraHp(c.hpCombateA, a != null ? a.maxHp : 1));
        ui.set("#LblHpA.Text",     "HP: " + c.hpCombateA + (a != null ? "/" + a.maxHp : ""));
        ui.set("#LblOpA.Text",     "OP: " + c.opA + "  " + EstadoCombate.barraOp(c.opA));

        // Digimon B
        String nomB = b != null ? b.nombre + " [" + b.elemento + "]" : "---";
        ui.set("#LblNombreB.Text", nomB);
        ui.set("#LblBarraB.Text",  EstadoCombate.barraHp(c.hpCombateB, b != null ? b.maxHp : 1));
        ui.set("#LblHpB.Text",     "HP: " + c.hpCombateB + (b != null ? "/" + b.maxHp : ""));
        ui.set("#LblOpB.Text",     "OP: " + c.opB + "  " + EstadoCombate.barraOp(c.opB));

        // Log
        ui.set("#LblLog.Text", c.getLogFormateado());
    }

    // ── Textos de botones por fase ─────────────────────────────────
    private void aplicarTextosBotonesNormal(UICommandBuilder ui, EstadoCombate c) {
        ui.set("#BtnSupport.Text",   "Support");
        ui.set("#BtnOrderRing.Text", "Order Ring");
        ui.set("#BtnTacticaA.Text",  "Tactica A: " + c.tacticaA);
        ui.set("#BtnTacticaB.Text",  "Tactica B: " + c.tacticaB);
        ui.set("#BtnHuir.Text",      "Huir");
    }

    private void aplicarTextosBotonesOrderRing(UICommandBuilder ui, EstadoCombate c) {
        ui.set("#BtnSupport.Text",   "Guard A (" + EstadoCombate.OP_GUARD + " OP)");
        ui.set("#BtnOrderRing.Text", "Guard B (" + EstadoCombate.OP_GUARD + " OP)");
        ui.set("#BtnTacticaA.Text",  "Atk A (" + EstadoCombate.OP_ATTACK + " OP)");
        ui.set("#BtnTacticaB.Text",  "Atk B (" + EstadoCombate.OP_ATTACK + " OP)");
        ui.set("#BtnHuir.Text",      "Cerrar OR");
    }

    private void aplicarTextosBotonesFin(UICommandBuilder ui) {
        ui.set("#BtnSupport.Text",   "Continuar");
        ui.set("#BtnOrderRing.Text", "");
        ui.set("#BtnTacticaA.Text",  "");
        ui.set("#BtnTacticaB.Text",  "");
        ui.set("#BtnHuir.Text",      "");
    }

    // ── Bind de eventos ────────────────────────────────────────────
    private void bindNormal(UIEventBuilder ev, EstadoCombate c) {
        DigiUIHelper.bindClick(ev, "#BtnSupport",  "Accion", "support");
        if (c.opA >= EstadoCombate.OP_GUARD || c.opB >= EstadoCombate.OP_GUARD) {
            DigiUIHelper.bindClick(ev, "#BtnOrderRing", "Accion", "open_order_ring");
        }
        DigiUIHelper.bindClick(ev, "#BtnTacticaA", "Accion", "tactica_a");
        DigiUIHelper.bindClick(ev, "#BtnTacticaB", "Accion", "tactica_b");
        DigiUIHelper.bindClick(ev, "#BtnHuir",     "Accion", "huir");
    }

    private void bindOrderRing(UIEventBuilder ev) {
        DigiUIHelper.bindClick(ev, "#BtnSupport",   "Accion", "orden_guard_a");
        DigiUIHelper.bindClick(ev, "#BtnOrderRing", "Accion", "orden_guard_b");
        DigiUIHelper.bindClick(ev, "#BtnTacticaA",  "Accion", "orden_attack_a");
        DigiUIHelper.bindClick(ev, "#BtnTacticaB",  "Accion", "orden_attack_b");
        DigiUIHelper.bindClick(ev, "#BtnHuir",      "Accion", "cerrar_order_ring");
    }

    // ── Handle de eventos del cliente ──────────────────────────────
    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);
        LOGGER.log(Level.INFO, "DigiBatallaMenuUI accion: " + data.accion);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null) return;

        EstadoCombate c = datos.combate;
        DatoDigimon a = datos.companeroA;
        DatoDigimon b = datos.companeroB;

        // Combate ya terminado: solo cerrar
        if (c == null || !c.activa) {
            if (data.accion.equals("cerrar") || c == null) {
                datos.combate = null;
                guardarYCerrar(ref, store, player, datos);
            }
            return;
        }

        switch (data.accion) {

            case "support"  -> SistemaBatalla.support(c, a, b);

            case "open_order_ring" -> {
                c.fase = EstadoCombate.Fase.ORDER_RING;
                c.logear("Order Ring abierto");
            }

            case "cerrar_order_ring" -> {
                c.fase = EstadoCombate.Fase.RESOLVIENDO;
                c.logear("Order Ring cerrado");
            }

            case "huir"     -> SistemaBatalla.huir(c, a, b);

            case "tactica_a" -> {
                c.tacticaA = ciclarTactica(c.tacticaA);
                c.logear("Tactica A -> " + c.tacticaA);
            }

            case "tactica_b" -> {
                c.tacticaB = ciclarTactica(c.tacticaB);
                c.logear("Tactica B -> " + c.tacticaB);
            }

            case "orden_guard_a"   -> SistemaBatalla.ejecutarOrden(c, EstadoCombate.Orden.GUARD_A,    a, b);
            case "orden_guard_b"   -> SistemaBatalla.ejecutarOrden(c, EstadoCombate.Orden.GUARD_B,    a, b);
            case "orden_attack_a"  -> SistemaBatalla.ejecutarOrden(c, EstadoCombate.Orden.ATTACK_A,   a, b);
            case "orden_attack_b"  -> SistemaBatalla.ejecutarOrden(c, EstadoCombate.Orden.ATTACK_B,   a, b);
            case "orden_special_a" -> SistemaBatalla.ejecutarOrden(c, EstadoCombate.Orden.SPECIAL_A,  a, b);
            case "orden_special_b" -> SistemaBatalla.ejecutarOrden(c, EstadoCombate.Orden.SPECIAL_B,  a, b);
            case "orden_exe"       -> SistemaBatalla.ejecutarOrden(c, EstadoCombate.Orden.EXE_FUSION, a, b);

            case "cerrar" -> {
                datos.combate = null;
                guardarYCerrar(ref, store, player, datos);
                return;
            }
        }

        // ── Enviar update con el nuevo estado ─────────────────────
        UICommandBuilder update = new UICommandBuilder();
        UIEventBuilder ev = new UIEventBuilder();

        aplicarEstado(update, c, a, b);

        if (!c.activa || c.fase == EstadoCombate.Fase.FIN_COMBATE) {
            aplicarTextosBotonesFin(update);
            DigiUIHelper.bindClick(ev, "#BtnSupport", "Accion", "cerrar");
        } else if (c.fase == EstadoCombate.Fase.ORDER_RING) {
            aplicarTextosBotonesOrderRing(update, c);
            bindOrderRing(ev);
        } else {
            aplicarTextosBotonesNormal(update, c);
            bindNormal(ev, c);
        }

        sendUpdate(update, ev, false);
    }

    // ── Helpers ───────────────────────────────────────────────────
    private EstadoCombate.Tactica ciclarTactica(EstadoCombate.Tactica t) {
        return switch (t) {
            case AGRESIVO    -> EstadoCombate.Tactica.EQUILIBRADO;
            case EQUILIBRADO -> EstadoCombate.Tactica.DEFENSIVO;
            case DEFENSIVO   -> EstadoCombate.Tactica.AGRESIVO;
        };
    }

    private void guardarYCerrar(Ref<EntityStore> ref, Store<EntityStore> store,
                                Player player, AlmacenJugadores.DatosJugador datos) {
        PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
        progress.guardarDesde(datos);
        player.getPageManager().setPage(ref, store, Page.None);
    }
}