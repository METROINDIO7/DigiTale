package com.digitale.datos;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Estado completo de una batalla activa.
 *
 * Mecánica principal:
 *  - El equipo del jugador (A y B) lucha AUTOMÁTICAMENTE cada ronda según su táctica.
 *  - El jugador acumula Order Points (OP) pulsando SUPPORT.
 *  - Con OP suficientes abre el Order Ring para dar órdenes directas.
 *  - A partir de cierta ronda (o cuando el enemigo llega al 60% de HP),
 *    aparece un REFUERZO SALVAJE que se une al combate.
 */
public class EstadoCombate {

    public enum Tactica { AGRESIVO, EQUILIBRADO, DEFENSIVO }

    public enum Orden {
        NINGUNA,
        GUARD_A,    // 10 OP — A defiende la próxima hit
        GUARD_B,    // 10 OP — B defiende la próxima hit
        ATTACK_A,   // 30 OP — A usa su mejor ataque esta ronda
        ATTACK_B,   // 30 OP — B usa su mejor ataque esta ronda
        SPECIAL_A,  // 150 OP — A usa ataque especial
        SPECIAL_B,  // 150 OP — B usa ataque especial
        EXE_FUSION  // 300 OP total, requiere lazo >= 70
    }

    public enum Fase { RESOLVIENDO, ORDER_RING, FIN_COMBATE }

    // ── Enemigo principal ──────────────────────────────────────────
    public String  enemigoNombre   = "";
    public String  enemigoElemento = "NEUTRO";
    public int     enemigoHp       = 0;
    public int     enemigoMaxHp    = 0;
    public int     enemigoAtk      = 0;
    public int     enemigoDef      = 0;
    public int     enemigoSpd      = 0;
    public int     enemigoWis      = 0;
    public int     enemigoExp      = 0;
    public boolean enemigoCargando = false;

    // ── Refuerzo (segundo salvaje que aparece a mitad del combate) ─
    public String  refuerzoNombre   = "";
    public String  refuerzoElemento = "NEUTRO";
    public int     refuerzoHp       = 0;
    public int     refuerzoMaxHp    = 0;
    public int     refuerzoAtk      = 0;
    public int     refuerzoDef      = 0;
    public int     refuerzoSpd      = 0;
    public int     refuerzoExp      = 0;
    public boolean refuerzoActivo   = false;  // true cuando ya apareció en escena
    public boolean refuerzoCargando = false;
    public boolean refuerzoAnunciado = false; // para mostrar el mensaje de llegada solo una vez

    // ── HP de combate (separados del HP del mundo) ─────────────────
    public int hpCombateA = 0;
    public int hpCombateB = 0;

    // ── Order Points ───────────────────────────────────────────────
    public int opA = 0;
    public int opB = 0;
    public static final int OP_GUARD   = 10;
    public static final int OP_ATTACK  = 30;
    public static final int OP_SPECIAL = 150;
    public static final int OP_EXE     = 300;

    // ── Tácticas ───────────────────────────────────────────────────
    public Tactica tacticaA = Tactica.EQUILIBRADO;
    public Tactica tacticaB = Tactica.EQUILIBRADO;

    // ── Órdenes activas para la próxima ronda ──────────────────────
    public Orden   ordenActiva     = Orden.NINGUNA;
    public boolean guardActivo_A   = false;
    public boolean guardActivo_B   = false;
    public boolean attackOrden_A   = false;
    public boolean attackOrden_B   = false;
    public boolean specialActivo_A = false;
    public boolean specialActivo_B = false;
    public boolean exeFusion       = false;
    public int     turnosExeRestantes = 0;

    // ── Estado general ─────────────────────────────────────────────
    public int     ronda    = 1;
    public Fase    fase     = Fase.RESOLVIENDO;
    public boolean activa   = true;
    public boolean victoria = false;
    public int     expTotal = 0;   // XP acumulada al terminar

    // ── Log de combate ─────────────────────────────────────────────
    private final Deque<String> logLineas = new ArrayDeque<>(6);

    public void logear(String linea) {
        if (logLineas.size() >= 5) logLineas.pollFirst();
        logLineas.addLast(linea);
    }

    public String getLogFormateado() {
        return logLineas.isEmpty() ? "El combate comienza..." : String.join("\n", logLineas);
    }

    // ── Helpers ────────────────────────────────────────────────────
    public float pctHpA(DatoDigimon a)   { return a == null || a.maxHp == 0 ? 0f : (float) hpCombateA / a.maxHp; }
    public float pctHpB(DatoDigimon b)   { return b == null || b.maxHp == 0 ? 0f : (float) hpCombateB / b.maxHp; }
    public float pctHpEnemigo()          { return enemigoMaxHp  == 0 ? 0f : (float) enemigoHp  / enemigoMaxHp; }
    public float pctHpRefuerzo()         { return refuerzoMaxHp == 0 ? 0f : (float) refuerzoHp / refuerzoMaxHp; }

    public boolean aVivo()               { return hpCombateA > 0; }
    public boolean bVivo()               { return hpCombateB > 0; }
    public boolean equipoVivo()          { return aVivo() || bVivo(); }
    public boolean enemigoVivo()         { return enemigoHp  > 0; }
    public boolean refuerzoVivo()        { return refuerzoActivo && refuerzoHp > 0; }
    public boolean todosEnemigosDerrota(){ return !enemigoVivo() && !refuerzoVivo(); }
    public int     opTotal()             { return opA + opB; }

    /** Barra visual de HP — 10 bloques */
    public static String barraHp(int hp, int max) {
        if (max <= 0) return "░░░░░░░░░░";
        int llenos = Math.max(0, Math.min(10, (int)((float) hp / max * 10)));
        return "█".repeat(llenos) + "░".repeat(10 - llenos);
    }

    /** Barra visual de OP — máx 150 OP (SPECIAL) */
    public static String barraOp(int op) {
        int llenos = Math.max(0, Math.min(10, (int)((float) op / OP_SPECIAL * 10)));
        return "▓".repeat(llenos) + "░".repeat(10 - llenos);
    }

    public void resetearOrdenes() {
        guardActivo_A  = false; guardActivo_B  = false;
        attackOrden_A  = false; attackOrden_B  = false;
        specialActivo_A = false; specialActivo_B = false;
        exeFusion   = false;
        ordenActiva = Orden.NINGUNA;
    }
}