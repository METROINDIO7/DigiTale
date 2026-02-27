package com.digitale.sistema;

import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EnemigoCombate;
import com.digitale.datos.EstadoCombate;
import com.digitale.datos.EstadoCombate.Fase;
import com.digitale.datos.EstadoCombate.Orden;
import com.digitale.datos.EstadoCombate.Tactica;

import java.util.Random;

/**
 * Motor de combate estilo Digimon World: Next Order.
 *
 * Ambos Digimon luchan AUTOMÁTICAMENTE según su táctica.
 * El jugador es el TAMER: acumula OP (Order Points) con SUPPORT
 * y los gasta en el Order Ring para dar órdenes estratégicas.
 *
 * Flujo por ronda:
 *   1. resolverRonda() → auto-combate (A auto, B auto, Enemigo auto)
 *   2. El tamer recibe OP por pulsar SUPPORT (simulado en UI)
 *   3. Opcional: tamer abre Order Ring y da una orden para la siguiente ronda
 *   4. Vuelve a resolverRonda()
 */
public class SistemaBatalla {

    private static final Random RNG = new Random();

    // ── OP que genera un SUPPORT bien ejecutado ───────────────────────
    private static final int OP_POR_SUPPORT = 22;

    // ══ API pública ════════════════════════════════════════════════════

    /** Inicia un nuevo combate. */
    public static EstadoCombate iniciarCombate(DatoDigimon a, DatoDigimon b) {
        int nivelProm = promedioNivel(a, b);
        EstadoCombate e = EnemigoCombate.generarCombate(nivelProm);
        e.hpCombateA = (a != null && a.vivo) ? a.maxHp : 0;
        e.hpCombateB = (b != null && b.vivo) ? b.maxHp : 0;
        e.opA = 0;
        e.opB = 0;
        e.fase   = Fase.RESOLVIENDO;
        e.ronda  = 1;
        e.activa = true;
        return e;
    }

    /**
     * El tamer pulsó SUPPORT — genera OP y resuelve la ronda automáticamente.
     * Esto simula el "animar en el momento preciso" de Next Order.
     */
    public static void supportYResolver(EstadoCombate e, DatoDigimon a, DatoDigimon b) {
        if (!e.activa || e.fase == Fase.FIN_COMBATE) return;

        // Generar OP (bonus +5 si Digimon tiene lazo alto)
        int bonusA = (a != null && a.lazo >= 70) ? 5 : 0;
        int bonusB = (b != null && b.lazo >= 70) ? 5 : 0;
        e.opA = Math.min(300, e.opA + OP_POR_SUPPORT + bonusA);
        e.opB = Math.min(300, e.opB + OP_POR_SUPPORT + bonusB);

        e.logear("SUPPORT! +" + (OP_POR_SUPPORT + bonusA) + " OP para A, +" + (OP_POR_SUPPORT + bonusB) + " OP para B");

        // Resolver la ronda con las órdenes activas
        resolverRonda(e, a, b);
    }

    /**
     * Aplica una orden del Order Ring y cierra el ring.
     * La orden se activa en la siguiente ronda.
     */
    public static boolean darOrden(EstadoCombate e, DatoDigimon a, DatoDigimon b, Orden orden) {
        switch (orden) {
            case GUARD_A -> {
                if (e.opA < EstadoCombate.OP_GUARD) return false;
                e.opA -= EstadoCombate.OP_GUARD;
                e.guardActivo_A = true;
                e.logear("[ORDER] Guard en " + (a != null ? a.nombre : "A"));
            }
            case GUARD_B -> {
                if (e.opB < EstadoCombate.OP_GUARD) return false;
                e.opB -= EstadoCombate.OP_GUARD;
                e.guardActivo_B = true;
                e.logear("[ORDER] Guard en " + (b != null ? b.nombre : "B"));
            }
            case ATTACK_A -> {
                if (e.opA < EstadoCombate.OP_ATTACK) return false;
                e.opA -= EstadoCombate.OP_ATTACK;
                e.attackOrden_A = true;
                e.logear("[ORDER] " + (a != null ? a.nombre : "A") + " usara su mejor ataque!");
            }
            case ATTACK_B -> {
                if (e.opB < EstadoCombate.OP_ATTACK) return false;
                e.opB -= EstadoCombate.OP_ATTACK;
                e.attackOrden_B = true;
                e.logear("[ORDER] " + (b != null ? b.nombre : "B") + " usara su mejor ataque!");
            }
            case SPECIAL_A -> {
                if (e.opA < EstadoCombate.OP_SPECIAL) return false;
                e.opA -= EstadoCombate.OP_SPECIAL;
                e.specialActivo_A = true;
                e.logear("[SPECIAL] " + (a != null ? a.nombre : "A") + " cargando movimiento especial!!");
            }
            case SPECIAL_B -> {
                if (e.opB < EstadoCombate.OP_SPECIAL) return false;
                e.opB -= EstadoCombate.OP_SPECIAL;
                e.specialActivo_B = true;
                e.logear("[SPECIAL] " + (b != null ? b.nombre : "B") + " cargando movimiento especial!!");
            }
            case EXE_FUSION -> {
                if (e.opTotal() < EstadoCombate.OP_EXE) return false;
                int lazoA = a != null ? a.lazo : 0;
                int lazoB = b != null ? b.lazo : 0;
                if (lazoA < 60 || lazoB < 60) return false; // requiere vínculo
                e.opA = 0; e.opB = 0;
                e.exeFusion = true;
                e.turnosExeRestantes = 3;
                e.logear("[ExE] FUSION ACTIVADA! Maxima potencia por 3 rondas!");
            }
            default -> { return false; }
        }
        e.fase = Fase.RESOLVIENDO; // cerrar el ring, volver a combate
        return true;
    }

    /** Intenta huir. */
    public static boolean intentarHuir(EstadoCombate e, DatoDigimon a, DatoDigimon b) {
        int spdMax = Math.max(a != null ? a.spd : 0, b != null ? b.spd : 0);
        int prob   = Math.max(15, Math.min(85, 45 + (spdMax - e.enemigoSpd) * 3));
        if (RNG.nextInt(100) < prob) {
            e.logear("Escapaste del combate!");
            e.activa = false; e.fase = Fase.FIN_COMBATE; e.victoria = false;
            return true;
        }
        e.logear("No pudiste escapar! El enemigo contraataca...");
        // El enemigo da un golpe gratis
        atacarEnemigo(e, a, b);
        verificarFin(e, a, b);
        e.ronda++;
        return false;
    }

    // ══ Resolución de ronda ════════════════════════════════════════════

    private static void resolverRonda(EstadoCombate e, DatoDigimon a, DatoDigimon b) {

        // ── ExE Fusion: daño masivo de ambos ─────────────────────────
        if (e.exeFusion && e.turnosExeRestantes > 0) {
            int danoExe = calcDanoExe(a, b, e);
            e.enemigoHp = Math.max(0, e.enemigoHp - danoExe);
            e.logear("[ExE] Fusion ataco por " + danoExe + " DMG MASIVO!!");
            e.turnosExeRestantes--;
            if (e.turnosExeRestantes == 0) {
                e.exeFusion = false;
                e.logear("[ExE] Fusion terminada.");
            }
        } else {
            // ── Ataques normales de los Digimon ───────────────────────
            if (e.aVivo() && a != null) atacarDigimon(e, a, b, true);
            if (e.bVivo() && b != null) atacarDigimon(e, a, b, false);
        }

        if (verificarFin(e, a, b)) return;

        // ── Ataque del enemigo ────────────────────────────────────────
        manejarEnemigo(e, a, b);

        // ── Fin de ronda ──────────────────────────────────────────────
        e.resetearOrdenes();
        verificarFin(e, a, b);
        if (e.activa) e.ronda++;
    }

    // ── Ataque automático de un Digimon (auto según táctica) ──────────
    private static void atacarDigimon(EstadoCombate e, DatoDigimon d, DatoDigimon otro,
                                       boolean esA) {

        boolean guard   = esA ? e.guardActivo_A   : e.guardActivo_B;
        boolean attack  = esA ? e.attackOrden_A   : e.attackOrden_B;
        boolean special = esA ? e.specialActivo_A : e.specialActivo_B;
        Tactica tactica = esA ? e.tacticaA : e.tacticaB;
        int hpD         = esA ? e.hpCombateA : e.hpCombateB;

        // Guard: no ataca, recupera algo de HP
        if (guard) {
            int recupera = Math.max(2, d.maxHp / 15);
            if (esA) e.hpCombateA = Math.min(d.maxHp, e.hpCombateA + recupera);
            else     e.hpCombateB = Math.min(d.maxHp, e.hpCombateB + recupera);
            e.logear(d.nombre + " en guardia. (+" + recupera + " HP)");
            return;
        }

        // Elegir multiplicador de ataque según táctica y orden
        double mult;
        String tipoAtaque;
        if (special) {
            mult = 3.5; tipoAtaque = "ESPECIAL";
        } else if (attack) {
            mult = 2.0; tipoAtaque = "ORDENADO";
        } else {
            // Auto-táctica
            boolean usaHabilidad = d.energia >= 30 && (tactica == Tactica.AGRESIVO
                    || (tactica == Tactica.EQUILIBRADO && RNG.nextInt(3) == 0));
            if (usaHabilidad) {
                d.energia = Math.max(0, d.energia - 25);
                mult = 1.6; tipoAtaque = "HABILIDAD";
            } else {
                // Agresivo ataca fuerte, defensivo ataca menos
                mult = switch (tactica) {
                    case AGRESIVO  -> 1.2 + RNG.nextDouble() * 0.3;
                    case DEFENSIVO -> 0.7 + RNG.nextDouble() * 0.2;
                    default        -> 0.9 + RNG.nextDouble() * 0.3;
                };
                tipoAtaque = "ATAQUE";
            }
        }

        int dano = calcDano(d.atk, e.enemigoDef, d.lazo, d.elemento, e.enemigoElemento, mult);
        e.enemigoHp = Math.max(0, e.enemigoHp - dano);
        e.logear(d.nombre + " [" + tipoAtaque + "] -> " + dano + " dmg");
    }

    // ── IA del enemigo ────────────────────────────────────────────────
    private static void manejarEnemigo(EstadoCombate e, DatoDigimon a, DatoDigimon b) {
        boolean enraged = e.pctHpEnemigo() < 0.30f;

        // Cargar telegrafía → siguiente ronda ataque doble
        if (e.enemigoCargando) {
            e.enemigoCargando = false;
            atacarEnemigo(e, a, b, 2.2);
            return;
        }

        int roll = RNG.nextInt(100);
        if (enraged) {
            if (roll < 20) { e.enemigoCargando = true; e.logear("!" + e.enemigoNombre + " SE ESTA CARGANDO!"); }
            else             atacarEnemigo(e, a, b, 1.0 + RNG.nextDouble() * 0.4);
        } else {
            if (roll < 8)  { e.enemigoCargando = true; e.logear("!" + e.enemigoNombre + " se esta cargando!"); }
            else if (roll < 20) { /* defiende — pasa turno */ e.logear(e.enemigoNombre + " se pone en guardia."); }
            else                  atacarEnemigo(e, a, b, 1.0 + RNG.nextDouble() * 0.2);
        }
    }

    private static void atacarEnemigo(EstadoCombate e, DatoDigimon a, DatoDigimon b) {
        atacarEnemigo(e, a, b, 1.0);
    }

    private static void atacarEnemigo(EstadoCombate e, DatoDigimon a, DatoDigimon b, double multExtra) {
        // Elige objetivo: el más débil, o el que no está en guard
        boolean atacaA;
        if (!e.aVivo())       atacaA = false;
        else if (!e.bVivo())  atacaA = true;
        else {
            // Prefiere al más débil en HP %
            float pctA = e.pctHpA(a);
            float pctB = e.pctHpB(b);
            atacaA = pctA <= pctB;
        }

        DatoDigimon objetivo = atacaA ? a : b;
        if (objetivo == null) return;

        boolean guardado  = atacaA ? e.guardActivo_A : e.guardActivo_B;
        double multGuard  = guardado ? 0.4 : 1.0;
        double multDisc   = 1.0 - (objetivo.disciplina / 250.0);

        int dano = calcDano(e.enemigoAtk, objetivo.def, 60,
                            e.enemigoElemento, objetivo.elemento,
                            multExtra * multGuard * multDisc);

        if (atacaA) e.hpCombateA = Math.max(0, e.hpCombateA - dano);
        else        e.hpCombateB = Math.max(0, e.hpCombateB - dano);

        String sufijo = multExtra > 1.5 ? " (GOLPE CARGADO!!)" : "";
        e.logear(e.enemigoNombre + " -> " + objetivo.nombre + " " + dano + " dmg" + sufijo);
    }

    // ── Verificación de fin ────────────────────────────────────────────
    private static boolean verificarFin(EstadoCombate e, DatoDigimon a, DatoDigimon b) {
        if (e.enemigoHp <= 0) {
            e.logear(e.enemigoNombre + " fue derrotado! VICTORIA!");
            e.victoria = true; e.activa = false; e.fase = Fase.FIN_COMBATE;
            // Recompensar supervivientes
            if (a != null && e.hpCombateA > 0) { a.victorias++; a.lazo = Math.min(100, a.lazo+3); a.abi = Math.min(99, a.abi+1); a.hpCombate = e.hpCombateA; }
            if (b != null && e.hpCombateB > 0) { b.victorias++; b.lazo = Math.min(100, b.lazo+3); b.abi = Math.min(99, b.abi+1); b.hpCombate = e.hpCombateB; }
            return true;
        }
        if (!e.equipoVivo()) {
            e.logear("Todo el equipo fue derrotado...");
            e.victoria = false; e.activa = false; e.fase = Fase.FIN_COMBATE;
            if (a != null) { a.disciplina = Math.max(0, a.disciplina-5); a.hpCombate = 1; }
            if (b != null) { b.disciplina = Math.max(0, b.disciplina-5); b.hpCombate = 1; }
            return true;
        }
        if (!e.aVivo() && a != null) { e.logear(a.nombre + " cayo en combate..."); }
        if (!e.bVivo() && b != null) { e.logear(b.nombre + " cayo en combate..."); }
        return false;
    }

    // ══ Cálculo de daño ═══════════════════════════════════════════════

    private static int calcDano(int atk, int def, int lazo,
                                  String atkElem, String defElem, double mult) {
        double multLazo = 0.70 + (lazo / 100.0) * 0.60;
        double multElem = EnemigoCombate.multElemental(atkElem, defElem);
        int base = Math.max(1, atk - def / 2);
        return Math.max(1, (int)(base * multLazo * multElem * mult));
    }

    private static int calcDanoExe(DatoDigimon a, DatoDigimon b, EstadoCombate e) {
        int atkA = a != null ? a.atk : 0;
        int atkB = b != null ? b.atk : 0;
        int lazo = a != null ? a.lazo : 50;
        // ExE: combinación de ambos ATK × 4, ignora la mitad de la DEF
        int base = Math.max(1, (atkA + atkB) * 2 - e.enemigoDef / 3);
        double multLazo = 0.85 + (lazo / 100.0) * 0.30;
        return Math.max(1, (int)(base * multLazo));
    }

    // ══ Helpers ═══════════════════════════════════════════════════════
    private static int promedioNivel(DatoDigimon a, DatoDigimon b) {
        int sum = 0, count = 0;
        if (a != null) { sum += a.nivel; count++; }
        if (b != null) { sum += b.nivel; count++; }
        return count > 0 ? sum / count : 2;
    }
}
