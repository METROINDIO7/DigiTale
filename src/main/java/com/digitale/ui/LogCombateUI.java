package com.digitale.ui;

import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;

import java.util.List;

/**
 * Pinta el registro de combate en una columna de etiquetas fijas.
 *
 * <p>En vez de un único Label con todo el texto (que se recorta por abajo y
 * oculta lo más reciente), se usan N huecos y se muestran <b>siempre las
 * últimas N líneas</b>: el registro "hace scroll" solo hacia lo nuevo. La línea
 * más reciente va en el último hueco con el prefijo {@code »}.</p>
 */
public final class LogCombateUI {

    private LogCombateUI() {
    }

    /**
     * @param cmd      builder de la UI
     * @param log      líneas del combate (orden antiguo → nuevo)
     * @param prefix   prefijo de los labels (p. ej. "HLog" para #HLog0..#HLogN)
     * @param slots    cantidad de etiquetas disponibles
     * @param maxChars recorte por línea (0 = sin recorte)
     */
    /** Recorta un texto para que quepa en un label de una línea. */
    public static String recortar(String texto, int maxChars) {
        if (texto == null) return "";
        if (maxChars <= 1 || texto.length() <= maxChars) return texto;
        return texto.substring(0, maxChars - 1) + "…";
    }

    /**
     * Texto del registro con auto-scroll: solo las últimas {@code maxLineas},
     * cada una recortada a una línea, con la más reciente marcada con {@code »}.
     */
    public static String texto(List<String> log, int maxLineas, int maxChars) {
        if (log == null || log.isEmpty() || maxLineas <= 0) return "";
        int n = Math.min(maxLineas, log.size());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            String linea = log.get(log.size() - n + i);
            if (linea == null) linea = "";
            if (maxChars > 1 && linea.length() > maxChars) {
                linea = linea.substring(0, maxChars - 1) + "…";
            }
            if (sb.length() > 0) sb.append('\n');
            sb.append(i == n - 1 ? "» " : "  ").append(linea);
        }
        return sb.toString();
    }
}
