package com.digitale.comandos;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
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

import java.util.UUID;

/**
 * /digi_start <especie> [nombre]
 *
 * Otorga el PRIMER Digimon (slot 0). El resto del equipo (hasta 6)
 * se consigue incubando DigiEggs/Digitama en las mesas de incubación.
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
            ctx.sendMessage(DigiMsg.parse("§cYa tienes companeros. Usa §e/digi_status §cpara verlos."));
            return;
        }

        String[] args = ctx.getInputString().trim().isEmpty()
            ? new String[0]
            : ctx.getInputString().trim().split("\\s+");

        if (args.length < 1) {
            ctx.sendMessage(DigiMsg.parse("§b====== DigiTale - Elige tu primer companero ======"));
            ctx.sendMessage(DigiMsg.parse("§eUso: §f/digi_start <especie> [nombre]"));
            ctx.sendMessage(DigiMsg.parse(""));
            ctx.sendMessage(DigiMsg.parse("§7Lineas de evolucion disponibles:"));
            ctx.sendMessage(DigiMsg.parse("  §c Botamon  §8> Koromon  > Agumon  > Greymon > MetalGreymon > WarGreymon"));
            ctx.sendMessage(DigiMsg.parse("  §b Punimon  §8> Tsunomon > Gabumon > GarurumonA > WereGarurumon > MetalGarurumon"));
            ctx.sendMessage(DigiMsg.parse("  §e Poyomon  §8> Tokomon  > Patamon > Angemon  > MagnaAngemon > Seraphimon"));
            ctx.sendMessage(DigiMsg.parse("  §a Yuramon  §8> Tanemon  > Palmon  > Togemon  > Lillymon > Rosemon"));
            ctx.sendMessage(DigiMsg.parse("  §9 Pichimon §8> Bukamon  > Gomamon > Ikkakumon > Zudomon > MarineAngemon"));
            ctx.sendMessage(DigiMsg.parse("  §d Nyokimon §8> Yokomon  > Biyomon > Birdramon > Garudamon > Phoenixmon"));
            ctx.sendMessage(DigiMsg.parse(""));
            ctx.sendMessage(DigiMsg.parse("§7Ejemplo: §f/digi_start Botamon Tai"));
            ctx.sendMessage(DigiMsg.parse("§7El resto del equipo se consigue incubando §eDigiEggs §7en las mesas de incubacion."));
            return;
        }

        String especie1 = capitalizar(args[0]);
        String nombre1  = args.length > 1 ? args[1] : especie1;

        if (!esValida(especie1)) {
            ctx.sendMessage(DigiMsg.parse("§cEspecie invalida. Usa: §fBotamon, Punimon, Poyomon, Yuramon, Pichimon §co §fNyokimon"));
            return;
        }

        DatoDigimon ini1 = DatoDigimon.crearInicial(especie1);
        ini1.nombre = nombre1;
        datos.setSlot(0, ini1);
        datos.activoA = 0;
        datos.activoB = 1;
        datos.tieneEquipo = true;

        // Persistir de inmediato para que no se pierda al salir.
        try {
            com.digitale.componentes.PetProgressComponent progress =
                ComponentRegistry.ensureAndGetProgress(store, ref);
            if (progress != null) progress.guardarDesde(datos);
        } catch (Exception e) {
            // No bloquear el comando si el guardado falla.
        }

        ctx.sendMessage(DigiMsg.parse("§6* !Tu aventura DigiTale comienza! *"));
        ctx.sendMessage(DigiMsg.parse("§a[1] §f" + nombre1 + " §8(" + especie1 + " Baby - " + ini1.elemento + ")"));
        ctx.sendMessage(DigiMsg.parse("§7Busca §eDigiEggs §7y usalos en una §emesa de incubacion §7para ampliar tu equipo (6 max)."));
        ctx.sendMessage(DigiMsg.parse("§7"));
        ctx.sendMessage(DigiMsg.parse("§7Cuidalo con §e/digi_tab §7y entrena con §e/digi_entrenar"));
        ctx.sendMessage(DigiMsg.parse("§7Cuando este listo, evolucionara solo o usa §e/digi_evolucionar"));
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
