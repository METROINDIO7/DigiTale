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
 * /digi entrenar <a|b|ambos> <atk|def|spd|wis|hp> [sesiones]
 */
public class DigiEntrenarComando extends AbstractPlayerCommand {

    public DigiEntrenarComando(@NonNullDecl String name, @NonNullDecl String description) {
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

        if (datos.enCombate) {
            ctx.sendMessage(Message.raw("§c¡Estás en combate! Termínalo primero."));
            return;
        }

        String[] args = ctx.getInputString().trim().isEmpty() ? new String[0] : ctx.getInputString().trim().split("\\s+");
        if (args.length < 2) {
            ctx.sendMessage(Message.raw("§eUso: §f/digi entrenar <a|b|ambos> <atk|def|spd|wis|hp> [1-5 sesiones]"));
            ctx.sendMessage(Message.raw("§7Cada sesión cuesta §e15 de energía §7y mejora el stat elegido."));
            return;
        }

        String quien   = args[0].toLowerCase();
        String stat    = args[1].toLowerCase();
        int    sesiones = args.length > 2 ? parseSafe(args[2], 1) : 1;
        sesiones = Math.max(1, Math.min(5, sesiones));

        if (!stat.matches("atk|def|spd|wis|hp")) {
            ctx.sendMessage(Message.raw("§cStat inválido. Usa: atk, def, spd, wis, hp"));
            return;
        }

        boolean entrenadoA = false, entrenadoB = false;

        if (quien.equals("a") || quien.equals("ambos")) {
            DatoDigimon d = datos.companeroA;
            if (d != null && d.vivo) {
                if (d.entrenar(stat, sesiones)) {
                    ctx.sendMessage(Message.raw("§a[A] " + d.nombre + " entrenó §e" + stat.toUpperCase() +
                        " §7(+" + sesiones + ") — Energía restante: §f" + d.energia));
                    entrenadoA = true;
                } else {
                    ctx.sendMessage(Message.raw("§c[A] " + d.nombre + " no tiene suficiente energía (necesita " + (sesiones*15) + ")."));
                }
            }
        }

        if (quien.equals("b") || quien.equals("ambos")) {
            DatoDigimon d = datos.companeroB;
            if (d != null && d.vivo) {
                if (d.entrenar(stat, sesiones)) {
                    ctx.sendMessage(Message.raw("§a[B] " + d.nombre + " entrenó §e" + stat.toUpperCase() +
                        " §7(+" + sesiones + ") — Energía restante: §f" + d.energia));
                    entrenadoB = true;
                } else {
                    ctx.sendMessage(Message.raw("§c[B] " + d.nombre + " no tiene suficiente energía (necesita " + (sesiones*15) + ")."));
                }
            }
        }

        if (!entrenadoA && !entrenadoB && !quien.matches("a|b|ambos")) {
            ctx.sendMessage(Message.raw("§cElige §ea§c, §eb§c o §eambos"));
        }

        if (entrenadoA || entrenadoB) {
            ctx.sendMessage(Message.raw("§7Tip: Usa §e/digi descansar §7para recuperar energía."));
        }
    }

    private int parseSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
}
