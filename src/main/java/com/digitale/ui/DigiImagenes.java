package com.digitale.ui;

import java.util.Map;

/**
 * Mapeo especie → retrato PNG para las UIs.
 *
 * Los PNG viven en {@code Common/UI/Custom/png_Digis/} y en el markup inline
 * se referencian con ruta absoluta desde Common:
 * {@code AssetPath: "UI/Custom/png_Digis/botamon.png"}.
 *
 * Si la especie no tiene PNG, retorna null y la UI debe ocultar el
 * AssetImage (Visible=false) en vez de apuntar a un archivo inexistente.
 */
public final class DigiImagenes {

    private DigiImagenes() {
    }

    /** Nombre exacto del archivo en disco (las rutas son case-sensitive). */
    private static final Map<String, String> ARCHIVO = Map.of(
        "botamon", "botamon.png",
        "chibimon", "Chibimon.png",
        "jyarimon", "Jyarimon.png",
        "kuramon", "Kuramon.png",
        "nyokimon", "Nyokimon.png",
        "pichimon", "Pichimon.png",
        "punimon", "Punimon.png",
        "veemon", "Veemon.png",
        "yukimibotamon", "YukimiBotamon.png"
    );

    /** Retrato por defecto (siempre existe). */
    public static final String POR_DEFECTO = "UI/Custom/png_Digis/botamon.png";

    /**
     * Icono del elemento. Ruta exacta (case-sensitive) o null si no hay.
     * Elementos del motor: FUEGO, AGUA, NATURALEZA, HIELO, TRUENO, VIENTO,
     * LUZ, OSCURIDAD, TIERRA, NEUTRO.
     */
    public static String iconoElemento(String elemento) {
        if (elemento == null) return null;
        String archivo = switch (elemento.toUpperCase()) {
            case "FUEGO" -> "Fire.png";
            case "AGUA" -> "Water.png";
            case "NATURALEZA" -> "Nature_Plant.png";
            case "HIELO" -> "Ice.png";
            case "TRUENO" -> "Electricity.png";
            case "VIENTO" -> "Wind.png";
            case "LUZ" -> "Holy_Light.png";
            case "OSCURIDAD" -> "Darkness.png";
            case "TIERRA" -> "Earth.png";
            case "NEUTRO" -> "Neutral.png";
            default -> null;
        };
        if (archivo == null) return null;
        return "UI/Textures/Icons/Elemental/" + archivo;
    }

    /**
     * Icono del atributo (DATA/VIRUS/VACCINE/FREE/NONE). Siempre retorna
     * algo (Unknown.png si no se reconoce) porque todo Digimon tiene atributo.
     */
    public static String iconoAtributo(String atributo) {
        String archivo = "Unknown.png";
        if (atributo != null) {
            archivo = switch (atributo.toUpperCase()) {
                case "DATA" -> "Data.png";
                case "VIRUS" -> "Virus.png";
                case "VACCINE" -> "Vaccine.png";
                case "FREE" -> "Free.png";
                case "NONE" -> "None.png";
                default -> "Unknown.png";
            };
        }
        return "UI/Textures/Icons/Type/" + archivo;
    }

    /**
     * Retorna la ruta del PNG para la especie, o null si no hay retrato.
     * Normaliza a minúsculas sin espacios ni guiones bajos.
     */
    public static String rutaPara(String especie) {
        if (especie == null || especie.isEmpty()) return null;
        String base = especie.toLowerCase().replace(" ", "").replace("_", "");
        String archivo = ARCHIVO.get(base);
        if (archivo == null) return null;
        return "UI/Custom/png_Digis/" + archivo;
    }
}
