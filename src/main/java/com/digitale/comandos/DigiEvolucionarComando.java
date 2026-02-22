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
 * /digi_evolucionar [a|b] [forma]
 */
public class DigiEvolucionarComando extends AbstractPlayerCommand {

    public DigiEvolucionarComando(@NonNullDecl String name, @NonNullDecl String description) {
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
            ctx.sendMessage(Message.raw("§cNo tienes compañeros. Usa §e/digi_start"));
            return;
        }

        String[] args = ctx.getInputString().trim().isEmpty()
            ? new String[0]
            : ctx.getInputString().trim().split("\\s+");

        String slot = args.length > 0 ? args[0].toLowerCase() : "";
        String forma = args.length > 1 ? args[1] : "";

        if (slot.isEmpty() || (!slot.equals("a") && !slot.equals("b"))) {
            mostrarCondiciones(ctx, datos.companeroA, "a");
            mostrarCondiciones(ctx, datos.companeroB, "b");
            return;
        }

        DatoDigimon d = slot.equals("a") ? datos.companeroA : datos.companeroB;
        if (d == null || !d.vivo) {
            ctx.sendMessage(Message.raw("§cNo hay compañero en ese slot."));
            return;
        }

        if (forma.isEmpty()) {
            mostrarCondiciones(ctx, d, slot);
            return;
        }

        if (d.puedeEvolucionar(forma)) {
            aplicarEvolucion(ctx, d, forma);
        } else {
            ctx.sendMessage(Message.raw("§c" + d.nombre + " no cumple los requisitos para §e" + forma + " §caún."));
            mostrarCondiciones(ctx, d, slot);
        }
    }

    private void mostrarCondiciones(CommandContext ctx, DatoDigimon d, String slot) {
        if (d == null || !d.vivo) return;
        ctx.sendMessage(Message.raw("§b[" + slot.toUpperCase() + "] §f" + d.nombre +
            " §7(" + d.especie + " - " + d.nombreNivel() + " - " + d.elemento + ")"));
        ctx.sendMessage(Message.raw("  §7Stats: ATK:" + d.atk + " DEF:" + d.def +
            " SPD:" + d.spd + " WIS:" + d.wis + " HP:" + d.maxHp +
            " | §dV:" + d.victorias + " L:" + d.lazo + " D:" + d.disciplina + " ABI:" + d.abi));

        String[][] opciones = evolucionesParaEspecie(d.especie);
        if (opciones.length == 0) {
            ctx.sendMessage(Message.raw("  §8No hay más evoluciones registradas para " + d.especie));
            return;
        }
        for (String[] op : opciones) {
            boolean puede = d.puedeEvolucionar(op[0]);
            ctx.sendMessage(Message.raw("  " + (puede ? "§a✔" : "§c✘") + " §e" + op[0] + " §8— " + op[1]));
        }
        ctx.sendMessage(Message.raw("  §7Uso: §f/digi_evolucionar " + slot + " <forma>"));
    }

    private String[][] evolucionesParaEspecie(String especie) {
        return switch (especie) {
            case "Botamon"      -> new String[][]{{"Koromon",      "Lazo≥30 ó 1 victoria"}};
            case "Punimon"      -> new String[][]{{"Tsunomon",     "Lazo≥30 ó 1 victoria"}};
            case "Poyomon"      -> new String[][]{{"Tokomon",      "Lazo≥30 ó 1 victoria"}};
            case "Yuramon"      -> new String[][]{{"Tanemon",      "Lazo≥30 ó 1 victoria"}};
            case "Pichimon"     -> new String[][]{{"Bukamon",      "Lazo≥30 ó 1 victoria"}};
            case "Nyokimon"     -> new String[][]{{"Yokomon",      "Lazo≥30 ó 1 victoria"}};
            case "Koromon"      -> new String[][]{{"Agumon",       "3 victorias, lazo≥40"}};
            case "Tsunomon"     -> new String[][]{{"Gabumon",      "3 victorias, def≥12"}};
            case "Tokomon"      -> new String[][]{{"Patamon",      "3 victorias, wis≥12"}};
            case "Tanemon"      -> new String[][]{{"Palmon",       "3 victorias, wis≥11"}};
            case "Bukamon"      -> new String[][]{{"Gomamon",      "3 victorias, spd≥14"}};
            case "Yokomon"      -> new String[][]{{"Biyomon",      "3 victorias, spd≥15"}};
            case "Agumon"       -> new String[][]{{"Greymon",      "10V, lazo≥50"}};
            case "Gabumon"      -> new String[][]{{"GarurumonA",   "10V, def≥20"}};
            case "Patamon"      -> new String[][]{{"Angemon",      "10V, wis≥20"}};
            case "Palmon"       -> new String[][]{{"Togemon",      "10V, def≥18"}};
            case "Gomamon"      -> new String[][]{{"Ikkakumon",    "10V, maxHp≥160"}};
            case "Biyomon"      -> new String[][]{{"Birdramon",    "10V, spd≥22"}};
            case "Greymon"      -> new String[][]{{"MetalGreymon", "25V, atk≥40, lazo≥70"}};
            case "GarurumonA"   -> new String[][]{{"WereGarurumon","25V, def≥45, disc≥60"}};
            case "Angemon"      -> new String[][]{{"MagnaAngemon", "25V, wis≥45"}};
            case "Togemon"      -> new String[][]{{"Lillymon",     "25V, wis≥40"}};
            case "Ikkakumon"    -> new String[][]{{"Zudomon",      "25V, def≥50"}};
            case "Birdramon"    -> new String[][]{{"Garudamon",    "25V, spd≥50"}};
            case "MetalGreymon" -> new String[][]{{"WarGreymon",     "50V, atk≥80, lazo≥90, abi≥20"}};
            case "WereGarurumon"-> new String[][]{{"MetalGarurumon", "50V, def≥80, abi≥20"}};
            case "MagnaAngemon" -> new String[][]{{"Seraphimon",     "50V, wis≥90, abi≥20"}};
            case "Lillymon"     -> new String[][]{{"Rosemon",        "50V, wis≥85, lazo≥85, abi≥20"}};
            case "Zudomon"      -> new String[][]{{"MarineAngemon",  "50V, maxHp≥400, abi≥20"}};
            case "Garudamon"    -> new String[][]{{"Phoenixmon",     "50V, spd≥90, abi≥20"}};
            default -> new String[0][0];
        };
    }

    private void aplicarEvolucion(CommandContext ctx, DatoDigimon d, String forma) {
        String anterior = d.especie;
        d.especie = forma;
        d.abi += 5;

        switch (forma) {
            // In-Training (nivel 2)
            case "Koromon"  -> { d.nivel=2; d.maxHp+=25; d.atk+=4; d.def+=3; d.spd+=2; d.wis+=2; }
            case "Tsunomon" -> { d.nivel=2; d.maxHp+=22; d.atk+=3; d.def+=4; d.spd+=2; d.wis+=3; }
            case "Tokomon"  -> { d.nivel=2; d.maxHp+=20; d.atk+=3; d.def+=2; d.spd+=3; d.wis+=5; }
            case "Tanemon"  -> { d.nivel=2; d.maxHp+=24; d.atk+=3; d.def+=3; d.spd+=2; d.wis+=4; }
            case "Bukamon"  -> { d.nivel=2; d.maxHp+=24; d.atk+=3; d.def+=3; d.spd+=4; d.wis+=2; }
            case "Yokomon"  -> { d.nivel=2; d.maxHp+=22; d.atk+=3; d.def+=2; d.spd+=5; d.wis+=3; }
            // Rookie (nivel 3)
            case "Agumon"    -> { d.nivel=3; d.maxHp+=55; d.atk+=7; d.def+=4; d.spd+=4; d.wis+=3; d.elemento="FUEGO"; }
            case "Gabumon"   -> { d.nivel=3; d.maxHp+=50; d.atk+=6; d.def+=9; d.spd+=5; d.wis+=4; d.elemento="HIELO"; }
            case "Patamon"   -> { d.nivel=3; d.maxHp+=45; d.atk+=6; d.def+=4; d.spd+=6; d.wis+=7; d.elemento="LUZ"; }
            case "Palmon"    -> { d.nivel=3; d.maxHp+=50; d.atk+=5; d.def+=6; d.spd+=4; d.wis+=6; d.elemento="NATURALEZA"; }
            case "Gomamon"   -> { d.nivel=3; d.maxHp+=53; d.atk+=5; d.def+=4; d.spd+=7; d.wis+=4; d.elemento="AGUA"; }
            case "Biyomon"   -> { d.nivel=3; d.maxHp+=48; d.atk+=5; d.def+=3; d.spd+=9; d.wis+=5; d.elemento="VIENTO"; }
            // Champion (nivel 4)
            case "Greymon"       -> { d.nivel=4; d.maxHp+=80;  d.atk+=15; d.def+=12; d.spd+=5;  d.wis+=5;  }
            case "GarurumonA"    -> { d.nivel=4; d.maxHp+=70;  d.atk+=12; d.def+=18; d.spd+=8;  d.wis+=7;  }
            case "Angemon"       -> { d.nivel=4; d.maxHp+=60;  d.atk+=13; d.def+=10; d.spd+=10; d.wis+=15; }
            case "Togemon"       -> { d.nivel=4; d.maxHp+=75;  d.atk+=10; d.def+=16; d.spd+=5;  d.wis+=10; }
            case "Ikkakumon"     -> { d.nivel=4; d.maxHp+=90;  d.atk+=12; d.def+=14; d.spd+=4;  d.wis+=6;  }
            case "Birdramon"     -> { d.nivel=4; d.maxHp+=65;  d.atk+=13; d.def+=9;  d.spd+=15; d.wis+=8;  }
            // Ultimate (nivel 5)
            case "MetalGreymon"  -> { d.nivel=5; d.maxHp+=120; d.atk+=25; d.def+=20; d.spd+=8;  d.wis+=10; }
            case "WereGarurumon" -> { d.nivel=5; d.maxHp+=100; d.atk+=20; d.def+=28; d.spd+=15; d.wis+=12; }
            case "MagnaAngemon"  -> { d.nivel=5; d.maxHp+=90;  d.atk+=20; d.def+=18; d.spd+=18; d.wis+=30; }
            case "Lillymon"      -> { d.nivel=5; d.maxHp+=85;  d.atk+=18; d.def+=16; d.spd+=20; d.wis+=28; }
            case "Zudomon"       -> { d.nivel=5; d.maxHp+=140; d.atk+=18; d.def+=32; d.spd+=6;  d.wis+=12; }
            case "Garudamon"     -> { d.nivel=5; d.maxHp+=95;  d.atk+=20; d.def+=15; d.spd+=28; d.wis+=14; }
            // Mega (nivel 6)
            case "WarGreymon"    -> { d.nivel=6; d.maxHp+=200; d.atk+=40; d.def+=35; d.spd+=20; d.wis+=20; }
            case "MetalGarurumon"-> { d.nivel=6; d.maxHp+=180; d.atk+=35; d.def+=42; d.spd+=25; d.wis+=22; }
            case "Seraphimon"    -> { d.nivel=6; d.maxHp+=160; d.atk+=35; d.def+=30; d.spd+=30; d.wis+=50; }
            case "Rosemon"       -> { d.nivel=6; d.maxHp+=150; d.atk+=30; d.def+=28; d.spd+=35; d.wis+=45; }
            case "MarineAngemon" -> { d.nivel=6; d.maxHp+=220; d.atk+=28; d.def+=40; d.spd+=28; d.wis+=30; }
            case "Phoenixmon"    -> { d.nivel=6; d.maxHp+=170; d.atk+=38; d.def+=30; d.spd+=40; d.wis+=30; }
        }

        d.hp = d.maxHp;
        ctx.sendMessage(Message.raw("§6✨ §f" + d.nombre + "§6 evolucionó de §e" + anterior + " §6→ §e" + forma + " §6✨"));
        ctx.sendMessage(Message.raw("  §7" + d.nombreNivel() + " | " + d.elemento +
            " | HP:" + d.maxHp + " ATK:" + d.atk + " DEF:" + d.def +
            " SPD:" + d.spd + " WIS:" + d.wis + " ABI:" + d.abi));
    }
}
