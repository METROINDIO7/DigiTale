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

import java.util.UUID;

/**
 * /digi_start <especie1> <especie2> <nombre1> [nombre2]
 *
 * Starters Baby disponibles:
 *   Botamon  (Fuego)   → Koromon  → Agumon  → Greymon  → ...
 *   Punimon  (Hielo)   → Tsunomon → Gabumon → GarurumonA → ...
 *   Poyomon  (Luz)     → Tokomon  → Patamon → Angemon  → ...
 *   Yuramon  (Natural) → Tanemon  → Palmon  → Togemon  → ...
 *   Pichimon (Agua)    → Bukamon  → Gomamon → Ikkakumon → ...
 *   Nyokimon (Viento)  → Yokomon  → Biyomon → Birdramon → ...
 */
public class DigiStartComando extends AbstractPlayerCommand {

    private static final String[] VALIDAS = {
        "Botamon","Punimon","Poyomon","Yuramon","Pichimon","Nyokimon"
    };

    public DigiStartComando(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext ctx,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {

        UUID uuid = playerRef.getUuid();
        DatosJugador datos = AlmacenJugadores.obtener(uuid);

        if (datos.tieneEquipo) {
            ctx.sendMessage(Message.raw("§cYa tienes compañeros. Usa §e/digi_status §cpara verlos."));
            return;
        }

        String[] args = ctx.getInputString().trim().isEmpty()
            ? new String[0]
            : ctx.getInputString().trim().split("\\s+");

        if (args.length < 2) {
            ctx.sendMessage(Message.raw("§b══════ DigiTale - Elige tus compañeros ══════"));
            ctx.sendMessage(Message.raw("§eUso: §f/digi_start <especie1> <especie2> <nombre1> [nombre2]"));
            ctx.sendMessage(Message.raw(""));
            ctx.sendMessage(Message.raw("§7Líneas de evolución disponibles:"));
            ctx.sendMessage(Message.raw("  §c🔥 Botamon  §8→ Koromon  → Agumon  → Greymon → MetalGreymon → WarGreymon"));
            ctx.sendMessage(Message.raw("  §b❄ Punimon  §8→ Tsunomon → Gabumon → GarurumonA → WereGarurumon → MetalGarurumon"));
            ctx.sendMessage(Message.raw("  §e✨ Poyomon  §8→ Tokomon  → Patamon → Angemon  → MagnaAngemon → Seraphimon"));
            ctx.sendMessage(Message.raw("  §a🌿 Yuramon  §8→ Tanemon  → Palmon  → Togemon  → Lillymon → Rosemon"));
            ctx.sendMessage(Message.raw("  §9💧 Pichimon §8→ Bukamon  → Gomamon → Ikkakumon → Zudomon → MarineAngemon"));
            ctx.sendMessage(Message.raw("  §d🌪 Nyokimon §8→ Yokomon  → Biyomon → Birdramon → Garudamon → Phoenixmon"));
            ctx.sendMessage(Message.raw(""));
            ctx.sendMessage(Message.raw("§7Ejemplo: §f/digi_start Botamon Punimon Tai Yamato"));
            return;
        }

        String especie1 = capitalizar(args[0]);
        String especie2 = args.length > 1 ? capitalizar(args[1]) : "Punimon";
        String nombre1  = args.length > 2 ? args[2] : especie1;
        String nombre2  = args.length > 3 ? args[3] : especie2;

        if (!esValida(especie1) || !esValida(especie2)) {
            ctx.sendMessage(Message.raw("§cEspecie inválida. Usa: §fBotamon, Punimon, Poyomon, Yuramon, Pichimon §co §fNyokimon"));
            return;
        }

        if (especie1.equals(especie2)) {
            ctx.sendMessage(Message.raw("§cElige dos especies §ediferentes §cpara tu equipo."));
            return;
        }

        datos.companeroA = DatoDigimon.crearInicial(especie1);
        datos.companeroA.nombre = nombre1;
        datos.companeroB = DatoDigimon.crearInicial(especie2);
        datos.companeroB.nombre = nombre2;
        datos.tieneEquipo = true;

        ctx.sendMessage(Message.raw("§6✦ ¡Tu aventura DigiTale comienza! ✦"));
        ctx.sendMessage(Message.raw("§a[A] §f" + nombre1 + " §8(" + especie1 + " Baby - " + datos.companeroA.elemento + ")"));
        ctx.sendMessage(Message.raw("§a[B] §f" + nombre2 + " §8(" + especie2 + " Baby - " + datos.companeroB.elemento + ")"));
        ctx.sendMessage(Message.raw("§7"));
        ctx.sendMessage(Message.raw("§7Cuídalos con §e/digi_cuidar §7y entrena con §e/digi_entrenar"));
        ctx.sendMessage(Message.raw("§7Cuando estén listos, evolucionarán solos o usa §e/digi_evolucionar"));
    }

    private boolean esValida(String e) {
        for (String v : VALIDAS) if (v.equals(e)) return true;
        return false;
    }

    private String capitalizar(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
