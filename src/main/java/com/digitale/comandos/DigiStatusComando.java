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
 * /digi status — muestra el estado de los dos compañeros
 */
public class DigiStatusComando extends AbstractPlayerCommand {

    public DigiStatusComando(@NonNullDecl String name, @NonNullDecl String description) {
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
            ctx.sendMessage(Message.raw("§cNo tienes compañeros. Usa §e/digi start§c para comenzar."));
            return;
        }

        ctx.sendMessage(Message.raw("§b══════ Estado de tu equipo ══════"));
        mostrarDigimon(ctx, datos.companeroA, "A", datos.tacticaA);
        ctx.sendMessage(Message.raw("§8───────────────────────────"));
        mostrarDigimon(ctx, datos.companeroB, "B", datos.tacticaB);
        ctx.sendMessage(Message.raw("§b═════════════════════════════"));

        if (datos.enCombate) {
            ctx.sendMessage(Message.raw("§c⚔ EN COMBATE vs §e" + datos.enemigoNombre +
                " §7[HP: " + datos.enemigoHp + "/" + datos.enemigoHpMax + "]"));
            ctx.sendMessage(Message.raw("§7Turno §f" + datos.turno + "§7 | Último: §f" + datos.logUltimo));
        }
    }

    private void mostrarDigimon(CommandContext ctx, DatoDigimon d, String slot, String tactica) {
        if (d == null || !d.vivo) {
            ctx.sendMessage(Message.raw("§7[" + slot + "] Sin compañero"));
            return;
        }

        String barraHp = barra(d.hp, d.maxHp, 10, "§a█", "§8░");
        String barraEn = barra(d.energia, 100, 10, "§e█", "§8░");
        String barraLz = barra(d.lazo, 100, 8, "§d█", "§8░");

        ctx.sendMessage(Message.raw(
            "§e[" + slot + "] §f" + d.nombre + " §7(" + d.especie + " " + d.nombreNivel() + ") §9" + d.elemento));
        ctx.sendMessage(Message.raw(
            "  §cHP  " + barraHp + " §f" + d.hp + "/" + d.maxHp));
        ctx.sendMessage(Message.raw(
            "  §eEN  " + barraEn + " §f" + d.energia + "/100"));
        ctx.sendMessage(Message.raw(
            "  §dLAZO" + barraLz + " §f" + d.lazo + "/100  §8| Disc: " + d.disciplina));
        ctx.sendMessage(Message.raw(
            "  §7ATK:" + d.atk + " DEF:" + d.def + " SPD:" + d.spd +
            " WIS:" + d.wis + " ABI:" + d.abi +
            " §8| V:" + d.victorias + " D:" + d.derrotas));
        ctx.sendMessage(Message.raw(
            "  §7Táctica: §f" + tactica + "  §7Hambre: §f" + d.hambre + "/100"));
    }

    private String barra(int val, int max, int tam, String lleno, String vacio) {
        if (max <= 0) return vacio.repeat(tam);
        int f = (int)((double) val / max * tam);
        return lleno.repeat(Math.max(0, Math.min(tam, f))) +
               vacio.repeat(Math.max(0, tam - Math.min(tam, f)));
    }
}
