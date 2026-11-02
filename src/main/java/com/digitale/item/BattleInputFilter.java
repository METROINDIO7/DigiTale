package com.digitale.item;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.inventory.DropItemStack;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bloquea el movimiento del jugador durante el combate. Los comandos ya no se
 * leen de WASD/ESPACIO/Q: el men&uacute; de batalla es una Page con botones
 * clickeables ({@code DigiBatallaComandosUI}).
 *
 * <p>Se consumen {@link ClientMovement} (para que el personaje no camine) y
 * {@link DropItemStack} (para que Q no tire el Sapotama durante la pelea).</p>
 */
public class BattleInputFilter implements PlayerPacketFilter {

    private static final Logger LOGGER = Logger.getLogger(BattleInputFilter.class.getName());

    private static final java.util.Map<java.util.UUID, String> ESTADO_LOG =
        new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public boolean test(PlayerRef playerRef, Packet packet) {
        DatosJugador d;
        try {
            d = AlmacenJugadores.getDatos(playerRef.getUuid());
        } catch (Exception e) {
            return false;
        }

        // Diagnóstico de transición (una línea por cambio de estado).
        try {
            String est = (d == null) ? "sin-datos"
                : ((d.menuAbierto ? "menu" : "-") + (d.enCombateUI() ? "+batalla" : ""));
            String ant = ESTADO_LOG.put(playerRef.getUuid(), est);
            if (!est.equals(ant)) {
                LOGGER.log(Level.INFO, "BattleInput estado " + playerRef.getUsername() + ": " + est);
            }
        } catch (Exception ignored) {
        }

        if (d == null) return false;

        boolean enBatalla = d.menuAbierto || d.enCombateUI();
        if (!enBatalla) return false;

        if (packet instanceof ClientMovement) return true;
        if (packet instanceof DropItemStack) return true;
        return false;
    }
}
