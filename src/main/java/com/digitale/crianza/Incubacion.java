package com.digitale.crianza;

/**
 * Timestamps, no ticks: 1 ciclo = 60 s reales. Sobrevive reinicios
 * y desconexiones porque todo es resta contra inicioSeg.
 */
public final class Incubacion {

    private Incubacion() {
    }

    /** Segundos reales por ciclo (legado: 1 ciclo = 60 s). */
    public static final int CICLO_SEG = 60;

    public static int totalSeg(int ciclos) {
        return totalSeg(ciclos, CICLO_SEG);
    }

    public static int totalSeg(int ciclos, int segPorCiclo) {
        return Math.max(0, ciclos) * Math.max(1, segPorCiclo);
    }

    public static int transcurridoSeg(int inicioSeg) {
        long ahora = System.currentTimeMillis() / 1000L;
        return (int) Math.max(0, ahora - (long) inicioSeg);
    }

    public static int restanteSeg(int inicioSeg, int ciclos) {
        return restanteSeg(inicioSeg, ciclos, CICLO_SEG);
    }

    public static int restanteSeg(int inicioSeg, int ciclos, int segPorCiclo) {
        return Math.max(0, totalSeg(ciclos, segPorCiclo) - transcurridoSeg(inicioSeg));
    }

    public static float progreso(int inicioSeg, int ciclos) {
        return progreso(inicioSeg, ciclos, CICLO_SEG);
    }

    public static float progreso(int inicioSeg, int ciclos, int segPorCiclo) {
        int total = totalSeg(ciclos, segPorCiclo);
        if (total <= 0) return 0f;
        return Math.min(1.0f, (float) transcurridoSeg(inicioSeg) / (float) total);
    }

    public static boolean listo(int inicioSeg, int ciclos) {
        return listo(inicioSeg, ciclos, CICLO_SEG);
    }

    public static boolean listo(int inicioSeg, int ciclos, int segPorCiclo) {
        return transcurridoSeg(inicioSeg) >= totalSeg(ciclos, segPorCiclo);
    }

    public static String mmSs(int segundos) {
        int s = Math.max(0, segundos);
        return (s / 60) + "m " + (s % 60) + "s";
    }
}
