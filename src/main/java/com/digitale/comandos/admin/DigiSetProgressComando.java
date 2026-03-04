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
 * /digi_setprogress <a|b|both> <level> [exp] — Comando de administrador para setear progreso específico.
 * 
 * Uso:
 *   /digi_setprogress a 10           - Setea compañero A a nivel 10, 0 XP
 *   /digi_setprogress b 5 50         - Setea compañero B a nivel 5, 50 XP
 *   /digi_setprogress both 20 75     - Setea ambos a nivel 20, 75 XP
 */
public class DigiSetProgressComando extends AbstractPlayerCommand {

    public DigiSetProgressComando(@NonNullDecl String name, @NonNullDecl String description) {
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

        if (args.length < 2) {
            ctx.sendMessage(Message.raw("§cUso: §f/digi_setprogress <a|b|both> <level> [exp]"));
            ctx.sendMessage(Message.raw("  §fEjemplo: §e/digi_setprogress a 10 50"));
            ctx.sendMessage(Message.raw("  §fEjemplo: §e/digi_setprogress both 5"));
            return;
        }

        String target = args[0].toLowerCase();
        if (!target.matches("^(a|b|both)$")) {
            ctx.sendMessage(Message.raw("§cArgumento de objetivo inválido. Usa: §fa, b §co §fboth"));
            return;
        }

        int level;
        int exp = 0;

        try {
            level = Integer.parseInt(args[1]);
            if (args.length > 2) {
                exp = Integer.parseInt(args[2]);
            }
        } catch (NumberFormatException e) {
            ctx.sendMessage(Message.raw("§cNivel y XP deben ser números."));
            return;
        }

        if (level < 1) {
            ctx.sendMessage(Message.raw("§cEl nivel debe ser mayor a 0."));
            return;
        }

        if (exp < 0 || exp > 100) {
            ctx.sendMessage(Message.raw("§cLa experiencia debe estar entre 0 y 100."));
            return;
        }

        // Hacer copias finales de las variables para usarlas en lambda
        final int finalLevel = level;
        final int finalExp = exp;

        // Ejecutar en el hilo del mundo
        world.execute(() -> {
            PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);

            if (target.equals("a")) {
                progress.setLevelA(finalLevel);
                progress.setExperienceA(finalExp);
                ctx.sendMessage(Message.raw("§a✓ Compañero A: Nivel §b" + finalLevel + " §a+ §b" + finalExp + " XP"));
            } else if (target.equals("b")) {
                progress.setLevelB(finalLevel);
                progress.setExperienceB(finalExp);
                ctx.sendMessage(Message.raw("§a✓ Compañero B: Nivel §b" + finalLevel + " §a+ §b" + finalExp + " XP"));
            } else {
                progress.setLevelA(finalLevel);
                progress.setExperienceA(finalExp);
                progress.setLevelB(finalLevel);
                progress.setExperienceB(finalExp);
                ctx.sendMessage(Message.raw("§a✓ Ambos compañeros: Nivel §b" + finalLevel + " §a+ §b" + finalExp + " XP"));
            }

            ctx.sendMessage(Message.raw("§7Nuevo estado: " + progress.getProgressString()));
        });
    }
}
