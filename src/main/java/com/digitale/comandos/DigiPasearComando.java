package com.digitale.comandos;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.sistema.SistemaPaseo;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * /digi_pasear
 * 
 * Alterna entre spawnear y recoger los compañeros actuales en el mundo.
 * Usa el sistema de spawn de NPCs de Hytale (/npc spawn).
 */
public class DigiPasearComando extends AbstractPlayerCommand {
    private static final Logger LOGGER = Logger.getLogger(DigiPasearComando.class.getName());

    public DigiPasearComando(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext ctx,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {

        UUID uuid = playerRef.getUuid();
        DatosJugador datos = AlmacenJugadores.obtener(uuid);

        if (!datos.tieneEquipo) {
            ctx.sendMessage(Message.raw("§cNo tienes compañeros. Usa §e/digi_start §cpara obtener tus primeros Digimon."));
            return;
        }

        if (datos.enCombate) {
            ctx.sendMessage(Message.raw("§c¡Estás en combate! No puedes pasear compañeros ahora."));
            return;
        }

        // ── Recoger compañeros (desactivar paseo)
        if (datos.paseoActivo) {
            LOGGER.log(Level.INFO, "[DigiPasear] Recogiendo compañeros de " + playerRef.getUsername());
            SistemaPaseo.recogerCompaneros(playerRef, ref, store);
            datos.paseoActivo = false;
            ctx.sendMessage(Message.raw("§a✓ Has recogido a tus compañeros."));
            return;
        }

        // ── Spawnear compañeros (activar paseo)
        LOGGER.log(Level.INFO, "[DigiPasear] Spawnando compañeros para " + playerRef.getUsername());
        
        try {
            // Registrar el intento de spawn (es asincrónico con world.execute())
            SistemaPaseo.spawnearCompaneros(playerRef, ref, store);
            
            // Preparar información de spawn para mostrar al usuario
            StringBuilder sb = new StringBuilder();
            sb.append("§a✓ Compañeros listos para pasear:\n");
            
            // Calcular posiciones (ya que world.execute() es asincrónico, lo hacemos aquí)
            if (datos.companeroA != null) {
                String posA = String.format("%.1f %.1f %.1f",
                    datos.posSpawnX + 2.5, datos.posSpawnY + 1.0, datos.posSpawnZ);
                sb.append("§7  [A] ").append(datos.companeroA.nombre).append(" (").append(datos.companeroA.especie).append(")\n");
                sb.append("     §8Posición: ").append(posA).append("\n");
                LOGGER.log(Level.INFO, "[DigiPasear] Rol A preparado: " + datos.companeroA.especie + "_Companero @ " + posA);
            }
            
            if (datos.companeroB != null) {
                String posB = String.format("%.1f %.1f %.1f",
                    datos.posSpawnX - 2.5, datos.posSpawnY + 1.0, datos.posSpawnZ);
                sb.append("§7  [B] ").append(datos.companeroB.nombre).append(" (").append(datos.companeroB.especie).append(")\n");
                sb.append("     §8Posición: ").append(posB).append("\n");
                LOGGER.log(Level.INFO, "[DigiPasear] Rol B preparado: " + datos.companeroB.especie + "_Companero @ " + posB);
            }
            
            datos.paseoActivo = true;
            ctx.sendMessage(Message.raw(sb.toString()));
            ctx.sendMessage(Message.raw("§7Usa §f/digi_pasear §7nuevamente para recoger."));
            LOGGER.log(Level.INFO, "[DigiPasear] Spawn iniciado exitosamente");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[DigiPasear] Error: " + e.getMessage());
            e.printStackTrace();
            ctx.sendMessage(Message.raw("§c✗ Error al preparar spawn. Revisa los logs."));
        }
    }
}
