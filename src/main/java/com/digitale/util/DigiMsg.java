package com.digitale.util;

import com.hypixel.hytale.server.core.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper de mensajes de chat.
 *
 * Hytale NO parsea códigos § dentro de Message.raw() (llegan literales y la
 * fuente del juego no tiene esos glifos → se ven como basura). Este helper
 * convierte los §0-9a-f al API real: Message.raw(parte).color("#HEX").
 */
public final class DigiMsg {

    private DigiMsg() {
    }

    /** Convierte "§cRojo §7gris" en un Message con colores reales. */
    public static Message parse(String s) {
        if (s == null) return Message.raw("");
        List<Message> partes = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        String color = null;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if ((ch == '§' || ch == '&') && i + 1 < s.length()) {
                vaciar(actual, color, partes);
                color = hexPara(s.charAt(++i));
            } else {
                actual.append(ch);
            }
        }
        vaciar(actual, color, partes);
        if (partes.isEmpty()) return Message.raw("");
        if (partes.size() == 1) return partes.get(0);
        return Message.join(partes.toArray(new Message[0]));
    }

    private static void vaciar(StringBuilder actual, String color, List<Message> partes) {
        if (actual.length() == 0) return;
        Message m = Message.raw(actual.toString());
        if (color != null) m = m.color(color);
        partes.add(m);
        actual.setLength(0);
    }

    private static String hexPara(char codigo) {
        return switch (Character.toLowerCase(codigo)) {
            case '0' -> "#000000";
            case '1' -> "#0000AA";
            case '2' -> "#00AA00";
            case '3' -> "#00AAAA";
            case '4' -> "#AA0000";
            case '5' -> "#AA00AA";
            case '6' -> "#FFAA00";
            case '7' -> "#AAAAAA";
            case '8' -> "#555555";
            case '9' -> "#5555FF";
            case 'a' -> "#55FF55";
            case 'b' -> "#55FFFF";
            case 'c' -> "#FF5555";
            case 'd' -> "#FF55FF";
            case 'e' -> "#FFFF55";
            case 'f' -> "#FFFFFF";
            default -> null;
        };
    }
}
