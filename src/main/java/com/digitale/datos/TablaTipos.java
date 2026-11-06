package com.digitale.datos;

/**
 * Tabla de tipos del combate de DigiTale.
 *
 * <p>Un ataque tiene <b>elemento</b> (FUEGO, AGUA, ...) y <b>atributo</b>
 * (VACCINE, VIRUS, DATA, FREE). El daño se multiplica por la ventaja de
 * elemento <b>y</b> por la ventaja de atributo, igual que en Digimon Story:
 * los ataques a un punto débil pueden llegar a <b>x3</b> (1.5 de elemento
 * por 2.0 de atributo).</p>
 *
 * <p>Esta clase es la <b>única fuente de verdad</b> del triángulo de tipos:
 * {@link EnemigoCombate#multElemental} y los Digimon compañeros delegan aquí,
 * así no hay dos tablas que se puedan desincronizar.</p>
 */
public final class TablaTipos {

    private TablaTipos() {
    }

    // ── Multiplicadores base ────────────────────────────────────────────
    public static final double FUERTE  = 1.5;
    public static final double NEUTRAL = 1.0;
    public static final double RESISTE = 0.5;
    public static final double INMUNE  = 0.0;
    /** Techo del multiplicador combinado (elemento x atributo). */
    public static final double MAX_MULT = 3.0;

    /** Atributos canónicos (el orden es el que se muestra en la UI). */
    public static final String[] ATRIBUTOS = { "VACCINE", "VIRUS", "DATA", "FREE" };

    /** Elementos canónicos (orden de presentación). */
    public static final String[] ELEMENTOS = {
        "FUEGO", "AGUA", "NATURALEZA", "HIELO", "TRUENO",
        "VIENTO", "LUZ", "OSCURIDAD", "TIERRA", "METAL", "NEUTRO"
    };

    // ── Elemento ────────────────────────────────────────────────────────

    /**
     * Multiplicador de elemento del atacante sobre el defensor.
     * 1.5 = ventaja, 1.0 = neutro, 0.5 = resiste.
     */
    public static double multElemental(String atkElem, String defElem) {
        String a = norm(atkElem);
        String d = norm(defElem);
        if (a.isEmpty() || d.isEmpty() || "NEUTRO".equals(a) || "NEUTRO".equals(d)) {
            return NEUTRAL;
        }
        return switch (a) {
            case "FUEGO" -> switch (d) {
                case "NATURALEZA", "HIELO", "METAL", "PLANTA" -> FUERTE;
                case "AGUA", "TIERRA" -> RESISTE;
                default -> NEUTRAL;
            };
            case "AGUA" -> switch (d) {
                case "FUEGO", "TIERRA" -> FUERTE;
                case "NATURALEZA", "PLANTA", "TRUENO" -> RESISTE;
                default -> NEUTRAL;
            };
            case "NATURALEZA", "PLANTA" -> switch (d) {
                case "AGUA", "TIERRA" -> FUERTE;
                case "FUEGO", "HIELO", "VIENTO" -> RESISTE;
                default -> NEUTRAL;
            };
            case "HIELO" -> switch (d) {
                case "NATURALEZA", "PLANTA", "VIENTO", "TIERRA" -> FUERTE;
                case "FUEGO", "METAL" -> RESISTE;
                default -> NEUTRAL;
            };
            case "TRUENO" -> switch (d) {
                case "AGUA", "VIENTO", "METAL" -> FUERTE;
                case "TIERRA", "NATURALEZA", "PLANTA" -> RESISTE;
                default -> NEUTRAL;
            };
            case "VIENTO" -> switch (d) {
                case "NATURALEZA", "PLANTA", "TRUENO" -> FUERTE;
                case "HIELO", "METAL" -> RESISTE;
                default -> NEUTRAL;
            };
            case "LUZ" -> switch (d) {
                case "OSCURIDAD" -> FUERTE;
                default -> NEUTRAL;
            };
            case "OSCURIDAD" -> switch (d) {
                case "LUZ" -> FUERTE;
                default -> NEUTRAL;
            };
            case "TIERRA" -> switch (d) {
                case "TRUENO", "FUEGO", "METAL" -> FUERTE;
                case "AGUA", "NATURALEZA", "PLANTA", "VIENTO" -> RESISTE;
                default -> NEUTRAL;
            };
            case "METAL" -> switch (d) {
                case "HIELO", "LUZ" -> FUERTE;
                case "TRUENO", "FUEGO", "TIERRA" -> RESISTE;
                default -> NEUTRAL;
            };
            default -> NEUTRAL;
        };
    }

    // ── Atributo ────────────────────────────────────────────────────────

    /**
     * Triángulo de atributos Digimon: VACCINE &gt; VIRUS &gt; DATA &gt; VACCINE.
     * FREE y los desconocidos son neutrales contra todo.
     */
    public static double multAtributo(String atkAtr, String defAtr) {
        String a = norm(atkAtr);
        String d = norm(defAtr);
        if (esLibre(a) || esLibre(d)) return NEUTRAL;
        if (a.equals(d)) return NEUTRAL;
        if ((a.equals("VACCINE") && d.equals("VIRUS"))
            || (a.equals("VIRUS") && d.equals("DATA"))
            || (a.equals("DATA") && d.equals("VACCINE"))) {
            return 2.0;
        }
        if ((d.equals("VACCINE") && a.equals("VIRUS"))
            || (d.equals("VIRUS") && a.equals("DATA"))
            || (d.equals("DATA") && a.equals("VACCINE"))) {
            return RESISTE;
        }
        return NEUTRAL;
    }

    /**
     * Multiplicador final: elemento x atributo, acotado a {@link #MAX_MULT}.
     * Un ataque sin atributo (FREE) nunca pasa de x1.5 por elemento.
     */
    public static double multTotal(String atkElem, String atkAtr,
                                   String defElem, String defAtr) {
        double m = multElemental(atkElem, defElem) * multAtributo(atkAtr, defAtr);
        return Math.min(MAX_MULT, Math.max(INMUNE, m));
    }

    /** ¿El ataque explota un punto débil? (umbral x1.5). */
    public static boolean esDebilidad(String atkElem, String atkAtr,
                                      String defElem, String defAtr) {
        return multTotal(atkElem, atkAtr, defElem, defAtr) >= FUERTE;
    }

    // ── Etiquetas para log / UI ─────────────────────────────────────────

    /** Texto corto para el resultado de un ataque. */
    public static String etiqueta(double mult) {
        if (mult >= 2.5) return "¡PUNTO CRÍTICO!";
        if (mult >= FUERTE) return "¡EFICAZ!";
        if (mult <= INMUNE) return "INMUNE";
        if (mult <= RESISTE) return "RESISTIDO";
        return "";
    }

    /** Etiqueta corta (sin signos) para el HUD. */
    public static String estadoContra(double mult) {
        if (mult >= 2.5) return "DEBIL x3";
        if (mult >= FUERTE) return "DEBIL x1.5";
        if (mult <= INMUNE) return "INMUNE";
        if (mult <= RESISTE) return "RESISTE";
        return "NEUTRO";
    }

    /**
     * Resumen de debilidades/resistencias de un defensor, para "Analizar".
     * Ej: {@code "DEBIL a LUZ+VACCINE / RESISTE a OSCURIDAD"}.
     */
    public static String analisis(String defElem, String defAtr) {
        String dE = norm(defElem);
        String dA = norm(defAtr);
        java.util.List<String> debiles = new java.util.ArrayList<>();
        java.util.List<String> resiste = new java.util.ArrayList<>();
        for (String e : ELEMENTOS) {
            if ("NEUTRO".equals(e)) continue;
            double m = multElemental(e, dE);
            if (m >= FUERTE) debiles.add(e);
            else if (m <= RESISTE) resiste.add(e);
        }
        StringBuilder sb = new StringBuilder();
        if (!debiles.isEmpty()) sb.append("DEBIL a ").append(String.join("/", debiles));
        if (!resiste.isEmpty()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append("RESISTE ").append(String.join("/", resiste));
        }
        if (sb.length() > 0) sb.append(" · ");
        sb.append("ATRIBUTO ").append(textoAtributo(dA));
        return sb.toString();
    }

    /**
     * Multiplicador de referencia "mi elemento contra ti" para el panel de
     * la party: sirve para pintar en verde los movimientos eficaces.
     */
    public static double ventajaDe(DatoDigimon atacante, String defElem, String defAtr) {
        if (atacante == null) return NEUTRAL;
        return multTotal(atacante.elemento, atacante.atributo, defElem, defAtr);
    }

    // ── Atributo por especie ────────────────────────────────────────────

    /** Texto legible del atributo para la UI. */
    public static String textoAtributo(String atr) {
        if (atr == null || atr.isBlank()) return "SIN ATRIBUTO";
        return switch (atr.trim().toUpperCase(java.util.Locale.ROOT)) {
            case "NONE", "NO_ATTRIBUTE", "NO ATTRIBUTE" -> "SIN ATRIBUTO";
            case "UNKNOWN" -> "DESCONOCIDO";
            case "FREE" -> "FREE";
            default -> atr.trim().toUpperCase(java.util.Locale.ROOT);
        };
    }

    /**
     * Canon: las etapas Baby (Fresh) e In-Training <b>no tienen atributo</b>
     * (No Attribute). Vaccin/Virus/Data empiezan en Child/Rookie.
     */
    private static boolean esSinAtributo(String e) {
        return switch (e) {
            // Baby / Fresh (nivel 1)
            case "BOTAMON", "PUNIMON", "POYOMON", "YURAMON", "PICHIMON",
                 "NYOKIMON", "KURAMON", "CHIBOMON", "JYARIMON",
                 "YUKIMIBOTAMON", "PAGUMON",
                 // In-Training (nivel 2)
                 "KOROMON", "TSUNOMON", "TOKOMON", "TANEMON", "BUKAMON",
                 "YOKOMON", "ARGOMON", "BEBYODOMON", "BIBIMON", "BOSAMON",
                 "BOWMON", "BUDMON", "CHAPMON", "CHICCHIMON", "CUPIMON",
                 "DEMIMERAMON", "DEMIVEEMON", "DORIMON", "FLUFFYMON",
                 "FRIMON", "GIGIMON", "GUMMYMON", "GURIMON", "HIYARIMON",
                 "HOPMON", "KAKKINMON", "KAPURIMON", "KEKKOMON", "KOKOMON",
                 "KOZENIMON", "KYAROMON", "KYOKYOMON", "MINOMON", "MISSIMON",
                 "MOCOCOMON", "MONIMON", "MOONMON", "MOTIMON", "NEGAMON",
                 "NYAROMON", "ONIBIMON", "PICKMON", "PINAMON", "POROMON",
                 "PUROROMON", "PUSURIMON", "PUYOYOMON", "SAKUTTOMON", "SUNMON",
                 "TOKOMON_XANTIBODY", "TORIKARABALLMON", "TSUMEMON",
                 "TUMBLEMON", "UPAMON", "VIXIMON", "WANYAMON", "XIAOMON",
                 "YAAMON" -> true;
            default -> false;
        };
    }

    /**
     * Atributo oficial (simplificado) de una especie de compañero.
     * Baby/In-Training = NONE (sin atributo); Vaccin/Virus/Data desde Rookie.
     * Calumon/NEO/Burpmon = UNKNOWN (canon). Desconocidos = FREE.
     */
    public static String atributoDeEspecie(String especie) {
        String e = norm(especie);
        if (e.isEmpty()) return "FREE";
        if (esSinAtributo(e)) return "NONE";
        if (e.equals("CALUMON") || e.equals("NEO") || e.equals("BURPMON")) {
            return "UNKNOWN";
        }
        return switch (e) {
            // VACCINE
            case "AGUMON", "PATAMON", "GOMAMON", "VEEMON", "KOROMON", "TOKOMON",
                 "BUKAMON", "POYOMON", "BOTAMON", "PUNIMON", "CHIBOMON",
                 "GREYMON", "ANGEMON", "IKKAKUMON", "METALGREYMON", "WARGREYMON",
                 "MAGNAANGEMON", "SERAPHIMON", "OMNIANGEMON", "MAGNAMON",
                 "NEO", "CALUMON" -> "VACCINE";
            // VIRUS
            case "GABUMON", "PALMON", "BIYOMON", "TSUNOMON", "TANEMON", "YOKOMON",
                 "PAGUMON", "KURAMON", "JYARIMON", "DEVIMON", "MYOTISMON",
                 "VENOMMYOTISMON", "GARURUMONA", "GARURUMON", "WEREGARURUMON",
                 "BIRDRAMON", "LILLYMON", "ROSEMON", "ROSEZMEMON", "PHOENIXMON",
                 "GARUDAMON", "FUJINMON", "CENTAURUSMON", "SATURNMON",
                 "BURPMON", "MARINEANGEMON" -> "VIRUS";
            // DATA
            case "YUROMON", "YURAMON", "PICHIMON", "NYOKIMON", "TOGEMON",
                 "ZUDOMON", "METALGARURUMON", "ARGOMON", "BEBYODOMON",
                 "BIBIMON", "BOSAMON", "BOWMON", "BUDMON", "CHAPMON",
                 "CHICCHIMON", "CUPIMON", "DEMIMERAMON", "DEMIVEEMON",
                 "DORIMON", "FLUFFYMON", "FRIMON", "GIGIMON", "GUMMYMON",
                 "GURIMON", "HIYARIMON", "HOPMON", "KAKKINMON", "KAPURIMON",
                 "KEKKOMON", "KOKOMON", "KOZENIMON", "KYAROMON", "KYOKYOMON",
                 "MINOMON", "MISSIMON", "MOCOCOMON", "MONIMON", "MOONMON",
                 "MOTIMON", "NEGAMON", "NYAROMON", "ONIBIMON", "PICKMON",
                 "PINAMON", "POROMON", "PUROROMON", "PUSURIMON", "PUYOYOMON",
                 "SAKUTTOMON", "SUNMON", "TOKOMON_XANTIBODY", "TORIKARABALLMON",
                 "TSUMEMON", "TUMBLEMON", "UPAMON", "VIXIMON", "WANYAMON",
                 "XIAOMON", "YAAMON" -> "DATA";
            default -> "FREE";
        };
    }

    /**
     * Atributo de un enemigo salvaje de la tabla {@link EnemigoCombate}.
     * El nombre puede venir con el sufijo " Salvaje".
     */
    public static String atributoDeEnemigo(String nombre) {
        String n = norm(nombre);
        if (n.endsWith("SALVAJE")) n = n.substring(0, n.length() - "SALVAJE".length());
        if (esSinAtributo(n)) return "NONE";
        if (n.equals("CALUMON") || n.equals("NEO") || n.equals("BURPMON")) {
            return "UNKNOWN";
        }
        return switch (n) {
            case "AGUMON", "PATAMON", "GOMAMON", "GREYMON", "ANGEMON",
                 "METALGREYMON", "WARGREYMON", "MAGNAANGEMON" -> "VACCINE";
            case "GABUMON", "BIYOMON", "DEVIMON", "MYOTISMON", "VENOMMYOTISMON",
                 "BIRDRAMON", "NUMEMON", "PALMON" -> "VIRUS";
            case "TOGEMON", "ELECMON", "GARURUMONA", "GARURUMON",
                 "WEREGARURUMON", "METALGARURUMON" -> "DATA";
            default -> "FREE";
        };
    }

    // ── Utilidades ──────────────────────────────────────────────────────

    private static boolean esLibre(String atr) {
        return atr.isEmpty() || "FREE".equals(atr) || "NONE".equals(atr)
            || "UNKNOWN".equals(atr) || "NO_DATA".equals(atr) || "VARIABLE".equals(atr);
    }

    /** Normaliza a mayúsculas sin espacios/guiones bajos. */
    private static String norm(String s) {
        if (s == null) return "";
        return s.trim().toUpperCase(java.util.Locale.ROOT)
            .replace(' ', '_').replace("-", "_");
    }
}
