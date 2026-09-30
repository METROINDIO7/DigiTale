package com.digitale.item;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.ui.MenuBatalla;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.inventory.DropItemStack;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Input del menú de combate por teclado (solo con menú abierto):
 * - ClientMovement: W/S navegan (flanco por cambio de dirección o 280ms),
 *   ESPACIO (salto, flanco de subida) acepta. Se bloquea el paquete (= quieto).
 * - DropItemStack (Q): retrocede y se bloquea (no tira el item).
 * Corre en hilo de red: solo lee, muta flags RAM y encola al mundo.
 */
public class BattleInputFilter implements PlayerPacketFilter {

    private static final Logger LOGGER = Logger.getLogger(BattleInputFilter.class.getName());
    private static final long TROTE_MS = 280L;

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
        if (d == null || !d.menuAbierto) {
            // Sin menú: congelar movimiento si hay batalla nueva activa.
            if (d != null && d.enCombateUI()) {
                if (packet instanceof ClientMovement) return true;
            }
            return false;
        }

        if (packet instanceof ClientMovement m) {
            manejarMovimiento(playerRef, d, m);
            return true;
        }
        if (packet instanceof DropItemStack) {
            enMundo(playerRef, () -> {
                // Releer por si cambió entre tanto.
                DatosJugador dd = AlmacenJugadores.getDatos(playerRef.getUuid());
                if (dd == null || !dd.menuAbierto) return;
                Ref<EntityStore> r = playerRef.getReference();
                if (r == null || !r.isValid()) return;
                MenuBatalla.atras(playerRef, r, r.getStore());
            });
            return true;
        }
        return false;
    }

    private void manejarMovimiento(PlayerRef playerRef, DatosJugador d, ClientMovement m) {
        try {
            boolean salto = m.movementStates != null && m.movementStates.jumping;
            if (salto && !d.menuLastJump) {
                d.menuLastJump = true;
                enMundo(playerRef, () -> aceptarSeguro(playerRef));
            } else if (!salto) {
                d.menuLastJump = false;
            }

            String dir = "";
            if (m.wishMovement != null) {
                double ax = Math.abs(m.wishMovement.x);
                double az = Math.abs(m.wishMovement.z);
                double dom = Math.max(ax, az);
                if (dom >= 0.3) {
                    double v = (az >= ax) ? m.wishMovement.z : m.wishMovement.x;
                    dir = v > 0 ? "up" : "down";
                }
            }
            long ahora = System.currentTimeMillis();
            if (!dir.isEmpty() && (!dir.equals(d.menuLastDir) || ahora - d.menuLastMoveMs >= TROTE_MS)) {
                d.menuLastDir = dir;
                d.menuLastMoveMs = ahora;
                String finalDir = dir;
                enMundo(playerRef, () -> moverSeguro(playerRef, finalDir));
            } else if (dir.isEmpty()) {
                d.menuLastDir = "";
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINEST, "BattleInput: " + e.getMessage());
        }
    }

    private void aceptarSeguro(PlayerRef playerRef) {
        try {
            DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
            if (d == null || !d.menuAbierto) return;
            Ref<EntityStore> r = playerRef.getReference();
            if (r == null || !r.isValid()) return;
            MenuBatalla.aceptar(playerRef, r, r.getStore());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "BattleInput aceptar: " + e.getMessage());
        }
    }

    private void moverSeguro(PlayerRef playerRef, String dir) {
        try {
            DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
            if (d == null || !d.menuAbierto) return;
            Ref<EntityStore> r = playerRef.getReference();
            if (r == null || !r.isValid()) return;
            MenuBatalla.mover(playerRef, r, r.getStore(), dir);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "BattleInput mover: " + e.getMessage());
        }
    }

    private void enMundo(PlayerRef playerRef, Runnable tarea) {
        try {
            Ref<EntityStore> r = playerRef.getReference();
            if (r == null || !r.isValid()) return;
            Store<EntityStore> store = r.getStore();
            store.getExternalData().getWorld().execute(() -> {
                try {
                    tarea.run();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "BattleInput mundo: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "BattleInput enMundo: " + e.getMessage());
        }
    }
}
