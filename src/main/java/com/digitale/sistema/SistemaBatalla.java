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

    public static EstadoCombate iniciarCombate(java.util.List<DatoDigimon> party) {
        return iniciarCombate(party, null);
    }

    public static EstadoCombate iniciarCombate(java.util.List<DatoDigimon> party, String especieEnemiga) {
        int nivel = 1;
        for (DatoDigimon d : party) if (d != null && d.nivel > nivel) nivel = d.nivel;
        EstadoCombate c = EnemigoCombate.generarCombatePara(nivel, especieEnemiga);
        DatoDigimon a = lado(party, 0);
        DatoDigimon b = lado(party, 1);
        DatoDigimon cc = lado(party, 2);
        c.hpCombateA = a != null ? a.maxHp : 0;
        c.hpCombateB = b != null ? b.maxHp : 0;
        c.hpCombateC = cc != null ? cc.maxHp : 0;

        inicializarGauge(c, party);
        cargarRefuerzo(c, nivel);
        c.party = new java.util.ArrayList<>(party);
        // Duelo simple por defecto (1 oleada); los encuentros salvajes usan N.
        c.nivelZona = nivel;
        c.totalOleada = 1;
        c.oleadaActual = 1;
        c.colaRestante = 0;
        LOGGER.log(Level.INFO, "Combate vs " + c.enemigoNombre + " | lados: " + party.size()
            + " gaugeA:" + c.gaugeA + " gaugeB:" + c.gaugeB + " gaugeC:" + c.gaugeC + " gaugeE:" + c.gaugeEnemigo);
        return c;
    }

    /**
     * Entrada oficial a combate (reglas de sueño + banco de 3):
     * - Requiere al menos un despierto (si no, null).
     * - Party = equipoCombate() (máx 3, despiertos primero).
     * - Los dormidos de relleno entran con efecto DORMIDO.
     */
    public static EstadoCombate iniciarCombatePara(AlmacenJugadores.DatosJugador datos) {
        if (datos == null || !datos.puedeCombatir()) return null;
        java.util.List<DatoDigimon> party = datos.equipoCombate();
        if (party.isEmpty()) return null;
        EstadoCombate c = iniciarCombate(party);
        c.calumonDobla = datos.tieneCalumon();
        if (c.calumonDobla) c.logear("¡Calumon apoya! Stats x2.");
        aplicarDormidosIniciales(c, party);
        datos.combate = c;
        return c;
    }

    /**
     * Encuentro salvaje por turnos: 1-5 oleadas según nivel de zona vs party
     * y tamaño del equipo. Zona más dura o equipo grande = más oleadas.
     */
    public static EstadoCombate iniciarEncuentroSalvaje(AlmacenJugadores.DatosJugador datos,
                                                        int nivelZona,
                                                        com.hypixel.hytale.component.Ref<?> objetivo) {
        return iniciarEncuentroSalvaje(datos, nivelZona, objetivo, null);
    }

    public static EstadoCombate iniciarEncuentroSalvaje(AlmacenJugadores.DatosJugador datos,
                                                        int nivelZona,
                                                        com.hypixel.hytale.component.Ref<?> objetivo,
                                                        String especieEnemiga) {
        if (datos == null || !datos.puedeCombatir()) return null;
        java.util.List<DatoDigimon> party = datos.equipoCombate();
        if (party.isEmpty()) return null;
        int nivelParty = 1;
        for (DatoDigimon d : party) if (d != null && d.nivel > nivelParty) nivelParty = d.nivel;
        int total = tamanoEncuentro(nivelParty, Math.max(1, nivelZona), party.size());
        EstadoCombate c = iniciarCombate(party, especieEnemiga);
        c.calumonDobla = datos.tieneCalumon();
        if (c.calumonDobla) c.logear("¡Calumon apoya! Stats x2.");
        c.nivelZona = Math.max(1, nivelZona);
        c.objetivo = objetivo;
        c.totalOleada = total;
        c.oleadaActual = 1;
        c.colaRestante = total - 1;
        aplicarDormidosIniciales(c, party);
        if (total > 1) c.logear("¡Manada de " + total + " salvajes! (oleada 1/" + total + ")");
        datos.combate = c;
        return c;
    }

    /** 1-5 según diferencia de nivel + tamaño del equipo + azar. */
    public static int tamanoEncuentro(int nivelParty, int nivelZona, int tamParty) {
        int n = 1;
        int diff = nivelZona - Math.max(1, nivelParty);
        if (diff >= 2) n += 2;
        else if (diff >= 0) n += 1;
        if (tamParty >= 3) n += 1;
        n += RNG.nextInt(2);
        return Math.max(1, Math.min(5, n));
    }

    private static void aplicarDormidosIniciales(EstadoCombate c, java.util.List<DatoDigimon> party) {
        String[] lados = {"A", "B", "C"};
        for (int i = 0; i < party.size() && i < 3; i++) {
            DatoDigimon d = party.get(i);
            if (d != null && d.dormido) {
                if (i == 0) { c.estadosA.add(EstadoAlterado.DORMIDO); c.turnosDormidoA = 2; }
                else if (i == 1) { c.estadosB.add(EstadoAlterado.DORMIDO); c.turnosDormidoB = 2; }
                else { c.estadosC.add(EstadoAlterado.DORMIDO); c.turnosDormidoC = 2; }
                c.logear(d.nombre + " (" + lados[i] + ") sale DORMIDO...");
            }
        }
    }

    private static DatoDigimon lado(java.util.List<DatoDigimon> party, int i) {
        return (party != null && i >= 0 && i < party.size()) ? party.get(i) : null;
    }

    // ── Stats efectivas: ×2 con Calumon vivo en el equipo (soporte) ──
    static int atkEf(DatoDigimon d, EstadoCombate c) {
        if (d == null) return 0;
        return (c != null && c.calumonDobla) ? d.atk * 2 : d.atk;
    }

    static int defEf(DatoDigimon d, EstadoCombate c) {
        if (d == null) return 0;
        return (c != null && c.calumonDobla) ? d.def * 2 : d.def;
    }

    static int spdEf(DatoDigimon d, EstadoCombate c) {
        if (d == null) return 5;
        return (c != null && c.calumonDobla) ? d.spd * 2 : d.spd;
    }

    static int wisEf(DatoDigimon d, EstadoCombate c) {
        if (d == null) return 0;
        return (c != null && c.calumonDobla) ? d.wis * 2 : d.wis;
    }

    private static int hpLado(EstadoCombate c, int i) {
        return switch (i) {
            case 0 -> c.hpCombateA;
            case 1 -> c.hpCombateB;
            default -> c.hpCombateC;
        };
    }

    private static void setHpLado(EstadoCombate c, int i, int hp) {
        switch (i) {
            case 0 -> c.hpCombateA = hp;
            case 1 -> c.hpCombateB = hp;
            default -> c.hpCombateC = hp;
        }
    }

    private static void inicializarGauge(EstadoCombate c, java.util.List<DatoDigimon> party) {
        DatoDigimon a = lado(party, 0);
        DatoDigimon b = lado(party, 1);
        DatoDigimon cc = lado(party, 2);
        int spdA = a != null ? spdEf(a, c) : 5;
        int spdB = b != null ? spdEf(b, c) : 5;
        int spdC = cc != null ? spdEf(cc, c) : 5;
        int spdE = c.enemigoSpd;
        int spdR = c.refuerzoSpd;

        c.gaugeA = EstadoCombate.GAUGE_MAX - spdA * 8 + RNG.nextInt(16);
        c.gaugeB = EstadoCombate.GAUGE_MAX - spdB * 8 + RNG.nextInt(16);
        c.gaugeC = EstadoCombate.GAUGE_MAX - spdC * 8 + RNG.nextInt(16);
        c.gaugeEnemigo = EstadoCombate.GAUGE_MAX - spdE * 8 + RNG.nextInt(16);
        c.gaugeRefuerzo = EstadoCombate.GAUGE_MAX - spdR * 8 + RNG.nextInt(16);

        c.gaugeA = Math.max(0, Math.min(EstadoCombate.GAUGE_MAX, c.gaugeA));
        c.gaugeB = Math.max(0, Math.min(EstadoCombate.GAUGE_MAX, c.gaugeB));
        c.gaugeC = Math.max(0, Math.min(EstadoCombate.GAUGE_MAX, c.gaugeC));
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

    public static void support(EstadoCombate c) {
        if (!c.activa || c.fase == Fase.FIN_COMBATE) return;
        if (c.aVivo()) c.opA = Math.min(EstadoCombate.OP_SPECIAL, c.opA + OP_POR_SUPPORT);
        if (c.bVivo()) c.opB = Math.min(EstadoCombate.OP_SPECIAL, c.opB + OP_POR_SUPPORT);
        if (c.cVivo()) c.opC = Math.min(EstadoCombate.OP_SPECIAL, c.opC + OP_POR_SUPPORT);

        c.fase = Fase.RESOLVIENDO;
        procesarGauge(c, c.party);
    }

    public static boolean ejecutarOrden(EstadoCombate c, Orden orden) {
        if (!c.activa || c.fase == Fase.FIN_COMBATE) return false;
        java.util.List<DatoDigimon> party = c.party;
        DatoDigimon a = lado(party, 0);
        DatoDigimon b = lado(party, 1);
        DatoDigimon cc = lado(party, 2);
        String nomA = a != null ? a.nombre : "A";
        String nomB = b != null ? b.nombre : "B";
        String nomC = cc != null ? cc.nombre : "C";
        boolean exito = true;

        switch (orden) {
            case GUARD_A  -> { if (c.opA < EstadoCombate.OP_GUARD) return false; c.opA -= EstadoCombate.OP_GUARD; c.guardActivo_A = true; c.opA += OP_POR_GUARD; c.logear("[G] " + nomA + " se defiende (+" + OP_POR_GUARD + " OP)"); }
            case GUARD_B  -> { if (c.opB < EstadoCombate.OP_GUARD) return false; c.opB -= EstadoCombate.OP_GUARD; c.guardActivo_B = true; c.opB += OP_POR_GUARD; c.logear("[G] " + nomB + " se defiende (+" + OP_POR_GUARD + " OP)"); }
            case GUARD_C  -> { if (c.opC < EstadoCombate.OP_GUARD) return false; c.opC -= EstadoCombate.OP_GUARD; c.guardActivo_C = true; c.opC += OP_POR_GUARD; c.logear("[G] " + nomC + " se defiende (+" + OP_POR_GUARD + " OP)"); }
            case ATTACK_A -> { if (c.opA < EstadoCombate.OP_ATTACK) return false; c.opA -= EstadoCombate.OP_ATTACK; c.attackOrden_A = true; c.logear("[ORD] " + nomA + " ataca con fuerza!"); }
            case ATTACK_B -> { if (c.opB < EstadoCombate.OP_ATTACK) return false; c.opB -= EstadoCombate.OP_ATTACK; c.attackOrden_B = true; c.logear("[ORD] " + nomB + " ataca con fuerza!"); }
            case ATTACK_C -> { if (c.opC < EstadoCombate.OP_ATTACK) return false; c.opC -= EstadoCombate.OP_ATTACK; c.attackOrden_C = true; c.logear("[ORD] " + nomC + " ataca con fuerza!"); }
            case SPECIAL_A -> { if (c.opA < EstadoCombate.OP_SPECIAL) return false; c.opA -= EstadoCombate.OP_SPECIAL; c.specialActivo_A = true; c.logear("*** ESPECIAL de " + nomA + "! ***"); }
            case SPECIAL_B -> { if (c.opB < EstadoCombate.OP_SPECIAL) return false; c.opB -= EstadoCombate.OP_SPECIAL; c.specialActivo_B = true; c.logear("*** ESPECIAL de " + nomB + "! ***"); }
            case SPECIAL_C -> { if (c.opC < EstadoCombate.OP_SPECIAL) return false; c.opC -= EstadoCombate.OP_SPECIAL; c.specialActivo_C = true; c.logear("*** ESPECIAL de " + nomC + "! ***"); }
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
            case SKILL_4 -> { return ordenDano(c, a, 4, EstadoCombate.OP_SKILL_4); }
            case SKILL_5 -> { return ordenDano(c, a, 5, EstadoCombate.OP_SKILL_5); }
            case SKILL_6 -> { return ordenDano(c, a, 6, EstadoCombate.OP_SKILL_6); }
            case SKILL_7 -> { return ordenDano(c, a, 7, EstadoCombate.OP_SKILL_7); }
            default -> { exito = false; }
        }
        if (exito) {
            c.ordenActiva = orden;
            c.fase = Fase.RESOLVIENDO;
            procesarGauge(c, party);
        }
        return exito;
    }

    /** Técnicas de daño 4-7 del líder (lado A): fijan multiplicador y avanzan. */
    private static boolean ordenDano(EstadoCombate c, DatoDigimon a, int slot, int coste) {
        if (a == null || !c.aVivo()) return false;
        if (c.opA < coste) {
            c.logear("OP insuficiente para la técnica " + slot + " (pide " + coste + ").");
            return false;
        }
        c.opA -= coste;
        double mult = switch (slot) {
            case 4 -> 2.0;
            case 5 -> 2.5;
            case 6 -> 3.0;
            case 7 -> 3.5;
            default -> 1.5;
        };
        c.habMult_A = mult;
        c.habNombre_A = a.nombresHabilidades().length >= slot ? a.nombresHabilidades()[slot - 1] : ("Tecnica " + slot);
        c.logear("*** " + a.nombre + " prepara " + c.habNombre_A + "! ***");
        c.ordenActiva = switch (slot) {
            case 4 -> Orden.SKILL_4;
            case 5 -> Orden.SKILL_5;
            case 6 -> Orden.SKILL_6;
            default -> Orden.SKILL_7;
        };
        c.fase = Fase.RESOLVIENDO;
        procesarGauge(c, c.party);
        return true;
    }

    /** Pasar turno sin actuar (cuesta la ronda). */
    public static void pasarTurno(EstadoCombate c) {
        if (!c.activa || c.fase == Fase.FIN_COMBATE) return;
        c.logear("Tomas aire... (pasas)");
        c.fase = Fase.RESOLVIENDO;
        procesarGauge(c, c.party);
    }

    /**
     * Ejecuta un Movimiento aprendido del líder (lado A): daño con mult
     * propio y/o estado al enemigo. Avanza la ronda.
     */
    public static boolean ejecutarMovimiento(EstadoCombate c, DatoDigimon a,
                                             DatoDigimon.Movimiento m) {
        if (!c.activa || c.fase == Fase.FIN_COMBATE) return false;
        if (a == null || !c.aVivo() || m == null) return false;
        if (c.opA < m.coste) {
            c.logear("OP insuficiente para " + m.nombre + " (pide " + m.coste + ").");
            return false;
        }
        c.opA -= m.coste;
        if (m.mult > 0) {
            c.habMult_A = m.mult;
            c.habNombre_A = m.nombre;
            c.habSuicida_A = "EXPLOSIVO".equals(m.categoria);
            c.logear("*** " + a.nombre + " prepara " + m.nombre + "! ***");
        }
        if (m.efecto != null) {
            if (c.enemigoVivo()) aplicarEstadoEnemigo(c, m.efecto, m.turnos, false);
            else if (c.refuerzoVivo()) aplicarEstadoEnemigo(c, m.efecto, m.turnos, true);
            c.logear(a.nombre + " usa " + m.nombre + "!");
        }
        c.ordenActiva = Orden.NINGUNA;
        c.fase = Fase.RESOLVIENDO;
        procesarGauge(c, c.party);
        return true;
    }

    private static void aplicarEstadoEnemigo(EstadoCombate c, EstadoAlterado e, int turnos, boolean ref) {
        if (!ref) {
            switch (e) {
                case QUEMADO -> { c.enemigosEstados.add(e); c.turnosQuemado = Math.max(c.turnosQuemado, turnos); }
                case VENENO, ENVENENADO_FUERTE -> { c.enemigosEstados.add(e); c.turnosVeneno = Math.max(c.turnosVeneno, turnos); }
                case PARALISIS, ATURDIDO -> { c.enemigosEstados.add(e); c.turnosParalisis = Math.max(c.turnosParalisis, turnos); }
                case DORMIDO -> { c.enemigosEstados.add(e); c.turnosDormido = Math.max(c.turnosDormido, turnos); }
                default -> c.enemigosEstados.add(e);
            }
        } else {
            switch (e) {
                case QUEMADO -> { c.refuerzoEstados.add(e); c.refTurnosQuemado = Math.max(c.refTurnosQuemado, turnos); }
                case VENENO, ENVENENADO_FUERTE -> { c.refuerzoEstados.add(e); c.refTurnosVeneno = Math.max(c.refTurnosVeneno, turnos); }
                case PARALISIS, ATURDIDO -> { c.refuerzoEstados.add(e); c.refTurnosParalisis = Math.max(c.refTurnosParalisis, turnos); }
                case DORMIDO -> { c.refuerzoEstados.add(e); c.refTurnosDormido = Math.max(c.refTurnosDormido, turnos); }
                default -> c.refuerzoEstados.add(e);
            }
        }
    }

    /**
     * Cambia al líder (lado A) por un suplente despierto de la banca.
     * Pierde el turno (gauge A a 0) y limpia sus estados.
     */
    public static boolean intercambiarLider(EstadoCombate c,
                                            AlmacenJugadores.DatosJugador datos, int benchSlot) {
        if (!c.activa || c.fase == Fase.FIN_COMBATE) return false;
        if (!AlmacenJugadores.DatosJugador.slotValido(benchSlot)) return false;
        java.util.List<DatoDigimon> party = c.party;
        DatoDigimon entra = datos.getSlot(benchSlot);
        if (entra == null || !entra.vivo || entra.dormido) return false;
        if (party.isEmpty() || party.contains(entra)) return false;
        DatoDigimon sale = party.get(0);
        // Intercambiar en party y en equipo.
        int slotSale = -1;
        for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
            if (datos.getSlot(i) == sale) {
                slotSale = i;
                break;
            }
        }
        if (slotSale == -1) return false;
        party.set(0, entra);
        datos.setSlot(benchSlot, sale);
        datos.setSlot(slotSale, entra);
        c.hpCombateA = entra.hp;
        c.opA = 0;
        c.gaugeA = 0;
        c.estadosA.clear();
        c.turnosQuemadoA = 0; c.turnosVenenoA = 0; c.turnosParalisisA = 0; c.turnosDormidoA = 0;
        c.guardActivo_A = false; c.attackOrden_A = false; c.specialActivo_A = false;
        c.habMult_A = 0; c.habNombre_A = ""; c.habSuicida_A = false;
        c.logear("¡Cambio! " + entra.nombre + " entra al frente.");
        return true;
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

    public static boolean huir(EstadoCombate c) {
        if (!c.activa) return false;
        java.util.List<DatoDigimon> party = c.party;
        int spdEq = 0;
        for (int i = 0; i < 3; i++) {
            DatoDigimon d = lado(party, i);
            if (d != null && spdEf(d, c) > spdEq) spdEq = spdEf(d, c);
        }
        int spdEn = c.enemigoSpd + (c.refuerzoActivo ? c.refuerzoSpd : 0);
        double prob = 0.35 + (spdEq / (double) Math.max(1, spdEq + spdEn)) * 0.45;
        if (RNG.nextDouble() < prob) {
            c.activa = false; c.victoria = false;
            c.fase = Fase.FIN_COMBATE;
            c.logear("Lograste escapar!");
            return true;
        } else {
            c.logear("No pudiste huir!");
            if (c.enemigoVivo()) enemigoAtaca(c, party, false);
            if (c.refuerzoVivo()) enemigoAtaca(c, party, true);
            return false;
        }
    }

    private static void procesarGauge(EstadoCombate c, java.util.List<DatoDigimon> party) {
        if (!c.activa || c.fase == Fase.FIN_COMBATE) return;

        boolean gaugeEnemigoListo = c.enemigoVivo() && c.gaugeEnemigo >= EstadoCombate.GAUGE_MAX;
        boolean gaugeRefListo    = c.refuerzoVivo() && c.gaugeRefuerzo >= EstadoCombate.GAUGE_MAX;

        DatoDigimon a = lado(party, 0);
        DatoDigimon b = lado(party, 1);
        DatoDigimon cc = lado(party, 2);

        if (c.aVivo() && c.gaugeA >= EstadoCombate.GAUGE_MAX && !c.tieneEstadoA(EstadoAlterado.DORMIDO)) {
            if (c.tieneEstadoA(EstadoAlterado.ATURDIDO)) {
                c.logear(a.nombre + " aturdido! No puede moverse!");
                if (--c.turnosAturdidoA <= 0) c.estadosA.remove(EstadoAlterado.ATURDIDO);
            } else if (!(c.tieneEstadoA(EstadoAlterado.PARALISIS) && RNG.nextDouble() < 0.25)) {
                atacarConDigimon(c, a, 0);
            } else {
                c.logear(a.nombre + " paralizado! No puede moverse!");
            }
        }
        if (c.bVivo() && c.gaugeB >= EstadoCombate.GAUGE_MAX && !c.tieneEstadoB(EstadoAlterado.DORMIDO)) {
            if (c.tieneEstadoB(EstadoAlterado.ATURDIDO)) {
                c.logear(b.nombre + " aturdido! No puede moverse!");
                if (--c.turnosAturdidoB <= 0) c.estadosB.remove(EstadoAlterado.ATURDIDO);
            } else if (!(c.tieneEstadoB(EstadoAlterado.PARALISIS) && RNG.nextDouble() < 0.25)) {
                atacarConDigimon(c, b, 1);
            } else {
                c.logear(b.nombre + " paralizado! No puede moverse!");
            }
        }
        if (c.cVivo() && c.gaugeC >= EstadoCombate.GAUGE_MAX && !c.tieneEstadoC(EstadoAlterado.DORMIDO)) {
            if (c.tieneEstadoC(EstadoAlterado.ATURDIDO)) {
                c.logear(cc.nombre + " aturdido! No puede moverse!");
                if (--c.turnosAturdidoC <= 0) c.estadosC.remove(EstadoAlterado.ATURDIDO);
            } else if (!(c.tieneEstadoC(EstadoAlterado.PARALISIS) && RNG.nextDouble() < 0.25)) {
                atacarConDigimon(c, cc, 2);
            } else {
                c.logear(cc.nombre + " paralizado! No puede moverse!");
            }
        }

        if (c.turnosExeRestantes > 0) c.turnosExeRestantes--;

        comprobarRefuerzo(c);

        if (gaugeEnemigoListo && c.enemigoVivo()) {
            if (c.tieneEstadoEnemigo(EstadoAlterado.ATURDIDO)) {
                c.logear(c.enemigoNombre + " aturdido!");
            } else if (!(c.tieneEstadoEnemigo(EstadoAlterado.PARALISIS) && RNG.nextDouble() < 0.25)) {
                enemigoAtaca(c, party, false);
            } else {
                c.logear(c.enemigoNombre + " paralizado!");
            }
        }
        if (gaugeRefListo && c.refuerzoVivo()) {
            if (c.tieneEstadoRefuerzo(EstadoAlterado.ATURDIDO)) {
                c.logear(c.refuerzoNombre + " aturdido!");
            } else if (!(c.tieneEstadoRefuerzo(EstadoAlterado.PARALISIS) && RNG.nextDouble() < 0.25)) {
                enemigoAtaca(c, party, true);
            } else {
                c.logear(c.refuerzoNombre + " paralizado!");
            }
        }

        danoResidualYDespertar(c, party);

        avanzarGauge(c, party);
        c.ronda++;

        if (c.todosEnemigosDerrota()) {
            // Cobrar la oleada caída (activo + refuerzo si participó).
            c.expTotal += c.enemigoExp + (c.refuerzoActivo ? c.refuerzoExp : 0);
            if (c.colaRestante > 0 && reponerEnemigo(c)) {
                c.fase = Fase.INICIATIVA;
            } else {
                c.victoria = true; c.activa = false;
                c.fase = Fase.FIN_COMBATE;
                int bonusCombo = c.comboMax * 5;
                c.expTotal += bonusCombo;
                c.logear("Bonus combo: +" + bonusCombo + " XP");
                c.logear(">>> VICTORIA! +" + c.expTotal + " XP <<<");
                aplicarVictoria(c, party);
            }
        } else if (!c.equipoVivo()) {
            c.victoria = false; c.activa = false;
            c.fase = Fase.FIN_COMBATE;
            c.logear("Derrota... el equipo cayo!");
            aplicarDerrota(party, c);
        } else {
            c.fase = Fase.INICIATIVA;
        }
    }

    private static void danoResidualYDespertar(EstadoCombate c, java.util.List<DatoDigimon> party) {
        DatoDigimon a = lado(party, 0);
        DatoDigimon b = lado(party, 1);
        DatoDigimon cc = lado(party, 2);
        int danoA = c.danoResidualA();
        if (danoA > 0 && a != null) c.logear(a.nombre + " recibe " + danoA + " dano residual!");
        int danoB = c.danoResidualB();
        if (danoB > 0 && b != null) c.logear(b.nombre + " recibe " + danoB + " dano residual!");
        int danoC = c.danoResidualC();
        if (danoC > 0 && cc != null) c.logear(cc.nombre + " recibe " + danoC + " dano residual!");
        int danoE = c.danoResidualEnemigo();
        if (danoE > 0) c.logear(c.enemigoNombre + " recibe " + danoE + " dano residual!");
        int danoR = c.danoResidualRefuerzo();
        if (danoR > 0) c.logear(c.refuerzoNombre + " recibe " + danoR + " dano residual!");
        c.verificarDespertar();
    }

    private static void avanzarGauge(EstadoCombate c, java.util.List<DatoDigimon> party) {
        DatoDigimon a = lado(party, 0);
        DatoDigimon b = lado(party, 1);
        DatoDigimon cc = lado(party, 2);
        int spdA = a != null ? spdEf(a, c) : 5;
        int spdB = b != null ? spdEf(b, c) : 5;
        int spdC = cc != null ? spdEf(cc, c) : 5;
        int spdE = c.enemigoSpd;
        int spdR = c.refuerzoSpd;

        c.gaugeA += spdA * 10 + RNG.nextInt(20);
        c.gaugeB += spdB * 10 + RNG.nextInt(20);
        c.gaugeC += spdC * 10 + RNG.nextInt(20);
        c.gaugeEnemigo += spdE * 10 + RNG.nextInt(20);
        c.gaugeRefuerzo += spdR * 10 + RNG.nextInt(20);

        if (c.gaugeA > EstadoCombate.GAUGE_MAX) c.gaugeA = EstadoCombate.GAUGE_MAX;
        if (c.gaugeB > EstadoCombate.GAUGE_MAX) c.gaugeB = EstadoCombate.GAUGE_MAX;
        if (c.gaugeC > EstadoCombate.GAUGE_MAX) c.gaugeC = EstadoCombate.GAUGE_MAX;
        if (c.gaugeEnemigo > EstadoCombate.GAUGE_MAX) c.gaugeEnemigo = EstadoCombate.GAUGE_MAX;
        if (c.gaugeRefuerzo > EstadoCombate.GAUGE_MAX) c.gaugeRefuerzo = EstadoCombate.GAUGE_MAX;
    }

    private static void atacarConDigimon(EstadoCombate c, DatoDigimon d, int ladoIdx) {
        if (d == null) return;
        if (hpLado(c, ladoIdx) <= 0) return;

        boolean especial = ladoIdx == 0 ? c.specialActivo_A : ladoIdx == 1 ? c.specialActivo_B : c.specialActivo_C;
        boolean forzado  = ladoIdx == 0 ? c.attackOrden_A   : ladoIdx == 1 ? c.attackOrden_B   : c.attackOrden_C;

        boolean atacaRef = c.refuerzoVivo() && (!c.enemigoVivo() || c.pctHpRefuerzo() < c.pctHpEnemigo());
        int defTarget = atacaRef ? c.refuerzoDef : c.enemigoDef;
        String elemTgt = atacaRef ? c.refuerzoElemento : c.enemigoElemento;
        String nomTgt = atacaRef ? c.refuerzoNombre : c.enemigoNombre;
        Set<EstadoAlterado> tgtEstados = atacaRef ? c.refuerzoEstados : c.enemigosEstados;

        double multElem = EnemigoCombate.multElemental(d.elemento, elemTgt);

        boolean muyCritico = RNG.nextDouble() < (PCT_MUY_CRIT_BASE + wisEf(d, c) * PCT_MUY_CRIT_WIS_SCALE);
        boolean critico    = !muyCritico && RNG.nextDouble() < (PCT_CRIT_BASE + wisEf(d, c) * PCT_CRIT_WIS_SCALE);
        boolean fallo      = RNG.nextDouble() < (PCT_FALLO_BASE + defTarget * PCT_FALLO_DEF_SCALE);

        int base;
        boolean usoHab = ladoIdx == 0 && c.habMult_A > 0;
        String nombreHab = usoHab ? c.habNombre_A : "";
        if (usoHab) {
            base = Math.max(1, (int) (atkEf(d, c) * c.habMult_A * multElem));
            c.habMult_A = 0;
            c.habNombre_A = "";
        } else if (c.exeFusion && c.turnosExeRestantes > 0) {
            base = (int)(atkEf(d, c) * 4.0 * multElem);
        } else if (especial) {
            base = (int)(atkEf(d, c) * 2.5 * multElem);
        } else if (forzado) {
            base = (int)(atkEf(d, c) * 1.6 * multElem);
        } else {
            double ratio = switch (ladoIdx == 0 ? c.tacticaA : ladoIdx == 1 ? c.tacticaB : c.tacticaC) {
                case AGRESIVO   -> 1.4;
                case DEFENSIVO   -> 0.7;
                default          -> 1.0;
            };
            base = Math.max(1, atkEf(d, c) - defTarget / 2);
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
        if (ladoIdx == 0 && c.habSuicida_A && !fallo) {
            c.habSuicida_A = false;
            setHpLado(c, 0, 0);
            c.logear("¡" + d.nombre + " se autodestruyo!");
        }

        String tag = switch (tipo) {
            case MUY_CRITICO -> "<<<< MUERTE >>>>";
            case CRITICO     -> "<CRIT!>";
            case FALLIDO     -> "<FALLO>";
            default          -> "";
        };

        if (especial) tag += "[ESP]";
        if (forzado)  tag += "[ORD]";
        if (usoHab) tag += "[HAB:" + nombreHab + "]";
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

    private static void enemigoAtaca(EstadoCombate c, java.util.List<DatoDigimon> party, boolean esRef) {
        int atk    = esRef ? c.refuerzoAtk : c.enemigoAtk;
        String nom = esRef ? c.refuerzoNombre : c.enemigoNombre;
        String elem = esRef ? c.refuerzoElemento : c.enemigoElemento;
        boolean fuerte = (c.ronda % 4 == 0);
        if (esRef) c.refuerzoCargando = fuerte; else c.enemigoCargando = fuerte;

        // Objetivo: el lado vivo con menor fracción de HP.
        int objetivo = -1;
        double peorFrac = 2.0;
        for (int i = 0; i < 3; i++) {
            DatoDigimon cand = lado(party, i);
            int hp = hpLado(c, i);
            if (cand == null || hp <= 0) continue;
            double frac = (double) hp / Math.max(1, cand.maxHp);
            if (frac < peorFrac) {
                peorFrac = frac;
                objetivo = i;
            }
        }
        if (objetivo == -1) return;

        DatoDigimon obj = lado(party, objetivo);
        if (obj == null) return;

        boolean guard = objetivo == 0 ? c.guardActivo_A : objetivo == 1 ? c.guardActivo_B : c.guardActivo_C;
        double multE  = EnemigoCombate.multElemental(elem, obj.elemento);
        boolean critico = RNG.nextDouble() < 0.08;
        boolean fallo   = RNG.nextDouble() < 0.05;

        int dano;
        if (fallo) {
            c.logear(nom + " ataca a " + obj.nombre + " pero FALLA!");
            dano = 0;
        } else {
            dano = Math.max(1, (int)(Math.max(1, atk - defEf(obj, c) / 2) * multE
                    * (guard ? 0.4 : 1.0)
                    * (fuerte ? 2.0 : 1.0)
                    * (critico ? 1.5 : 1.0)
                    * (0.85 + RNG.nextDouble() * 0.3)));
        }

        setHpLado(c, objetivo, Math.max(0, hpLado(c, objetivo) - dano));

        String tag = critico ? " <CRIT!>" : (fuerte ? " [!FUERTE!]" : "") + (guard ? "(BLOQUEADO)" : "");
        if (!fallo) {
            Set<EstadoAlterado> objEstados = objetivo == 0 ? c.estadosA : objetivo == 1 ? c.estadosB : c.estadosC;
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

    /**
     * Siguiente oleada: genera un salvaje fresco de la zona y lo pone como
     * enemigo activo. Retorna false si no se pudo (no debería pasar).
     */
    private static boolean reponerEnemigo(EstadoCombate c) {
        EstadoCombate tmp = EnemigoCombate.generarCombate(Math.max(1, c.nivelZona));
        if (tmp == null) return false;
        c.enemigoNombre = tmp.enemigoNombre;
        c.enemigoElemento = tmp.enemigoElemento;
        c.enemigoHp = tmp.enemigoHp;
        c.enemigoMaxHp = tmp.enemigoMaxHp;
        c.enemigoAtk = tmp.enemigoAtk;
        c.enemigoDef = tmp.enemigoDef;
        c.enemigoSpd = tmp.enemigoSpd;
        c.enemigoWis = tmp.enemigoWis;
        c.enemigoExp = tmp.enemigoExp;
        c.enemigoCargando = false;
        c.enemigosEstados.clear();
        c.turnosQuemado = 0; c.turnosVeneno = 0; c.turnosParalisis = 0; c.turnosDormido = 0;
        c.gaugeEnemigo = Math.max(0, EstadoCombate.GAUGE_MAX - c.enemigoSpd * 8 + RNG.nextInt(16));
        c.refuerzoActivo = false;
        c.refuerzoAnunciado = false;
        c.colaRestante--;
        c.oleadaActual++;
        c.logear("¡Otro salvaje aparece! " + c.enemigoNombre + " [" + c.enemigoElemento + "]"
            + " (oleada " + c.oleadaActual + "/" + c.totalOleada + ")");
        return true;
    }

    private static void comprobarRefuerzo(EstadoCombate c) {
        if (c.refuerzoActivo || c.refuerzoMaxHp == 0) return;
        // Con oleadas pendientes no hay refuerzo (ya viene más compañía).
        if (c.colaRestante > 0) return;
        if (c.pctHpEnemigo() <= PCT_HP_REFUERZO || c.ronda >= RONDA_REFUERZO) {
            c.refuerzoActivo = true;
            c.refuerzoAnunciado = false;
            c.gaugeRefuerzo = 0;
            c.logear(">> UN " + c.refuerzoNombre + " [" + c.refuerzoElemento + "] SALVAJE APARECE! <<");
        }
    }

    private static void aplicarVictoria(EstadoCombate c, java.util.List<DatoDigimon> party) {
        // expTotal ya trae oleadas cobradas + bonus combo.
        boolean[] vivos = {c.aVivo(), c.bVivo(), c.cVivo()};
        int[] hps = {c.hpCombateA, c.hpCombateB, c.hpCombateC};
        for (int i = 0; i < 3; i++) {
            DatoDigimon d = lado(party, i);
            if (d != null && vivos[i]) {
                d.victorias++;
                d.lazo = Math.min(100, d.lazo + 2);
                d.hp = hps[i];
                d.energia = Math.max(10, d.energia - 15);
                d.abi = Math.min(100, d.abi + 1);
            }
        }
    }

    /**
     * Derrota en encuentro salvaje/jefe (NO PvP): derrotas/energía/lazo como
     * siempre + los CAÍDOS pierden 20% de stats (10% con Calumon vivo).
     */
    public static void aplicarDerrota(java.util.List<DatoDigimon> party, EstadoCombate c) {
        boolean pvp = c != null && c.esPvP;
        boolean calumon = c != null && c.calumonDobla;
        double factor = calumon ? 0.9 : 0.8;
        for (int i = 0; i < 3; i++) {
            DatoDigimon d = lado(party, i);
            if (d != null) {
                d.derrotas++;
                d.energia = Math.max(5, d.energia - 25);
                d.lazo = Math.max(0, d.lazo - 3);
                if (!pvp && d.hpCombate <= 0) {
                    d.atk = Math.max(1, (int) (d.atk * factor));
                    d.def = Math.max(1, (int) (d.def * factor));
                    d.spd = Math.max(1, (int) (d.spd * factor));
                    d.wis = Math.max(1, (int) (d.wis * factor));
                    d.maxHp = Math.max(10, (int) (d.maxHp * factor));
                    d.hp = Math.min(d.hp, d.maxHp);
                    if (c != null) {
                        c.logear(d.nombre + " cae derrotado: -" + (calumon ? "10" : "20")
                            + "% stats" + (calumon ? " (Calumon protege)" : "") + ".");
                    }
                }
            }
        }
    }

    /** Compatibilidad: derrota sin contexto (cuenta como salvaje sin Calumon). */
    public static void aplicarDerrota(java.util.List<DatoDigimon> party) {
        aplicarDerrota(party, null);
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
