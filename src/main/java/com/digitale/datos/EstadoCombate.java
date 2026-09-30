package com.digitale.datos;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Set;
import java.util.HashSet;

public class EstadoCombate {

    public enum Tactica { AGRESIVO, EQUILIBRADO, DEFENSIVO }

    public enum Orden {
        NINGUNA,
        GUARD_A, GUARD_B, GUARD_C,
        ATTACK_A, ATTACK_B, ATTACK_C,
        SPECIAL_A, SPECIAL_B, SPECIAL_C,
        EXE_FUSION,
        SKILL_1, SKILL_2, SKILL_3, SKILL_4, SKILL_5, SKILL_6, SKILL_7
    }

    public enum Fase { INICIATIVA, RESOLVIENDO, ORDER_RING, FIN_COMBATE }

    public enum EstadoAlterado {
        QUEMADO,
        VENENO,
        PARALISIS,
        DORMIDO,
        ATURDIDO,
        ENVENENADO_FUERTE,
        SANGRANDO
    }

    public enum TipoDano {
        NORMAL, CRITICO, MUY_CRITICO, FALLIDO, RESISTIDO, INMUNE
    }

    // ══ ENEMIGO PRINCIPAL ══════════════════════════════════════════════
    public String  enemigoNombre    = "";
    public String  enemigoElemento = "NEUTRO";
    public int     enemigoHp       = 0;
    public int     enemigoMaxHp    = 0;
    public int     enemigoAtk     = 0;
    public int     enemigoDef      = 0;
    public int     enemigoSpd      = 0;
    public int     enemigoWis      = 0;
    public int     enemigoExp = 0;
    public boolean enemigoCargando = false;
    /** Calumon vivo en el equipo: duplica atk/def/spd/wis aliados. */
    public boolean calumonDobla = false;
    /** PvP entre jugadores: sin penalización de derrota. */
    public boolean esPvP = false;
    public Set<EstadoAlterado> enemigosEstados = EnumSet.noneOf(EstadoAlterado.class);
    public int     turnosQuemado  = 0;
    public int     turnosVeneno   = 0;
    public int     turnosParalisis = 0;
    public int     turnosDormido  = 0;

    // ══ REFUERZO ═══════════════════════════════════════════════════════
    public String  refuerzoNombre    = "";
    public String  refuerzoElemento  = "NEUTRO";
    public int     refuerzoHp        = 0;
    public int     refuerzoMaxHp     = 0;
    public int     refuerzoAtk      = 0;
    public int     refuerzoDef      = 0;
    public int     refuerzoSpd      = 0;
    public int     refuerzoExp      = 0;
    public boolean refuerzoActivo    = false;
    public boolean refuerzoCargando = false;
    public boolean refuerzoAnunciado = false;
    public Set<EstadoAlterado> refuerzoEstados = EnumSet.noneOf(EstadoAlterado.class);
    public int     refTurnosQuemado  = 0;
    public int     refTurnosVeneno   = 0;
    public int     refTurnosParalisis = 0;
    public int     refTurnosDormido   = 0;

    // ══ HP DE COMBATE ═══════════════════════════════════════════════════
    public int hpCombateA = 0;
    public int hpCombateB = 0;
    public int hpCombateC = 0;
    public Set<EstadoAlterado> estadosA = EnumSet.noneOf(EstadoAlterado.class);
    public Set<EstadoAlterado> estadosB = EnumSet.noneOf(EstadoAlterado.class);
    public Set<EstadoAlterado> estadosC = EnumSet.noneOf(EstadoAlterado.class);
    public int turnosQuemadoA = 0, turnosVenenoA = 0, turnosParalisisA = 0, turnosDormidoA = 0;
    public int turnosQuemadoB = 0, turnosVenenoB = 0, turnosParalisisB = 0, turnosDormidoB = 0;
    public int turnosQuemadoC = 0, turnosVenenoC = 0, turnosParalisisC = 0, turnosDormidoC = 0;
    public int turnosAturdidoA = 0, turnosAturdidoB = 0, turnosAturdidoC = 0;

    // ══ ORDER POINTS ════════════════════════════════════════════════════
    public int opA = 0;
    public int opB = 0;
    public int opC = 0;
    public static final int OP_GUARD   = 10;
    public static final int OP_ATTACK  = 30;
    public static final int OP_SPECIAL = 150;
    public static final int OP_EXE     = 300;
    public static final int OP_SKILL_1 = 20;
    public static final int OP_SKILL_2 = 50;
    public static final int OP_SKILL_3 = 100;
    public static final int OP_SKILL_4 = 60;
    public static final int OP_SKILL_5 = 90;
    public static final int OP_SKILL_6 = 120;
    public static final int OP_SKILL_7 = 150;

    // ══ TÁCTICAS ════════════════════════════════════════════════════════
    public Tactica tacticaA = Tactica.EQUILIBRADO;
    public Tactica tacticaB = Tactica.EQUILIBRADO;
    public Tactica tacticaC = Tactica.EQUILIBRADO;

    // ══ ÓRDENES ACTIVAS ════════════════════════════════════════════════
    public Orden  ordenActiva      = Orden.NINGUNA;
    public boolean guardActivo_A   = false;
    public boolean guardActivo_B   = false;
    public boolean guardActivo_C   = false;
    public boolean attackOrden_A   = false;
    public boolean attackOrden_B   = false;
    public boolean attackOrden_C   = false;
    public boolean specialActivo_A = false;
    public boolean specialActivo_B = false;
    public boolean specialActivo_C = false;
    /** Multiplicador de técnica de daño pendiente para el lado A (0 = ninguna). */
    public double habMult_A = 0;
    public String habNombre_A = "";
    /** La técnica suicida se cobra tras impactar. */
    public boolean habSuicida_A = false;
    public boolean exeFusion       = false;
    public int     turnosExeRestantes = 0;

    // ══ SISTEMA DE GAUGE DE VELOCIDAD ══════════════════════════════════
    public static final int GAUGE_MAX = 1000;
    public int gaugeA = 0;
    public int gaugeB = 0;
    public int gaugeC = 0;
    public int gaugeEnemigo = 0;
    public int gaugeRefuerzo = 0;

    // ══ OLEADAS (1-5 salvajes por encuentro) ═════════════════════════════
    public int nivelZona = 1;
    public int totalOleada = 1;
    public int oleadaActual = 1;
    public int colaRestante = 0;

    // ══ ESTADO GENERAL ══════════════════════════════════════════════════
    public int  ronda      = 1;
    public Fase fase       = Fase.INICIATIVA;
    public boolean activa  = true;
    public boolean victoria = false;
    /** Lados del jugador en este combate (máx 3, solo RAM). */
    public java.util.List<com.digitale.datos.DatoDigimon> party = new java.util.ArrayList<>();
    /** Ref del salvaje objetivo (para puesta en escena, solo RAM). */
    public com.hypixel.hytale.component.Ref<?> objetivo = null;
    public int  expTotal   = 0;
    public int  danoTotalAcumulado = 0;
    public int  comboCount = 0;
    public int  comboMax   = 0;

    // ══ LOG DE COMBATE ═════════════════════════════════════════════════
    private final Deque<String> logLineas = new ArrayDeque<>(8);

    public void logear(String linea) {
        if (logLineas.size() >= 7) logLineas.pollFirst();
        logLineas.addLast(linea);
    }

    public String getLogFormateado() {
        return logLineas.isEmpty() ? "El combate comienza..." : String.join("\n", logLineas);
    }

    // ══ HELPERS DE HP ════════════════════════════════════════════════════
    public float pctHpA()       { return hpCombateA <= 0 ? 0f : 1f; }
    public float pctHpB()       { return hpCombateB <= 0 ? 0f : 1f; }
    public float pctHpC()       { return hpCombateC <= 0 ? 0f : 1f; }
    public float pctHpEnemigo() { return enemigoMaxHp  == 0 ? 0f : (float) enemigoHp  / enemigoMaxHp; }
    public float pctHpRefuerzo(){ return refuerzoMaxHp == 0 ? 0f : (float) refuerzoHp / refuerzoMaxHp; }

    public boolean aVivo()            { return hpCombateA > 0; }
    public boolean bVivo()            { return hpCombateB > 0; }
    public boolean cVivo()            { return hpCombateC > 0; }
    public boolean equipoVivo()        { return aVivo() || bVivo() || cVivo(); }
    public boolean enemigoVivo()       { return enemigoHp  > 0; }
    public boolean refuerzoVivo()       { return refuerzoActivo && refuerzoHp > 0; }
    public boolean todosEnemigosDerrota(){ return !enemigoVivo() && !refuerzoVivo(); }
    public int     opTotal()           { return opA + opB; }

    // ══ HELPERS DE ESTADOS ══════════════════════════════════════════════
    public boolean tieneEstadoA(EstadoAlterado e) { return estadosA.contains(e); }
    public boolean tieneEstadoB(EstadoAlterado e) { return estadosB.contains(e); }
    public boolean tieneEstadoC(EstadoAlterado e) { return estadosC.contains(e); }
    public boolean tieneEstadoEnemigo(EstadoAlterado e) { return enemigosEstados.contains(e); }
    public boolean tieneEstadoRefuerzo(EstadoAlterado e) { return refuerzoEstados.contains(e); }

    // ══ BARRAS VISUALES ══════════════════════════════════════════════════
    public static String barraHp(int hp, int max) {
        if (max <= 0) return "░░░░░░░░░░";
        int llenos = Math.max(0, Math.min(10, (int)((float) hp / max * 10)));
        return "█".repeat(llenos) + "░".repeat(10 - llenos);
    }

    public static String barraGauge(int gauge) {
        int llenos = Math.max(0, Math.min(10, (int)((float) gauge / GAUGE_MAX * 10)));
        return "▓".repeat(llenos) + "░".repeat(10 - llenos);
    }

    public static String barraOp(int op) {
        int llenos = Math.max(0, Math.min(10, (int)((float) op / OP_SPECIAL * 10)));
        return "▓".repeat(llenos) + "░".repeat(10 - llenos);
    }

    // ══ RESETEAR ÓRDENES ════════════════════════════════════════════════
    public void resetearOrdenes() {
        guardActivo_A  = false; guardActivo_B  = false; guardActivo_C  = false;
        attackOrden_A  = false; attackOrden_B  = false; attackOrden_C  = false;
        specialActivo_A = false; specialActivo_B = false; specialActivo_C = false;
        habMult_A = 0; habNombre_A = ""; habSuicida_A = false;
        exeFusion   = false;
        ordenActiva = Orden.NINGUNA;
    }

    // ══ PROCESAR ESTADOS ALTERADOS (daño residual por turno) ════════════
    public int danoResidualA() {
        int dano = 0;
        if (estadosA.contains(EstadoAlterado.QUEMADO)) {
            dano += Math.max(1, hpCombateA * 3 / 100);
            turnosQuemadoA--;
            if (turnosQuemadoA <= 0) estadosA.remove(EstadoAlterado.QUEMADO);
        }
        if (estadosA.contains(EstadoAlterado.VENENO) || estadosA.contains(EstadoAlterado.ENVENENADO_FUERTE)) {
            dano += estadosA.contains(EstadoAlterado.ENVENENADO_FUERTE)
                    ? Math.max(1, hpCombateA * 6 / 100)
                    : Math.max(1, hpCombateA * 3 / 100);
            turnosVenenoA--;
            if (turnosVenenoA <= 0) { estadosA.remove(EstadoAlterado.VENENO); estadosA.remove(EstadoAlterado.ENVENENADO_FUERTE); }
        }
        if (estadosA.contains(EstadoAlterado.SANGRANDO)) {
            dano += Math.max(1, hpCombateA * 2 / 100);
        }
        hpCombateA = Math.max(0, hpCombateA - dano);
        return dano;
    }

    public int danoResidualB() {
        int dano = 0;
        if (estadosB.contains(EstadoAlterado.QUEMADO)) {
            dano += Math.max(1, hpCombateB * 3 / 100);
            turnosQuemadoB--;
            if (turnosQuemadoB <= 0) estadosB.remove(EstadoAlterado.QUEMADO);
        }
        if (estadosB.contains(EstadoAlterado.VENENO) || estadosB.contains(EstadoAlterado.ENVENENADO_FUERTE)) {
            dano += estadosB.contains(EstadoAlterado.ENVENENADO_FUERTE)
                    ? Math.max(1, hpCombateB * 6 / 100)
                    : Math.max(1, hpCombateB * 3 / 100);
            turnosVenenoB--;
            if (turnosVenenoB <= 0) { estadosB.remove(EstadoAlterado.VENENO); estadosB.remove(EstadoAlterado.ENVENENADO_FUERTE); }
        }
        if (estadosB.contains(EstadoAlterado.SANGRANDO)) {
            dano += Math.max(1, hpCombateB * 2 / 100);
        }
        hpCombateB = Math.max(0, hpCombateB - dano);
        return dano;
    }

    public int danoResidualC() {
        int dano = 0;
        if (estadosC.contains(EstadoAlterado.QUEMADO)) {
            dano += Math.max(1, hpCombateC * 3 / 100);
            turnosQuemadoC--;
            if (turnosQuemadoC <= 0) estadosC.remove(EstadoAlterado.QUEMADO);
        }
        if (estadosC.contains(EstadoAlterado.VENENO) || estadosC.contains(EstadoAlterado.ENVENENADO_FUERTE)) {
            dano += estadosC.contains(EstadoAlterado.ENVENENADO_FUERTE)
                    ? Math.max(1, hpCombateC * 6 / 100)
                    : Math.max(1, hpCombateC * 3 / 100);
            turnosVenenoC--;
            if (turnosVenenoC <= 0) { estadosC.remove(EstadoAlterado.VENENO); estadosC.remove(EstadoAlterado.ENVENENADO_FUERTE); }
        }
        if (estadosC.contains(EstadoAlterado.SANGRANDO)) {
            dano += Math.max(1, hpCombateC * 2 / 100);
        }
        hpCombateC = Math.max(0, hpCombateC - dano);
        return dano;
    }

    public int danoResidualEnemigo() {
        int dano = 0;
        if (enemigosEstados.contains(EstadoAlterado.QUEMADO)) {
            dano += Math.max(1, enemigoHp * 3 / 100);
            turnosQuemado--;
            if (turnosQuemado <= 0) enemigosEstados.remove(EstadoAlterado.QUEMADO);
        }
        if (enemigosEstados.contains(EstadoAlterado.VENENO) || enemigosEstados.contains(EstadoAlterado.ENVENENADO_FUERTE)) {
            dano += enemigosEstados.contains(EstadoAlterado.ENVENENADO_FUERTE)
                    ? Math.max(1, enemigoHp * 6 / 100)
                    : Math.max(1, enemigoHp * 3 / 100);
            turnosVeneno--;
            if (turnosVeneno <= 0) { enemigosEstados.remove(EstadoAlterado.VENENO); enemigosEstados.remove(EstadoAlterado.ENVENENADO_FUERTE); }
        }
        if (enemigosEstados.contains(EstadoAlterado.SANGRANDO)) {
            dano += Math.max(1, enemigoHp * 2 / 100);
        }
        enemigoHp = Math.max(0, enemigoHp - dano);
        return dano;
    }

    public int danoResidualRefuerzo() {
        if (!refuerzoActivo) return 0;
        int dano = 0;
        if (refuerzoEstados.contains(EstadoAlterado.QUEMADO)) {
            dano += Math.max(1, refuerzoHp * 3 / 100);
            refTurnosQuemado--;
            if (refTurnosQuemado <= 0) refuerzoEstados.remove(EstadoAlterado.QUEMADO);
        }
        if (refuerzoEstados.contains(EstadoAlterado.VENENO) || refuerzoEstados.contains(EstadoAlterado.ENVENENADO_FUERTE)) {
            dano += refuerzoEstados.contains(EstadoAlterado.ENVENENADO_FUERTE)
                    ? Math.max(1, refuerzoHp * 6 / 100)
                    : Math.max(1, refuerzoHp * 3 / 100);
            refTurnosVeneno--;
            if (refTurnosVeneno <= 0) { refuerzoEstados.remove(EstadoAlterado.VENENO); refuerzoEstados.remove(EstadoAlterado.ENVENENADO_FUERTE); }
        }
        if (refuerzoEstados.contains(EstadoAlterado.SANGRANDO)) {
            dano += Math.max(1, refuerzoHp * 2 / 100);
        }
        refuerzoHp = Math.max(0, refuerzoHp - dano);
        return dano;
    }

    // ══ DESPERTAR (al final del turno del que-duerme) ══════════════════
    public void verificarDespertar() {
        if (estadosA.contains(EstadoAlterado.DORMIDO)) {
            if (turnosDormidoA <= 0 || Math.random() < 0.3) {
                estadosA.remove(EstadoAlterado.DORMIDO);
                logear("A desperto!");
            } else {
                turnosDormidoA--;
            }
        }
        if (estadosB.contains(EstadoAlterado.DORMIDO)) {
            if (turnosDormidoB <= 0 || Math.random() < 0.3) {
                estadosB.remove(EstadoAlterado.DORMIDO);
                logear("B desperto!");
            } else {
                turnosDormidoB--;
            }
        }
        if (estadosC.contains(EstadoAlterado.DORMIDO)) {
            if (turnosDormidoC <= 0 || Math.random() < 0.3) {
                estadosC.remove(EstadoAlterado.DORMIDO);
                logear("C desperto!");
            } else {
                turnosDormidoC--;
            }
        }
    }

    public void resolverParalisis(String nombre, Set<EstadoAlterado> estados, Runnable fallback) {
        if (estados.contains(EstadoAlterado.PARALISIS)) {
            if (Math.random() < 0.25) {
                return;
            }
        }
        fallback.run();
    }
}
