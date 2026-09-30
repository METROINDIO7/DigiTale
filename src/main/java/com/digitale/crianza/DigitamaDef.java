package com.digitale.crianza;

/**
 * Definición de un Digitama: huevo + receta de incubación + resultado.
 * Los ingredientes/entorno son datos para la Mesa de Crianza (los valida ella);
 * el motor usa especie/nivel/odds.
 */
public class DigitamaDef {

    /** Id del item (Items/Eggs/<id>.json sin extensión). */
    public String idItem;
    public String nombre;
    /** Dragon's Roar, Deep Savers, Nightmare Soldiers, Metal Empire, Nature Spirits, Virus Busters, Desconocido. */
    public String familia;
    /** Data, Vaccine, Virus, Desconocido. */
    public String atributo;
    /** Basico, Medio, Avanzado, Legendario. */
    public String tier;
    public String[] ingredientes;
    public String entorno;
    /** Energía requerida 1-10. */
    public int energia;
    /** Ciclos de incubación. */
    public int ciclos;
    /** Segundos reales por ciclo (1 ciclo por defecto; varía por tier). */
    public int segPorCiclo = 60;
    /** Especie al nacer. */
    public String especie;
    /** Nivel evolutivo al nacer (1 baby, 2 in-training, 3 rookie). */
    public int nivelRes;
    /** Pool de especies (la principal primero). Si falta, se usa especie. */
    public String[] especies;
    /** Apariencia para huevos salvajes (modelos Digitame_* existentes). */
    public String modeloNpc;

    public DigitamaDef(String idItem, String nombre, String familia, String atributo, String tier,
                       String[] ingredientes, String entorno, int energia, int ciclos,
                       String especie, int nivelRes, String modeloNpc) {
        this.idItem = idItem;
        this.nombre = nombre;
        this.familia = familia;
        this.atributo = atributo;
        this.tier = tier;
        this.ingredientes = ingredientes;
        this.entorno = entorno;
        this.energia = energia;
        this.ciclos = ciclos;
        this.especie = especie;
        this.nivelRes = nivelRes;
        this.modeloNpc = modeloNpc;
    }

    public static final DigitamaDef[] TODOS = {
        new DigitamaDef("Digitama_Verde", "Huevo Verde", "Nature Spirits", "Data", "Basico",
            new String[]{"5x DigiManzana", "1x Bloque de Tierra", "1x Semilla"},
            "Cerca de arboles/cesped o bosque/llanura", 2, 2, "Yuramon", 1, "Digitame_Planta"),
        new DigitamaDef("Digitama_Naranja", "Huevo Naranja", "Dragon's Roar", "Vaccine", "Basico",
            new String[]{"5x DigiCarne", "1x Piedra de Fuego (o Carbon)"},
            "Cerca de lava/fuego o bioma calido", 5, 2, "Botamon", 1, "Digitame_Bestia"),
        new DigitamaDef("Digitama_Amarillo", "Huevo Amarillo", "Metal Empire", "Vaccine", "Basico",
            new String[]{"5x Lingote de Hierro", "1x Redstone"},
            "Cerca de metal o construccion artificial", 5, 3, "Kuramon", 1, "Digitame_Bestia"),
        new DigitamaDef("Digitama_Rombos", "Huevo Azul (Rombos)", "Deep Savers", "Data", "Basico",
            new String[]{"5x DigiPescado", "1x Alga (o Kelp)"},
            "Rodeado de agua u oceano/rio", 3, 2, "Pichimon", 1, "Digitame_Planta"),
        new DigitamaDef("Digitama_Morado", "Huevo Morado", "Nightmare Soldiers", "Virus", "Basico",
            new String[]{"5x Carne Podrida", "1x Polvo de Hueso"},
            "De noche o cueva oscura (luz 0)", 3, 2, "YukimiBotamon", 1, "Digitame_Oscuro"),
        new DigitamaDef("Digitama_Rojo", "Huevo Rojo", "Dragon's Roar", "Virus", "Basico",
            new String[]{"5x Carne Podrida", "1x Ceniza"},
            "De noche o bioma arido", 5, 2, "Jyarimon", 1, "Digitame_Bestia_II"),
        new DigitamaDef("Digitama_Olas", "Huevo Azul Oscuro", "Deep Savers", "Data", "Avanzado",
            new String[]{"10x DigiPescado", "1x Perla", "1x Prismarina"},
            "Bajo el agua (Y<60) u oceano profundo", 7, 4, "Bukamon", 2, "Digitame_Planta"),
        new DigitamaDef("Digitama_Llamas", "Huevo Rojo-Blanco", "Nightmare Soldiers", "Virus", "Medio",
            new String[]{"10x Carne Podrida", "1x Ojo de Arana", "1x Soul Sand"},
            "De noche en Badlands o Nether", 6, 3, "Yokomon", 2, "Digitame_Bestia_II"),
        new DigitamaDef("Digitama_Azul", "Huevo Azul Claro", "Metal Empire", "Data", "Medio",
            new String[]{"5x Lingote de Oro", "1x Hielo", "1x Circuito"},
            "Bioma frio o cerca de hielo", 7, 4, "Tokomon", 2, "Digitame_Planta"),
        new DigitamaDef("Digitama_Calavera", "Huevo Calavera", "Nightmare Soldiers", "Virus", "Avanzado",
            new String[]{"1x Huevo Morado", "1x Craneo", "5x Huesos", "1x Datos de Virus"},
            "Cueva profunda (Y<30), noche, sin luz", 9, 6, "Pagumon", 1, "Digitame_Oscuro"),
        new DigitamaDef("Digitama_Corazon", "Huevo Corazon", "Virus Busters", "Vaccine", "Basico",
            new String[]{"5x DigiManzana Dorada", "1x Flor", "1x Glowstone"},
            "De dia con sol directo o altar", 5, 3, "Poyomon", 1, "Digitame_Planta"),
        new DigitamaDef("Digitama_Gris", "Huevo Gris", "Metal Empire", "Vaccine", "Avanzado",
            new String[]{"1x Huevo Amarillo", "10x Hierro", "1x Diamante"},
            "Mesa con energia (generador/rayo)", 10, 6, "Tanemon", 2, "Digitame_Bestia"),
        new DigitamaDef("Digitama_Dorado", "Huevo Dorado", "Virus Busters", "Vaccine", "Legendario",
            new String[]{"1x Huevo Rosa", "1x Huevo Corazon", "1x Oro", "1x Nucleo Sagrado"},
            "Y>150, de dia, despejado", 10, 8, "Patamon", 3, "Digitame_Dragon"),
        new DigitamaDef("Digitama_Dragon", "Huevo Dragon", "Dragon's Roar", "Vaccine", "Avanzado",
            new String[]{"1x Huevo Naranja", "10x DigiCarne", "1x Escama de Dragon"},
            "Mesa sobre Y=100", 8, 5, "Koromon", 2, "Digitame_Dragon"),
        new DigitamaDef("Digitama_Rosa", "Huevo Rosa", "Virus Busters", "Vaccine", "Medio",
            new String[]{"10x Flores", "1x Anillo Sagrado", "1x Polvo de Hada"},
            "Flower Forest o luz sagrada", 8, 5, "Chibimon", 1, "Digitame_Planta"),
    };

    public static DigitamaDef porItem(String idItem) {
        DigitamaDef d = SistemaIncubacion.porItem(idItem);
        return (d != null) ? d : porItemBase(idItem);
    }

    /** Pool efectivo (nunca vacío si hay especie). */
    public java.util.List<String> pool() {
        java.util.List<String> out = new java.util.ArrayList<>();
        if (especies != null) {
            for (String e : especies) if (e != null && !e.isEmpty() && !out.contains(e)) out.add(e);
        }
        if (out.isEmpty() && especie != null && !especie.isEmpty()) out.add(especie);
        return out;
    }

    public static DigitamaDef porBebe(String especie) {
        DigitamaDef d = SistemaIncubacion.porBebe(especie);
        return (d != null) ? d : porBebeBase(especie);
    }

    static DigitamaDef porItemBase(String idItem) {
        if (idItem == null) return null;
        for (DigitamaDef d : TODOS) {
            if (d.idItem.equals(idItem)) return d;
        }
        return null;
    }

    static DigitamaDef porBebeBase(String especie) {
        if (especie == null) return null;
        for (DigitamaDef d : TODOS) {
            if (d.especie.equals(especie)) return d;
        }
        return null;
    }
}
