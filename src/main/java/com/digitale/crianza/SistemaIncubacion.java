package com.digitale.crianza;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Motor de incubación de Digitamas (datos + odds). La Mesa de Crianza
 * (bench) lo consumirá cuando tenga UI; el comando admin lo usa para probar.
 */
public final class SistemaIncubacion {

    private SistemaIncubacion() {
    }

    private static final Random RNG = new Random();

    /** Combustible del huevo desconocido: basura, puro, completo o nada. */
    public enum FuelDesconocido {
        NADA, BASURA, PURO, COMPLETO
    }

    /** Entorno del huevo desconocido. */
    public enum EntornoDesconocido {
        NORMAL, BASURA, SAGRADO
    }

    public static class ResultadoDesconocido {
        public final String especie;
        public final List<String> log;

        public ResultadoDesconocido(String especie, List<String> log) {
            this.especie = especie;
            this.log = log;
        }
    }

    /**
     * Resuelve un Digitama_Desconocido.
     * Base: Burpmon 60 / Calumon 30 / NEO 10, con sesgos de fuel,
     * entorno, energía (1-10) y ciclos. NEO exige noche + Y baja + ciclos>=10
     * (salvo sobrecarga 8-10 sin requisitos: 5% NEO, resto Burpmon).
     */
    public static ResultadoDesconocido resolverDesconocido(FuelDesconocido fuel,
                                                           EntornoDesconocido entorno,
                                                           int energia, int ciclos,
                                                           boolean noche, boolean yBaja) {
        List<String> log = new ArrayList<>();
        double wB = 60.0, wC = 30.0, wN = 10.0;

        switch (fuel) {
            case BASURA -> {
                wB += 40.0;
                log.add("Datos basura: sesgo a Burpmon.");
            }
            case PURO -> {
                wC += 50.0;
                log.add("Datos puros: sesgo a Calumon.");
            }
            case COMPLETO -> {
                log.add("Nucleo completo detectado.");
                if (noche && yBaja && ciclos >= 10 && energia >= 8) {
                    log.add("Requisitos NEO cumplidos.");
                    return new ResultadoDesconocido("NEO", log);
                }
                wN += 20.0;
            }
            default -> { }
        }

        switch (entorno) {
            case BASURA -> {
                wB += 20.0;
                log.add("Entorno degradado: +Burpmon.");
            }
            case SAGRADO -> {
                wC += 20.0;
                log.add("Entorno sagrado: +Calumon.");
            }
            default -> { }
        }

        if (energia >= 1 && energia <= 3) {
            wB += 25.0;
            log.add("Energia baja: el huevo apenas acumula datos.");
        } else if (energia >= 4 && energia <= 7) {
            wC += 25.0;
            log.add("Energia equilibrada.");
        } else if (energia >= 8) {
            // Sobrecarga sin requisitos NEO: 5% NEO, resto colapsa en Burpmon.
            if (RNG.nextDouble() < 0.05) {
                log.add("Sobrecarga crítica: ¡NEO!");
                return new ResultadoDesconocido("NEO", log);
            }
            log.add("Sobrecarga: el huevo colapsa en datos...");
            return new ResultadoDesconocido("Burpmon", log);
        }

        if (ciclos <= 1) {
            wB += 30.0;
            log.add("Eclosion rapida: nace lo que come.");
        } else if (ciclos == 3) {
            wC += 20.0;
        }

        boolean neoPermitido = noche && yBaja && ciclos >= 10;
        if (!neoPermitido) wN = 0.0;

        double total = wB + wC + wN;
        double tiro = RNG.nextDouble() * total;
        String especie = tiro < wB ? "Burpmon" : (tiro < wB + wC ? "Calumon" : "NEO");
        log.add("Resultado: " + especie + ".");
        return new ResultadoDesconocido(especie, log);
    }

    /**
     * Eclosiona un huevo normal al primer slot libre.
     * Retorna el slot o -1 (lleno). El Digimon nace vivo nivel 1.
     */
    public static int eclosionar(AlmacenJugadores.DatosJugador datos, DigitamaDef def) {
        if (datos == null || def == null) return -1;
        DatoDigimon bebe = DatoDigimon.crearInicial(def.especie);
        if (def.nivelRes > 1) {
            // Eclosión avanzada: stats base de su etapa (crearInicial ya escala por especie).
            bebe.nivel = def.nivelRes;
            bebe.vivo = true;
        }
        bebe.vivo = true;
        return datos.agregarAlEquipo(bebe);
    }

    /** Bebe vinculado a un enemigo vencido (para DatoIncubacion). Null si no aplica. */
    public static String bebeDe(String enemigoNombre) {
        if (enemigoNombre == null) return null;
        String base = enemigoNombre.endsWith(" Salvaje")
            ? enemigoNombre.substring(0, enemigoNombre.length() - " Salvaje".length())
            : enemigoNombre;
        Map<String, String> mapa = new HashMap<>();
        mapa.put("Botamon", "Botamon");
        mapa.put("Punimon", "Punimon");
        mapa.put("Poyomon", "Poyomon");
        mapa.put("Yuramon", "Yuramon");
        mapa.put("Pichimon", "Pichimon");
        mapa.put("Nyokimon", "Nyokimon");
        mapa.put("Kuramon", "Kuramon");
        mapa.put("Chibimon", "Chibimon");
        mapa.put("Jyarimon", "Jyarimon");
        mapa.put("YukimiBotamon", "YukimiBotamon");
        mapa.put("Koromon", "Botamon");
        mapa.put("Tsunomon", "Punimon");
        mapa.put("Tokomon", "Poyomon");
        mapa.put("Tanemon", "Yuramon");
        mapa.put("Bukamon", "Pichimon");
        mapa.put("Yokomon", "Nyokimon");
        mapa.put("Agumon", "Botamon");
        mapa.put("Gabumon", "Punimon");
        mapa.put("Patamon", "Poyomon");
        mapa.put("Palmon", "Yuramon");
        mapa.put("Gomamon", "Pichimon");
        mapa.put("Biyomon", "Nyokimon");
        mapa.put("Veemon", "Botamon");
        return mapa.get(base);
    }
}
