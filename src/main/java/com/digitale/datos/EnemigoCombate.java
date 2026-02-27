package com.digitale.datos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Plantillas de enemigos para el SistemaBatalla.
 * Cada enemigo tiene stats base y un nivel mínimo recomendado.
 */
public class EnemigoCombate {

    public final String nombre;
    public final String elemento;
    public final int    hp;
    public final int    atk;
    public final int    def;
    public final int    spd;
    public final int    wis;
    public final int    exp;
    public final int    nivelMin; // nivel mínimo del equipo para que aparezca

    public EnemigoCombate(String nombre, String elemento,
                          int hp, int atk, int def, int spd, int wis,
                          int exp, int nivelMin) {
        this.nombre    = nombre;
        this.elemento  = elemento;
        this.hp        = hp;
        this.atk       = atk;
        this.def       = def;
        this.spd       = spd;
        this.wis       = wis;
        this.exp       = exp;
        this.nivelMin  = nivelMin;
    }

    // ══ Tabla de enemigos ══════════════════════════════════════════

    private static final EnemigoCombate[] TABLA = {
        // ── Baby / In-Training (nivel 1-2) ─────────────────────────
        new EnemigoCombate("Botamon Salvaje", "NEUTRO",   40,  5,  4,  5,  3,  20, 1),
        new EnemigoCombate("Punimon Salvaje", "NEUTRO",   38,  4,  6,  4,  3,  18, 1),
        new EnemigoCombate("Nyokimon Salvaje","VIENTO",   36,  4,  3,  7,  4,  22, 1),

        // ── Rookie (nivel 2-3) ──────────────────────────────────────
        new EnemigoCombate("Agumon",          "FUEGO",   100, 12,  8, 10,  7,  45, 2),
        new EnemigoCombate("Gabumon",         "HIELO",    90, 10, 12,  8,  8,  42, 2),
        new EnemigoCombate("Patamon",         "LUZ",      85,  9,  8, 11, 11,  40, 2),
        new EnemigoCombate("Palmon",          "NATURALEZA",88, 9,  9,  8, 10,  38, 2),
        new EnemigoCombate("Gomamon",         "AGUA",     92, 10,  8, 12,  7,  40, 2),
        new EnemigoCombate("Biyomon",         "VIENTO",   85,  9,  7, 13,  9,  40, 2),
        new EnemigoCombate("Elecmon",         "TRUENO",  110, 14,  9, 10,  8,  55, 2),
        new EnemigoCombate("Numemon",         "NEUTRO",   80,  7,  5,  6,  5,  30, 2),

        // ── Champion (nivel 3-4) ────────────────────────────────────
        new EnemigoCombate("Greymon",         "FUEGO",   220, 25, 18, 16, 14, 120, 3),
        new EnemigoCombate("GarurumonA",      "HIELO",   200, 20, 24, 14, 15, 115, 3),
        new EnemigoCombate("Angemon",         "LUZ",     190, 18, 16, 17, 22, 110, 3),
        new EnemigoCombate("Togemon",         "NATURALEZA",210,19,22, 13, 18, 112, 3),
        new EnemigoCombate("Birdramon",       "FUEGO",   195, 22, 15, 22, 14, 110, 3),
        new EnemigoCombate("Devimon",         "OSCURIDAD",230,27, 16, 15, 12, 130, 3),

        // ── Ultimate (nivel 4-5) ────────────────────────────────────
        new EnemigoCombate("MetalGreymon",    "FUEGO",   420, 45, 35, 25, 25, 280, 4),
        new EnemigoCombate("WereGarurumon",   "HIELO",   400, 40, 48, 22, 28, 270, 4),
        new EnemigoCombate("MagnaAngemon",    "LUZ",     380, 38, 32, 28, 42, 260, 4),
        new EnemigoCombate("Myotismon",       "OSCURIDAD",440,48, 36, 24, 30, 300, 4),

        // ── Mega (nivel 5-6) ────────────────────────────────────────
        new EnemigoCombate("WarGreymon",      "FUEGO",   800, 85, 65, 45, 50, 600, 5),
        new EnemigoCombate("MetalGarurumon",  "HIELO",   780, 78, 80, 42, 52, 590, 5),
        new EnemigoCombate("VenomMyotismon",  "OSCURIDAD",850,90, 68, 40, 45, 650, 5),
    };

    // ══ Selección de enemigo ═══════════════════════════════════════

    private static final Random RNG = new Random();

    /**
     * Elige un enemigo apropiado para el nivel del equipo.
     * Escala ligeramente los stats para variedad.
     */
    public static EstadoCombate generarCombate(int nivelEquipo) {
        // Filtrar enemigos apropiados (nivel ± 1)
        List<EnemigoCombate> pool = new ArrayList<>();
        for (EnemigoCombate e : TABLA) {
            if (e.nivelMin <= nivelEquipo && e.nivelMin >= Math.max(1, nivelEquipo - 1)) {
                pool.add(e);
            }
        }
        if (pool.isEmpty()) {
            // Fallback: usar cualquier enemigo del nivel
            for (EnemigoCombate e : TABLA) {
                if (e.nivelMin <= nivelEquipo) pool.add(e);
            }
        }
        if (pool.isEmpty()) pool.add(TABLA[3]); // Agumon como último recurso

        EnemigoCombate tmpl = pool.get(RNG.nextInt(pool.size()));

        // Escala aleatoria ±10% para variedad
        double escala = 0.9 + RNG.nextDouble() * 0.2;

        EstadoCombate estado = new EstadoCombate();
        estado.enemigoNombre   = tmpl.nombre;
        estado.enemigoElemento = tmpl.elemento;
        estado.enemigoHp       = (int)(tmpl.hp  * escala);
        estado.enemigoMaxHp    = estado.enemigoHp;
        estado.enemigoAtk      = (int)(tmpl.atk * escala);
        estado.enemigoDef      = (int)(tmpl.def * escala);
        estado.enemigoSpd      = (int)(tmpl.spd * escala);
        estado.enemigoWis      = (int)(tmpl.wis * escala);
        estado.enemigoExp      = (int)(tmpl.exp * escala);

        estado.logear("¡" + estado.enemigoNombre + " [" + estado.enemigoElemento + "] quiere pelear!");

        return estado;
    }

    /** Multiplicador elemental: ¿atacante tiene ventaja sobre defensor? */
    public static double multElemental(String atkElem, String defElem) {
        return switch (atkElem) {
            case "FUEGO"      -> switch (defElem) { case "NATURALEZA","HIELO" -> 1.5; case "AGUA" -> 0.7; default -> 1.0; };
            case "AGUA"       -> switch (defElem) { case "FUEGO","TRUENO"    -> 1.5; case "NATURALEZA" -> 0.7; default -> 1.0; };
            case "NATURALEZA" -> switch (defElem) { case "AGUA","TIERRA"     -> 1.5; case "FUEGO" -> 0.7; default -> 1.0; };
            case "HIELO"      -> switch (defElem) { case "VIENTO","AGUA"     -> 1.5; case "FUEGO" -> 0.7; default -> 1.0; };
            case "TRUENO"     -> switch (defElem) { case "AGUA","VIENTO"     -> 1.5; case "TIERRA" -> 0.7; default -> 1.0; };
            case "VIENTO"     -> switch (defElem) { case "TRUENO"            -> 1.5; case "HIELO" -> 0.7; default -> 1.0; };
            case "LUZ"        -> switch (defElem) { case "OSCURIDAD"         -> 1.5; default -> 1.0; };
            case "OSCURIDAD"  -> switch (defElem) { case "LUZ"               -> 1.5; default -> 1.0; };
            default -> 1.0;
        };
    }
}
