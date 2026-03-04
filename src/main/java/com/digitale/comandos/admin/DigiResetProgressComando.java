package com.digitale.comandos.admin;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
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
 * /digi_resetprogress [a|b|all] — Comando de administrador para resetear progreso de compañeros.
 * 
 * Uso:
 *   /digi_resetprogress all     - Resetea ambos compañeros (nivel 1, sin XP)
 *   /digi_resetprogress a       - Resetea solo el compañero A
 *   /digi_resetprogress b       - Resetea solo el compañero B
 * 
 * Requiere permisos de administrador.
 */
public class DigiResetProgressComando extends AbstractPlayerCommand {

    public DigiResetProgressComando(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
        // Nota: Para restringir a admins, podrías usar:
        // requirePermission("admin.digi.resetprogress");
    }

    @Override
    protected void execute(@NonNullDecl CommandContext ctx,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {

        // Parsear argumento (a, b, o all)
        String[] args = ctx.getInputString().trim().isEmpty()
            ? new String[]{"all"}
            : ctx.getInputString().trim().split("\\s+");
        
        String target = args.length > 0 ? args[0].toLowerCase() : "all";

        if (!target.matches("^(a|b|all)$")) {
            ctx.sendMessage(Message.raw("§cUso: §f/digi_resetprogress [a|b|all]"));
            ctx.sendMessage(Message.raw("  §fa§8 - Resetea compañero A"));
            ctx.sendMessage(Message.raw("  §fb§8 - Resetea compañero B"));
            ctx.sendMessage(Message.raw("  §fall§8 - Resetea ambos compañeros"));
            return;
        }

        // Ejecutar en el hilo del mundo para modificar componentes de forma segura
        world.execute(() -> {
            // Obtener o crear el componente
            PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);

            // Resetear según el argumento
            if (target.equals("a")) {
                progress.resetA();
                ctx.sendMessage(Message.raw("§a✓ Progreso del compañero A reiniciado."));
            } else if (target.equals("b")) {
                progress.resetB();
                ctx.sendMessage(Message.raw("§a✓ Progreso del compañero B reiniciado."));
            } else {
                progress.resetAll();
                ctx.sendMessage(Message.raw("§a✓ Progreso de ambos compañeros reiniciado."));
            }

            ctx.sendMessage(Message.raw("§7Nuevo estado: " + progress.getProgressString()));
        });
    }
}
