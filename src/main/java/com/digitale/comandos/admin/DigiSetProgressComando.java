package com.digitale.comandos.admin;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
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
 * /digi_setprogress <a-f|1-6|both|all> <level> [exp] — Setea progreso específico.
 *
 * Uso:
 *   /digi_setprogress a 10           - Setea slot 1 a nivel 10, 0 XP
 *   /digi_setprogress c 5 50         - Setea slot 3 a nivel 5, 50 XP
 *   /digi_setprogress both 20 75     - Setea A+B a nivel 20, 75 XP
 *   /digi_setprogress all 20         - Setea los 6 slots a nivel 20
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
            ctx.sendMessage(DigiMsg.parse("§cUso: §f/digi_setprogress <a-f|1-6|both|all> <level> [exp]"));
            ctx.sendMessage(DigiMsg.parse("  §fEjemplo: §e/digi_setprogress a 10 50"));
            ctx.sendMessage(DigiMsg.parse("  §fEjemplo: §e/digi_setprogress all 5"));
            return;
        }

        String target = args[0].toLowerCase();
        if (!target.matches("^(a|b|c|d|e|f|1|2|3|4|5|6|both|all)$")) {
            ctx.sendMessage(DigiMsg.parse("§cObjetivo invalido. Usa: §fa-f, 1-6, both o all"));
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
            ctx.sendMessage(DigiMsg.parse("§cNivel y XP deben ser numeros."));
            return;
        }

        if (level < 1) {
            ctx.sendMessage(DigiMsg.parse("§cEl nivel debe ser mayor a 0."));
            return;
        }

        if (exp < 0 || exp > 100) {
            ctx.sendMessage(DigiMsg.parse("§cLa experiencia debe estar entre 0 y 100."));
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
                ctx.sendMessage(DigiMsg.parse("§a> Slot 1 (A): Nivel §b" + finalLevel + " §a+ §b" + finalExp + " XP"));
            } else if (target.equals("b")) {
                progress.setLevelB(finalLevel);
                progress.setExperienceB(finalExp);
                ctx.sendMessage(DigiMsg.parse("§a> Slot 2 (B): Nivel §b" + finalLevel + " §a+ §b" + finalExp + " XP"));
            } else if (target.equals("both")) {
                progress.setLevelA(finalLevel);
                progress.setExperienceA(finalExp);
                progress.setLevelB(finalLevel);
                progress.setExperienceB(finalExp);
                ctx.sendMessage(DigiMsg.parse("§a> Slots 1-2 (A+B): Nivel §b" + finalLevel + " §a+ §b" + finalExp + " XP"));
            } else if (target.equals("all")) {
                for (int i = 0; i < 6; i++) progress.setSlotProgress(i, finalLevel, finalExp);
                ctx.sendMessage(DigiMsg.parse("§a> Los 6 slots: Nivel §b" + finalLevel + " §a+ §b" + finalExp + " XP"));
            } else {
                int slot = PetProgressComponent.slotDesdeNombre(target);
                progress.setSlotProgress(slot, finalLevel, finalExp);
                ctx.sendMessage(DigiMsg.parse("§a> Slot " + (slot + 1) + ": Nivel §b" + finalLevel + " §a+ §b" + finalExp + " XP"));
            }

            // Sincronizar nivel de aventura en RAM (desbloquea movimientos sin relog).
            AlmacenJugadores.DatosJugador dd = AlmacenJugadores.getDatos(playerRef.getUuid());
            if (dd != null) {
                int[] afectados;
                if (target.equals("all")) afectados = new int[]{0, 1, 2, 3, 4, 5};
                else if (target.equals("both")) afectados = new int[]{0, 1};
                else {
                    int s = PetProgressComponent.slotDesdeNombre(target);
                    afectados = s >= 0 ? new int[]{s} : new int[0];
                }
                for (int s : afectados) {
                    DatoDigimon digi = dd.getSlot(s);
                    if (digi != null) digi.nivelAv = Math.max(1, finalLevel);
                }
            }

            ctx.sendMessage(DigiMsg.parse("§7Nuevo estado: " + progress.getProgressString()));
        });
    }
}
