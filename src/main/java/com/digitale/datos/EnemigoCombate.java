package com.digitale.datos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Plantillas de enemigos y generación de <b>grupos</b> de salvajes.
 *
 * <p>Un encuentro tiene de 1 a 5 enemigos que aparecen <b>todos a la vez</b>
 * (como en Digimon Story). El primero es la especie del salvaje que golpeaste;
 * el resto sale del pool del nivel de zona.</p>
 */
public class EnemigoCombate {

    public final String nombre;
    public final String elemento;
    public final String atributo;
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
        this.atributo  = nivelMin <= 1 ? "NONE" : TablaTipos.atributoDeEnemigo(nombre);
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
        new EnemigoCombate("Botamon Salvaje", "NEUTRO",   18,  4,  3,  5,  3,  14, 1),
        new EnemigoCombate("Punimon Salvaje", "NEUTRO",   16,  4,  4,  4,  3,  12, 1),
        new EnemigoCombate("Nyokimon Salvaje","VIENTO",   15,  4,  3,  7,  4,  14, 1),

        // ── Rookie (nivel 2-3) ──────────────────────────────────────
        new EnemigoCombate("Agumon",          "FUEGO",    72, 11,  7, 10,  7,  36, 2),
        new EnemigoCombate("Gabumon",         "HIELO",    64,  9, 11,  8,  8,  34, 2),
        new EnemigoCombate("Patamon",         "LUZ",      60,  8,  7, 11, 11,  32, 2),
        new EnemigoCombate("Palmon",          "NATURALEZA",63, 8,  8,  8, 10,  30, 2),
        new EnemigoCombate("Gomamon",         "AGUA",     66,  9,  7, 12,  7,  32, 2),
        new EnemigoCombate("Biyomon",         "VIENTO",   60,  8,  6, 13,  9,  32, 2),
        new EnemigoCombate("Elecmon",         "TRUENO",   78, 13,  8, 10,  8,  44, 2),
        new EnemigoCombate("Numemon",         "NEUTRO",   56,  6,  4,  6,  5,  24, 2),

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

    // ══ Alias de rol (nombre de plantilla → rol NPC real) ══════════
    private static final java.util.Map<String, String> ALIAS_ROL = java.util.Map.of(
        "GarurumonA",   "Garurumon",
        "MetalGreymon", "MetalGreymon_Virus"
    );

    private static final Random RNG = new Random();

    // ══ Generación de grupo ════════════════════════════════════════

    /**
     * Genera el grupo enemigo completo de un encuentro.
     *
     * @param nivelZona         nivel del equipo (decide los stats)
     * @param especiePrincipal  especie del salvaje golpeado (primer enemigo)
     * @param cantidad          1..5 salvajes, todos a la vez
     */
    public static List<EstadoCombate.Enemigo> generarGrupo(int nivelZona,
                                                            String especiePrincipal,
                                                            int cantidad) {
        int nivel = Math.max(1, nivelZona);
        int n = Math.max(1, Math.min(5, cantidad));
        List<EstadoCombate.Enemigo> grupo = new ArrayList<>();

        // 1) El salvaje que golpeaste (o uno aleatorio si no hay especie).
        EnemigoCombate principal = buscarPorNombre(especiePrincipal);
        if (especiePrincipal != null && !especiePrincipal.isBlank() && principal == null) {
            grupo.add(sintetico(especiePrincipal.trim(), nivel));
        } else {
            if (principal == null) principal = elegirDelPool(nivel);
            if (principal == null) principal = TABLA[3];
            grupo.add(crear(principal, nivel));
        }

        // 2) El resto de la manada: mismo escalón que el principal (así el
        //    grupo es coherente: 3 Rookies, 3 Champions... no mezclas raras).
        List<EnemigoCombate> pool = new ArrayList<>();
        EnemigoCombate principalTmpl = buscarPorNombre(grupo.get(0).nombre);
        if (principalTmpl != null) {
            for (EnemigoCombate t : TABLA) {
                if (t.nivelMin == principalTmpl.nivelMin) pool.add(t);
            }
        }
        if (pool.isEmpty()) pool = poolDelNivel(nivel);
        java.util.Collections.shuffle(pool, RNG);
        int cursor = 0;
        for (int i = 1; i < n; i++) {
            EnemigoCombate t = null;
            for (int k = 0; k < pool.size(); k++) {
                EnemigoCombate cand = pool.get((cursor + k) % pool.size());
                if (!cand.nombre.equals(grupo.get(0).nombre)) {
                    t = cand;
                    cursor = (cursor + k + 1) % Math.max(1, pool.size());
                    break;
                }
            }
            if (t == null) t = pool.isEmpty() ? TABLA[3] : pool.get(0);
            grupo.add(crear(t, nivel));
        }
        return grupo;
    }

    public static EstadoCombate.Enemigo crear(EnemigoCombate tmpl, int nivelZona) {
        double variacion = 0.92 + RNG.nextDouble() * 0.16;
        double escalaNivel = 1.0 + Math.min(1.4, 0.12 * Math.max(0, nivelZona - tmpl.nivelMin));
        double escala = variacion * escalaNivel;

        EstadoCombate.Enemigo e = new EstadoCombate.Enemigo();
        e.nombre   = tmpl.nombre;
        e.elemento = tmpl.elemento;
        e.atributo = tmpl.atributo;
        e.hp       = Math.max(1, (int) (tmpl.hp * escala));
        e.maxHp    = e.hp;
        e.atk      = Math.max(1, (int) (tmpl.atk * escala));
        e.def      = Math.max(1, (int) (tmpl.def * escala));
        e.spd      = Math.max(1, (int) (tmpl.spd * escala));
        e.wis      = Math.max(1, (int) (tmpl.wis * escala));
        e.exp      = Math.max(1, (int) (tmpl.exp * escala));
        return e;
    }

    /**
     * Enemigo "a medida" para una especie que no está en la tabla (la mayoría
     * de roles salvajes del mod). Usa la curva de stats por nivel de zona y
     * toma el elemento/atributo de {@link DatoDigimon#crearInicial(String)}.
     */
    public static EstadoCombate.Enemigo sintetico(String especie, int nivelZona) {
        DatoDigimon base = DatoDigimon.crearInicial(especie);
        double variacion = 0.92 + RNG.nextDouble() * 0.16;
        double hp = 38.0 * Math.pow(2.15, Math.max(0, nivelZona - 1)) * variacion;

        EstadoCombate.Enemigo e = new EstadoCombate.Enemigo();
        e.nombre   = especie;
        e.elemento = base.elemento != null ? base.elemento : "NEUTRO";
        String atr = base.atributo;
        e.atributo = (atr == null || atr.isBlank() || "NONE".equalsIgnoreCase(atr)
            || "UNKNOWN".equalsIgnoreCase(atr))
            ? TablaTipos.atributoDeEspecie(especie) : atr;
        e.hp    = Math.max(1, (int) hp);
        e.maxHp = e.hp;
        e.atk   = Math.max(1, (int) (hp * 0.105));
        e.def   = Math.max(1, (int) (hp * 0.090));
        e.spd   = Math.max(1, 5 + nivelZona * 3);
        e.wis   = Math.max(1, (int) (hp * 0.090));
        e.exp   = Math.max(1, (int) (hp * 0.70));
        return e;
    }

    private static List<EnemigoCombate> poolDelNivel(int nivel) {
        List<EnemigoCombate> pool = new ArrayList<>();
        for (EnemigoCombate e : TABLA) {
            if (e.nivelMin <= nivel && e.nivelMin >= Math.max(1, nivel - 1)) pool.add(e);
        }
        if (pool.isEmpty()) {
            for (EnemigoCombate e : TABLA) if (e.nivelMin <= nivel) pool.add(e);
        }
        if (pool.isEmpty()) pool.add(TABLA[3]);
        return pool;
    }

    private static EnemigoCombate elegirDelPool(int nivel) {
        List<EnemigoCombate> pool = poolDelNivel(nivel);
        return pool.get(RNG.nextInt(pool.size()));
    }

    /** Busca una plantilla por nombre (tolera sufijo " Salvaje" y mayúsculas). */
    public static EnemigoCombate buscarPorNombre(String especie) {
        if (especie == null || especie.isBlank()) return null;
        String buscada = normalizar(especie);
        for (EnemigoCombate e : TABLA) {
            String nombre = normalizar(e.nombre);
            if (nombre.equals(buscada)) return e;
            String sinSalvaje = normalizar(e.nombre.replace(" Salvaje", ""));
            if (sinSalvaje.equals(buscada)) return e;
        }
        return null;
    }

    /** id del rol NPC para una especie de la tabla (para spawnear el grupo). */
    public static String rolSalvajeDe(String especie) {
        EnemigoCombate tmpl = buscarPorNombre(especie);
        String base = tmpl != null
            ? tmpl.nombre.replace(" Salvaje", "")
            : String.valueOf(especie).replace(" Salvaje", "").trim();
        base = ALIAS_ROL.getOrDefault(base, base);
        return base + "_Salvaje";
    }

    /** Etiqueta corta del grupo para el HUD: "Agumon ×3". */
    public static String resumenGrupo(List<EstadoCombate.Enemigo> grupo) {
        if (grupo == null || grupo.isEmpty()) return "---";
        java.util.Map<String, Integer> cuenta = new java.util.LinkedHashMap<>();
        for (EstadoCombate.Enemigo e : grupo) {
            if (e == null || !e.vivo()) continue;
            cuenta.merge(e.nombre, 1, Integer::sum);
        }
        if (cuenta.isEmpty()) return "---";
        StringBuilder sb = new StringBuilder();
        for (var en : cuenta.entrySet()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(en.getKey());
            if (en.getValue() > 1) sb.append(" x").append(en.getValue());
        }
        return sb.toString();
    }

    private static String normalizar(String s) {
        return s.trim().toUpperCase(Locale.ROOT).replace("_", " ").replace("-", " ");
    }

    // ══ Tabla de tipos (delegada a TablaTipos) ═════════════════════
    public static double multElemental(String atkElem, String defElem) {
        return TablaTipos.multElemental(atkElem, defElem);
    }

    public static final String[] ELEMENTOS = TablaTipos.ELEMENTOS;

    public static List<String> fuerteContra(String elem) {
        List<String> out = new ArrayList<>();
        if (elem == null) return out;
        for (String otro : ELEMENTOS) {
            if (TablaTipos.multElemental(elem, otro) > 1.0) out.add(otro);
        }
        return out;
    }

    public static List<String> debilContra(String elem) {
        List<String> out = new ArrayList<>();
        if (elem == null) return out;
        for (String otro : ELEMENTOS) {
            if (TablaTipos.multElemental(otro, elem) > 1.0) out.add(otro);
        }
        return out;
    }
}
