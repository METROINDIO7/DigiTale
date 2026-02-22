package com.digitale.comandos;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
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
 * /digi cuidar <a|b|ambos> [alimentar|mimar|descansar]
 */
public class DigiCuidarComando extends AbstractPlayerCommand {

    public DigiCuidarComando(@NonNullDecl String name, @NonNullDecl String description) {
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
            ctx.sendMessage(Message.raw("§cNo tienes compañeros. Usa §e/digi start"));
            return;
        }

        String[] args = ctx.getInputString().trim().isEmpty() ? new String[0] : ctx.getInputString().trim().split("\\s+");
        String quien  = args.length > 0 ? args[0].toLowerCase() : "ambos";
        String accion = args.length > 1 ? args[1].toLowerCase() : "alimentar";

        boolean aA = quien.equals("a") || quien.equals("ambos");
        boolean aB = quien.equals("b") || quien.equals("ambos");

        if (aA && datos.companeroA != null) aplicarCuidado(ctx, datos.companeroA, "A", accion);
        if (aB && datos.companeroB != null) aplicarCuidado(ctx, datos.companeroB, "B", accion);

        if (!aA && !aB) {
            ctx.sendMessage(Message.raw("§eUso: §f/digi cuidar <a|b|ambos> [alimentar|mimar|descansar]"));
        }
    }

    private void aplicarCuidado(CommandContext ctx, DatoDigimon d, String slot, String accion) {
        if (!d.vivo) { ctx.sendMessage(Message.raw("§8[" + slot + "] No hay compañero.")); return; }
        switch (accion) {
            case "alimentar" -> {
                d.alimentar(30);
                ctx.sendMessage(Message.raw("§a[" + slot + "] " + d.nombre +
                    " §7comió. Hambre: §f" + d.hambre + "/100  Energía: §f" + d.energia + "/100"));
            }
            case "mimar" -> {
                d.interactuar();
                ctx.sendMessage(Message.raw("§d[" + slot + "] " + d.nombre +
                    " §7parece contento. Lazo: §f" + d.lazo + "/100"));
            }
            case "descansar" -> {
                d.descansar();
                ctx.sendMessage(Message.raw("§e[" + slot + "] " + d.nombre +
                    " §7descansó. HP: §f" + d.hp + "/" + d.maxHp +
                    "  Energía: §f" + d.energia + "/100"));
            }
            default ->
                ctx.sendMessage(Message.raw("§eAcciones: §falimentar §8| §fmimar §8| §fdescansar"));
        }
    }
}
