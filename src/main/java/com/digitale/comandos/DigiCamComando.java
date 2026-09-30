package com.digitale.comandos;

import com.digitale.util.DigiMsg;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.MouseInputTargetType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * /digi_cam [dist] [lock] [cursor] — diagnóstico de cámara fija.
 * Ej: /digi_cam 7 1 1 (7 bloques, fija, con cursor). /digi_cam 0 0 0 libera.
 */
public class DigiCamComando extends AbstractPlayerCommand {

    public DigiCamComando(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext ctx,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {
        String[] args = ctx.getInputString().trim().isEmpty()
            ? new String[0]
            : ctx.getInputString().trim().split("\\s+");
        float dist = 9f;
        boolean lock = true;
        boolean cursor = true;
        float pitch = -0.85f;
        try {
            if (args.length > 0) dist = Float.parseFloat(args[0]);
            if (args.length > 1) lock = !"0".equals(args[1]);
            if (args.length > 2) cursor = !"0".equals(args[2]);
            if (args.length > 3) pitch = Float.parseFloat(args[3]);
        } catch (Exception ignored) {
        }
        try {
            if (lock) {
                com.digitale.sistema.CamaraCombate.entrar(playerRef, dist, pitch);
            } else {
                com.digitale.sistema.CamaraCombate.salir(playerRef);
                if (cursor) {
                    com.digitale.sistema.CamaraCombate.entrar(playerRef, dist <= 0 ? 9f : dist, pitch);
                }
            }
            ctx.sendMessage(DigiMsg.parse("§eCam dist=" + dist + " lock=" + lock + " pitch=" + pitch));
        } catch (Exception e) {
            ctx.sendMessage(DigiMsg.parse("§cFallo camara: " + e.getMessage()));
        }
    }
}
