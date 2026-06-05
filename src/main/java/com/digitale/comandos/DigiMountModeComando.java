package com.digitale.comandos;

import com.digitale.sistema.MountMovementSupport;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

import java.util.Locale;
import java.util.UUID;

public class DigiMountModeComando extends AbstractPlayerCommand {

    public DigiMountModeComando(@Nonnull String name, @Nonnull String description) {
        super(name, description);
    }

    @Override
    @SuppressWarnings("nullness")
    protected void execute(@Nonnull CommandContext ctx,
                           @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef,
                           @Nonnull World world) {

        UUID uuid = playerRef.getUuid();
        String input = ctx.getInputString() == null ? "" : ctx.getInputString().trim().toLowerCase(Locale.ROOT);

        if (input.isEmpty()) {
            MountMovementSupport.MountMode current = MountMovementSupport.getMode(uuid);
            ctx.sendMessage(Message.raw("§bModo actual de montura: §f" + current));
            ctx.sendMessage(Message.raw("§7Uso: §f/digi_mountmode <default|auto|fly|swim>"));
            return;
        }

        MountMovementSupport.MountMode mode = switch (input) {
            case "default", "normal", "off" -> MountMovementSupport.MountMode.DEFAULT;
            case "auto" -> MountMovementSupport.MountMode.AUTO;
            case "fly", "vuelo", "aire" -> MountMovementSupport.MountMode.FLY;
            case "swim", "agua", "nadar" -> MountMovementSupport.MountMode.SWIM;
            default -> null;
        };

        if (mode == null) {
            ctx.sendMessage(Message.raw("§cModo invalido. Usa: §edefault§c, §eauto§c, §efly§c o §eswim§c."));
            return;
        }

        MountMovementSupport.setMode(uuid, mode);

        if (mode == MountMovementSupport.MountMode.DEFAULT) {
            MountMovementSupport.restoreOriginalIfNeeded(playerRef, ref, store, uuid);
        }

        ctx.sendMessage(Message.raw("§aModo de montura cambiado a §f" + mode + "§a."));
        ctx.sendMessage(Message.raw("§7Tip: se aplica cuando estes montado en un Digimon."));
    }
}
