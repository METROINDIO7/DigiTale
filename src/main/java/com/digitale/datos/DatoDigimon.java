package com.digitale.datos;

/**
 * Datos de un Digimon compañero (estilo Next Order).
 * Dos instancias por jugador: compañero A y compañero B.
 */
public class DatoDigimon {

    // ── Identidad ──────────────────────────────────────────────────
    public String nombre        = "Sin nombre";
    public String especie       = "Ninguno";
    public String elemento      = "NEUTRO";    // Elemento (Fuego/Agua/etc.) — debilidades
    public String atributo      = "NONE";       // Attribute (DATA/VIRUS/VACCINE/FREE/NONE)
    public int    nivel         = 1;  // 1=Baby, 2=In-Training, 3=Rookie, 4=Champion, 5=Ultimate, 6=Mega
    /** Nivel de aventura (sube con XP; desbloquea movimientos). */
    public int nivelAv = 1;
    /** Veces seguidas comiendo a tope (3 = se vuelve Burpmon). */
    public int vecesLleno = 0;

    // ── Stats de combate ───────────────────────────────────────────
    public int hp     = 40;
    public int maxHp  = 40;
    public int atk    = 3;
    public int def    = 3;
    public int spd    = 4;
    public int wis    = 3;

    // ── Stats de entrenamiento ─────────────────────────────────────
    public int entAtk = 0;
    public int entDef = 0;
    public int entSpd = 0;
    public int entWis = 0;
    public int entHp  = 0;

    // ── Lazo y disciplina ──────────────────────────────────────────
    public int lazo        = 20;
    public int disciplina  = 20;
    public int victorias   = 0;
    public int derrotas    = 0;

    // ── Estado ────────────────────────────────────────────────────
    public boolean vivo    = false;
    public int     hambre  = 100;
    public int     energia = 100;
    /** Dormido (banco): no come, no pasea, y en combate entra con DORMIDO. */
    public boolean dormido = false;
    /** Soporte (Calumon): no combate, potencia al equipo. */
    public boolean noCombate = false;
    /** Segundos epoch al dormirse (0 = no registrado). Para cura proporcional. */
    public int dormidoEnSeg = 0;
    /** Segundos de sueño para recuperación completa (como horno de pan). */
    public static final int SEG_DESCANSO = 300;

    // ── Evolución ─────────────────────────────────────────────────
    public int abi = 0;

    // ── Combate activo ────────────────────────────────────────────
    public int hpCombate = 0;

    // ─────────────────────────────────────────────────────────────

    /**
     * Starters disponibles (Baby / In-Training):
     *
     *  Botamon  → Koromon   → Agumon       (Fuego)
     *  Punimon  → Tsunomon  → Gabumon      (Hielo)
     *  Poyomon  → Tokomon   → Patamon      (Luz)
     *  Yuramon  → Tanemon   → Palmon       (Naturaleza)
     *  Pichimon → Bukamon   → Gomamon      (Agua)
     *  Nyokimon → Yokomon   → Biyomon      (Viento)
     *  Kuramon  → Koromon   → Agumon       (Fuego)
     *  Chibimon → Tanemon   → Palmon       (Naturaleza)
     */
    public static DatoDigimon crearInicial(String especie) {
        DatoDigimon d = new DatoDigimon();
        d.vivo = true;
        d.especie = especie;
        d.nombre  = especie;

        switch (especie) {
            // ── Baby (nivel 1) ──────────────────────────────────────
            case "Botamon"  -> { d.elemento = "NEUTRO";    d.atributo = "NONE";   d.maxHp = 40; d.atk = 3; d.def = 3; d.spd = 4; d.wis = 3; }
            case "Punimon"  -> { d.elemento = "NEUTRO";    d.atributo = "NONE";   d.maxHp = 38; d.atk = 2; d.def = 5; d.spd = 3; d.wis = 3; }
            case "Poyomon"  -> { d.elemento = "NEUTRO";    d.atributo = "NONE";   d.maxHp = 35; d.atk = 2; d.def = 3; d.spd = 5; d.wis = 5; }
            case "Yuramon"  -> { d.elemento = "NEUTRO";    d.atributo = "NONE";   d.maxHp = 36; d.atk = 3; d.def = 4; d.spd = 3; d.wis = 4; }
            case "Pichimon" -> { d.elemento = "NEUTRO";    d.atributo = "NONE";   d.maxHp = 38; d.atk = 3; d.def = 3; d.spd = 5; d.wis = 3; }
            case "Nyokimon" -> { d.elemento = "NEUTRO";    d.atributo = "NONE";   d.maxHp = 36; d.atk = 2; d.def = 3; d.spd = 6; d.wis = 4; }
            case "Kuramon"  -> { d.elemento = "NEUTRO";    d.atributo = "NONE";   d.maxHp = 39; d.atk = 3; d.def = 4; d.spd = 3; d.wis = 3; }
            case "Chibimon" -> { d.elemento = "NEUTRO";    d.atributo = "NONE";   d.maxHp = 35; d.atk = 2; d.def = 3; d.spd = 5; d.wis = 5; }
            case "Jyarimon" -> { d.elemento = "NEUTRO";    d.atributo = "NONE";   d.maxHp = 37; d.atk = 4; d.def = 2; d.spd = 4; d.wis = 3; }
            case "YukimiBotamon" -> { d.elemento = "HIELO"; d.atributo = "NONE";  d.maxHp = 36; d.atk = 2; d.def = 3; d.spd = 4; d.wis = 5; }
            case "Pagumon"  -> { d.elemento = "NEUTRO";    d.atributo = "VIRUS";  d.maxHp = 38; d.atk = 4; d.def = 3; d.spd = 4; d.wis = 2; }
            // ── Desconocidos (sin línea evolutiva normal) ───────────────
            case "Burpmon"  -> { d.elemento = "NEUTRO";    d.atributo = "UNKNOWN"; d.maxHp = 55; d.atk = 2; d.def = 2; d.spd = 2; d.wis = 1; }
            case "Calumon"  -> { d.elemento = "LUZ";       d.atributo = "UNKNOWN"; d.maxHp = 30; d.atk = 1; d.def = 1; d.spd = 3; d.wis = 10; d.noCombate = true; }
            case "NEO"      -> { d.elemento = "NEUTRO";    d.atributo = "UNKNOWN"; d.maxHp = 500; d.atk = 80; d.def = 70; d.spd = 60; d.wis = 60; }
            // ── In-Training 1 (nivel 2) — adquieren atributo DATA ──────
            case "Koromon"  -> { d.elemento = "NEUTRO"; d.atributo = "DATA";   d.maxHp = 65; d.atk = 7; d.def = 6; d.spd = 6; d.wis = 5; }
            case "Tsunomon" -> { d.elemento = "NEUTRO"; d.atributo = "DATA";   d.maxHp = 60; d.atk = 5; d.def = 9; d.spd = 5; d.wis = 6; }
            case "Tokomon"  -> { d.elemento = "NEUTRO"; d.atributo = "DATA";   d.maxHp = 55; d.atk = 5; d.def = 5; d.spd = 8; d.wis = 8; }
            case "Tanemon"  -> { d.elemento = "NEUTRO"; d.atributo = "DATA";   d.maxHp = 60; d.atk = 6; d.def = 7; d.spd = 5; d.wis = 7; }
            case "Bukamon"  -> { d.elemento = "NEUTRO"; d.atributo = "DATA";   d.maxHp = 62; d.atk = 6; d.def = 6; d.spd = 8; d.wis = 5; }
            case "Yokomon"  -> { d.elemento = "NEUTRO"; d.atributo = "DATA";   d.maxHp = 58; d.atk = 5; d.def = 5; d.spd = 9; d.wis = 7; }
            // ── In-Training 2 (nivel 3) — VIRUS o VACCINE ───────────────
            case "Agumon"   -> { d.elemento = "FUEGO"; d.atributo = "VACCINE"; d.maxHp = 90; d.atk = 12; d.def = 10; d.spd = 8; d.wis = 7; }
            case "Gabumon"  -> { d.elemento = "HIELO"; d.atributo = "VIRUS";   d.maxHp = 85; d.atk = 10; d.def = 14; d.spd = 7; d.wis = 8; }
            case "Patamon"  -> { d.elemento = "LUZ";   d.atributo = "VACCINE"; d.maxHp = 80; d.atk = 10; d.def = 9;  d.spd = 10; d.wis = 12; }
            case "Palmon"   -> { d.elemento = "NATURALEZA"; d.atributo = "VIRUS"; d.maxHp = 85; d.atk = 11; d.def = 12; d.spd = 7; d.wis = 11; }
            case "Gomamon"  -> { d.elemento = "AGUA"; d.atributo = "VACCINE"; d.maxHp = 88; d.atk = 11; d.def = 11; d.spd = 12; d.wis = 8; }
            case "Biyomon"  -> { d.elemento = "VIENTO"; d.atributo = "VIRUS";  d.maxHp = 82; d.atk = 9;  d.def = 10; d.spd = 14; d.wis = 12; }
            case "Veemon"   -> { d.elemento = "NEUTRO"; d.atributo = "VACCINE"; d.maxHp = 88; d.atk = 11; d.def = 10; d.spd = 13; d.wis = 9; }
            default -> { d.elemento = "NEUTRO"; d.maxHp = 40; d.atk = 3; d.def = 3; d.spd = 4; d.wis = 3; }
        }

        d.hp = d.maxHp;
        d.hpCombate = d.maxHp;
        d.nivel = switch (especie) {
            case "Botamon","Punimon","Poyomon","Yuramon","Pichimon","Nyokimon","Kuramon","Chibimon",
                 "Jyarimon","YukimiBotamon","Pagumon","Burpmon","Calumon","NEO" -> 1;
            case "Koromon","Tsunomon","Tokomon","Tanemon","Bukamon","Yokomon" -> 2;
            case "Agumon","Gabumon","Patamon","Palmon","Gomamon","Biyomon","Veemon" -> 3;
            default -> 1;
        };
        d.lazo = 20;
        d.disciplina = 20;
        return d;
    }

    /** Entrenar un stat. Cuesta energía. */
    public boolean entrenar(String stat, int sesiones) {
        int coste = sesiones * 15;
        if (energia < coste) return false;
        energia -= coste;
        switch (stat) {
            case "atk" -> { entAtk += sesiones * 2; atk += sesiones; }
            case "def" -> { entDef += sesiones * 2; def += sesiones; }
            case "spd" -> { entSpd += sesiones * 2; spd += sesiones; }
            case "wis" -> { entWis += sesiones * 2; wis += sesiones; }
            case "hp"  -> { entHp  += sesiones * 2; maxHp += sesiones * 5; hp = maxHp; }
        }
        return true;
    }

    /** Alimentar: recupera hambre, algo de energía y un poco de HP. */
    public void alimentar(int cantidad) {
        comer(cantidad, cantidad / 2, 10);
    }

    /** Comer comida de verdad: H hambre, E energía, P% del HP máx. */
    public void comer(int h, int e, int hpPctMax) {
        hambre  = Math.min(100, hambre  + h);
        energia = Math.min(100, energia + e);
        hp      = Math.min(maxHp, hp + maxHp * hpPctMax / 100);
    }

    /** Descansar: recupera energía y HP. */
    public void descansar() {
        energia = Math.min(100, energia + 25);
        hp      = Math.min(maxHp, hp + maxHp / 5);
    }

    /** Mandar a dormir: queda en el banco hasta que lo despierten. */
    public void dormir() {
        dormido = true;
        dormidoEnSeg = (int) (System.currentTimeMillis() / 1000L);
    }

    /**
     * Despertar: cura proporcional al tiempo dormido (5 min = full).
     * Dormir y despertar al instante casi no cura (anti-exploit, ni con relog:
     * sin timestamp se asume recién dormido). Retorna fracción 0..1.
     */
    public double despertar() {
        dormido = false;
        long ahora = System.currentTimeMillis() / 1000L;
        long inicio = dormidoEnSeg > 0 ? dormidoEnSeg : ahora;
        double frac = Math.min(1.0, Math.max(0.0, (ahora - inicio) / (double) SEG_DESCANSO));
        dormidoEnSeg = 0;
        hp = Math.min(maxHp, hp + (int) Math.round((maxHp - hp) * frac));
        energia = Math.min(100, energia + (int) Math.round((100 - energia) * frac));
        hambre = Math.max(0, hambre - (int) Math.round(25 * frac));
        return frac;
    }

    /** ¿Puede irse a dormir? (no con hambre crítica). */
    public boolean puedeDormir() {
        return !dormido && hambre >= 20;
    }

    /** Interactuar: sube lazo. */
    public void interactuar() {
        lazo = Math.min(100, lazo + 5);
    }

    /** Daño en combate (lazo afecta precisión/multiplicador). */
    public int calcularDanio(int defEnemigo) {
        double multLazo = 0.7 + (lazo / 100.0) * 0.6;  // 70% ~ 130%
        int dañoBase = Math.max(1, atk - defEnemigo / 2);
        return (int)(dañoBase * multLazo);
    }

    // ── Condiciones de evolución (árbol completo) ──────────────────
    public boolean puedeEvolucionar(String objetivo) {
        return puedeEvolucionar(objetivo, 0, 0);
    }

    /**
     * Con bonus de equipo (Calumon: +lazo/+victorias virtuales).
     * No muta nada, solo evalúa.
     */
    public boolean puedeEvolucionar(String objetivo, int bonoLazo, int bonoVic) {
        int lazoEf = lazo + bonoLazo;
        int vicEf = victorias + bonoVic;
        return switch (objetivo) {
            // Baby → In-Training
            case "Koromon"   -> especie.equals("Botamon")  && nivel == 1 && (lazoEf >= 30 || vicEf >= 1);
            case "Tsunomon"  -> especie.equals("Punimon")  && nivel == 1 && (lazoEf >= 30 || vicEf >= 1);
            case "Tokomon"   -> especie.equals("Poyomon")  && nivel == 1 && (lazoEf >= 30 || vicEf >= 1);
            case "Tanemon"   -> especie.equals("Yuramon")  && nivel == 1 && (lazoEf >= 30 || vicEf >= 1);
            case "Bukamon"   -> especie.equals("Pichimon") && nivel == 1 && (lazoEf >= 30 || vicEf >= 1);
            case "Yokomon"   -> especie.equals("Nyokimon") && nivel == 1 && (lazoEf >= 30 || vicEf >= 1);
            // In-Training 1 → In-Training 2
            case "Agumon"   -> especie.equals("Koromon")   && nivel == 2 && vicEf >= 3  && lazoEf >= 40;
            case "Gabumon"  -> especie.equals("Tsunomon")  && nivel == 2 && vicEf >= 3  && def >= 12;
            case "Patamon"  -> especie.equals("Tokomon")   && nivel == 2 && vicEf >= 3  && wis >= 12;
            case "Palmon"   -> especie.equals("Tanemon")   && nivel == 2 && vicEf >= 3  && wis >= 11;
            case "Gomamon"  -> especie.equals("Bukamon")   && nivel == 2 && vicEf >= 3  && spd >= 14;
            case "Biyomon"  -> especie.equals("Yokomon")   && nivel == 2 && vicEf >= 3  && spd >= 15;
            // In-Training 2 → Rookie
            case "Greymon"       -> especie.equals("Agumon")  && nivel == 3 && vicEf >= 10 && lazoEf >= 50;
            case "GarurumonA"    -> especie.equals("Gabumon") && nivel == 3 && vicEf >= 10 && def >= 20;
            case "Angemon"       -> especie.equals("Patamon") && nivel == 3 && vicEf >= 10 && wis >= 20;
            case "Togemon"       -> especie.equals("Palmon")  && nivel == 3 && vicEf >= 10 && def >= 18;
            case "Ikkakumon"     -> especie.equals("Gomamon") && nivel == 3 && vicEf >= 10 && hp >= 160;
            case "Birdramon"     -> especie.equals("Biyomon") && nivel == 3 && vicEf >= 10 && spd >= 22;
            // Rookie → Champion
            case "MetalGreymon"  -> especie.equals("Greymon")    && nivel == 4 && vicEf >= 25 && atk >= 40 && lazoEf >= 70;
            case "WereGarurumon" -> especie.equals("GarurumonA") && nivel == 4 && vicEf >= 25 && def >= 45 && disciplina >= 60;
            case "MagnaAngemon"  -> especie.equals("Angemon")    && nivel == 4 && vicEf >= 25 && wis >= 45;
            case "Lillymon"      -> especie.equals("Togemon")    && nivel == 4 && vicEf >= 25 && wis >= 40;
            case "Zudomon"       -> especie.equals("Ikkakumon")  && nivel == 4 && vicEf >= 25 && def >= 50;
            case "Garudamon"     -> especie.equals("Birdramon")  && nivel == 4 && vicEf >= 25 && spd >= 50;
            // Champion → Ultimate
            case "WarGreymon"       -> especie.equals("MetalGreymon")  && nivel == 5 && vicEf >= 50 && atk >= 80 && lazoEf >= 90 && abi >= 20;
            case "MetalGarurumon"  -> especie.equals("WereGarurumon")&& nivel == 5 && vicEf >= 50 && def >= 80 && abi >= 20;
            case "Seraphimon"      -> especie.equals("MagnaAngemon")  && nivel == 5 && vicEf >= 50 && wis >= 90 && abi >= 20;
            case "Rosemon"         -> especie.equals("Lillymon")      && nivel == 5 && vicEf >= 50 && wis >= 85 && lazoEf >= 85 && abi >= 20;
            case "MarineAngemon"   -> especie.equals("Zudomon")       && nivel == 5 && vicEf >= 50 && hp >= 400 && abi >= 20;
            case "Phoenixmon"      -> especie.equals("Garudamon")     && nivel == 5 && vicEf >= 50 && spd >= 90 && abi >= 20;
            // Ultimate → Mega
            case "Centaurusmon"    -> especie.equals("WarGreymon")   && nivel == 6 && vicEf >= 75 && atk >= 100 && lazoEf >= 100 && abi >= 30;
            case "Fujinmon"        -> especie.equals("WereGarurumon")&& nivel == 6 && vicEf >= 75 && def >= 100 && abi >= 30;
            case "OmniAngemon"     -> especie.equals("MagnaAngemon")  && nivel == 6 && vicEf >= 75 && wis >= 100 && abi >= 30;
            case "Rosezmemon"      -> especie.equals("Lillymon")      && nivel == 6 && vicEf >= 75 && wis >= 100 && lazoEf >= 100 && abi >= 30;
            case "Magnamon"        -> especie.equals("Zudomon")       && nivel == 6 && vicEf >= 75 && hp >= 500 && abi >= 30;
            case "Saturnmon"       -> especie.equals("Garudamon")     && nivel == 6 && vicEf >= 75 && spd >= 100 && abi >= 30;
            default -> false;
        };
    }

    public String nombreNivel() {
        return switch (nivel) {
            case 1 -> "Baby";
            case 2 -> "In-Training 1";
            case 3 -> "In-Training 2";
            case 4 -> "Rookie";
            case 5 -> "Champion";
            case 6 -> "Ultimate";
            case 7 -> "Mega";
            default -> "??";
        };
    }

    public int critico() {
        return (int) Math.min(50, 5 + (wis * 0.4));
    }

    public int evasion() {
        return (int) Math.min(50, 5 + (spd * 0.5));
    }

    public String[] nombresHabilidades() {
        return switch (nivel) {
            case 1, 2 -> new String[]{"Bola Debil", "Llama Bebe", "Toque Curativo",
                "Mordisco", "Cabezazo", "Torbellino", "Chispa Vital"};
            case 3, 4 -> new String[]{"Llama Media", "Corte Aereo", "Veneno",
                "Garra Veloz", "Embestida", "Onda Fria", "Rugido"};
            default   -> new String[]{"Mega Llamarada", "Tormenta", "Rayo Sagrado",
                "Colmillo Mitico", "Ciclon Final", "Puno Sagrado", "Juicio"};
        };
    }

    public int danoHabilidad(int slot) {
        return switch (slot) {
            case 1 -> max(2, atk * 1);
            case 2 -> max(3, atk * 3 / 2);
            case 3 -> max(5, atk * 2);
            case 4 -> max(6, atk * 5 / 2);
            case 5 -> max(8, atk * 3);
            case 6 -> max(10, atk * 7 / 2);
            case 7 -> max(12, atk * 4);
            default -> 0;
        };
    }

    private int max(int a, int b) { return Math.max(a, b); }

    // ── Movimientos (lógica In-Training I + genéricos) ─────────────

    /** Un movimiento aprendible: categoría define su efecto en el motor. */
    public static class Movimiento {
        public final String nombre;
        /** PROYECTIL, FISICO, DEBUFF, ESTADO, EXPLOSIVO */
        public final String categoria;
        /** Multiplicador de daño (0 = solo estado). */
        public final double mult;
        /** Estado que aplica (null si daño puro). */
        public final EstadoCombate.EstadoAlterado efecto;
        public final int turnos;
        public final int coste;
        public final int nivelReq;

        public Movimiento(String nombre, String categoria, double mult,
                          EstadoCombate.EstadoAlterado efecto, int turnos,
                          int coste, int nivelReq) {
            this.nombre = nombre;
            this.categoria = categoria;
            this.mult = mult;
            this.efecto = efecto;
            this.turnos = turnos;
            this.coste = coste;
            this.nivelReq = nivelReq;
        }
    }

    /** Pool In-Training I en orden de aprendizaje (por nivel de aventura). */
    public static final Movimiento[] MOVS_INTRAINING_1 = {
        new Movimiento("Bubbles", "PROYECTIL", 1.2, null, 0, 10, 1),
        new Movimiento("Dark Bubbles", "PROYECTIL", 1.3, null, 0, 12, 1),
        new Movimiento("Fluffy Feather", "PROYECTIL", 1.2, null, 0, 10, 1),
        new Movimiento("Crack-Crack-Crackle", "PROYECTIL", 1.4, null, 0, 15, 1),
        new Movimiento("Poop", "PROYECTIL", 1.5, null, 0, 15, 1),
        new Movimiento("Little Iron Beads", "FISICO", 1.8, null, 0, 20, 1),
        new Movimiento("Sticky Bubble", "DEBUFF", 0, EstadoCombate.EstadoAlterado.PARALISIS, 2, 30, 3),
        new Movimiento("Bubble Blow", "DEBUFF", 0, EstadoCombate.EstadoAlterado.VENENO, 3, 35, 3),
        new Movimiento("Poison Bubble", "PROYECTIL", 1.5, EstadoCombate.EstadoAlterado.VENENO, 3, 45, 3),
        new Movimiento("Smoke Screen", "DEBUFF", 0, EstadoCombate.EstadoAlterado.DORMIDO, 2, 40, 3),
        new Movimiento("Scare", "ESTADO", 0, EstadoCombate.EstadoAlterado.ATURDIDO, 1, 40, 5),
        new Movimiento("Blind", "ESTADO", 0, EstadoCombate.EstadoAlterado.ATURDIDO, 1, 45, 5),
        new Movimiento("Stun", "ESTADO", 0, EstadoCombate.EstadoAlterado.ATURDIDO, 2, 50, 5),
        new Movimiento("Small Energy Shot", "PROYECTIL", 1.6, null, 0, 40, 5),
        new Movimiento("Small Fire Shot", "PROYECTIL", 1.8, null, 0, 45, 5),
        new Movimiento("Small Water Shot", "PROYECTIL", 1.8, null, 0, 45, 5),
        new Movimiento("Small Wind Shot", "PROYECTIL", 1.7, null, 0, 45, 5),
        new Movimiento("Small Dark Shot", "PROYECTIL", 2.0, null, 0, 50, 5),
        new Movimiento("Body Slam", "FISICO", 2.5, null, 0, 70, 7),
        new Movimiento("Headbutt", "FISICO", 2.6, null, 0, 70, 7),
        new Movimiento("Tail Attack", "FISICO", 2.4, null, 0, 65, 7),
        new Movimiento("Bite", "FISICO", 2.8, null, 0, 80, 7),
        new Movimiento("Self-Destruct", "EXPLOSIVO", 6.0, null, 0, 150, 10),
    };

    /** Movimientos conocidos según nivel de aventura (en orden). */
    public java.util.List<Movimiento> movimientosConocidos() {
        java.util.List<Movimiento> out = new java.util.ArrayList<>();
        if (nivel == 2) {
            for (Movimiento m : MOVS_INTRAINING_1) {
                if (m.nivelReq <= nivelAv) out.add(m);
            }
            return out;
        }
        String[] base = nombresHabilidades();
        double[] mults = {1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0};
        int[] costes = {20, 50, 100, 60, 90, 120, 150};
        for (int i = 0; i < Math.min(base.length, 7); i++) {
            out.add(new Movimiento(base[i], "PROYECTIL", mults[i], null, 0, costes[i], 1));
        }
        return out;
    }

    /** Equipados: los últimos 7 conocidos (máx estilo Cyber Sleuth). */
    public java.util.List<Movimiento> movimientosEquipados() {
        java.util.List<Movimiento> conocidos = movimientosConocidos();
        if (conocidos.size() <= 7) return conocidos;
        return new java.util.ArrayList<>(conocidos.subList(conocidos.size() - 7, conocidos.size()));
    }
}
