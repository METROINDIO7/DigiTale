package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

/**
 * HUD persistente del combate DigiTale.
 *
 * Se muestra en pantalla sin capturar input mientras hay batalla activa.
 * Se actualiza con cada SUPPORT pulsado y desaparece al terminar.
 *
 * Para actualizar: hud.actualizarDatos(datos)
 * Para ocultar:    hud.ocultar()
 */
public class DigiBatallaHud extends CustomUIHud {

    private static final String HUD_DOCUMENT = "HUD/DigiBatallaHud.ui";

    private boolean oculto = false;

    // Guardamos referencia al playerRef para poder acceder desde el manager
    private final PlayerRef playerRef;

    public DigiBatallaHud(PlayerRef playerRef) {
        super(playerRef);
        this.playerRef = playerRef;
    }

    public PlayerRef getPlayerRef() {
        return playerRef;
    }

    // ── build: carga el .ui cuando se asigna el HUD al jugador ─────
    @Override
    protected void build(UICommandBuilder builder) {
        builder.append(HUD_DOCUMENT);
    }

    // ── Actualizar con datos de combate ─────────────────────────────
    /**
     * Envía una actualización parcial (rebuild=false) con los valores actuales.
     * Llamar tras cada resolución de ronda.
     */
    public void actualizarDatos(DatosJugador datos) {
        if (oculto || datos == null || datos.combate == null) return;

        EstadoCombate e = datos.combate;
        DatoDigimon a   = datos.companeroA;
        DatoDigimon b   = datos.companeroB;

        UICommandBuilder builder = new UICommandBuilder();

        // ── Ronda ──────────────────────────────────────────────────
        builder.set("#HudTextoRonda.Text", "Ronda " + e.ronda);

        // ── Digimon A ──────────────────────────────────────────────
        if (a != null) {
            String tacA = switch (e.tacticaA) {
                case AGRESIVO  -> "AGR";
                case DEFENSIVO -> "DEF";
                default        -> "EQU";
            };
            builder.set("#HudNombreA.Text",  a.nombre + " [" + tacA + "]");
            builder.set("#HudHpTextoA.Text", e.hpCombateA + "/" + a.maxHp);
            builder.set("#HudOpTextoA.Text", e.opA + " OP");
            // Ancho de barra HP (max 140px)
            int anchoA = a.maxHp > 0 ? (int)(140f * e.hpCombateA / a.maxHp) : 0;
            builder.set("#HudBarraHpA.Anchor", "(Left: 0, Top: 0, Bottom: 0, Width: " + Math.max(0, anchoA) + ")");
        }

        // ── Digimon B ──────────────────────────────────────────────
        if (b != null) {
            String tacB = switch (e.tacticaB) {
                case AGRESIVO  -> "AGR";
                case DEFENSIVO -> "DEF";
                default        -> "EQU";
            };
            builder.set("#HudNombreB.Text",  b.nombre + " [" + tacB + "]");
            builder.set("#HudHpTextoB.Text", e.hpCombateB + "/" + b.maxHp);
            builder.set("#HudOpTextoB.Text", e.opB + " OP");
            int anchoB = b.maxHp > 0 ? (int)(140f * e.hpCombateB / b.maxHp) : 0;
            builder.set("#HudBarraHpB.Anchor", "(Left: 0, Top: 0, Bottom: 0, Width: " + Math.max(0, anchoB) + ")");
        }

        // ── Enemigo ────────────────────────────────────────────────
        builder.set("#HudNombreEnemigo.Text",  e.enemigoNombre + " [" + e.enemigoElemento + "]");
        builder.set("#HudHpTextoEnemigo.Text", e.enemigoHp + "/" + e.enemigoMaxHp);
        int anchoEnem = e.enemigoMaxHp > 0 ? (int)(200f * e.enemigoHp / e.enemigoMaxHp) : 0;
        builder.set("#HudBarraHpEnemigo.Anchor", "(Left: 0, Top: 0, Bottom: 0, Width: " + Math.max(0, anchoEnem) + ")");

        // ── Alerta carga ───────────────────────────────────────────
        builder.set("#HudAlerta.Visible", e.enemigoCargando ? "true" : "false");

        // ── ExE disponible ─────────────────────────────────────────
        int lazoA = a != null ? a.lazo : 0;
        int lazoB = b != null ? b.lazo : 0;
        boolean exeDisp = e.opTotal() >= EstadoCombate.OP_EXE && lazoA >= 60 && lazoB >= 60;
        builder.set("#HudExe.Visible", exeDisp ? "true" : "false");

        // rebuild=false → actualización parcial (solo las propiedades que cambian)
        update(false, builder);
    }

    // ── Ocultar HUD (cuando termina el combate) ─────────────────────
    /**
     * Limpia el HUD enviando un update total con builder vacío.
     * NUNCA usar setCustomHud(null) — crashea el cliente.
     */
    public void ocultar() {
        oculto = true;
        UICommandBuilder builderVacio = new UICommandBuilder();
        // rebuild=true + builder vacío = el HUD desaparece
        update(true, builderVacio);
    }

    // ── Mostrar HUD (reutilizar instancia si el jugador reinicia combate) ──
    public void mostrar() {
        oculto = false;
        UICommandBuilder builder = new UICommandBuilder();
        builder.append(HUD_DOCUMENT);
        update(true, builder);
    }

    public boolean estaOculto() { return oculto; }
    public void setOculto(boolean oculto) { this.oculto = oculto; }
}
