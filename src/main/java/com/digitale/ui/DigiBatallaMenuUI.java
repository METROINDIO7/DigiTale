package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.digitale.datos.EstadoCombate.Fase;
import com.digitale.datos.EstadoCombate.Orden;
import com.digitale.datos.EstadoCombate.Tactica;
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

/**
 * UI de batalla estilo Digimon World: Next Order.
 *
 * Vista COMBATE (fase RESOLVIENDO):
 *   - Muestra HP de enemigo, HP de A y B, OP actuales, log
 *   - Botón SUPPORT: genera OP + resuelve la ronda automáticamente
 *   - Botón ORDER RING: abre el ring de órdenes (si hay suficientes OP)
 *   - Botón TÁCTICAS: cambia la táctica de A y/o B
 *   - Botón HUIR
 *
 * Vista ORDER RING (fase ORDER_RING):
 *   - Muestra coste de cada orden
 *   - Botones habilitados/deshabilitados según OP disponibles
 *   - Incluye ExE si hay 300 OP + lazo alto
 */
public class DigiBatallaMenuUI extends InteractiveCustomUIPage<DigiBatallaMenuUI.Data> {

    public static class Data {
        public String accion = "";

        public static final BuilderCodec<Data> CODEC = BuilderCodec
                .builder(Data.class, Data::new)
                .append(new KeyedCodec<>("Accion", Codec.STRING),
                        (d, v) -> d.accion = v,
                        d -> d.accion)
                .add()
                .build();
    }

    private final PlayerRef playerRef;

    public DigiBatallaMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
    }

    // ── Build ──────────────────────────────────────────────────────────
    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder b,
                      @Nonnull UIEventBuilder ev,
                      @Nonnull Store<EntityStore> store) {

        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        EstadoCombate e    = datos.combate;

        if (e == null) {
            b.append("Pages/DigiBatallaMenu.ui");
            b.set("#LblTurno.Text",   "Sin combate activo");
            b.set("#LblEnemigo.Text", "---");
            b.set("#LblEnemHp.Text",  "HP: ---");
            b.set("#LblBarraEnem.Text","░░░░░░░░░░");
            b.set("#LblNombreA.Text", "---");
            b.set("#LblBarraA.Text",  "░░░░░░░░░░");
            b.set("#LblHpA.Text",     "HP: ---");
            b.set("#LblOpA.Text",     "OP: 0");
            b.set("#LblNombreB.Text", "---");
            b.set("#LblBarraB.Text",  "░░░░░░░░░░");
            b.set("#LblHpB.Text",     "HP: ---");
            b.set("#LblOpB.Text",     "OP: 0");
            b.set("#LblAlerta.Text",  "");
            b.set("#LblLog.Text",     "Equipa el Sapotama y pulsa F para iniciar un combate.");
            b.set("#LblResultado.Text","");
            DigiUIHelper.bindClick(ev, "#BtnSupport",   "Accion", "salir");
            DigiUIHelper.bindClick(ev, "#BtnOrderRing", "Accion", "salir");
            DigiUIHelper.bindClick(ev, "#BtnHuir",      "Accion", "salir");
            return;
        }

        DatoDigimon a = datos.companeroA;
        DatoDigimon b2 = datos.companeroB;

        // ── ¿Estamos en el Order Ring? ─────────────────────────────────
        if (e.fase == Fase.ORDER_RING) {
            buildOrderRing(b, ev, e, a, b2);
            return;
        }

        // ── Vista principal de combate ─────────────────────────────────
        b.append("Pages/DigiBatallaMenu.ui");

        String turnoTxt = e.fase == Fase.FIN_COMBATE
            ? (e.victoria ? "!VICTORIA!" : "DERROTA")
            : "Ronda " + e.ronda;
        b.set("#LblTurno.Text", turnoTxt);

        // Enemigo
        b.set("#LblEnemigo.Text",  e.enemigoNombre + " [" + e.enemigoElemento + "]");
        b.set("#LblEnemHp.Text",   "HP: " + e.enemigoHp + "/" + e.enemigoMaxHp);
        b.set("#LblBarraEnem.Text", EstadoCombate.barraHp(e.enemigoHp, e.enemigoMaxHp));
        b.set("#LblAlerta.Text",   e.enemigoCargando ? "! ENEMIGO CARGANDO GOLPE DEVASTADOR !" : "");

        // Digimon A
        if (a != null) {
            String tacA = switch (e.tacticaA) { case AGRESIVO -> "[AGR]"; case DEFENSIVO -> "[DEF]"; default -> "[EQU]"; };
            b.set("#LblNombreA.Text", a.nombre + " " + tacA);
            b.set("#LblBarraA.Text",  EstadoCombate.barraHp(e.hpCombateA, a.maxHp));
            b.set("#LblHpA.Text",     "HP " + e.hpCombateA + "/" + a.maxHp);
            b.set("#LblOpA.Text",     "OP: " + e.opA + " " + EstadoCombate.barraOp(e.opA));
        } else {
            b.set("#LblNombreA.Text", "Sin companero A");
            b.set("#LblBarraA.Text",  "░░░░░░░░░░");
            b.set("#LblHpA.Text",     "---");
            b.set("#LblOpA.Text",     "OP: 0");
        }

        // Digimon B
        if (b2 != null) {
            String tacB = switch (e.tacticaB) { case AGRESIVO -> "[AGR]"; case DEFENSIVO -> "[DEF]"; default -> "[EQU]"; };
            b.set("#LblNombreB.Text", b2.nombre + " " + tacB);
            b.set("#LblBarraB.Text",  EstadoCombate.barraHp(e.hpCombateB, b2.maxHp));
            b.set("#LblHpB.Text",     "HP " + e.hpCombateB + "/" + b2.maxHp);
            b.set("#LblOpB.Text",     "OP: " + e.opB + " " + EstadoCombate.barraOp(e.opB));
        } else {
            b.set("#LblNombreB.Text", "Sin companero B");
            b.set("#LblBarraB.Text",  "░░░░░░░░░░");
            b.set("#LblHpB.Text",     "---");
            b.set("#LblOpB.Text",     "OP: 0");
        }

        // Log
        b.set("#LblLog.Text", e.getLogFormateado());

        // Resultado final
        if (e.fase == Fase.FIN_COMBATE) {
            String res = e.victoria
                ? "+" + e.enemigoExp + " EXP  |  +Lazo  |  +ABI"
                : "Tus companeros necesitan descanso.";
            b.set("#LblResultado.Text", res);
        } else {
            b.set("#LblResultado.Text", "");
        }

        // Botones
        if (e.fase == Fase.FIN_COMBATE) {
            DigiUIHelper.bindClick(ev, "#BtnSupport",   "Accion", "volver");
            DigiUIHelper.bindClick(ev, "#BtnOrderRing", "Accion", "volver");
            DigiUIHelper.bindClick(ev, "#BtnHuir",      "Accion", "volver");
        } else {
            DigiUIHelper.bindClick(ev, "#BtnSupport",   "Accion", "support");
            DigiUIHelper.bindClick(ev, "#BtnOrderRing", "Accion", "abrir_ring");
            DigiUIHelper.bindClick(ev, "#BtnHuir",      "Accion", "huir");
            // Ciclar tácticas
            DigiUIHelper.bindClick(ev, "#BtnTacticaA",  "Accion", "tactica_a");
            DigiUIHelper.bindClick(ev, "#BtnTacticaB",  "Accion", "tactica_b");
        }
    }

    /** Vista del Order Ring con todos los botones de orden */
    private void buildOrderRing(@Nonnull UICommandBuilder b,
                                 @Nonnull UIEventBuilder ev,
                                 @Nonnull EstadoCombate e,
                                 DatoDigimon a, DatoDigimon b2) {
        b.append("Pages/DigiOrderRing.ui");

        // Mostrar OP disponibles
        b.set("#LblOpRingA.Text", "OP de " + (a != null ? a.nombre : "A") + ": " + e.opA);
        b.set("#LblOpRingB.Text", "OP de " + (b2 != null ? b2.nombre : "B") + ": " + e.opB);
        b.set("#LblOpTotal.Text", "TOTAL: " + e.opTotal() + " OP");

        // Costes para referencia
        b.set("#LblCosteGuard.Text",   "Guard = " + EstadoCombate.OP_GUARD + " OP");
        b.set("#LblCosteAttack.Text",  "Order Attack = " + EstadoCombate.OP_ATTACK + " OP");
        b.set("#LblCosteSpecial.Text", "Special = " + EstadoCombate.OP_SPECIAL + " OP");
        b.set("#LblCosteExe.Text",     "ExE Fusion = " + EstadoCombate.OP_EXE + " OP total");

        // Disponibilidad de ExE
        int lazoA = a != null ? a.lazo : 0;
        int lazoB = b2 != null ? b2.lazo : 0;
        boolean exeDisp = e.opTotal() >= EstadoCombate.OP_EXE && lazoA >= 60 && lazoB >= 60;
        b.set("#LblExeEstado.Text", exeDisp ? "ExE DISPONIBLE!" : "ExE: lazo insuficiente o sin OP");

        // Binds
        DigiUIHelper.bindClick(ev, "#BtnGuardA",   "Accion", "guard_a");
        DigiUIHelper.bindClick(ev, "#BtnGuardB",   "Accion", "guard_b");
        DigiUIHelper.bindClick(ev, "#BtnAttackA",  "Accion", "attack_a");
        DigiUIHelper.bindClick(ev, "#BtnAttackB",  "Accion", "attack_b");
        DigiUIHelper.bindClick(ev, "#BtnSpecialA", "Accion", "special_a");
        DigiUIHelper.bindClick(ev, "#BtnSpecialB", "Accion", "special_b");
        DigiUIHelper.bindClick(ev, "#BtnExe",      "Accion", "exe");
        DigiUIHelper.bindClick(ev, "#BtnCerrarRing","Accion", "cerrar_ring");
    }

    // ── Handle ────────────────────────────────────────────────────────
    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        EstadoCombate e    = datos.combate;

        switch (data.accion) {

            case "support" -> {
                if (e == null || !e.activa) { refrescar(player, ref, store); return; }
                SistemaBatalla.supportYResolver(e, datos.companeroA, datos.companeroB);
                // Actualizar HUD con los nuevos valores de la ronda
                DigiBatallaHudManager.actualizar(playerRef, ref, store, datos);
                // Si el combate terminó, ocultar HUD
                if (!e.activa) DigiBatallaHudManager.ocultar(playerRef, ref, store);
                refrescar(player, ref, store);
            }

            case "abrir_ring" -> {
                if (e == null) { refrescar(player, ref, store); return; }
                e.fase = Fase.ORDER_RING;
                refrescar(player, ref, store);
            }

            case "cerrar_ring" -> {
                if (e != null) e.fase = Fase.RESOLVIENDO;
                refrescar(player, ref, store);
            }

            // Order Ring — dar órdenes
            case "guard_a"   -> ejecutarOrden(e, datos, Orden.GUARD_A,   player, ref, store);
            case "guard_b"   -> ejecutarOrden(e, datos, Orden.GUARD_B,   player, ref, store);
            case "attack_a"  -> ejecutarOrden(e, datos, Orden.ATTACK_A,  player, ref, store);
            case "attack_b"  -> ejecutarOrden(e, datos, Orden.ATTACK_B,  player, ref, store);
            case "special_a" -> ejecutarOrden(e, datos, Orden.SPECIAL_A, player, ref, store);
            case "special_b" -> ejecutarOrden(e, datos, Orden.SPECIAL_B, player, ref, store);
            case "exe"       -> ejecutarOrden(e, datos, Orden.EXE_FUSION, player, ref, store);

            // Ciclar táctica A
            case "tactica_a" -> {
                if (e != null) e.tacticaA = ciclaTactica(e.tacticaA);
                refrescar(player, ref, store);
            }

            // Ciclar táctica B
            case "tactica_b" -> {
                if (e != null) e.tacticaB = ciclaTactica(e.tacticaB);
                refrescar(player, ref, store);
            }

            case "huir" -> {
                if (e == null || e.fase == Fase.FIN_COMBATE) {
                    player.getPageManager().setPage(ref, store, Page.None);
                    return;
                }
                SistemaBatalla.intentarHuir(e, datos.companeroA, datos.companeroB);
                refrescar(player, ref, store);
            }

            case "volver", "salir" -> {
                if (e == null || !e.activa) DigiBatallaHudManager.ocultar(playerRef, ref, store);
                player.getPageManager().setPage(ref, store, Page.None);
            }

            default -> refrescar(player, ref, store);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private void ejecutarOrden(EstadoCombate e, DatosJugador datos, Orden orden,
                                Player player, Ref<EntityStore> ref, Store<EntityStore> store) {
        if (e == null) { refrescar(player, ref, store); return; }
        SistemaBatalla.darOrden(e, datos.companeroA, datos.companeroB, orden);
        refrescar(player, ref, store);
    }

    private Tactica ciclaTactica(Tactica actual) {
        return switch (actual) {
            case EQUILIBRADO -> Tactica.AGRESIVO;
            case AGRESIVO    -> Tactica.DEFENSIVO;
            case DEFENSIVO   -> Tactica.EQUILIBRADO;
        };
    }

    private void refrescar(Player player, Ref<EntityStore> ref, Store<EntityStore> store) {
        player.getPageManager().openCustomPage(ref, store, new DigiBatallaMenuUI(playerRef));
    }
}
