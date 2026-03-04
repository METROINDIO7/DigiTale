package com.digitale.sistema;

import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EnemigoCombate;
import com.digitale.datos.EstadoCombate;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SistemaBatalla {

    private static final Logger LOGGER = Logger.getLogger(SistemaBatalla.class.getName());
    private static final Random RNG = new Random();

    private static final float PCT_HP_REFUERZO = 0.60f;
    private static final int RONDA_REFUERZO = 4;
    private static final int OP_POR_SUPPORT = 25;

    public static EstadoCombate iniciarCombate(DatoDigimon a, DatoDigimon b) {
        int nivel = Math.max(a != null ? a.nivel : 1, b != null ? b.nivel : 1);
        EstadoCombate c = EnemigoCombate.generarCombate(nivel);
        c.hpCombateA = a != null ? a.maxHp : 0;
        c.hpCombateB = b != null ? b.maxHp : 0;
        cargarRefuerzo(c, nivel);
        LOGGER.log(Level.INFO, "Combate vs " + c.enemigoNombre + " | refuerzo: " + c.refuerzoNombre);
        return c;
    }

    private static void cargarRefuerzo(EstadoCombate c, int nivel) {
        EstadoCombate tmp = EnemigoCombate.generarCombate(nivel);
        c.refuerzoNombre = tmp.enemigoNombre;
        c.refuerzoElemento = tmp.enemigoElemento;
        c.refuerzoMaxHp = Math.max(1, (int)(tmp.enemigoMaxHp * 0.70f));
        c.refuerzoHp = c.refuerzoMaxHp;
        c.refuerzoAtk = Math.max(1, (int)(tmp.enemigoAtk * 0.70f));
        c.refuerzoDef = Math.max(1, (int)(tmp.enemigoDef * 0.70f));
        c.refuerzoSpd = Math.max(1, (int)(tmp.enemigoSpd * 0.70f));
        c.refuerzoExp = (int)(tmp.enemigoExp * 0.50f);
    }

    public static void support(EstadoCombate c, DatoDigimon a, DatoDigimon b) {
        if (!c.activa || c.fase == EstadoCombate.Fase.FIN_COMBATE) return;
        if (c.aVivo()) c.opA = Math.min(EstadoCombate.OP_SPECIAL, c.opA + OP_POR_SUPPORT);
        if (c.bVivo()) c.opB = Math.min(EstadoCombate.OP_SPECIAL, c.opB + OP_POR_SUPPORT);
        c.logear("SUPPORT! OP+" + OP_POR_SUPPORT + " [A:" + c.opA + " B:" + c.opB + "]");
        resolverRonda(c, a, b);
    }

    public static boolean ejecutarOrden(EstadoCombate c, EstadoCombate.Orden orden, DatoDigimon a, DatoDigimon b) {
        if (!c.activa || c.fase == EstadoCombate.Fase.FIN_COMBATE) return false;
        String nomA = a != null ? a.nombre : "A";
        String nomB = b != null ? b.nombre : "B";
        switch (orden) {
            case GUARD_A -> { if (c.opA < EstadoCombate.OP_GUARD) return false; c.opA -= EstadoCombate.OP_GUARD; c.guardActivo_A = true; c.logear(nomA + " se pone en guardia"); }
            case GUARD_B -> { if (c.opB < EstadoCombate.OP_GUARD) return false; c.opB -= EstadoCombate.OP_GUARD; c.guardActivo_B = true; c.logear(nomB + " se pone en guardia"); }
            case ATTACK_A -> { if (c.opA < EstadoCombate.OP_ATTACK) return false; c.opA -= EstadoCombate.OP_ATTACK; c.attackOrden_A = true; c.logear(nomA + " ataca con fuerza!"); }
            case ATTACK_B -> { if (c.opB < EstadoCombate.OP_ATTACK) return false; c.opB -= EstadoCombate.OP_ATTACK; c.attackOrden_B = true; c.logear(nomB + " ataca con fuerza!"); }
            case SPECIAL_A -> { if (c.opA < EstadoCombate.OP_SPECIAL) return false; c.opA -= EstadoCombate.OP_SPECIAL; c.specialActivo_A = true; c.logear("ESPECIAL de " + nomA + "!"); }
            case SPECIAL_B -> { if (c.opB < EstadoCombate.OP_SPECIAL) return false; c.opB -= EstadoCombate.OP_SPECIAL; c.specialActivo_B = true; c.logear("ESPECIAL de " + nomB + "!"); }
            case EXE_FUSION -> {
                if (c.opTotal() < EstadoCombate.OP_EXE) return false;
                if (a != null && a.lazo < 70) { c.logear("Lazo insuficiente para ExE Fusion"); return false; }
                c.exeFusion = true; c.turnosExeRestantes = 2; c.opA = 0; c.opB = 0;
                c.logear("ExE FUSION!! " + nomA + " + " + nomB + " se fusionan!");
            }
            default -> { return false; }
        }
        c.ordenActiva = orden;
        resolverRonda(c, a, b);
        return true;
    }

    public static boolean huir(EstadoCombate c, DatoDigimon a, DatoDigimon b) {
        if (!c.activa) return false;
        int spdEq = Math.max(a != null ? a.spd : 0, b != null ? b.spd : 0);
        int spdEn = c.enemigoSpd + (c.refuerzoActivo ? c.refuerzoSpd : 0);
        double prob = 0.30 + (spdEq / (double) Math.max(1, spdEq + spdEn)) * 0.50;
        if (RNG.nextDouble() < prob) {
            c.activa = false; c.victoria = false;
            c.fase = EstadoCombate.Fase.FIN_COMBATE;
            c.logear("Lograste escapar!");
            return true;
        } else {
            c.logear("No pudiste huir! Los enemigos atacan!");
            if (c.enemigoVivo()) enemigoAtaca(c, a, b, false);
            if (c.refuerzoVivo()) enemigoAtaca(c, a, b, true);
            return false;
        }
    }

    public static void resolverRonda(EstadoCombate c, DatoDigimon a, DatoDigimon b) {
        if (!c.activa || c.fase == EstadoCombate.Fase.FIN_COMBATE) return;

        atacarConDigimon(c, a, true);
        atacarConDigimon(c, b, false);

        if (c.turnosExeRestantes > 0) c.turnosExeRestantes--;

        comprobarRefuerzo(c);

        if (c.enemigoVivo()) enemigoAtaca(c, a, b, false);
        if (c.refuerzoVivo()) enemigoAtaca(c, a, b, true);

        c.ronda++;
        c.resetearOrdenes();

        if (c.todosEnemigosDerrota()) {
            c.victoria = true; c.activa = false;
            c.fase = EstadoCombate.Fase.FIN_COMBATE;
            c.expTotal = c.enemigoExp + (c.refuerzoActivo ? c.refuerzoExp : 0);
            c.logear("VICTORIA! +" + c.expTotal + " XP");
            aplicarVictoria(c, a, b);
        } else if (!c.equipoVivo()) {
            c.victoria = false; c.activa = false;
            c.fase = EstadoCombate.Fase.FIN_COMBATE;
            c.logear("Derrota... el equipo cayo!");
            aplicarDerrota(a, b);
        } else {
            c.fase = EstadoCombate.Fase.RESOLVIENDO;
        }
    }

    private static void atacarConDigimon(EstadoCombate c, DatoDigimon d, boolean esA) {
        if (d == null) return;
        if ((esA ? c.hpCombateA : c.hpCombateB) <= 0) return;

        boolean special = esA ? c.specialActivo_A : c.specialActivo_B;
        boolean forced  = esA ? c.attackOrden_A   : c.attackOrden_B;

        boolean atacaRef = c.refuerzoVivo() && (!c.enemigoVivo() || c.pctHpRefuerzo() < c.pctHpEnemigo());
        int defTarget    = atacaRef ? c.refuerzoDef      : c.enemigoDef;
        String elemTgt   = atacaRef ? c.refuerzoElemento : c.enemigoElemento;
        String nomTgt    = atacaRef ? c.refuerzoNombre   : c.enemigoNombre;

        double mult = EnemigoCombate.multElemental(d.elemento, elemTgt);
        int base;
        if (c.exeFusion && c.turnosExeRestantes > 0) base = (int)(d.atk * 3.0 * mult);
        else if (special) base = (int)(d.atk * 2.2 * mult);
        else if (forced)  base = (int)(d.atk * 1.5 * mult);
        else {
            double ratio = switch (esA ? c.tacticaA : c.tacticaB) {
                case AGRESIVO -> 1.3; case DEFENSIVO -> 0.75; default -> 1.0;
            };
            base = (int)(Math.max(1, d.atk - defTarget / 2) * ratio * mult);
        }
        int dano = Math.max(1, (int)(base * (0.9 + RNG.nextDouble() * 0.2)));
        if (atacaRef) c.refuerzoHp = Math.max(0, c.refuerzoHp - dano);
        else          c.enemigoHp  = Math.max(0, c.enemigoHp  - dano);

        String tag = special ? "[ESP]" : forced ? "[ORD]" : c.exeFusion ? "[FUS]" : "";
        c.logear(d.nombre + " > " + nomTgt + " -" + dano + " " + tag);
    }

    private static void enemigoAtaca(EstadoCombate c, DatoDigimon a, DatoDigimon b, boolean esRef) {
        int atk    = esRef ? c.refuerzoAtk      : c.enemigoAtk;
        String nom = esRef ? c.refuerzoNombre   : c.enemigoNombre;
        String elem = esRef ? c.refuerzoElemento : c.enemigoElemento;
        boolean fuerte = (c.ronda % 3 == 0);
        if (esRef) c.refuerzoCargando = fuerte; else c.enemigoCargando = fuerte;

        boolean atacaA = (b == null || c.hpCombateB <= 0)
                || (a != null && c.hpCombateA > 0
                && (float) c.hpCombateA / Math.max(1, a.maxHp)
                <= (float) c.hpCombateB / Math.max(1, b.maxHp));

        DatoDigimon obj = atacaA ? a : b;
        if (obj == null) return;

        boolean guard = atacaA ? c.guardActivo_A : c.guardActivo_B;
        double multE  = EnemigoCombate.multElemental(elem, obj.elemento);
        int dano = Math.max(1, (int)(Math.max(1, atk - obj.def / 2) * multE
                * (guard ? 0.5 : 1.0) * (fuerte ? 1.8 : 1.0)
                * (0.9 + RNG.nextDouble() * 0.2)));

        if (atacaA) c.hpCombateA = Math.max(0, c.hpCombateA - dano);
        else        c.hpCombateB = Math.max(0, c.hpCombateB - dano);

        c.logear(nom + " > " + obj.nombre + " -" + dano + (fuerte ? " [!]" : "") + (guard ? "(blq)" : ""));
    }

    private static void comprobarRefuerzo(EstadoCombate c) {
        if (c.refuerzoActivo || c.refuerzoMaxHp == 0) return;
        if (c.pctHpEnemigo() <= PCT_HP_REFUERZO || c.ronda >= RONDA_REFUERZO) {
            c.refuerzoActivo = true;
            c.refuerzoAnunciado = false;
            c.logear("Un " + c.refuerzoNombre + " salvaje aparece como refuerzo!");
        }
    }

    private static void aplicarVictoria(EstadoCombate c, DatoDigimon a, DatoDigimon b) {
        if (a != null && c.aVivo()) { a.victorias++; a.lazo = Math.min(100, a.lazo + 2); a.hp = c.hpCombateA; a.energia = Math.max(10, a.energia - 20); a.abi = Math.min(100, a.abi + 1); }
        if (b != null && c.bVivo()) { b.victorias++; b.lazo = Math.min(100, b.lazo + 2); b.hp = c.hpCombateB; b.energia = Math.max(10, b.energia - 20); b.abi = Math.min(100, b.abi + 1); }
    }

    public static void aplicarDerrota(DatoDigimon a, DatoDigimon b) {
        if (a != null) { a.derrotas++; a.energia = Math.max(5, a.energia - 30); a.lazo = Math.max(0, a.lazo - 3); }
        if (b != null) { b.derrotas++; b.energia = Math.max(5, b.energia - 30); b.lazo = Math.max(0, b.lazo - 3); }
    }
}