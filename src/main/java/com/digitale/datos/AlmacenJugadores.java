package com.digitale.datos;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacén en memoria de datos de jugadores.
 * Dos compañeros Digimon por jugador (estilo Next Order).
 */
public class AlmacenJugadores {

    // ── Datos de jugador ───────────────────────────────────────────
    public static class DatosJugador {
        public DatoDigimon companeroA = null;
        public DatoDigimon companeroB = null;
        public boolean tieneEquipo = false;

        // Estado de combate activo
        public boolean enCombate     = false;
        public String  enemigoNombre = "";
        public String  enemigoElem   = "NEUTRO";
        public int     enemigoHp     = 0;
        public int     enemigoHpMax  = 0;
        public int     enemigoAtk    = 0;
        public int     enemigoDef    = 0;
        public int     enemigoExp    = 0;
        public int     turno         = 0;
        public String  logUltimo     = "";

        // Modo de combate Next Order: elegir táctica de cada compañero
        public String tacticaA = "BALANCEADO"; // AGRESIVO, BALANCEADO, DEFENSIVO
        public String tacticaB = "BALANCEADO";

        public void iniciarCombate(String nombre, String elem,
                                   int hp, int atk, int def, int exp) {
            enCombate = true;
            enemigoNombre = nombre; enemigoElem = elem;
            enemigoHp = hp; enemigoHpMax = hp;
            enemigoAtk = atk; enemigoDef = def;
            enemigoExp = exp; turno = 1;
            logUltimo = "¡" + nombre + " quiere pelear!";
            if (companeroA != null) companeroA.hpCombate = companeroA.hp;
            if (companeroB != null) companeroB.hpCombate = companeroB.hp;
        }

        public void terminarCombate() {
            enCombate = false;
            enemigoNombre = ""; enemigoHp = 0; turno = 0;
            if (companeroA != null) companeroA.hp = Math.max(1, companeroA.hpCombate);
            if (companeroB != null) companeroB.hp = Math.max(1, companeroB.hpCombate);
        }

        public boolean ambosVivos() {
            return companeroA != null && companeroA.vivo &&
                   companeroB != null && companeroB.vivo;
        }

        public boolean alguienVivo() {
            return (companeroA != null && companeroA.vivo && companeroA.hpCombate > 0) ||
                   (companeroB != null && companeroB.vivo && companeroB.hpCombate > 0);
        }
    }

    // ── Almacén estático ───────────────────────────────────────────
    private static final ConcurrentHashMap<UUID, DatosJugador> DATOS
        = new ConcurrentHashMap<>();

    public static DatosJugador obtener(UUID uuid) {
        return DATOS.computeIfAbsent(uuid, k -> new DatosJugador());
    }

    public static boolean existe(UUID uuid) {
        DatosJugador d = DATOS.get(uuid);
        return d != null && d.tieneEquipo;
    }
}
