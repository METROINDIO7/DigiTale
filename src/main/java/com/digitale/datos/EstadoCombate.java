package com.digitale.datos;

import com.hypixel.hytale.component.Ref;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Estado completo de un combate por turnos (ATB).
 *
 * <p><b>Grupo enemigo</b>: un encuentro tiene de 1 a 5 salvajes
 * ({@link Enemigo}) que aparecen <b>todos a la vez</b> y actúan por turnos,
 * igual que en Digimon Story. Ya no hay oleadas que se reemplazan.</p>
 *
 * <p><b>Turnos</b>: cada actor (los 3 aliados y cada salvaje) tiene una barra
 * que sube según su SPD. Quien llega antes a {@link #GAUGE_MAX} actúa y su
 * barra vuelve a cero. El jugador controla al líder (aliado 0); los aliados
 * 1–2 son IA.</p>
 *
 * <p><b>Economía</b>: OP (Order Points) por Digimon paga los movimientos; CP
 * (Critical Points) es del equipo y paga los Artes Cruzados.</p>
 */
public class EstadoCombate {

    // ══ ENUMS ══════════════════════════════════════════════════════════
    public enum Tactica { AGRESIVO, EQUILIBRADO, DEFENSIVO }

    public enum Fase { INICIATIVA, RESOLVIENDO, FIN_COMBATE }

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

    /** Lados de la party (el jugador controla A). */
    public enum Lado { A, B, C }

    /**
     * Actor de un turno: un aliado (índice 0..2) o un enemigo
     * (índice dentro de {@link #enemigos}).
     */
    public record Actor(boolean aliado, int idx) {
        public static Actor aliado(int i) { return new Actor(true, i); }
        public static Actor enemigo(int i) { return new Actor(false, i); }
        public boolean esLider() { return aliado && idx == 0; }
        public String clave() { return (aliado ? "A" : "E") + idx; }
    }

    // ══ GRUPO ENEMIGO (1-5 salvajes a la vez) ══════════════════════════
    /** Un salvaje del grupo enemigo, con su propia barra y sus estados. */
    public static final class Enemigo {
        public String nombre = "";
        public String elemento = "NEUTRO";
        public String atributo = "FREE";
        public int hp = 0;
        public int maxHp = 0;
        public int atk = 0;
        public int def = 0;
        public int spd = 0;
        public int wis = 0;
        public int exp = 0;
        public boolean cargando = false;
        public boolean analizado = false;
        public int gauge = 0;
        public Set<EstadoAlterado> estados = EnumSet.noneOf(EstadoAlterado.class);
        public int turnosQuemado = 0;
        public int turnosVeneno = 0;
        public int turnosParalisis = 0;
        public int turnosDormido = 0;
        public int turnosSangrado = 0;
        /** NPC en escena (puede ser el que golpeaste o uno spawneado). */
        public Ref<?> ref = null;
        /** ¿Lo spawneó la batalla? (los propios se limpian siempre). */
        public boolean propio = false;
        /** El rol NPC no existe: no reintentar el spawn cada tick. */
        public boolean spawnFallido = false;
        /** Momento (epoch ms) en el que ya se puede retirar su NPC tras morir. */
        public long despawnEn = 0;

        public boolean vivo() { return hp > 0; }

        public float pctHp() {
            if (maxHp <= 0) return 0f;
            return Math.max(0f, Math.min(1f, (float) hp / maxHp));
        }

        /** Daño residual del turno (ya descuenta contadores y HP). */
        public int tickEstados() {
            if (hp <= 0) return 0;
            int dano = 0;
            if (estados.contains(EstadoAlterado.QUEMADO)) {
                dano += Math.max(1, hp * 3 / 100);
            }
            if (estados.contains(EstadoAlterado.VENENO)
                || estados.contains(EstadoAlterado.ENVENENADO_FUERTE)) {
                dano += estados.contains(EstadoAlterado.ENVENENADO_FUERTE)
                    ? Math.max(1, hp * 6 / 100)
                    : Math.max(1, hp * 3 / 100);
            }
            if (estados.contains(EstadoAlterado.SANGRANDO)) {
                dano += Math.max(2, hp * 4 / 100);
            }
            if (estados.contains(EstadoAlterado.QUEMADO) && --turnosQuemado <= 0) {
                estados.remove(EstadoAlterado.QUEMADO);
            }
            if (estados.contains(EstadoAlterado.VENENO)
                || estados.contains(EstadoAlterado.ENVENENADO_FUERTE)) {
                if (--turnosVeneno <= 0) {
                    estados.remove(EstadoAlterado.VENENO);
                    estados.remove(EstadoAlterado.ENVENENADO_FUERTE);
                }
            }
            if (estados.contains(EstadoAlterado.SANGRANDO) && --turnosSangrado <= 0) {
                estados.remove(EstadoAlterado.SANGRANDO);
            }
            hp = Math.max(0, hp - dano);
            return dano;
        }

        public void aplicarEstado(EstadoAlterado e, int turnos) {
            int t = Math.max(1, turnos);
            switch (e) {
                case QUEMADO -> turnosQuemado = Math.max(turnosQuemado, Math.max(3, t));
                case VENENO, ENVENENADO_FUERTE -> turnosVeneno = Math.max(turnosVeneno, Math.max(3, t));
                case PARALISIS -> turnosParalisis = Math.max(turnosParalisis, Math.max(2, t));
                case DORMIDO -> turnosDormido = Math.max(turnosDormido, Math.max(2, t));
                case SANGRANDO -> turnosSangrado = Math.max(turnosSangrado, Math.max(3, t));
                case ATURDIDO -> { }
            }
            estados.add(e);
        }
    }

    /**
     * Evento visual de un golpe: la capa de presentación enfoca al atacante,
     * reproduce su animación de ataque, enfoca al objetivo y reproduce
     * Hurt/Death. El motor solo lo registra; la escena lo reproduce.
     */
    public static final class EventoVisual {
        public final boolean atacanteAliado;
        public final int atacanteIdx;
        public final boolean objetivoAliado;
        public final int objetivoIdx;
        public final int dano;
        public final boolean muerte;

        public EventoVisual(boolean atacanteAliado, int atacanteIdx,
                            boolean objetivoAliado, int objetivoIdx,
                            int dano, boolean muerte) {
            this.atacanteAliado = atacanteAliado;
            this.atacanteIdx = atacanteIdx;
            this.objetivoAliado = objetivoAliado;
            this.objetivoIdx = objetivoIdx;
            this.dano = dano;
            this.muerte = muerte;
        }
    }

    /** Eventos visuales pendientes de reproducir (los consume la escena). */
    public final List<EventoVisual> eventos = new ArrayList<>();
    /** true mientras la escena reproduce el foco de ataque/daño. */
    public boolean cinematicaActiva = false;

    /** Grupo enemigo del encuentro (1..5). */
    public final List<Enemigo> enemigos = new ArrayList<>();

    /** Índice del enemigo que se ataca por defecto (auto-objetivo). */
    public int objetivoIdx = 0;

    // ══ HP DE COMBATE DE LA PARTY ══════════════════════════════════════
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
    public int turnosSangradoA = 0, turnosSangradoB = 0, turnosSangradoC = 0;

    /** Calumon vivo en el equipo: duplica atk/def/spd/wis aliados. */
    public boolean calumonDobla = false;
    /** PvP entre jugadores: sin penalización de derrota. */
    public boolean esPvP = false;

    // ══ ORDER POINTS (por Digimon) ═════════════════════════════════════
    public int opA = 0;
    public int opB = 0;
    public int opC = 0;
    public static final int OP_MAX     = 200;
    public static final int OP_INICIAL = 50;
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

    // ══ CRITICAL POINTS (del equipo; pagan Artes Cruzados) ═════════════
    public static final int CP_MAX = 100;
    public static final int CP_ARTE_CRUZADO = 100;
    public int cp = 0;
    public int cpGanado = 0;

    // ══ TÁCTICAS (IA aliada) ═══════════════════════════════════════════
    public Tactica tacticaA = Tactica.EQUILIBRADO;
    public Tactica tacticaB = Tactica.EQUILIBRADO;
    public Tactica tacticaC = Tactica.EQUILIBRADO;

    // ══ GUARDIA (dura hasta el próximo turno del que la usa) ═══════════
    public boolean guardActivo_A = false;
    public boolean guardActivo_B = false;
    public boolean guardActivo_C = false;

    // ══ SISTEMA DE TURNOS (ATB) ════════════════════════════════════════
    public static final int GAUGE_MAX = 1000;
    public int gaugeA = 0;
    public int gaugeB = 0;
    public int gaugeC = 0;

    /** Quién actúa ahora (null = nadie). */
    public Actor actorActual = null;
    /** true = el combate espera una orden del jugador para el líder. */
    public boolean esperandoJugador = false;
    /** Turnos resueltos del líder (la "ronda" que ve el jugador). */
    public int rondasJugador = 0;
    /** Contador global de turnos enemigos (cadencia de carga). */
    public int turnosEnemigo = 0;

    // ══ ESTADO GENERAL ═════════════════════════════════════════════════
    public int nivelZona = 1;
    public int ronda = 1;
    public Fase fase = Fase.INICIATIVA;
    public boolean activa = true;
    public boolean victoria = false;
    public List<DatoDigimon> party = new ArrayList<>();
    /** Ref del salvaje que golpeaste (el primer enemigo del grupo). */
    public Ref<?> objetivo = null;
    public int expTotal = 0;
    public int danoTotalAcumulado = 0;
    public int comboCount = 0;
    public int comboMax = 0;
    /** El jugador ya analizó al objetivo actual. */
    public boolean analizado = false;
    /** Recompensas/guardado de fin de combate ya aplicados (evita duplicados). */
    public boolean recompensasDadas = false;
    /** Altura media (hitbox) de los Digimon en combate; 0 = sin calcular. */
    public float alturaMedia = 0f;

    // ══ LOG DE COMBATE ═════════════════════════════════════════════════
    private static final int LOG_MAX = 10;
    private final Deque<String> logLineas = new ArrayDeque<>(LOG_MAX);

    public void logear(String linea) {
        if (linea == null || linea.isEmpty()) return;
        if (logLineas.size() >= LOG_MAX) logLineas.pollFirst();
        logLineas.addLast(linea);
    }

    public String getLogFormateado() {
        return logLineas.isEmpty() ? "El combate comienza..." : String.join("\n", logLineas);
    }

    public List<String> getLog() {
        return new ArrayList<>(logLineas);
    }

    // ══ HELPERS DE HP DE LA PARTY ══════════════════════════════════════
    public float pctHpA() { return fraccion(hpCombateA, maxHpDe(0)); }
    public float pctHpB() { return fraccion(hpCombateB, maxHpDe(1)); }
    public float pctHpC() { return fraccion(hpCombateC, maxHpDe(2)); }

    private static float fraccion(int hp, int max) {
        if (max <= 0 || hp <= 0) return 0f;
        return Math.min(1f, (float) hp / max);
    }

    private int maxHpDe(int i) {
        if (party == null || i < 0 || i >= party.size()) return 1;
        DatoDigimon d = party.get(i);
        return d != null ? Math.max(1, d.maxHp) : 1;
    }

    public boolean aVivo() { return hpCombateA > 0; }
    public boolean bVivo() { return hpCombateB > 0; }
    public boolean cVivo() { return hpCombateC > 0; }
    public boolean equipoVivo() { return aVivo() || bVivo() || cVivo(); }
    public int opTotal() { return opA + opB; }

    public boolean enemigoVivo() { return !enemigosVivos().isEmpty(); }

    public boolean todosEnemigosDerrota() { return enemigosVivos().isEmpty(); }

    public List<Enemigo> enemigosVivos() {
        List<Enemigo> out = new ArrayList<>();
        for (Enemigo e : enemigos) if (e != null && e.vivo()) out.add(e);
        return out;
    }

    public int enemigosVivosCount() { return enemigosVivos().size(); }

    public Enemigo enemigo(int i) {
        return (i >= 0 && i < enemigos.size()) ? enemigos.get(i) : null;
    }

    /** Enemigo al que se apunta por defecto (vivo). */
    public Enemigo objetivoActual() {
        Enemigo e = enemigo(objetivoIdx);
        if (e != null && e.vivo()) return e;
        List<Enemigo> vivos = enemigosVivos();
        return vivos.isEmpty() ? null : vivos.get(0);
    }

    // ══ HELPERS DE ESTADOS DE LA PARTY ═════════════════════════════════
    public boolean tieneEstadoA(EstadoAlterado e) { return estadosA.contains(e); }
    public boolean tieneEstadoB(EstadoAlterado e) { return estadosB.contains(e); }
    public boolean tieneEstadoC(EstadoAlterado e) { return estadosC.contains(e); }

    public Set<EstadoAlterado> estadosDeAliado(int i) {
        return switch (i) {
            case 0 -> estadosA;
            case 1 -> estadosB;
            default -> estadosC;
        };
    }

    // ══ ACCESO GENÉRICO POR ACTOR ══════════════════════════════════════
    public DatoDigimon digimonDe(int i) {
        if (i < 0 || party == null || i >= party.size()) return null;
        return party.get(i);
    }

    public DatoDigimon digimonDe(Actor a) {
        return a == null || !a.aliado() ? null : digimonDe(a.idx());
    }

    public boolean vivo(Actor a) {
        if (a == null) return false;
        if (a.aliado()) {
            return switch (a.idx()) {
                case 0 -> aVivo();
                case 1 -> bVivo();
                default -> cVivo();
            };
        }
        Enemigo e = enemigo(a.idx());
        return e != null && e.vivo();
    }

    public String nombreDe(Actor a) {
        if (a == null) return "?";
        if (a.aliado()) {
            DatoDigimon d = digimonDe(a.idx());
            return d != null ? d.nombre : ("Aliado " + (a.idx() + 1));
        }
        Enemigo e = enemigo(a.idx());
        return e != null ? e.nombre : "Enemigo";
    }

    public int gaugeDe(Actor a) {
        if (a == null) return 0;
        if (a.aliado()) {
            return switch (a.idx()) {
                case 0 -> gaugeA;
                case 1 -> gaugeB;
                default -> gaugeC;
            };
        }
        Enemigo e = enemigo(a.idx());
        return e != null ? e.gauge : 0;
    }

    public void setGauge(Actor a, int v) {
        int x = Math.max(0, Math.min(GAUGE_MAX, v));
        if (a == null) return;
        if (a.aliado()) {
            switch (a.idx()) {
                case 0 -> gaugeA = x;
                case 1 -> gaugeB = x;
                default -> gaugeC = x;
            }
        } else {
            Enemigo e = enemigo(a.idx());
            if (e != null) e.gauge = x;
        }
    }

    public int opDe(int i) {
        return switch (i) {
            case 0 -> opA;
            case 1 -> opB;
            case 2 -> opC;
            default -> 0;
        };
    }

    public void setOp(int i, int v) {
        int x = Math.max(0, Math.min(OP_MAX, v));
        switch (i) {
            case 0 -> opA = x;
            case 1 -> opB = x;
            case 2 -> opC = x;
            default -> { }
        }
    }

    public int hpDe(Actor a) {
        if (a == null) return 0;
        if (a.aliado()) {
            return switch (a.idx()) {
                case 0 -> hpCombateA;
                case 1 -> hpCombateB;
                default -> hpCombateC;
            };
        }
        Enemigo e = enemigo(a.idx());
        return e != null ? e.hp : 0;
    }

    public void setHpDe(Actor a, int hp) {
        int x = Math.max(0, hp);
        if (a == null) return;
        if (a.aliado()) {
            switch (a.idx()) {
                case 0 -> hpCombateA = x;
                case 1 -> hpCombateB = x;
                default -> hpCombateC = x;
            }
        } else {
            Enemigo e = enemigo(a.idx());
            if (e != null) e.hp = x;
        }
    }

    public float pctHpDe(Actor a) {
        if (a == null) return 0f;
        if (a.aliado()) {
            DatoDigimon d = digimonDe(a.idx());
            return fraccion(hpDe(a), d != null ? d.maxHp : 1);
        }
        Enemigo e = enemigo(a.idx());
        return e != null ? e.pctHp() : 0f;
    }

    public boolean guardiaDe(int i) {
        return switch (i) {
            case 0 -> guardActivo_A;
            case 1 -> guardActivo_B;
            case 2 -> guardActivo_C;
            default -> false;
        };
    }

    public void setGuardia(int i, boolean v) {
        switch (i) {
            case 0 -> guardActivo_A = v;
            case 1 -> guardActivo_B = v;
            case 2 -> guardActivo_C = v;
            default -> { }
        }
    }

    // ══ CP ═════════════════════════════════════════════════════════════
    public void ganarCp(int cantidad) {
        if (cantidad <= 0) return;
        cp = Math.min(CP_MAX, cp + cantidad);
        cpGanado += cantidad;
    }

    public boolean cpListoParaArte() { return cp >= CP_ARTE_CRUZADO; }

    // ══ BARRAS VISUALES ════════════════════════════════════════════════
    public static String barraHp(int hp, int max) {
        if (max <= 0) return "----------";
        int llenos = Math.max(0, Math.min(10, (int) ((float) hp / max * 10)));
        return "#".repeat(llenos) + "-".repeat(10 - llenos);
    }

    public static String barraGauge(int gauge) {
        int llenos = Math.max(0, Math.min(10, (int) ((float) gauge / GAUGE_MAX * 10)));
        return "=".repeat(llenos) + "-".repeat(10 - llenos);
    }

    public static String barraOp(int op) {
        int llenos = Math.max(0, Math.min(10, (int) ((float) op / OP_MAX * 10)));
        return "=".repeat(llenos) + "-".repeat(10 - llenos);
    }

    public static String barraCp(int cp) {
        int llenos = Math.max(0, Math.min(10, (int) ((float) cp / CP_MAX * 10)));
        return "=".repeat(llenos) + "-".repeat(10 - llenos);
    }

    // ══ DAÑO RESIDUAL DE LA PARTY ══════════════════════════════════════
    public int danoResidualA() {
        int dano = residualDe(estadosA, 0);
        hpCombateA = Math.max(0, hpCombateA - dano);
        return dano;
    }

    public int danoResidualB() {
        int dano = residualDe(estadosB, 1);
        hpCombateB = Math.max(0, hpCombateB - dano);
        return dano;
    }

    public int danoResidualC() {
        int dano = residualDe(estadosC, 2);
        hpCombateC = Math.max(0, hpCombateC - dano);
        return dano;
    }

    private int residualDe(Set<EstadoAlterado> estados, int idx) {
        int hp = switch (idx) {
            case 0 -> hpCombateA;
            case 1 -> hpCombateB;
            default -> hpCombateC;
        };
        if (hp <= 0 || estados.isEmpty()) return 0;
        int dano = 0;
        if (estados.contains(EstadoAlterado.QUEMADO)) dano += Math.max(1, hp * 3 / 100);
        if (estados.contains(EstadoAlterado.VENENO) || estados.contains(EstadoAlterado.ENVENENADO_FUERTE)) {
            dano += estados.contains(EstadoAlterado.ENVENENADO_FUERTE)
                ? Math.max(1, hp * 6 / 100)
                : Math.max(1, hp * 3 / 100);
        }
        if (estados.contains(EstadoAlterado.SANGRANDO)) dano += Math.max(2, hp * 4 / 100);

        int[] c = contadoresDe(idx);
        if (estados.contains(EstadoAlterado.QUEMADO) && --c[0] <= 0) estados.remove(EstadoAlterado.QUEMADO);
        if (estados.contains(EstadoAlterado.VENENO) || estados.contains(EstadoAlterado.ENVENENADO_FUERTE)) {
            if (--c[1] <= 0) {
                estados.remove(EstadoAlterado.VENENO);
                estados.remove(EstadoAlterado.ENVENENADO_FUERTE);
            }
        }
        if (estados.contains(EstadoAlterado.SANGRANDO) && --c[2] <= 0) estados.remove(EstadoAlterado.SANGRANDO);
        guardarContadores(idx, c);
        return dano;
    }

    private int[] contadoresDe(int idx) {
        return switch (idx) {
            case 0 -> new int[]{turnosQuemadoA, turnosVenenoA, turnosSangradoA};
            case 1 -> new int[]{turnosQuemadoB, turnosVenenoB, turnosSangradoB};
            default -> new int[]{turnosQuemadoC, turnosVenenoC, turnosSangradoC};
        };
    }

    private void guardarContadores(int idx, int[] c) {
        switch (idx) {
            case 0 -> { turnosQuemadoA = c[0]; turnosVenenoA = c[1]; turnosSangradoA = c[2]; }
            case 1 -> { turnosQuemadoB = c[0]; turnosVenenoB = c[1]; turnosSangradoB = c[2]; }
            default -> { turnosQuemadoC = c[0]; turnosVenenoC = c[1]; turnosSangradoC = c[2]; }
        }
    }

    // ══ TEXTO DE ESTADOS ═══════════════════════════════════════════════
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
        if (estados == null || estados.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (EstadoAlterado e : estados) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(nombreEstado(e));
        }
        return sb.toString();
    }
}
