package com.digitale.comandos;

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
 * /digi_progress — muestra el progreso (nivel y experiencia) de los compañeros del jugador.
 * 
 * Usa el componente PetProgressComponent para acceder a datos persistentes.
 */
public class DigiProgressComando extends AbstractPlayerCommand {

    public DigiProgressComando(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext ctx,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {

        // Ejecutamos en el hilo del mundo para acceder al componente de forma segura
        world.execute(() -> {
            // Obtener el componente persistente
            PetProgressComponent progress = ComponentRegistry.getProgress(store, ref);

            if (progress == null) {
                ctx.sendMessage(Message.raw("§cNo hay datos de progreso. Inicia un equipo con §e/digi_start"));
                return;
            }

            // Mostrar progreso
            ctx.sendMessage(Message.raw("§b═══════ Progreso de tus Compañeros ═══════"));
            ctx.sendMessage(Message.raw(""));

            // Compañero A
            ctx.sendMessage(Message.raw("§e[A] Compañero A"));
            ctx.sendMessage(Message.raw(
                "  §fNivel: §b" + progress.getLevelA() +
                " §8| Experiencia: §b" + progress.getExperienceA() + "§8/100"
            ));
            mostrarBarraExp(ctx, progress.getExperienceA());

            ctx.sendMessage(Message.raw(""));

            // Compañero B
            ctx.sendMessage(Message.raw("§e[B] Compañero B"));
            ctx.sendMessage(Message.raw(
                "  §fNivel: §b" + progress.getLevelB() +
                " §8| Experiencia: §b" + progress.getExperienceB() + "§8/100"
            ));
            mostrarBarraExp(ctx, progress.getExperienceB());

            ctx.sendMessage(Message.raw(""));
            ctx.sendMessage(Message.raw("§7Usa §e/digi_entrenar §7para ganar experiencia."));
            ctx.sendMessage(Message.raw("§b═════════════════════════════════════════"));
        });
    }

    /**
     * Muestra una barra visual de experiencia.
     */
    private void mostrarBarraExp(CommandContext ctx, int exp) {
        int barLength = 20;
        int filled = (exp * barLength) / 100;
        String filled_str = "§a█".repeat(filled);
        String empty_str = "§8░".repeat(barLength - filled);
        ctx.sendMessage(Message.raw("  " + filled_str + empty_str));
    }
}
