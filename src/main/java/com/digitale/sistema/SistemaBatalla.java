package com.digitale.sistema;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EnemigoCombate;
import com.digitale.datos.EstadoCombate;
import com.digitale.datos.EstadoCombate.Actor;
import com.digitale.datos.EstadoCombate.Enemigo;
import com.digitale.datos.EstadoCombate.EstadoAlterado;
import com.digitale.datos.EstadoCombate.Fase;
import com.digitale.datos.TablaTipos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Motor de combate por turnos (ATB) de DigiTale.
 *
 * <p>Un encuentro enfrenta a la party (máx 3) contra un <b>grupo de 1 a 5
 * salvajes</b> que aparecen todos a la vez. Cada actor tiene su barra; quien
 * llega antes a {@link EstadoCombate#GAUGE_MAX} actúa. Cuando le toca al líder
 * (aliado 0) el motor se detiene y espera la orden del jugador.</p>
 */
public class SistemaBatalla {

    private static final Logger LOGGER = Logger.getLogger(SistemaBatalla.class.getName());
    private static final Random RNG = new Random();

    private static final int MAX_OLEADAS = 5; // ahora = tamaño máximo del grupo
    private static final int MAX_PASOS_PROCESO = 600;
    private static final int MAX_TICKS = 4000;

    // ── Economía OP / CP ────────────────────────────────────────────────
    private static final int OP_POR_TURNO     = 8;
    private static final int OP_POR_IMPACTO   = 12;
    private static final int OP_POR_APOYO     = 25;
    private static final int OP_POR_GUARDIA   = 20;
    private static final int CP_POR_IMPACTO   = 6;
    private static final int CP_POR_DEBILIDAD = 10;
    private static final int CP_POR_CRITICO   = 5;
    private static final int CP_POR_GUARDIA   = 8;
    private static final int CP_POR_APOYO     = 25;
    private static final int CP_POR_DANO      = 3;
    private static final int CP_POR_ANALIZAR  = 5;

    // ── Probabilidades ──────────────────────────────────────────────────
    private static final double PCT_CRIT_BASE      = 0.05;
    private static final double PCT_CRIT_WIS_SCALE = 0.004;
    private static final double PCT_MUY_CRIT_BASE  = 0.01;
    private static final double PCT_MUY_CRIT_WIS_SCALE = 0.002;
    private static final double PCT_FALLO_BASE     = 0.04;

    // ── ATB ─────────────────────────────────────────────────────────────
    private static final int AVANCE_SPD = 12;
    private static final int AVANCE_RND = 12;

    public static int getOpPorSupport() { return OP_POR_APOYO; }

    // ══════════════════════════════════════════════════════════════════
    // INICIO DE COMBATE
    // ══════════════════════════════════════════════════════════════════

    public static EstadoCombate iniciarCombate(List<DatoDigimon> party) {
        return iniciarCombate(party, null, 1);
    }

    public static EstadoCombate iniciarCombate(List<DatoDigimon> party, String especieEnemiga) {
        return iniciarCombate(party, especieEnemiga, 1);
    }

    /** Combate contra un grupo de 1..5 salvajes (todos a la vez). */
    public static EstadoCombate iniciarCombate(List<DatoDigimon> party, String especieEnemiga,
                                               int cantidadEnemigos) {
        if (party == null || party.isEmpty()) return null;
        int nivel = 1;
        for (DatoDigimon d : party) if (d != null && d.nivel > nivel) nivel = d.nivel;
        return iniciarCombate(party, especieEnemiga, cantidadEnemigos, nivel);
    }

    public static EstadoCombate iniciarCombate(List<DatoDigimon> party, String especieEnemiga,
                                               int cantidadEnemigos, int nivelZona) {
        if (party == null || party.isEmpty()) return null;
        int nivelEquipo = 1;
        for (DatoDigimon d : party) if (d != null && d.nivel > nivelEquipo) nivelEquipo = d.nivel;
        int zona = Math.max(1, nivelZona);

        EstadoCombate c = new EstadoCombate();
        c.party = new ArrayList<>(party);
        prepararLados(c);
        c.enemigos.addAll(EnemigoCombate.generarGrupo(zona, especieEnemiga, cantidadEnemigos));
        c.nivelZona = zona;
        c.objetivoIdx = 0;
        c.opA = c.opB = c.opC = EstadoCombate.OP_INICIAL;
        c.cp = 0;
        c.ronda = 1;
        c.rondasJugador = 0;
        c.fase = Fase.INICIATIVA;
        c.turnosEnemigo = 0;

        // Iniciativa del jugador (DigiAttack preventivo): actúa primero.
        c.actorActual = Actor.aliado(0);
        c.esperandoJugador = c.equipoVivo();
        c.gaugeA = EstadoCombate.GAUGE_MAX;
        actualizarObjetivo(c);

        c.logear("¡" + EnemigoCombate.resumenGrupo(c.enemigos) + " quiere pelear!");
        if (c.enemigos.size() > 1) {
            c.logear("Grupo de " + c.enemigos.size() + " salvajes.");
        }
        c.logear("▶ Tu turno, " + c.nombreDe(Actor.aliado(0)) + ".");
        LOGGER.log(Level.INFO, "Combate grupo=" + c.enemigos.size()
            + " (" + EnemigoCombate.resumenGrupo(c.enemigos) + ")"
            + " | nivelZona=" + zona + " | nivelEquipo=" + nivelEquipo
            + " | party=" + party.size());
        return c;
    }

    /** Entrada con reglas de sueño + banco de 3. */
    public static EstadoCombate iniciarCombatePara(AlmacenJugadores.DatosJugador datos) {
        if (datos == null || !datos.puedeCombatir()) return null;
        List<DatoDigimon> party = datos.equipoCombate();
        if (party.isEmpty()) return null;
        EstadoCombate c = iniciarCombate(party);
        if (c == null) return null;
        c.calumonDobla = datos.tieneCalumon();
        if (c.calumonDobla) c.logear("¡Calumon apoya! Stats x2.");
        aplicarDormidosIniciales(c, party);
        datos.combate = c;
        return c;
    }

    /** Encuentro salvaje: 1-5 salvajes que aparecen TODOS a la vez. */
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
        List<DatoDigimon> party = datos.equipoCombate();
        if (party.isEmpty()) return null;

        int nivelParty = 1;
        for (DatoDigimon d : party) if (d != null && d.nivel > nivelParty) nivelParty = d.nivel;
        int zona = Math.max(1, nivelZona);
        int total = tamanoEncuentro(nivelParty, zona, party.size());

        EstadoCombate c = iniciarCombate(party, especieEnemiga, total, zona);
        if (c == null) return null;
        c.calumonDobla = datos.tieneCalumon();
        if (c.calumonDobla) c.logear("¡Calumon apoya! Stats x2.");
        c.objetivo = objetivo;
        // El primer enemigo es el NPC que golpeaste (mismo modelo en escena).
        if (objetivo != null && !c.enemigos.isEmpty()) {
            c.enemigos.get(0).ref = objetivo;
            c.enemigos.get(0).propio = false;
        }
        aplicarDormidosIniciales(c, party);
        datos.combate = c;
        return c;
    }

    /** 1-5 salvajes según diferencia de nivel, tamaño del equipo y azar. */
    public static int tamanoEncuentro(int nivelParty, int nivelZona, int tamParty) {
        int n = 1;
        int diff = nivelZona - Math.max(1, nivelParty);
        if (diff >= 2) n += 2;
        else if (diff >= 0) n += 1;
        if (tamParty >= 3) n += 1;
        n += RNG.nextInt(2);
        return Math.max(1, Math.min(MAX_OLEADAS, n));
    }

    private static void prepararLados(EstadoCombate c) {
        c.hpCombateA = hpInicial(lado(c.party, 0));
        c.hpCombateB = hpInicial(lado(c.party, 1));
        c.hpCombateC = hpInicial(lado(c.party, 2));
        c.estadosA.clear();
        c.estadosB.clear();
        c.estadosC.clear();
    }

    private static int hpInicial(DatoDigimon d) {
        if (d == null || !d.vivo || d.hp <= 0) return 0;
        return Math.min(d.maxHp, Math.max(1, d.hp));
    }

    private static DatoDigimon lado(List<DatoDigimon> party, int i) {
        return (party != null && i >= 0 && i < party.size()) ? party.get(i) : null;
    }

    private static void aplicarDormidosIniciales(EstadoCombate c, List<DatoDigimon> party) {
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

    // ══════════════════════════════════════════════════════════════════
    // STATS EFECTIVAS (Calumon x2)
    // ══════════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════════
    // BUCLE DE TURNOS (ATB)
    // ══════════════════════════════════════════════════════════════════

    /**
     * Resuelve <b>un solo actor</b> (o deja el combate esperando al jugador).
     *
     * <p>La escena ({@link AnimacionesCombate}) vuelve a llamar aquí cuando
     * termina de reproducir la animación del turno; así el siguiente turno no
     * empieza hasta que se ha visto el golpe anterior.</p>
     */
    public static void procesarTurnos(EstadoCombate c) {
        if (c == null || !c.activa || c.fase == Fase.FIN_COMBATE) return;
        if (c.cinematicaActiva) return;
        // Comprobar el fin SIEMPRE (también mientras se espera al jugador: un
        // Arte Cruzado o el daño residual pueden terminar el combate).
        if (comprobarFin(c)) return;
        if (c.esperandoJugador) {
            c.fase = Fase.INICIATIVA;
            return;
        }
        c.fase = Fase.RESOLVIENDO;

        Actor actor = siguienteActor(c);
        if (actor == null) {
            c.fase = Fase.INICIATIVA;
            c.esperandoJugador = true;
            c.actorActual = Actor.aliado(primerAliadoVivo(c));
            return;
        }

        c.actorActual = actor;
        boolean actua = iniciarTurno(c, actor);

        if (actor.aliado()) {
            // El jugador controla a los TRES aliados (modelo Time Stranger).
            // Si está incapacitado, se pierde el turno y sigue el siguiente.
            if (!actua) {
                finalizarTurno(c, actor);
                return;
            }
            c.esperandoJugador = true;
            c.fase = Fase.INICIATIVA;
            actualizarObjetivo(c);
            c.logear("▶ Turno de " + c.nombreDe(actor) + " (" + letra(actor.idx())
                + "). Elige una orden.");
            return;
        }

        if (actua) turnoEnemigo(c, actor.idx());
        finalizarTurno(c, actor);
    }

    /** Letra del slot (A/B/C) para los mensajes. */
    private static String letra(int idx) {
        return switch (idx) {
            case 0 -> "A";
            case 1 -> "B";
            default -> "C";
        };
    }

    private static int primerAliadoVivo(EstadoCombate c) {
        for (int i = 0; i < 3; i++) if (c.vivo(Actor.aliado(i))) return i;
        return 0;
    }

    /** Avanza las barras hasta que alguien esté listo y lo devuelve. */
    private static Actor siguienteActor(EstadoCombate c) {
        for (int tick = 0; tick < MAX_TICKS; tick++) {
            avanzarGauges(c);
            Actor mejor = null;
            int mejorGauge = -1;
            int mejorSpd = -1;
            for (Actor a : actoresVivos(c)) {
                int g = c.gaugeDe(a);
                if (g < EstadoCombate.GAUGE_MAX) continue;
                int spd = spdDe(c, a);
                if (g > mejorGauge || (g == mejorGauge && spd > mejorSpd)) {
                    mejor = a;
                    mejorGauge = g;
                    mejorSpd = spd;
                }
            }
            if (mejor != null) return mejor;
        }
        return null;
    }

    private static List<Actor> actoresVivos(EstadoCombate c) {
        List<Actor> out = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Actor a = Actor.aliado(i);
            if (c.vivo(a)) out.add(a);
        }
        for (int i = 0; i < c.enemigos.size(); i++) {
            Actor a = Actor.enemigo(i);
            if (c.vivo(a)) out.add(a);
        }
        return out;
    }

    private static int spdDe(EstadoCombate c, Actor a) {
        if (!a.aliado()) {
            Enemigo e = c.enemigo(a.idx());
            return e != null ? Math.max(1, e.spd) : 1;
        }
        DatoDigimon d = c.digimonDe(a.idx());
        return d != null ? Math.max(1, spdEf(d, c)) : 1;
    }

    private static void avanzarGauges(EstadoCombate c) {
        for (int i = 0; i < 3; i++) {
            Actor a = Actor.aliado(i);
            if (!c.vivo(a)) continue;
            int g = c.gaugeDe(a);
            int inc = spdDe(c, a) * AVANCE_SPD + RNG.nextInt(AVANCE_RND);
            c.setGauge(a, Math.min(EstadoCombate.GAUGE_MAX, g + inc));
        }
        for (int i = 0; i < c.enemigos.size(); i++) {
            Enemigo e = c.enemigos.get(i);
            if (e == null || !e.vivo()) continue;
            int inc = Math.max(1, e.spd) * AVANCE_SPD + RNG.nextInt(AVANCE_RND);
            e.gauge = Math.min(EstadoCombate.GAUGE_MAX, e.gauge + inc);
        }
    }

    /**
     * Prepara el turno de un actor: guardia, daño residual, estados y OP pasivo.
     * @return false si pierde el turno (dormido, aturdido, paralizado o KO).
     */
    private static boolean iniciarTurno(EstadoCombate c, Actor actor) {
        if (!c.vivo(actor)) return false;

        if (actor.aliado()) {
            c.setGuardia(actor.idx(), false); // la guardia dura hasta tu próximo turno
            int residual = switch (actor.idx()) {
                case 0 -> c.danoResidualA();
                case 1 -> c.danoResidualB();
                default -> c.danoResidualC();
            };
            if (residual > 0) {
                c.logear(c.nombreDe(actor) + " sufre " + residual + " de daño residual"
                    + EstadoCombate.estadosToString(c.estadosDeAliado(actor.idx())) + ".");
            }
            if (!c.vivo(actor)) {
                c.logear(c.nombreDe(actor) + " cae por el daño residual.");
                return false;
            }
            Set<EstadoAlterado> estados = c.estadosDeAliado(actor.idx());
            if (incapacitado(c, actor, estados, true)) return false;
            c.setOp(actor.idx(), c.opDe(actor.idx()) + OP_POR_TURNO);
            return true;
        }

        Enemigo e = c.enemigo(actor.idx());
        if (e == null) return false;
        int residual = e.tickEstados();
        if (residual > 0) {
            c.logear(e.nombre + " sufre " + residual + " de daño residual"
                + EstadoCombate.estadosToString(e.estados) + ".");
        }
        if (!e.vivo()) {
            c.logear("¡" + e.nombre + " cae por el daño residual!");
            return false;
        }
        if (e.estados.contains(EstadoAlterado.DORMIDO)) {
            e.turnosDormido--;
            boolean despierta = e.turnosDormido <= 0 || RNG.nextDouble() < 0.30;
            if (despierta) {
                e.estados.remove(EstadoAlterado.DORMIDO);
                c.logear(e.nombre + " se despertó.");
            } else {
                c.logear(e.nombre + " sigue dormido.");
            }
            return false;
        }
        if (e.estados.contains(EstadoAlterado.ATURDIDO)) {
            e.estados.remove(EstadoAlterado.ATURDIDO);
            c.logear(e.nombre + " está aturdido y pierde el turno.");
            return false;
        }
        if (e.estados.contains(EstadoAlterado.PARALISIS)) {
            e.turnosParalisis--;
            if (e.turnosParalisis <= 0) {
                e.estados.remove(EstadoAlterado.PARALISIS);
            } else if (RNG.nextDouble() < 0.25) {
                c.logear(e.nombre + " está paralizado y no puede moverse.");
                return false;
            }
        }
        return true;
    }

    private static boolean incapacitado(EstadoCombate c, Actor actor,
                                        Set<EstadoAlterado> estados, boolean esAliado) {
        int i = actor.idx();
        if (estados.contains(EstadoAlterado.DORMIDO)) {
            int t = turnosDormido(c, i) - 1;
            setTurnosDormido(c, i, t);
            if (t <= 0 || RNG.nextDouble() < 0.30) {
                estados.remove(EstadoAlterado.DORMIDO);
                c.logear(c.nombreDe(actor) + " se despertó.");
            } else {
                c.logear(c.nombreDe(actor) + " sigue dormido (" + t + ").");
            }
            return true;
        }
        if (estados.contains(EstadoAlterado.ATURDIDO)) {
            int t = turnosAturdido(c, i) - 1;
            setTurnosAturdido(c, i, t);
            if (t <= 0) estados.remove(EstadoAlterado.ATURDIDO);
            c.logear(c.nombreDe(actor) + " está aturdido y pierde el turno.");
            return true;
        }
        if (estados.contains(EstadoAlterado.PARALISIS)) {
            int t = turnosParalisis(c, i) - 1;
            setTurnosParalisis(c, i, t);
            if (t <= 0) {
                estados.remove(EstadoAlterado.PARALISIS);
            } else if (RNG.nextDouble() < 0.25) {
                c.logear(c.nombreDe(actor) + " está paralizado y no puede moverse.");
                return true;
            }
        }
        return false;
    }

    private static void finalizarTurno(EstadoCombate c, Actor actor) {
        c.setGauge(actor, 0);
        if (actor.aliado()) {
            c.rondasJugador++;
            c.ronda = c.rondasJugador + 1;
            c.comboCount = 0;
            c.esperandoJugador = false;
        }
        if (c.actorActual != null && c.actorActual.equals(actor)) c.actorActual = null;
    }

    // ══════════════════════════════════════════════════════════════════
    // IA ALIADA (aliados 1 y 2)
    // ══════════════════════════════════════════════════════════════════

    // ══════════════════════════════════════════════════════════════════
    // IA ENEMIGA (carga + ataque fuerte)
    // ══════════════════════════════════════════════════════════════════

    private static void turnoEnemigo(EstadoCombate c, int idx) {
        Enemigo e = c.enemigo(idx);
        if (e == null || !e.vivo()) return;
        c.turnosEnemigo++;

        if (e.cargando) {
            e.cargando = false;
            c.logear("*** ¡" + e.nombre + " DESATA SU ATAQUE CARGADO! ***");
            ataqueDeEnemigo(c, idx, 2.1);
            return;
        }
        if (c.turnosEnemigo % 4 == 0) {
            e.cargando = true;
            c.logear("⚠ " + e.nombre + " está CARGANDO energía... ¡protégete!");
            return;
        }
        ataqueDeEnemigo(c, idx, 1.0);
    }

    // ══════════════════════════════════════════════════════════════════
    // OBJETIVOS
    // ══════════════════════════════════════════════════════════════════

    private static double ventaja(String elem, String atr, Enemigo e) {
        if (e == null) return 1.0;
        return TablaTipos.multTotal(elem, atr, e.elemento, e.atributo);
    }

    /** Mejor enemigo para un ataque: máxima ventaja de tipo y, a igualdad, menos HP. */
    public static Enemigo mejorObjetivo(EstadoCombate c, String elem, String atr) {
        Enemigo mejor = null;
        double mejorPunt = -1;
        for (Enemigo e : c.enemigos) {
            if (e == null || !e.vivo()) continue;
            double punt = ventaja(elem, atr, e) * 2.0 + (1.0 - e.pctHp());
            if (punt > mejorPunt) {
                mejorPunt = punt;
                mejor = e;
            }
        }
        return mejor;
    }

    /** Reajusta el objetivo por defecto (el que se marca en el HUD). */
    private static void actualizarObjetivo(EstadoCombate c) {
        DatoDigimon activo = null;
        if (c.actorActual != null && c.actorActual.aliado()) {
            activo = c.digimonDe(c.actorActual.idx());
        }
        if (activo == null) activo = c.digimonDe(primerAliadoVivo(c));
        Enemigo mejor = activo != null
            ? mejorObjetivo(c, activo.elemento, activo.atributo)
            : (c.enemigosVivos().isEmpty() ? null : c.enemigosVivos().get(0));
        if (mejor == null) return;
        int idx = c.enemigos.indexOf(mejor);
        if (idx >= 0) c.objetivoIdx = idx;
    }

    /** Fija el objetivo a un enemigo concreto (acción gratuita). */
    public static boolean seleccionarObjetivo(EstadoCombate c, int idx) {
        if (c == null || !c.activa || c.fase == Fase.FIN_COMBATE) return false;
        if (c.cinematicaActiva) return false;
        Enemigo e = c.enemigo(idx);
        if (e == null || !e.vivo()) return false;
        c.objetivoIdx = idx;
        c.logear("🎯 Objetivo: " + e.nombre + " [" + e.elemento + "/" + e.atributo
            + "] HP " + e.hp + "/" + e.maxHp);
        return true;
    }

    /** Cicla el objetivo entre los enemigos vivos (acción gratuita). */
    public static boolean cambiarObjetivo(EstadoCombate c) {
        if (c == null || !c.activa || c.fase == Fase.FIN_COMBATE) return false;
        if (c.cinematicaActiva) return false;
        List<Integer> vivos = new ArrayList<>();
        for (int i = 0; i < c.enemigos.size(); i++) {
            Enemigo e = c.enemigos.get(i);
            if (e != null && e.vivo()) vivos.add(i);
        }
        if (vivos.isEmpty()) return false;
        int pos = vivos.indexOf(c.objetivoIdx);
        c.objetivoIdx = vivos.get((pos + 1) % vivos.size());
        Enemigo e = c.enemigo(c.objetivoIdx);
        if (e != null) {
            c.logear("🎯 Objetivo: " + e.nombre + " [" + e.elemento + "/" + e.atributo
                + "] HP " + e.hp + "/" + e.maxHp);
        }
        return true;
    }

    // ══════════════════════════════════════════════════════════════════
    // ATAQUES
    // ══════════════════════════════════════════════════════════════════

    /** Registra un golpe para que la escena lo anime (foco + Attack/Hurt/Death). */
    private static void registrarEvento(EstadoCombate c, Actor atacante, Actor objetivo,
                                        int dano, boolean muerte) {
        if (c == null || atacante == null || objetivo == null) return;
        // La cola es solo visual: si nadie la consume (p. ej. sin escena),
        // no debe crecer sin límite.
        if (c.eventos.size() >= 32) c.eventos.clear();
        c.eventos.add(new EstadoCombate.EventoVisual(
            atacante.aliado(), atacante.idx(),
            objetivo.aliado(), objetivo.idx(), dano, muerte));
    }

    private static void ataqueDeAliado(EstadoCombate c, Actor actor, DatoDigimon d,
                                       Enemigo objetivo, double multBase, String nombreAtaque) {
        if (objetivo == null || !objetivo.vivo()) {
            c.logear(d.nombre + " ataca al aire: no quedan enemigos.");
            return;
        }
        String nomTgt = objetivo.nombre;
        double multTipo = TablaTipos.multTotal(d.elemento, d.atributo,
            objetivo.elemento, objetivo.atributo);

        boolean fallo = RNG.nextDouble() < PCT_FALLO_BASE;
        boolean muyCritico = RNG.nextDouble()
            < (PCT_MUY_CRIT_BASE + wisEf(d, c) * PCT_MUY_CRIT_WIS_SCALE);
        boolean critico = !muyCritico && RNG.nextDouble()
            < (PCT_CRIT_BASE + wisEf(d, c) * PCT_CRIT_WIS_SCALE);

        double base = Math.max(1, atkEf(d, c) * Math.max(0.1, multBase) - objetivo.def * 0.35);
        int suelo = Math.max(1, objetivo.maxHp / 20);

        int dano;
        EstadoCombate.TipoDano tipo;
        if (multTipo <= TablaTipos.INMUNE) {
            tipo = EstadoCombate.TipoDano.INMUNE;
            dano = 0;
        } else if (fallo) {
            tipo = EstadoCombate.TipoDano.FALLIDO;
            dano = 0;
        } else if (muyCritico) {
            tipo = EstadoCombate.TipoDano.MUY_CRITICO;
            dano = Math.max(suelo, (int) (base * multTipo * 1.8 * (0.85 + RNG.nextDouble() * 0.3)));
        } else if (critico) {
            tipo = EstadoCombate.TipoDano.CRITICO;
            dano = Math.max(suelo, (int) (base * multTipo * 1.4 * (0.85 + RNG.nextDouble() * 0.3)));
        } else if (multTipo <= TablaTipos.RESISTE) {
            tipo = EstadoCombate.TipoDano.RESISTIDO;
            dano = Math.max(1, (int) (base * multTipo * (0.9 + RNG.nextDouble() * 0.2)));
        } else {
            tipo = EstadoCombate.TipoDano.NORMAL;
            dano = Math.max(suelo, (int) (base * multTipo * (0.9 + RNG.nextDouble() * 0.2)));
        }

        objetivo.hp = Math.max(0, objetivo.hp - dano);
        c.danoTotalAcumulado += dano;

        if (dano > 0) {
            int cp = CP_POR_IMPACTO;
            if (multTipo >= TablaTipos.FUERTE) cp += CP_POR_DEBILIDAD;
            if (critico || muyCritico) cp += CP_POR_CRITICO;
            c.ganarCp(cp);
            if (actor.aliado()) c.setOp(actor.idx(), c.opDe(actor.idx()) + OP_POR_IMPACTO);
            c.comboCount++;
            if (c.comboCount > c.comboMax) c.comboMax = c.comboCount;
        }

        String tag = switch (tipo) {
            case MUY_CRITICO -> " <<MUERTE>>";
            case CRITICO -> " <CRIT!>";
            case FALLIDO -> " <FALLO>";
            case RESISTIDO -> " [RESISTIDO]";
            case INMUNE -> " [INMUNE]";
            default -> "";
        };
        String etiqueta = TablaTipos.etiqueta(multTipo);
        if (!etiqueta.isEmpty() && dano > 0) tag += " [" + etiqueta + "]";
        if (c.comboCount > 1) tag += " (combo x" + c.comboCount + ")";

        String pref = (nombreAtaque == null || nombreAtaque.isBlank()) ? "" : " [" + nombreAtaque + "]";
        c.logear(d.nombre + pref + " > " + nomTgt + "  -" + dano + " HP" + tag);

        int idxTgt = c.enemigos.indexOf(objetivo);
        if (idxTgt >= 0) {
            registrarEvento(c, actor, Actor.enemigo(idxTgt), dano, !objetivo.vivo());
        }
        if (!objetivo.vivo()) {
            c.logear("¡" + nomTgt + " ha caído!");
            actualizarObjetivo(c);
        }
    }

    private static void ataqueDeEnemigo(EstadoCombate c, int enemigoIdx, double multBase) {
        Enemigo atacante = c.enemigo(enemigoIdx);
        if (atacante == null || !atacante.vivo()) return;

        Actor tgt = elegirObjetivoAliado(c);
        if (tgt == null) return;
        DatoDigimon obj = c.digimonDe(tgt.idx());
        if (obj == null) return;

        boolean guard = c.guardiaDe(tgt.idx());
        double multTipo = TablaTipos.multTotal(atacante.elemento, atacante.atributo,
            obj.elemento, obj.atributo);
        boolean critico = RNG.nextDouble() < 0.08;
        boolean fallo = RNG.nextDouble() < 0.05;

        int dano;
        if (multTipo <= TablaTipos.INMUNE) {
            dano = 0;
            c.logear(atacante.nombre + " ataca a " + obj.nombre
                + " pero es INMUNE a " + atacante.elemento + ".");
        } else if (fallo) {
            dano = 0;
            c.logear(atacante.nombre + " ataca a " + obj.nombre + " pero FALLA!");
        } else {
            double base = Math.max(1, atacante.atk * multBase - defEf(obj, c) * 0.35);
            double bruto = Math.max(Math.max(1, obj.maxHp / 20),
                base * multTipo * (critico ? 1.5 : 1.0) * (0.85 + RNG.nextDouble() * 0.3));
            dano = Math.max(1, (int) (bruto * (guard ? 0.4 : 1.0)));

            String tag = (critico ? " <CRIT!>" : "") + (guard ? "(BLOQUEADO)" : "");
            String etiqueta = TablaTipos.etiqueta(multTipo);
            if (!etiqueta.isEmpty()) tag += " [" + etiqueta + "]";
            c.logear(atacante.nombre + " > " + obj.nombre + "  -" + dano + " HP" + tag);
        }

        if (dano <= 0) return;
        c.setHpDe(tgt, Math.max(0, c.hpDe(tgt) - dano));
        c.danoTotalAcumulado += dano;
        c.comboCount = 0;
        c.ganarCp(CP_POR_DANO);
        c.setOp(tgt.idx(), c.opDe(tgt.idx()) + OP_POR_TURNO);

        Set<EstadoAlterado> estados = c.estadosDeAliado(tgt.idx());
        if (critico && c.vivo(tgt) && RNG.nextDouble() < 0.20) {
            EstadoAlterado e = multBase > 1.5 ? EstadoAlterado.ENVENENADO_FUERTE : EstadoAlterado.VENENO;
            estados.add(e);
            setTurnosVeneno(c, tgt.idx(), Math.max(turnosVeneno(c, tgt.idx()), 4));
            c.logear(obj.nombre + " queda " + (e == EstadoAlterado.ENVENENADO_FUERTE
                ? "ENVENENADO FUERTE" : "envenenado") + ".");
        }

        registrarEvento(c, Actor.enemigo(enemigoIdx), tgt, dano, !c.vivo(tgt));
        if (!c.vivo(tgt)) c.logear("¡" + obj.nombre + " ha caído!");
    }

    /** 50% al que está peor de HP, 50% al azar (así la guardia y el daño se reparten). */
    private static Actor elegirObjetivoAliado(EstadoCombate c) {
        List<Actor> vivos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Actor a = Actor.aliado(i);
            if (c.vivo(a)) vivos.add(a);
        }
        if (vivos.isEmpty()) return null;
        if (RNG.nextDouble() < 0.50) {
            Actor peor = vivos.get(0);
            double peorFrac = 2.0;
            for (Actor a : vivos) {
                double f = c.pctHpDe(a);
                if (f < peorFrac) {
                    peorFrac = f;
                    peor = a;
                }
            }
            return peor;
        }
        return vivos.get(RNG.nextInt(vivos.size()));
    }

    // ══════════════════════════════════════════════════════════════════
    // MOVIMIENTOS DEL JUGADOR Y LA IA
    // ══════════════════════════════════════════════════════════════════

    /** Ejecuta un Movimiento del líder (aliado 0). Consume OP y el turno. */
    public static boolean ejecutarMovimiento(EstadoCombate c, DatoDigimon a,
                                             DatoDigimon.Movimiento m) {
        return ejecutarMovimiento(c, a, m, -1);
    }

    /**
     * Ejecuta un movimiento sobre el enemigo elegido (índice en el grupo).
     * @param objetivoIdx -1 = objetivo automático
     */
    public static boolean ejecutarMovimiento(EstadoCombate c, DatoDigimon a,
                                             DatoDigimon.Movimiento m, int objetivoIdx) {
        if (!puedeOrdenar(c)) return false;
        if (!aliadoPuedeActuar(c)) return false;
        if (m == null) return false;
        Actor actor = c.actorActual;
        int idx = actor.idx();
        DatoDigimon d = c.digimonDe(idx);
        if (d == null) d = a;
        if (d == null) return false;

        int op = c.opDe(idx);
        if (op < m.coste) {
            c.logear("OP insuficiente para " + m.nombre + " (pide " + m.coste
                + ", tienes " + op + ").");
            return false;
        }
        c.setOp(idx, op - m.coste);
        aplicarMovimiento(c, actor, d, objetivoElegido(c, objetivoIdx), m);
        finalizarTurno(c, actor);
        // El siguiente turno lo dispara la escena al acabar la animación.
        return true;
    }

    /** Objetivo indicado por el jugador o, si no, el automático. */
    private static Enemigo objetivoElegido(EstadoCombate c, int idx) {
        if (idx >= 0) {
            Enemigo e = c.enemigo(idx);
            if (e != null && e.vivo()) {
                c.objetivoIdx = idx;
                return e;
            }
        }
        return c.objetivoActual();
    }

    private static void aplicarMovimiento(EstadoCombate c, Actor actor, DatoDigimon d,
                                          Enemigo objetivo, DatoDigimon.Movimiento m) {
        Enemigo tgt = objetivo != null && objetivo.vivo()
            ? objetivo
            : mejorObjetivo(c, d.elemento, d.atributo);
        if (m.mult > 0) {
            ataqueDeAliado(c, actor, d, tgt, m.mult, m.nombre);
        } else {
            c.logear(d.nombre + " usa " + m.nombre + ".");
        }
        if (m.efecto != null && tgt != null && tgt.vivo()) {
            if (RNG.nextDouble() < 0.75) {
                tgt.aplicarEstado(m.efecto, m.turnos);
                c.logear(tgt.nombre + " queda " + nombreEstado(m.efecto) + ".");
            } else {
                c.logear("¡Pero " + tgt.nombre + " resistió el efecto!");
            }
        }
        if ("EXPLOSIVO".equals(m.categoria) && actor.aliado()) {
            c.setHpDe(actor, 0);
            c.logear("¡" + d.nombre + " se sacrificó en la explosión!");
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // ACCIONES DEL JUGADOR (menú)
    // ══════════════════════════════════════════════════════════════════

    public static boolean puedeOrdenar(EstadoCombate c) {
        if (c == null || !c.activa || c.fase == Fase.FIN_COMBATE) return false;
        if (c.cinematicaActiva) {
            c.logear("Espera a que termine la animación de combate...");
            return false;
        }
        if (!c.esperandoJugador || c.actorActual == null || !c.actorActual.aliado()) {
            c.logear("Espera tu turno...");
            return false;
        }
        return true;
    }

    /** El aliado que tiene el turno debe estar vivo para atacar/usar habilidad. */
    private static boolean aliadoPuedeActuar(EstadoCombate c) {
        if (c.actorActual == null || !c.vivo(c.actorActual)) {
            c.logear("Ese Digimon está fuera de combate: usa CAMBIAR o Pasar.");
            return false;
        }
        return true;
    }

    /** APOYO: carga OP y CP de todo el equipo y cura un poco. Consume turno. */
    public static void support(EstadoCombate c) {
        if (!puedeOrdenar(c)) return;
        if (!aliadoPuedeActuar(c)) return;
        Actor actor = c.actorActual;
        c.ganarCp(CP_POR_APOYO);
        for (int i = 0; i < 3; i++) {
            Actor a = Actor.aliado(i);
            if (!c.vivo(a)) continue;
            c.setOp(i, c.opDe(i) + OP_POR_APOYO);
            DatoDigimon d = c.digimonDe(i);
            if (d != null) {
                int cura = Math.max(1, d.maxHp / 20);
                c.setHpDe(a, Math.min(d.maxHp, c.hpDe(a) + cura));
            }
        }
        c.logear("APOYO de " + c.nombreDe(actor) + ": el equipo se concentra (+"
            + OP_POR_APOYO + " OP, +" + CP_POR_APOYO + " CP, cura leve).");
        finalizarTurno(c, actor);
    }

    /** GUARDIA: reduce el daño recibido hasta tu próximo turno. Consume turno. */
    public static void guardar(EstadoCombate c) {
        if (!puedeOrdenar(c)) return;
        if (!aliadoPuedeActuar(c)) return;
        Actor actor = c.actorActual;
        int idx = actor.idx();
        c.setGuardia(idx, true);
        c.ganarCp(CP_POR_GUARDIA);
        c.setOp(idx, c.opDe(idx) + OP_POR_GUARDIA);
        c.logear("🛡 " + c.nombreDe(actor)
            + " se protege: recibirá 60% menos daño hasta su próximo turno.");
        finalizarTurno(c, actor);
    }

    /** ANALIZAR: revela debilidades del objetivo. NO consume turno. */
    public static boolean analizar(EstadoCombate c) {
        if (c == null || !c.activa || c.fase == Fase.FIN_COMBATE) return false;
        if (c.cinematicaActiva) return false;
        if (!c.esperandoJugador || c.actorActual == null || !c.actorActual.aliado()) {
            c.logear("Espera tu turno para analizar.");
            return false;
        }
        Enemigo e = c.objetivoActual();
        if (e == null) return false;
        if (e.analizado) {
            c.logear("Ya tienes analizado a " + e.nombre + ".");
            return false;
        }
        e.analizado = true;
        c.ganarCp(CP_POR_ANALIZAR);
        c.logear("🔍 ANÁLISIS de " + e.nombre + " [" + e.elemento + "/" + e.atributo
            + "]: " + TablaTipos.analisis(e.elemento, e.atributo));
        return true;
    }

    /** ARTE CRUZADO: todos los aliados vivos atacan a la vez. 100 CP, sin turno. */
    public static boolean arteCruzado(EstadoCombate c) {
        if (c == null || !c.activa || c.fase == Fase.FIN_COMBATE) return false;
        if (c.cinematicaActiva) return false;
        if (!c.esperandoJugador || c.actorActual == null || !c.actorActual.aliado()) {
            c.logear("Espera tu turno para el Arte Cruzado.");
            return false;
        }
        if (!aliadoPuedeActuar(c)) return false;
        if (c.cp < EstadoCombate.CP_ARTE_CRUZADO) {
            c.logear("CP insuficiente para el Arte Cruzado (" + c.cp + "/"
                + EstadoCombate.CP_ARTE_CRUZADO + ").");
            return false;
        }
        Enemigo tgt = c.objetivoActual();
        if (tgt == null) return false;
        c.cp = 0;
        c.logear(">>> ¡ARTE CRUZADO! Todo el equipo ataca al unísono. <<<");
        int total = 0;
        for (int i = 0; i < 3; i++) {
            Actor a = Actor.aliado(i);
            if (!c.vivo(a)) continue;
            DatoDigimon d = c.digimonDe(i);
            if (d == null) continue;
            double mult = TablaTipos.multTotal(d.elemento, d.atributo, tgt.elemento, tgt.atributo);
            int dano = mult <= TablaTipos.INMUNE ? 0 : Math.max(1, (int) (atkEf(d, c) * 1.8 * mult));
            total += dano;
            c.logear("  " + d.nombre + " golpea con " + d.elemento + " (-" + dano + ")");
        }
        tgt.hp = Math.max(0, tgt.hp - total);
        c.danoTotalAcumulado += total;
        c.logear(">>> " + tgt.nombre + " recibe -" + total + " HP en total <<<");
        int idxTgt = c.enemigos.indexOf(tgt);
        if (idxTgt >= 0) {
            registrarEvento(c, Actor.aliado(0), Actor.enemigo(idxTgt), total, !tgt.vivo());
        }
        if (!tgt.vivo()) {
            c.logear("¡" + tgt.nombre + " ha caído!");
            actualizarObjetivo(c);
        }
        return true;
    }

    public static void pasarTurno(EstadoCombate c) {
        if (!puedeOrdenar(c)) return;
        Actor actor = c.actorActual;
        c.logear(c.nombreDe(actor) + " pasa el turno.");
        finalizarTurno(c, actor);
    }

    /**
     * CAMBIO: reemplaza al Digimon que tiene el turno por una reserva.
     * NO consume el turno (el que entra puede atacar de inmediato).
     */
    public static boolean cambiarActivo(EstadoCombate c,
                                        AlmacenJugadores.DatosJugador datos, int benchSlot) {
        if (c == null || !c.activa || c.fase == Fase.FIN_COMBATE) return false;
        if (c.cinematicaActiva) return false;
        if (!c.esperandoJugador || c.actorActual == null || !c.actorActual.aliado()) {
            c.logear("Solo puedes cambiar en tu turno.");
            return false;
        }
        if (datos == null || !AlmacenJugadores.DatosJugador.slotValido(benchSlot)) return false;

        Actor actor = c.actorActual;
        int idx = actor.idx();
        DatoDigimon entra = datos.getSlot(benchSlot);
        if (entra == null || !entra.vivo || entra.dormido || entra.noCombate || entra.hp <= 0) {
            return false;
        }
        List<DatoDigimon> party = c.party;
        if (party.isEmpty() || idx >= party.size() || party.contains(entra)) return false;

        DatoDigimon sale = party.get(idx);
        int slotSale = -1;
        for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
            if (datos.getSlot(i) == sale) {
                slotSale = i;
                break;
            }
        }
        if (slotSale == -1) return false;

        party.set(idx, entra);
        datos.setSlot(benchSlot, sale);
        datos.setSlot(slotSale, entra);

        sale.hp = Math.max(1, c.hpDe(actor));
        c.setHpDe(actor, Math.min(entra.maxHp, Math.max(1, entra.hp)));
        c.setOp(idx, EstadoCombate.OP_INICIAL);
        c.estadosDeAliado(idx).clear();
        switch (idx) {
            case 0 -> {
                c.turnosQuemadoA = 0; c.turnosVenenoA = 0; c.turnosParalisisA = 0;
                c.turnosDormidoA = 0; c.turnosAturdidoA = 0; c.turnosSangradoA = 0;
            }
            case 1 -> {
                c.turnosQuemadoB = 0; c.turnosVenenoB = 0; c.turnosParalisisB = 0;
                c.turnosDormidoB = 0; c.turnosAturdidoB = 0; c.turnosSangradoB = 0;
            }
            default -> {
                c.turnosQuemadoC = 0; c.turnosVenenoC = 0; c.turnosParalisisC = 0;
                c.turnosDormidoC = 0; c.turnosAturdidoC = 0; c.turnosSangradoC = 0;
            }
        }
        c.setGuardia(idx, false);
        // Sigue siendo su turno: puede actuar con el que acaba de entrar.
        c.setGauge(actor, EstadoCombate.GAUGE_MAX);
        c.logear("¡Cambio! " + entra.nombre + " reemplaza a " + sale.nombre
            + " (no consume turno).");
        return true;
    }

    public static boolean huir(EstadoCombate c) {
        if (c == null || !c.activa) return false;
        if (!puedeOrdenar(c)) return false;

        int spdEq = 0;
        for (int i = 0; i < 3; i++) {
            Actor a = Actor.aliado(i);
            if (!c.vivo(a)) continue;
            DatoDigimon d = c.digimonDe(i);
            if (d != null) spdEq = Math.max(spdEq, spdEf(d, c));
        }
        int spdEn = 1;
        for (Enemigo e : c.enemigos) {
            if (e != null && e.vivo()) spdEn = Math.max(spdEn, e.spd);
        }
        double prob = 0.35 + (spdEq / (double) Math.max(1, spdEq + spdEn)) * 0.45;

        if (RNG.nextDouble() < prob) {
            c.activa = false;
            c.victoria = false;
            c.fase = Fase.FIN_COMBATE;
            c.esperandoJugador = false;
            c.logear("¡Lograste escapar!");
            return true;
        }

        c.logear("¡No pudiste huir! El enemigo aprovecha para atacar.");
        for (int i = 0; i < c.enemigos.size(); i++) {
            Enemigo e = c.enemigos.get(i);
            if (e != null && e.vivo() && RNG.nextDouble() < 0.5) {
                ataqueDeEnemigo(c, i, 1.0);
            }
        }
        finalizarTurno(c, c.actorActual != null ? c.actorActual : Actor.aliado(0));
        return false;
    }

    // ══════════════════════════════════════════════════════════════════
    // FIN DE COMBATE
    // ══════════════════════════════════════════════════════════════════

    private static boolean comprobarFin(EstadoCombate c) {
        if (!c.activa || c.fase == Fase.FIN_COMBATE) return true;

        if (!c.equipoVivo()) {
            c.victoria = false;
            c.activa = false;
            c.fase = Fase.FIN_COMBATE;
            c.esperandoJugador = false;
            c.logear("Derrota... el equipo cayó.");
            aplicarDerrota(c.party, c);
            return true;
        }

        if (!c.todosEnemigosDerrota()) return false;

        for (Enemigo e : c.enemigos) {
            if (e != null) c.expTotal += e.exp;
        }
        c.victoria = true;
        c.activa = false;
        c.fase = Fase.FIN_COMBATE;
        c.esperandoJugador = false;
        int bonusCombo = c.comboMax * 5;
        c.expTotal += bonusCombo;
        if (bonusCombo > 0) c.logear("Bonus combo: +" + bonusCombo + " XP");
        c.logear(">>> VICTORIA! +" + c.expTotal + " XP <<<");
        aplicarVictoria(c.party, c);
        return true;
    }

    private static void aplicarVictoria(List<DatoDigimon> party, EstadoCombate c) {
        for (int i = 0; i < 3; i++) {
            DatoDigimon d = lado(party, i);
            if (d == null) continue;
            Actor a = Actor.aliado(i);
            boolean vivo = c.vivo(a);
            if (vivo) {
                d.victorias++;
                d.lazo = Math.min(100, d.lazo + 2);
                d.abi = Math.min(100, d.abi + 1);
                d.hp = Math.max(1, c.hpDe(a));
                d.xp += Math.max(1, c.expTotal);
                int antes = d.nivelAv;
                d.subirNivelAventura();
                if (d.nivelAv > antes) {
                    c.logear("¡" + d.nombre + " sube a nivel de aventura " + d.nivelAv + "!");
                }
            } else {
                d.hp = Math.max(1, d.maxHp / 4);
            }
            d.energia = Math.max(10, d.energia - 15);
            d.hpCombate = d.hp;
        }
    }

    public static void aplicarDerrota(List<DatoDigimon> party, EstadoCombate c) {
        boolean pvp = c != null && c.esPvP;
        boolean calumon = c != null && c.calumonDobla;
        double factor = calumon ? 0.9 : 0.8;
        for (int i = 0; i < 3; i++) {
            DatoDigimon d = lado(party, i);
            if (d == null) continue;
            d.derrotas++;
            d.energia = Math.max(5, d.energia - 25);
            d.lazo = Math.max(0, d.lazo - 3);
            boolean cayo = c == null || c.hpDe(Actor.aliado(i)) <= 0;
            if (!pvp && cayo) {
                d.atk = Math.max(1, (int) (d.atk * factor));
                d.def = Math.max(1, (int) (d.def * factor));
                d.spd = Math.max(1, (int) (d.spd * factor));
                d.wis = Math.max(1, (int) (d.wis * factor));
                d.maxHp = Math.max(10, (int) (d.maxHp * factor));
                d.hp = Math.max(1, Math.min(d.hp, d.maxHp / 4));
                d.hpCombate = d.hp;
                if (c != null) {
                    c.logear(d.nombre + " cae derrotado: -" + (calumon ? "10" : "20")
                        + "% stats" + (calumon ? " (Calumon protege)" : "") + ".");
                }
            }
        }
    }

    public static void aplicarDerrota(List<DatoDigimon> party) {
        aplicarDerrota(party, null);
    }

    // ══════════════════════════════════════════════════════════════════
    // TURNOS: PREDICCIÓN PARA LA UI
    // ══════════════════════════════════════════════════════════════════

    /**
     * Predice los próximos {@code n} turnos sin tocar el estado real.
     * Si el combate está esperando al jugador, el primero es el líder
     * (que es quien actúa AHORA), no el siguiente de la simulación.
     */
    public static List<Actor> predecirTurnos(EstadoCombate c, int n) {
        List<Actor> out = new ArrayList<>();
        if (c == null || n <= 0) return out;

        boolean esperando = (c.esperandoJugador || c.cinematicaActiva)
            && c.actorActual != null && c.vivo(c.actorActual);
        List<Actor> actores = actoresVivos(c);
        int[] g = new int[actores.size()];
        int[] spd = new int[actores.size()];
        int[] avance = new int[actores.size()];
        for (int i = 0; i < actores.size(); i++) {
            Actor a = actores.get(i);
            g[i] = c.gaugeDe(a);
            spd[i] = spdDe(c, a);
            avance[i] = Math.max(1, spd[i]) * AVANCE_SPD + AVANCE_RND / 2;
        }

        if (esperando) {
            out.add(c.actorActual);
            // El actor que tiene el turno (o lo está animando) ya reinició su barra.
            int idxActor = actores.indexOf(c.actorActual);
            if (idxActor >= 0) g[idxActor] = 0;
            if (out.size() >= n) return out;
        }

        // Un actor por tick, igual que siguienteActor(): así la cola refleja
        // de verdad el orden de turnos y no "adelanta" dos en el mismo tick.
        for (int tick = 0; tick < 4000 && out.size() < n; tick++) {
            for (int i = 0; i < actores.size(); i++) {
                g[i] = Math.min(EstadoCombate.GAUGE_MAX, g[i] + avance[i]);
            }
            int mejor = -1;
            for (int i = 0; i < actores.size(); i++) {
                if (g[i] < EstadoCombate.GAUGE_MAX) continue;
                if (mejor < 0 || g[i] > g[mejor]
                    || (g[i] == g[mejor] && spd[i] > spd[mejor])) {
                    mejor = i;
                }
            }
            if (mejor < 0) continue;
            out.add(actores.get(mejor));
            g[mejor] = 0;
        }
        return out;
    }

    // ══════════════════════════════════════════════════════════════════
    // HELPERS DE CONTADORES DE ESTADO (party)
    // ══════════════════════════════════════════════════════════════════

    private static int turnosVeneno(EstadoCombate c, int i) {
        return switch (i) {
            case 0 -> c.turnosVenenoA;
            case 1 -> c.turnosVenenoB;
            default -> c.turnosVenenoC;
        };
    }

    private static void setTurnosVeneno(EstadoCombate c, int i, int v) {
        switch (i) {
            case 0 -> c.turnosVenenoA = v;
            case 1 -> c.turnosVenenoB = v;
            default -> c.turnosVenenoC = v;
        }
    }

    private static int turnosParalisis(EstadoCombate c, int i) {
        return switch (i) {
            case 0 -> c.turnosParalisisA;
            case 1 -> c.turnosParalisisB;
            default -> c.turnosParalisisC;
        };
    }

    private static void setTurnosParalisis(EstadoCombate c, int i, int v) {
        switch (i) {
            case 0 -> c.turnosParalisisA = v;
            case 1 -> c.turnosParalisisB = v;
            default -> c.turnosParalisisC = v;
        }
    }

    private static int turnosDormido(EstadoCombate c, int i) {
        return switch (i) {
            case 0 -> c.turnosDormidoA;
            case 1 -> c.turnosDormidoB;
            default -> c.turnosDormidoC;
        };
    }

    private static void setTurnosDormido(EstadoCombate c, int i, int v) {
        switch (i) {
            case 0 -> c.turnosDormidoA = v;
            case 1 -> c.turnosDormidoB = v;
            default -> c.turnosDormidoC = v;
        }
    }

    private static int turnosAturdido(EstadoCombate c, int i) {
        return switch (i) {
            case 0 -> c.turnosAturdidoA;
            case 1 -> c.turnosAturdidoB;
            default -> c.turnosAturdidoC;
        };
    }

    private static void setTurnosAturdido(EstadoCombate c, int i, int v) {
        switch (i) {
            case 0 -> c.turnosAturdidoA = v;
            case 1 -> c.turnosAturdidoB = v;
            default -> c.turnosAturdidoC = v;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // TEXTO DE ESTADOS
    // ══════════════════════════════════════════════════════════════════

    public static String nombreEstado(EstadoAlterado e) {
        return EstadoCombate.nombreEstado(e);
    }

    public static String estadosToString(Set<EstadoAlterado> estados) {
        return EstadoCombate.estadosToString(estados);
    }
}
