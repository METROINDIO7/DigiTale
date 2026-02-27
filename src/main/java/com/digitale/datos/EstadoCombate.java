package com.digitale.datos;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Estado completo de una batalla activa.
 *
 * Diseño inspirado en Digimon World Next Order:
 *  - Ambos Digimon luchan AUTOMÁTICAMENTE cada ronda según su táctica
 *  - El jugador actúa como TAMER: acumula Order Points (OP) pulsando SUPPORT
 *  - Con OP suficientes abre el Order Ring para dar órdenes directas
 *  - ExE Fusion disponible con 300 OP totales y vínculo alto
 *
 * Fases de la UI:
 *   RESOLVIENDO  → la ronda se acaba de resolver, esperando input del tamer
 *   ORDER_RING   → el tamer abrió el Order Ring, elige orden
 *   FIN_COMBATE  → victoria / derrota / huida
 */
public class EstadoCombate {

    // ── Táctica de cada Digimon (la del jugador puede cambiar en Order Ring) ──
    public enum Tactica {
        AGRESIVO,    // Prioriza ataques fuertes, ignora def
        EQUILIBRADO, // Balance atk/def
        DEFENSIVO    // Prioriza guardia y contraataques
    }

    // ── Orden que el tamer puede dar desde el Order Ring ─────────────
    public enum Orden {
        NINGUNA,
        GUARD_A,     // A defiende la próxima hit (10 OP)
        GUARD_B,     // B defiende la próxima hit (10 OP)
        ATTACK_A,    // A usa su mejor ataque esta ronda (30 OP)
        ATTACK_B,    // B usa su mejor ataque esta ronda (30 OP)
        SPECIAL_A,   // A usa ataque especial (150 OP)
        SPECIAL_B,   // B usa ataque especial (150 OP)
        EXE_FUSION   // Fusión temporal A+B (300 OP, requiere lazo alto)
    }

    public enum Fase {
        RESOLVIENDO, // Esperando acción del tamer
        ORDER_RING,  // Order Ring abierto
        FIN_COMBATE
    }

    // ══ Enemigo ═══════════════════════════════════════════════════════
    public String enemigoNombre   = "";
    public String enemigoElemento = "NEUTRO";
    public int    enemigoHp       = 0;
    public int    enemigoMaxHp    = 0;
    public int    enemigoAtk      = 0;
    public int    enemigoDef      = 0;
    public int    enemigoSpd      = 0;
    public int    enemigoWis      = 0;
    public int    enemigoExp      = 0;

    // ══ HP de combate (separados del HP del mundo) ════════════════════
    public int hpCombateA = 0;
    public int hpCombateB = 0;

    // ══ Order Points ══════════════════════════════════════════════════
    public int opA = 0;   // OP acumulados para Digimon A
    public int opB = 0;   // OP acumulados para Digimon B
    public static final int OP_GUARD   = 10;
    public static final int OP_ATTACK  = 30;
    public static final int OP_SPECIAL = 150;
    public static final int OP_EXE     = 300; // total opA+opB

    // ══ Tácticas ══════════════════════════════════════════════════════
    public Tactica tacticaA = Tactica.EQUILIBRADO;
    public Tactica tacticaB = Tactica.EQUILIBRADO;

    // ══ Órdenes activas para la próxima ronda ════════════════════════
    public Orden ordenActiva = Orden.NINGUNA;

    // ══ Efectos temporales ════════════════════════════════════════════
    public boolean guardActivo_A     = false; // A defiende esta ronda
    public boolean guardActivo_B     = false; // B defiende esta ronda
    public boolean attackOrden_A     = false; // A usa mejor ataque
    public boolean attackOrden_B     = false; // B usa mejor ataque
    public boolean specialActivo_A   = false; // A usa especial
    public boolean specialActivo_B   = false; // B usa especial
    public boolean exeFusion         = false; // ExE activa esta ronda
    public int     turnosExeRestantes = 0;

    // ══ Estado general ════════════════════════════════════════════════
    public int   ronda     = 1;
    public Fase  fase      = Fase.RESOLVIENDO;
    public boolean activa  = true;
    public boolean victoria = false;

    // ══ Enemigo puede cargar un ataque potente ════════════════════════
    public boolean enemigoCargando = false; // telegrafía ataque fuerte

    // ══ Log de combate ════════════════════════════════════════════════
    private final Deque<String> logLineas = new ArrayDeque<>(6);

    public void logear(String linea) {
        if (logLineas.size() >= 5) logLineas.pollFirst();
        logLineas.addLast(linea);
    }

    public String getLogFormateado() {
        if (logLineas.isEmpty()) return "El combate comienza...";
        return String.join("\n", logLineas);
    }

    // ══ Helpers ═══════════════════════════════════════════════════════

    public float pctHpA(DatoDigimon a) {
        return a == null || a.maxHp == 0 ? 0f : (float) hpCombateA / a.maxHp;
    }

    public float pctHpB(DatoDigimon b) {
        return b == null || b.maxHp == 0 ? 0f : (float) hpCombateB / b.maxHp;
    }

    public float pctHpEnemigo() {
        return enemigoMaxHp == 0 ? 0f : (float) enemigoHp / enemigoMaxHp;
    }

    public boolean aVivo() { return hpCombateA > 0; }
    public boolean bVivo() { return hpCombateB > 0; }
    public boolean equipoVivo() { return aVivo() || bVivo(); }

    public int opTotal() { return opA + opB; }

    /** Barra visual de HP — 10 bloques */
    public static String barraHp(int hp, int max) {
        if (max <= 0) return "░░░░░░░░░░";
        int llenos = Math.max(0, Math.min(10, (int)((float) hp / max * 10)));
        return "█".repeat(llenos) + "░".repeat(10 - llenos);
    }

    /** Barra visual de OP — 10 bloques (máx 300 OP) */
    public static String barraOp(int op) {
        int llenos = Math.max(0, Math.min(10, (int)((float) op / OP_SPECIAL * 10)));
        return "▓".repeat(llenos) + "░".repeat(10 - llenos);
    }

    /** Resetear órdenes activas al final de la ronda */
    public void resetearOrdenes() {
        guardActivo_A  = false;
        guardActivo_B  = false;
        attackOrden_A  = false;
        attackOrden_B  = false;
        specialActivo_A = false;
        specialActivo_B = false;
        exeFusion      = false;
        ordenActiva    = Orden.NINGUNA;
    }
}
