package com.digitale.comandos.admin;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.crianza.DigitamaDef;
import com.digitale.crianza.SistemaIncubacion;
import com.digitale.crianza.SistemaIncubacion.FuelDesconocido;
import com.digitale.crianza.SistemaIncubacion.EntornoDesconocido;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
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
 * /digi_incubar — eclosión de prueba (la Mesa de Crianza lo hará con recetas).
 *
 * Uso:
 *   /digi_incubar Digitama_Verde
 *   /digi_incubar desconocido <basura|puro|completo> <energia 1-10> <ciclos> [noche] [ybaja]
 *     ej: /digi_incubar desconocido puro 5 3
 */
public class DigiIncubarComando extends AbstractPlayerCommand {

    public DigiIncubarComando(@NonNullDecl String name, @NonNullDecl String description) {
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
        if (args.length < 1) {
            ctx.sendMessage(DigiMsg.parse("§cUso: §f/digi_incubar <idHuevo|desconocido> [...]"));
            ctx.sendMessage(DigiMsg.parse("  §fEjemplo: §e/digi_incubar Digitama_Verde"));
            ctx.sendMessage(DigiMsg.parse("  §fEjemplo: §e/digi_incubar desconocido puro 5 3"));
            return;
        }

        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        if (!datos.tieneEspacio()) {
            ctx.sendMessage(DigiMsg.parse("§cEquipo lleno (6/6)."));
            return;
        }

        if ("desconocido".equalsIgnoreCase(args[0])) {
            FuelDesconocido fuel = FuelDesconocido.NADA;
            int energia = 5;
            int ciclos = 2;
            boolean noche = false;
            boolean yBaja = false;
            try {
                if (args.length > 1) fuel = FuelDesconocido.valueOf(args[1].toUpperCase());
                if (args.length > 2) energia = Math.max(1, Math.min(10, Integer.parseInt(args[2])));
                if (args.length > 3) ciclos = Math.max(1, Integer.parseInt(args[3]));
                if (args.length > 4) noche = args[4].equalsIgnoreCase("si");
                if (args.length > 5) yBaja = args[5].equalsIgnoreCase("si");
            } catch (Exception e) {
                ctx.sendMessage(DigiMsg.parse("§cArgs: desconocido <basura|puro|completo|nada> <energia> <ciclos> [noche] [ybaja]"));
                return;
            }
            SistemaIncubacion.ResultadoDesconocido res =
                SistemaIncubacion.resolverDesconocido(fuel, EntornoDesconocido.NORMAL, energia, ciclos, noche, yBaja);
            for (String linea : res.log) {
                ctx.sendMessage(DigiMsg.parse("§7" + linea));
            }
            nacer(ctx, store, ref, datos, res.especie);
            return;
        }

        DigitamaDef def = DigitamaDef.porItem(args[0]);
        if (def == null) {
            ctx.sendMessage(DigiMsg.parse("§cHuevo desconocido. Ids: Digitama_Verde, Digitama_Naranja, ... o 'desconocido'."));
            return;
        }
        int slot = SistemaIncubacion.eclosionar(datos, def);
        if (slot < 0) {
            ctx.sendMessage(DigiMsg.parse("§cEquipo lleno (6/6)."));
            return;
        }
        guardar(store, ref, datos);
        ctx.sendMessage(DigiMsg.parse("§a> Eclosiono " + def.nombre + ": §f" + datos.getSlot(slot).nombre
            + " §8(" + def.especie + " Nv" + def.nivelRes + ") §aen slot " + (slot + 1) + "."));
    }

    private void nacer(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref,
                       DatosJugador datos, String especie) {
        DatoDigimon bebe = DatoDigimon.crearInicial(especie);
        int slot = datos.agregarAlEquipo(bebe);
        if (slot < 0) {
            ctx.sendMessage(DigiMsg.parse("§cEquipo lleno (6/6)."));
            return;
        }
        guardar(store, ref, datos);
        ctx.sendMessage(DigiMsg.parse("§d> Nacio " + especie + " en slot " + (slot + 1) + "."));
    }

    private void guardar(Store<EntityStore> store, Ref<EntityStore> ref, DatosJugador datos) {
        try {
            PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
            if (progress != null) progress.guardarDesde(datos);
        } catch (Exception ignored) {
        }
    }
}
