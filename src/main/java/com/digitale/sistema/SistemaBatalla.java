package com.digitale.sistema;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EnemigoCombate;
import com.digitale.datos.EstadoCombate;
import com.digitale.datos.EstadoCombate.EstadoAlterado;
import com.digitale.datos.EstadoCombate.Tactica;
import com.digitale.datos.EstadoCombate.Orden;
import com.digitale.datos.EstadoCombate.TipoDano;
import com.digitale.datos.EstadoCombate.Fase;

import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SistemaBatalla {

    private static final Logger LOGGER = Logger.getLogger(SistemaBatalla.class.getName());
    private static final Random RNG = new Random();

    private static final float PCT_HP_REFUERZO = 0.65f;
    private static final int RONDA_REFUERZO = 3;
    private static final int OP_POR_SUPPORT = 20;
    private static final int OP_POR_GUARD   = 5;

    public static int getOpPorSupport() { return OP_POR_SUPPORT; }

    private static final double PCT_CRIT_BASE    = 0.05;
    private static final double PCT_CRIT_WIS_SCALE = 0.004;
    private static final double PCT_MUY_CRIT_BASE = 0.01;
    private static final double PCT_MUY_CRIT_WIS_SCALE = 0.002;

    private static final double PCT_FALLO_BASE   = 0.05;
    private static final double PCT_FALLO_DEF_SCALE = 0.002;

    public static EstadoCombate iniciarCombate(DatoDigimon a, DatoDigimon b) {
        int nivel = Math.max(a != null ? a.nivel : 1, b != null ? b.nivel : 1);
        EstadoCombate c = EnemigoCombate.generarCombate(nivel);
        c.hpCombateA = a != null ? a.maxHp : 0;
        c.hpCombateB = b != null ? b.maxHp : 0;

        inicializarGauge(c, a, b);
        cargarRefuerzo(c, nivel);
        LOGGER.log(Level.INFO, "Combate vs " + c.enemigoNombre + " | gaugeA:" + c.gaugeA + " gaugeB:" + c.gaugeB + " gaugeE:" + c.gaugeEnemigo);
        return c;
    }

    private static void inicializarGauge(EstadoCombate c, DatoDigimon a, DatoDigimon b) {
        int spdA = a != null ? a.spd : 5;
        int spdB = b != null ? b.spd : 5;
        int spdE = c.enemigoSpd;
        int spdR = c.refuerzoSpd;

        c.gaugeA = EstadoCombate.GAUGE_MAX - spdA * 8 + RNG.nextInt(16);
        c.gaugeB = EstadoCombate.GAUGE_MAX - spdB * 8 + RNG.nextInt(16);
        c.gaugeEnemigo = EstadoCombate.GAUGE_MAX - spdE * 8 + RNG.nextInt(16);
        c.gaugeRefuerzo = EstadoCombate.GAUGE_MAX - spdR * 8 + RNG.nextInt(16);

        c.gaugeA = Math.max(0, Math.min(EstadoCombate.GAUGE_MAX, c.gaugeA));
        c.gaugeB = Math.max(0, Math.min(EstadoCombate.GAUGE_MAX, c.gaugeB));
        c.gaugeEnemigo = Math.max(0, Math.min(EstadoCombate.GAUGE_MAX, c.gaugeEnemigo));
        c.gaugeRefuerzo = Math.max(0, Math.min(EstadoCombate.GAUGE_MAX, c.gaugeRefuerzo));
    }

    private static void cargarRefuerzo(EstadoCombate c, int nivel) {
        EstadoCombate tmp = EnemigoCombate.generarCombate(nivel);
        c.refuerzoNombre   = tmp.enemigoNombre;
        c.refuerzoElemento = tmp.enemigoElemento;
        c.refuerzoMaxHp    = Math.max(1, (int)(tmp.enemigoMaxHp * 0.65f));
        c.refuerzoHp       = c.refuerzoMaxHp;
        c.refuerzoAtk      = Math.max(1, (int)(tmp.enemigoAtk * 0.65f));
        c.refuerzoDef      = Math.max(1, (int)(tmp.enemigoDef * 0.65f));
        c.refuerzoSpd      = Math.max(1, (int)(tmp.enemigoSpd * 0.65f));
        c.refuerzoExp      = (int)(tmp.enemigoExp * 0.50f);
    }

    public static void support(EstadoCombate c, DatoDigimon a, DatoDigimon b) {
        if (!c.activa || c.fase == Fase.FIN_COMBATE) return;
        if (c.aVivo()) c.opA = Math.min(EstadoCombate.OP_SPECIAL, c.opA + OP_POR_SUPPORT);
        if (c.bVivo()) c.opB = Math.min(EstadoCombate.OP_SPECIAL, c.opB + OP_POR_SUPPORT);

        c.fase = Fase.RESOLVIENDO;
        procesarGauge(c, a, b);
    }

    public static boolean ejecutarOrden(EstadoCombate c, Orden orden, DatoDigimon a, DatoDigimon b) {
        if (!c.activa || c.fase == Fase.FIN_COMBATE) return false;
        String nomA = a != null ? a.nombre : "A";
        String nomB = b != null ? b.nombre : "B";
        boolean exito = true;

        switch (orden) {
            case GUARD_A  -> { if (c.opA < EstadoCombate.OP_GUARD) return false; c.opA -= EstadoCombate.OP_GUARD; c.guardActivo_A = true; c.opA += OP_POR_GUARD; c.logear("[G] " + nomA + " se defiende (+" + OP_POR_GUARD + " OP)"); }
            case GUARD_B  -> { if (c.opB < EstadoCombate.OP_GUARD) return false; c.opB -= EstadoCombate.OP_GUARD; c.guardActivo_B = true; c.opB += OP_POR_GUARD; c.logear("[G] " + nomB + " se defiende (+" + OP_POR_GUARD + " OP)"); }
            case ATTACK_A -> { if (c.opA < EstadoCombate.OP_ATTACK) return false; c.opA -= EstadoCombate.OP_ATTACK; c.attackOrden_A = true; c.logear("[ORD] " + nomA + " ataca con fuerza!"); }
            case ATTACK_B -> { if (c.opB < EstadoCombate.OP_ATTACK) return false; c.opB -= EstadoCombate.OP_ATTACK; c.attackOrden_B = true; c.logear("[ORD] " + nomB + " ataca con fuerza!"); }
            case SPECIAL_A -> { if (c.opA < EstadoCombate.OP_SPECIAL) return false; c.opA -= EstadoCombate.OP_SPECIAL; c.specialActivo_A = true; c.logear("*** ESPECIAL de " + nomA + "! ***"); }
            case SPECIAL_B -> { if (c.opB < EstadoCombate.OP_SPECIAL) return false; c.opB -= EstadoCombate.OP_SPECIAL; c.specialActivo_B = true; c.logear("*** ESPECIAL de " + nomB + "! ***"); }
            case EXE_FUSION -> {
                if (c.opTotal() < EstadoCombate.OP_EXE) return false;
                if (a != null && a.lazo < 70) { c.logear("Lazo insuficiente para ExE Fusion!"); return false; }
                c.exeFusion = true; c.turnosExeRestantes = 2; c.opA = 0; c.opB = 0;
                c.logear(">>> EXE FUSION! " + nomA + " + " + nomB + " se fusionan! <<<");
            }
            case SKILL_1 -> {
                if (c.opA < EstadoCombate.OP_SKILL_1) return false;
                c.opA -= EstadoCombate.OP_SKILL_1;
                aplicarSkill(c, a, b, 1, false);
            }
            case SKILL_2 -> {
                if (c.opA < EstadoCombate.OP_SKILL_2) return false;
                c.opA -= EstadoCombate.OP_SKILL_2;
                aplicarSkill(c, a, b, 2, false);
            }
            case SKILL_3 -> {
                if (c.opA < EstadoCombate.OP_SKILL_3) return false;
                c.opA -= EstadoCombate.OP_SKILL_3;
                aplicarSkill(c, a, b, 3, false);
            }
            default -> { exito = false; }
        }
        if (exito) {
            c.ordenActiva = orden;
            c.fase = Fase.RESOLVIENDO;
            procesarGauge(c, a, b);
        }
        return exito;
    }

    private static void aplicarSkill(EstadoCombate c, DatoDigimon digi, DatoDigimon otro, int nivel, boolean esRefuerzo) {
        if (digi == null) return;
        String nom = digi.nombre;

        switch (nivel) {
            case 1 -> {
                c.logear(nom + " usa QUEMAR! (quema al objetivo)");
                if (!esRefuerzo) {
                    if (c.enemigoVivo()) { c.enemigosEstados.add(EstadoAlterado.QUEMADO); c.turnosQuemado = 3; }
                    else if (c.refuerzoVivo()) { c.refuerzoEstados.add(EstadoAlterado.QUEMADO); c.refTurnosQuemado = 3; }
                } else {
                    if (c.aVivo()) { c.estadosA.add(EstadoAlterado.QUEMADO); c.turnosQuemadoA = 3; }
                    if (c.bVivo()) { c.estadosB.add(EstadoAlterado.QUEMADO); c.turnosQuemadoB = 3; }
                }
            }
            case 2 -> {
                c.logear(nom + " usa VENENO! (envenena al objetivo)");
                if (!esRefuerzo) {
                    if (c.enemigoVivo()) { c.enemigosEstados.add(EstadoAlterado.VENENO); c.turnosVeneno = 4; }
                    else if (c.refuerzoVivo()) { c.refuerzoEstados.add(EstadoAlterado.VENENO); c.refTurnosVeneno = 4; }
                } else {
                    if (c.aVivo()) { c.estadosA.add(EstadoAlterado.VENENO); c.turnosVenenoA = 4; }
                    if (c.bVivo()) { c.estadosB.add(EstadoAlterado.VENENO); c.turnosVenenoB = 4; }
                }
            }
            case 3 -> {
                c.logear(nom + " usa PARALIZAR! (puede paralizar)");
                boolean paralizo = RNG.nextDouble() < 0.40;
                if (!esRefuerzo) {
                    if (c.enemigoVivo()) {
                        c.enemigosEstados.add(EstadoAlterado.PARALISIS);
                        c.turnosParalisis = paralizo ? 2 : 0;
                        c.logear(paralizo ? "Enemigo paralizado!" : "Pero fallo!");
                    }
                } else {
                    if (c.aVivo()) {
                        c.estadosA.add(EstadoAlterado.PARALISIS);
                        c.turnosParalisisA = paralizo ? 2 : 0;
                    }
                    if (c.bVivo()) {
                        c.estadosB.add(EstadoAlterado.PARALISIS);
                        c.turnosParalisisB = paralizo ? 2 : 0;
                    }
                }
            }
        }
    }

    public static boolean huir(EstadoCombate c, DatoDigimon a, DatoDigimon b) {
        if (!c.activa) return false;
        int spdEq = Math.max(a != null ? a.spd : 0, b != null ? b.spd : 0);
        int spdEn = c.enemigoSpd + (c.refuerzoActivo ? c.refuerzoSpd : 0);
        double prob = 0.35 + (spdEq / (double) Math.max(1, spdEq + spdEn)) * 0.45;
        if (RNG.nextDouble() < prob) {
            c.activa = false; c.victoria = false;
            c.fase = Fase.FIN_COMBATE;
            c.logear("Lograste escapar!");
            return true;
        } else {
            c.logear("No pudiste huir!");
            if (c.enemigoVivo()) enemigoAtaca(c, a, b, false);
            if (c.refuerzoVivo()) enemigoAtaca(c, a, b, true);
            return false;
        }
    }

    private static void procesarGauge(EstadoCombate c, DatoDigimon a, DatoDigimon b) {
        if (!c.activa || c.fase == Fase.FIN_COMBATE) return;

        boolean gaugeEnemigoListo = c.enemigoVivo() && c.gaugeEnemigo >= EstadoCombate.GAUGE_MAX;
        boolean gaugeRefListo    = c.refuerzoVivo() && c.gaugeRefuerzo >= EstadoCombate.GAUGE_MAX;

        if (c.aVivo() && c.gaugeA >= EstadoCombate.GAUGE_MAX && !c.tieneEstadoA(EstadoAlterado.DORMIDO)) {
            if (!(c.tieneEstadoA(EstadoAlterado.PARALISIS) && RNG.nextDouble() < 0.25)) {
                atacarConDigimon(c, a, true);
            } else {
                c.logear(a.nombre + " paralizado! No puede moverse!");
            }
        }
        if (c.bVivo() && c.gaugeB >= EstadoCombate.GAUGE_MAX && !c.tieneEstadoB(EstadoAlterado.DORMIDO)) {
            if (!(c.tieneEstadoB(EstadoAlterado.PARALISIS) && RNG.nextDouble() < 0.25)) {
                atacarConDigimon(c, b, false);
            } else {
                c.logear(b.nombre + " paralizado! No puede moverse!");
            }
        }

        if (c.turnosExeRestantes > 0) c.turnosExeRestantes--;

        comprobarRefuerzo(c);

        if (gaugeEnemigoListo && c.enemigoVivo()) {
            if (!(c.tieneEstadoEnemigo(EstadoAlterado.PARALISIS) && RNG.nextDouble() < 0.25)) {
                enemigoAtaca(c, a, b, false);
            } else {
                c.logear(c.enemigoNombre + " paralizado!");
            }
        }
        if (gaugeRefListo && c.refuerzoVivo()) {
            if (!(c.tieneEstadoRefuerzo(EstadoAlterado.PARALISIS) && RNG.nextDouble() < 0.25)) {
                enemigoAtaca(c, a, b, true);
            } else {
                c.logear(c.refuerzoNombre + " paralizado!");
            }
        }

        danoResidualYDespertar(c, a, b);

        avanzarGauge(c, a, b);
        c.ronda++;

        if (c.todosEnemigosDerrota()) {
            c.victoria = true; c.activa = false;
            c.fase = Fase.FIN_COMBATE;
            c.expTotal = c.enemigoExp + (c.refuerzoActivo ? c.refuerzoExp : 0);
            c.logear(">>> VICTORIA! +" + c.expTotal + " XP <<<");
            aplicarVictoria(c, a, b);
        } else if (!c.equipoVivo()) {
            c.victoria = false; c.activa = false;
            c.fase = Fase.FIN_COMBATE;
            c.logear("Derrota... el equipo cayo!");
            aplicarDerrota(a, b);
        } else {
            c.fase = Fase.INICIATIVA;
        }
    }

    private static void danoResidualYDespertar(EstadoCombate c, DatoDigimon a, DatoDigimon b) {
        int danoA = c.danoResidualA();
        if (danoA > 0 && a != null) c.logear(a.nombre + " recibe " + danoA + " dano residual!");
        int danoB = c.danoResidualB();
        if (danoB > 0 && b != null) c.logear(b.nombre + " recibe " + danoB + " dano residual!");
        int danoE = c.danoResidualEnemigo();
        if (danoE > 0) c.logear(c.enemigoNombre + " recibe " + danoE + " dano residual!");
        int danoR = c.danoResidualRefuerzo();
        if (danoR > 0) c.logear(c.refuerzoNombre + " recibe " + danoR + " dano residual!");
        c.verificarDespertar();
    }

    private static void avanzarGauge(EstadoCombate c, DatoDigimon a, DatoDigimon b) {
        int spdA = a != null ? a.spd : 5;
        int spdB = b != null ? b.spd : 5;
        int spdE = c.enemigoSpd;
        int spdR = c.refuerzoSpd;

        c.gaugeA += spdA * 10 + RNG.nextInt(20);
        c.gaugeB += spdB * 10 + RNG.nextInt(20);
        c.gaugeEnemigo += spdE * 10 + RNG.nextInt(20);
        c.gaugeRefuerzo += spdR * 10 + RNG.nextInt(20);

        if (c.gaugeA > EstadoCombate.GAUGE_MAX) c.gaugeA = EstadoCombate.GAUGE_MAX;
        if (c.gaugeB > EstadoCombate.GAUGE_MAX) c.gaugeB = EstadoCombate.GAUGE_MAX;
        if (c.gaugeEnemigo > EstadoCombate.GAUGE_MAX) c.gaugeEnemigo = EstadoCombate.GAUGE_MAX;
        if (c.gaugeRefuerzo > EstadoCombate.GAUGE_MAX) c.gaugeRefuerzo = EstadoCombate.GAUGE_MAX;
    }

    private static void atacarConDigimon(EstadoCombate c, DatoDigimon d, boolean esA) {
        if (d == null) return;
        if ((esA ? c.hpCombateA : c.hpCombateB) <= 0) return;

        boolean especial = esA ? c.specialActivo_A : c.specialActivo_B;
        boolean forzado  = esA ? c.attackOrden_A   : c.attackOrden_B;

        boolean atacaRef = c.refuerzoVivo() && (!c.enemigoVivo() || c.pctHpRefuerzo() < c.pctHpEnemigo());
        int defTarget = atacaRef ? c.refuerzoDef : c.enemigoDef;
        String elemTgt = atacaRef ? c.refuerzoElemento : c.enemigoElemento;
        String nomTgt = atacaRef ? c.refuerzoNombre : c.enemigoNombre;
        Set<EstadoAlterado> tgtEstados = atacaRef ? c.refuerzoEstados : c.enemigosEstados;

        double multElem = EnemigoCombate.multElemental(d.elemento, elemTgt);

        boolean muyCritico = RNG.nextDouble() < (PCT_MUY_CRIT_BASE + d.wis * PCT_MUY_CRIT_WIS_SCALE);
        boolean critico    = !muyCritico && RNG.nextDouble() < (PCT_CRIT_BASE + d.wis * PCT_CRIT_WIS_SCALE);
        boolean fallo      = RNG.nextDouble() < (PCT_FALLO_BASE + defTarget * PCT_FALLO_DEF_SCALE);

        int base;
        if (c.exeFusion && c.turnosExeRestantes > 0) {
            base = (int)(d.atk * 4.0 * multElem);
        } else if (especial) {
            base = (int)(d.atk * 2.5 * multElem);
        } else if (forzado) {
            base = (int)(d.atk * 1.6 * multElem);
        } else {
            double ratio = switch (esA ? c.tacticaA : c.tacticaB) {
                case AGRESIVO   -> 1.4;
                case DEFENSIVO   -> 0.7;
                default          -> 1.0;
            };
            base = Math.max(1, d.atk - defTarget / 2);
            base = (int)(base * ratio * multElem);
        }

        TipoDano tipo;
        int dano;

        if (fallo) {
            tipo = TipoDano.FALLIDO;
            dano = 0;
            c.logear(d.nombre + " ataca a " + nomTgt + " pero FALLA!");
        } else if (muyCritico) {
            tipo = TipoDano.MUY_CRITICO;
            dano = Math.max(1, (int)(base * 1.8 * (0.85 + RNG.nextDouble() * 0.3)));
            aplicarEstadoPostAtaque(d, tgtEstados);
        } else if (critico) {
            tipo = TipoDano.CRITICO;
            dano = Math.max(1, (int)(base * 1.4 * (0.85 + RNG.nextDouble() * 0.3)));
            aplicarEstadoPostAtaque(d, tgtEstados);
        } else {
            tipo = TipoDano.NORMAL;
            dano = Math.max(1, (int)(base * (0.85 + RNG.nextDouble() * 0.3)));
        }

        if (atacaRef) c.refuerzoHp = Math.max(0, c.refuerzoHp - dano);
        else          c.enemigoHp  = Math.max(0, c.enemigoHp  - dano);

        c.danoTotalAcumulado += dano;
        c.comboCount++;
        if (c.comboCount > c.comboMax) c.comboMax = c.comboCount;

        String tag = switch (tipo) {
            case MUY_CRITICO -> "<<<< MUERTE >>>>";
            case CRITICO     -> "<CRIT!>";
            case FALLIDO     -> "<FALLO>";
            default          -> "";
        };

        if (especial) tag += "[ESP]";
        if (forzado)  tag += "[ORD]";
        if (c.exeFusion && c.turnosExeRestantes > 0) tag += "[FUS]";

        c.logear(d.nombre + " > " + nomTgt + " -" + dano + " HP " + tag);
    }

    private static void aplicarEstadoPostAtaque(DatoDigimon atk, Set<EstadoAlterado> estados) {
        double r = RNG.nextDouble();
        if (r < 0.15) {
            estados.add(EstadoAlterado.QUEMADO);
        } else if (r < 0.25) {
            estados.add(EstadoAlterado.VENENO);
        } else if (r < 0.35) {
            estados.add(EstadoAlterado.SANGRANDO);
        }
    }

    private static void enemigoAtaca(EstadoCombate c, DatoDigimon a, DatoDigimon b, boolean esRef) {
        int atk    = esRef ? c.refuerzoAtk : c.enemigoAtk;
        String nom = esRef ? c.refuerzoNombre : c.enemigoNombre;
        String elem = esRef ? c.refuerzoElemento : c.enemigoElemento;
        boolean fuerte = (c.ronda % 4 == 0);
        if (esRef) c.refuerzoCargando = fuerte; else c.enemigoCargando = fuerte;

        boolean atacaA = (b == null || c.hpCombateB <= 0)
                || (a != null && c.hpCombateA > 0
                && (float) c.hpCombateA / Math.max(1, a.maxHp)
                <= (float) c.hpCombateB / Math.max(1, b.maxHp));

        DatoDigimon obj = atacaA ? a : b;
        if (obj == null) return;

        boolean guard = atacaA ? c.guardActivo_A : c.guardActivo_B;
        double multE  = EnemigoCombate.multElemental(elem, obj.elemento);
        boolean critico = RNG.nextDouble() < 0.08;
        boolean fallo   = RNG.nextDouble() < 0.05;

        int dano;
        if (fallo) {
            c.logear(nom + " ataca a " + obj.nombre + " pero FALLA!");
            dano = 0;
        } else {
            dano = Math.max(1, (int)(Math.max(1, atk - obj.def / 2) * multE
                    * (guard ? 0.4 : 1.0)
                    * (fuerte ? 2.0 : 1.0)
                    * (critico ? 1.5 : 1.0)
                    * (0.85 + RNG.nextDouble() * 0.3)));
        }

        if (atacaA) c.hpCombateA = Math.max(0, c.hpCombateA - dano);
        else        c.hpCombateB = Math.max(0, c.hpCombateB - dano);

        String tag = critico ? " <CRIT!>" : (fuerte ? " [!FUERTE!]" : "") + (guard ? "(BLOQUEADO)" : "");
        if (!fallo) {
            Set<EstadoAlterado> objEstados = atacaA ? c.estadosA : c.estadosB;
            if (critico && RNG.nextDouble() < 0.20) {
                if (fuerte) {
                    objEstados.add(EstadoAlterado.ENVENENADO_FUERTE);
                    c.logear(nom + " > " + obj.nombre + " -" + dano + " HP" + tag + " (VENENO FUERTE!)");
                } else {
                    objEstados.add(EstadoAlterado.VENENO);
                    c.logear(nom + " > " + obj.nombre + " -" + dano + " HP" + tag + " (envenenado)");
                }
            } else {
                c.logear(nom + " > " + obj.nombre + " -" + dano + " HP" + tag);
            }
        }
    }

    private static void comprobarRefuerzo(EstadoCombate c) {
        if (c.refuerzoActivo || c.refuerzoMaxHp == 0) return;
        if (c.pctHpEnemigo() <= PCT_HP_REFUERZO || c.ronda >= RONDA_REFUERZO) {
            c.refuerzoActivo = true;
            c.refuerzoAnunciado = false;
            c.gaugeRefuerzo = 0;
            c.logear(">> UN " + c.refuerzoNombre + " [" + c.refuerzoElemento + "] SALVAJE APARECE! <<");
        }
    }

    private static void aplicarVictoria(EstadoCombate c, DatoDigimon a, DatoDigimon b) {
        int bonusCombo = c.comboMax * 5;
        c.expTotal += bonusCombo;
        c.logear("Bonus combo: +" + bonusCombo + " XP");

        if (a != null && c.aVivo()) {
            a.victorias++;
            a.lazo = Math.min(100, a.lazo + 2);
            a.hp = c.hpCombateA;
            a.energia = Math.max(10, a.energia - 15);
            a.abi = Math.min(100, a.abi + 1);
        }
        if (b != null && c.bVivo()) {
            b.victorias++;
            b.lazo = Math.min(100, b.lazo + 2);
            b.hp = c.hpCombateB;
            b.energia = Math.max(10, b.energia - 15);
            b.abi = Math.min(100, b.abi + 1);
        }
    }

    public static void aplicarDerrota(DatoDigimon a, DatoDigimon b) {
        if (a != null) { a.derrotas++; a.energia = Math.max(5, a.energia - 25); a.lazo = Math.max(0, a.lazo - 3); }
        if (b != null) { b.derrotas++; b.energia = Math.max(5, b.energia - 25); b.lazo = Math.max(0, b.lazo - 3); }
    }

    public static String nombreEstado(EstadoAlterado e) {
        return switch (e) {
            case QUEMADO -> "🔥";
            case VENENO, ENVENENADO_FUERTE -> "☠";
            case PARALISIS -> "⚡";
            case DORMIDO -> "💤";
            case ATURDIDO -> "😵";
            case SANGRANDO -> "🩸";
        };
    }

    public static String estadosToString(Set<EstadoAlterado> estados) {
        if (estados.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (EstadoAlterado e : estados) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(nombreEstado(e));
        }
        return sb.toString();
    }
}
