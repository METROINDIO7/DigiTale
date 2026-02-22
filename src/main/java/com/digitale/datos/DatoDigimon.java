package com.digitale.datos;

/**
 * Datos de un Digimon compañero (estilo Next Order).
 * Dos instancias por jugador: compañero A y compañero B.
 */
public class DatoDigimon {

    // ── Identidad ──────────────────────────────────────────────────
    public String nombre        = "Sin nombre";
    public String especie       = "Ninguno";
    public String elemento      = "NEUTRO";
    public int    nivel         = 1;  // 1=Baby, 2=In-Training, 3=Rookie, 4=Champion, 5=Ultimate, 6=Mega

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

    // ── Evolución ─────────────────────────────────────────────────
    public int abi = 0;

    // ── Combate activo ────────────────────────────────────────────
    public int hpCombate = 0;

    // ─────────────────────────────────────────────────────────────

    /**
     * Starters disponibles (Baby / In-Training):
     *
     *  Botamon  → Koromon   → Agumon    (Fuego)
     *  Punimon  → Tsunomon  → Gabumon   (Hielo)
     *  Poyomon  → Tokomon   → Patamon   (Luz)
     *  Yuramon  → Tanemon   → Palmon    (Naturaleza)
     *  Pichimon → Bukamon   → Gomamon   (Agua)
     *  Nyokimon → Yokomon   → Biyomon   (Viento)
     */
    public static DatoDigimon crearInicial(String especie) {
        DatoDigimon d = new DatoDigimon();
        d.vivo = true;
        d.especie = especie;
        d.nombre  = especie;

        switch (especie) {
            // ── Baby (nivel 1) ──────────────────────────────────────
            case "Botamon" -> { d.elemento = "FUEGO";      d.maxHp = 40; d.atk = 3; d.def = 3; d.spd = 4; d.wis = 3; }
            case "Punimon" -> { d.elemento = "HIELO";      d.maxHp = 38; d.atk = 2; d.def = 5; d.spd = 3; d.wis = 3; }
            case "Poyomon" -> { d.elemento = "LUZ";        d.maxHp = 35; d.atk = 2; d.def = 3; d.spd = 5; d.wis = 5; }
            case "Yuramon" -> { d.elemento = "NATURALEZA"; d.maxHp = 36; d.atk = 3; d.def = 4; d.spd = 3; d.wis = 4; }
            case "Pichimon"-> { d.elemento = "AGUA";       d.maxHp = 38; d.atk = 3; d.def = 3; d.spd = 5; d.wis = 3; }
            case "Nyokimon"-> { d.elemento = "VIENTO";     d.maxHp = 36; d.atk = 2; d.def = 3; d.spd = 6; d.wis = 4; }
            // ── In-Training (nivel 2) ───────────────────────────────
            case "Koromon"  -> { d.elemento = "FUEGO";     d.maxHp = 65; d.atk = 7; d.def = 6; d.spd = 6; d.wis = 5; }
            case "Tsunomon" -> { d.elemento = "HIELO";     d.maxHp = 60; d.atk = 5; d.def = 9; d.spd = 5; d.wis = 6; }
            case "Tokomon"  -> { d.elemento = "LUZ";       d.maxHp = 55; d.atk = 5; d.def = 5; d.spd = 8; d.wis = 8; }
            case "Tanemon"  -> { d.elemento = "NATURALEZA";d.maxHp = 60; d.atk = 6; d.def = 7; d.spd = 5; d.wis = 7; }
            case "Bukamon"  -> { d.elemento = "AGUA";      d.maxHp = 62; d.atk = 6; d.def = 6; d.spd = 8; d.wis = 5; }
            case "Yokomon"  -> { d.elemento = "VIENTO";    d.maxHp = 58; d.atk = 5; d.def = 5; d.spd = 9; d.wis = 7; }
            default -> { d.elemento = "NEUTRO"; d.maxHp = 40; d.atk = 3; d.def = 3; d.spd = 4; d.wis = 3; }
        }

        d.hp = d.maxHp;
        d.hpCombate = d.maxHp;
        d.nivel = switch (especie) {
            case "Botamon","Punimon","Poyomon","Yuramon","Pichimon","Nyokimon" -> 1;
            default -> 2;
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

    /** Alimentar: recupera hambre y un poco de energía. */
    public void alimentar(int cantidad) {
        hambre  = Math.min(100, hambre  + cantidad);
        energia = Math.min(100, energia + cantidad / 2);
    }

    /** Descansar: recupera energía y HP. */
    public void descansar() {
        energia = Math.min(100, energia + 25);
        hp      = Math.min(maxHp, hp + maxHp / 5);
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
        return switch (objetivo) {
            // Baby → In-Training
            case "Koromon"   -> especie.equals("Botamon")  && nivel == 1 && (lazo >= 30 || victorias >= 1);
            case "Tsunomon"  -> especie.equals("Punimon")  && nivel == 1 && (lazo >= 30 || victorias >= 1);
            case "Tokomon"   -> especie.equals("Poyomon")  && nivel == 1 && (lazo >= 30 || victorias >= 1);
            case "Tanemon"   -> especie.equals("Yuramon")  && nivel == 1 && (lazo >= 30 || victorias >= 1);
            case "Bukamon"   -> especie.equals("Pichimon") && nivel == 1 && (lazo >= 30 || victorias >= 1);
            case "Yokomon"   -> especie.equals("Nyokimon") && nivel == 1 && (lazo >= 30 || victorias >= 1);
            // In-Training → Rookie
            case "Agumon"   -> especie.equals("Koromon")   && nivel == 2 && victorias >= 3  && lazo >= 40;
            case "Gabumon"  -> especie.equals("Tsunomon")  && nivel == 2 && victorias >= 3  && def >= 12;
            case "Patamon"  -> especie.equals("Tokomon")   && nivel == 2 && victorias >= 3  && wis >= 12;
            case "Palmon"   -> especie.equals("Tanemon")   && nivel == 2 && victorias >= 3  && wis >= 11;
            case "Gomamon"  -> especie.equals("Bukamon")   && nivel == 2 && victorias >= 3  && spd >= 14;
            case "Biyomon"  -> especie.equals("Yokomon")   && nivel == 2 && victorias >= 3  && spd >= 15;
            // Rookie → Champion
            case "Greymon"       -> especie.equals("Agumon")  && nivel == 3 && victorias >= 10 && lazo >= 50;
            case "GarurumonA"    -> especie.equals("Gabumon") && nivel == 3 && victorias >= 10 && def >= 20;
            case "Angemon"       -> especie.equals("Patamon") && nivel == 3 && victorias >= 10 && wis >= 20;
            case "Togemon"       -> especie.equals("Palmon")  && nivel == 3 && victorias >= 10 && def >= 18;
            case "Ikkakumon"     -> especie.equals("Gomamon") && nivel == 3 && victorias >= 10 && hp >= 160;
            case "Birdramon"     -> especie.equals("Biyomon") && nivel == 3 && victorias >= 10 && spd >= 22;
            // Champion → Ultimate
            case "MetalGreymon"  -> especie.equals("Greymon")    && nivel == 4 && victorias >= 25 && atk >= 40 && lazo >= 70;
            case "WereGarurumon" -> especie.equals("GarurumonA") && nivel == 4 && victorias >= 25 && def >= 45 && disciplina >= 60;
            case "MagnaAngemon"  -> especie.equals("Angemon")    && nivel == 4 && victorias >= 25 && wis >= 45;
            case "Lillymon"      -> especie.equals("Togemon")    && nivel == 4 && victorias >= 25 && wis >= 40;
            case "Zudomon"       -> especie.equals("Ikkakumon")  && nivel == 4 && victorias >= 25 && def >= 50;
            case "Garudamon"     -> especie.equals("Birdramon")  && nivel == 4 && victorias >= 25 && spd >= 50;
            // Ultimate → Mega
            case "WarGreymon"    -> especie.equals("MetalGreymon")  && nivel == 5 && victorias >= 50 && atk >= 80 && lazo >= 90 && abi >= 20;
            case "MetalGarurumon"-> especie.equals("WereGarurumon")&& nivel == 5 && victorias >= 50 && def >= 80 && abi >= 20;
            case "Seraphimon"    -> especie.equals("MagnaAngemon")  && nivel == 5 && victorias >= 50 && wis >= 90 && abi >= 20;
            case "Rosemon"       -> especie.equals("Lillymon")      && nivel == 5 && victorias >= 50 && wis >= 85 && lazo >= 85 && abi >= 20;
            case "MarineAngemon" -> especie.equals("Zudomon")       && nivel == 5 && victorias >= 50 && hp >= 400 && abi >= 20;
            case "Phoenixmon"    -> especie.equals("Garudamon")     && nivel == 5 && victorias >= 50 && spd >= 90 && abi >= 20;
            default -> false;
        };
    }

    public String nombreNivel() {
        return switch (nivel) {
            case 1 -> "Baby";
            case 2 -> "In-Training";
            case 3 -> "Rookie";
            case 4 -> "Champion";
            case 5 -> "Ultimate";
            case 6 -> "Mega";
            default -> "??";
        };
    }
}
