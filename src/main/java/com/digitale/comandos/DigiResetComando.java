package com.digitale.comandos;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * /digi_reset — Reinicia el equipo del jugador borrando todos los digimons.
 * Vuelve al estado inicial para elegir nuevos compañeros.
 */
public class DigiResetComando extends AbstractPlayerCommand {

    public DigiResetComando(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext ctx,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {

        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());

        if (!datos.tieneEquipo) {
            ctx.sendMessage(Message.raw("§cNo tienes equipo. Nada que reiniciar."));
            ctx.sendMessage(Message.raw("§7Usa §e/digi_start §7para elegir compañeros."));
            return;
        }

        // Guardar los nombres antes de borrar
        String nombreA = datos.companeroA != null ? datos.companeroA.nombre : "ninguno";
        String nombreB = datos.companeroB != null ? datos.companeroB.nombre : "ninguno";

        // Limpiar todo
        datos.companeroA = null;
        datos.companeroB = null;
        datos.tieneEquipo = false;
        datos.enCombate = false;
        datos.enemigoNombre = "";
        datos.enemigoHp = 0;
        datos.enemigoHpMax = 0;
        datos.enemigoAtk = 0;
        datos.enemigoDef = 0;
        datos.enemigoExp = 0;
        datos.turno = 0;
        datos.logUltimo = "";
        datos.paseoActivo = false;
        datos.combate = null;

        ctx.sendMessage(Message.raw("§c╔══════════════════════════════════╗"));
        ctx.sendMessage(Message.raw("§c║ §fREINICIO DE EQUIPO §c║"));
        ctx.sendMessage(Message.raw("§c╠══════════════════════════════════╣"));
        ctx.sendMessage(Message.raw("§c║ §7Compañero A: §f" + nombreA + " §7 eliminado"));
        ctx.sendMessage(Message.raw("§c║ §7Compañero B: §f" + nombreB + " §7 eliminado"));
        ctx.sendMessage(Message.raw("§c╚══════════════════════════════════╝"));
        ctx.sendMessage(Message.raw(""));
        ctx.sendMessage(Message.raw("§e✓ Equipo reiniciado correctamente."));
        ctx.sendMessage(Message.raw("§7Usa §e/digi_start §7o §e/digi_tab §7para elegir nuevos compañeros."));
    }
}
