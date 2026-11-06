package com.digitale.comandos;

import com.digitale.util.DigiMsg;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * /digi_cam [dist] [lock] [cursor] [altura] — prueba la cámara de combate.
 * Ej:
 * <ul>
 *   <li>{@code /digi_cam} → perfil por defecto (6.6 atrás, 3.1 de alto).</li>
 *   <li>{@code /digi_cam 8 1 1 3.5} → más lejos y más alta.</li>
 *   <li>{@code /digi_cam 0 0 0} → libera la cámara.</li>
 *   <li>{@code /digi_cam estado} → imprime la pose calculada en el log.</li>
 * </ul>
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
        String entrada = ctx.getInputString() == null ? "" : ctx.getInputString().trim();
        String[] args = entrada.isEmpty() ? new String[0] : entrada.split("\\s+");

        if (args.length > 0 && ("estado".equalsIgnoreCase(args[0]) || "debug".equalsIgnoreCase(args[0]))) {
            ctx.sendMessage(DigiMsg.parse("§eCamara: " + com.digitale.sistema.CamaraCombate.describir(playerRef)));
            return;
        }

        float dist = com.digitale.sistema.CamaraCombate.DISTANCIA;
        float altura = com.digitale.sistema.CamaraCombate.ALTURA;
        boolean lock = true;
        try {
            if (args.length > 0) dist = Float.parseFloat(args[0]);
            if (args.length > 1) lock = !"0".equals(args[1]);
            if (args.length > 3) altura = Float.parseFloat(args[3]);
        } catch (Exception ignored) {
        }

        try {
            if (lock) {
                com.digitale.sistema.CamaraCombate.entrar(playerRef,
                    dist <= 0 ? com.digitale.sistema.CamaraCombate.DISTANCIA : dist,
                    altura <= 0 ? com.digitale.sistema.CamaraCombate.ALTURA : altura);
                ctx.sendMessage(DigiMsg.parse("§eCam dist=" + dist + " altura=" + altura
                    + " | " + com.digitale.sistema.CamaraCombate.describir(playerRef)));
            } else {
                com.digitale.sistema.CamaraCombate.salir(playerRef);
                ctx.sendMessage(DigiMsg.parse("§eCamara liberada."));
            }
        } catch (Exception e) {
            ctx.sendMessage(DigiMsg.parse("§cFallo camara: " + e.getMessage()));
        }
    }
}
