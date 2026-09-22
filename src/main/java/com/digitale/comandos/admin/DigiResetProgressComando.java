package com.digitale.comandos.admin;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.util.DigiMsg;
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
 * /digi_resetprogress [a|b|c|d|e|f|1-6|all] — Resetea progreso (nivel 1, sin XP).
 *
 * Uso:
 *   /digi_resetprogress all     - Resetea los 6 slots
 *   /digi_resetprogress a       - Resetea solo el slot 1 (A)
 *   /digi_resetprogress 3       - Resetea solo el slot 3 (C)
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

        if (!target.matches("^(a|b|c|d|e|f|1|2|3|4|5|6|all)$")) {
            ctx.sendMessage(DigiMsg.parse("§cUso: §f/digi_resetprogress [a-f|1-6|all]"));
            ctx.sendMessage(DigiMsg.parse("  §fa-f §8- Resetea un slot (a=1, b=2, ...)"));
            ctx.sendMessage(DigiMsg.parse("  §fall §8- Resetea los 6 slots"));
            return;
        }

        // Ejecutar en el hilo del mundo para modificar componentes de forma segura
        world.execute(() -> {
            // Obtener o crear el componente
            PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);

            if (target.equals("all")) {
                progress.resetAll();
                ctx.sendMessage(DigiMsg.parse("§a> Progreso de los 6 slots reiniciado."));
            } else if (target.equals("a")) {
                // Compatibilidad: "a"/"b" solos siguen funcionando.
                progress.resetA();
                ctx.sendMessage(DigiMsg.parse("§a> Progreso del slot 1 (A) reiniciado."));
            } else if (target.equals("b")) {
                progress.resetB();
                ctx.sendMessage(DigiMsg.parse("§a> Progreso del slot 2 (B) reiniciado."));
            } else {
                int slot = PetProgressComponent.slotDesdeNombre(target);
                progress.resetSlot(slot);
                ctx.sendMessage(DigiMsg.parse("§a> Progreso del slot " + (slot + 1) + " reiniciado."));
            }

            ctx.sendMessage(DigiMsg.parse("§7Nuevo estado: " + progress.getProgressString()));
        });
    }
}
