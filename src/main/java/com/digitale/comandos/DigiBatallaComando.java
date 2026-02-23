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

import java.util.Random;

/**
 * /digi batalla [atacar|defender|huir|tactica <a|b> <agresivo|balanceado|defensivo>]
 *
 * Combate estilo Next Order: los dos compañeros actúan según su táctica.
 * Los turnos se resuelven con /digi batalla atacar/defender/huir.
 */
public class DigiBatallaComando extends AbstractPlayerCommand {

    private static final Random RNG = new Random();

    record PlantillaEnemigo(String nombre, String elem, int hp, int atk, int def, int exp) {}

    private static final PlantillaEnemigo[] ENEMIGOS = {
        new PlantillaEnemigo("Numemon",   "NEUTRO",  80,  8,  5, 30),
        new PlantillaEnemigo("Agumon",    "FUEGO",  100, 12,  8, 45),
        new PlantillaEnemigo("Gabumon",   "HIELO",   90, 10, 11, 40),
        new PlantillaEnemigo("Patamon",   "VIENTO",  85, 10,  8, 38),
        new PlantillaEnemigo("Elecmon",   "TRUENO", 110, 14,  9, 55),
        new PlantillaEnemigo("Greymon",   "FUEGO",  220, 25, 18, 120),
        new PlantillaEnemigo("GarurumonA","HIELO",  200, 20, 22, 115)
    };

    public DigiBatallaComando(@NonNullDecl String name, @NonNullDecl String description) {
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
            ctx.sendMessage(Message.raw("§cNecesitas compañeros. Usa §e/digi start"));
            return;
        }

        String[] args = ctx.getInputString().trim().isEmpty() ? new String[0] : ctx.getInputString().trim().split("\\s+");
        String accion = args.length > 0 ? args[0].toLowerCase() : "";

        // ── Cambio de táctica ──────────────────────────────────────
        if (accion.equals("tactica")) {
            cambiarTactica(ctx, datos, args);
            return;
        }

        // ── Iniciar combate ────────────────────────────────────────
        if (!datos.enCombate) {
            iniciarCombate(ctx, datos);
            return;
        }

        // ── Acciones en combate ────────────────────────────────────
        switch (accion) {
            case "atacar", "a" -> resolverTurno(ctx, datos, false);
            case "defender", "d" -> resolverTurnoDefensivo(ctx, datos);
            case "huir", "h"   -> intentarHuida(ctx, datos);
            default -> {
                mostrarEstadoCombate(ctx, datos);
                ctx.sendMessage(Message.raw("§7Acciones: §fataccar §8| §fdefender §8| §fhuir"));
            }
        }
    }

    // ── Iniciar combate aleatorio ──────────────────────────────────

    private void iniciarCombate(CommandContext ctx, DatosJugador datos) {
        if (!datos.alguienVivo()) {
            ctx.sendMessage(Message.raw("§cTus compañeros no están en condiciones de luchar."));
            return;
        }

        // Escalar el enemigo según el nivel del equipo
        int nivelEquipo = promedioNivel(datos);
        PlantillaEnemigo[] pool = nivelEquipo <= 3 ? filtrarPool(4) : ENEMIGOS;
        PlantillaEnemigo tmpl = pool[RNG.nextInt(pool.length)];

        double escala = 1.0 + (nivelEquipo - 3) * 0.2;
        int hp  = (int)(tmpl.hp()  * escala);
        int atk = (int)(tmpl.atk() * escala);
        int def = (int)(tmpl.def() * escala);
        int exp = (int)(tmpl.exp() * escala);

        datos.iniciarCombate(tmpl.nombre(), tmpl.elem(), hp, atk, def, exp);

        ctx.sendMessage(Message.raw("§c⚔ ¡Un " + tmpl.nombre() + " [" + tmpl.elem() + "] apareció! ⚔"));
        ctx.sendMessage(Message.raw("§7HP enemigo: §c" + hp + "  ATK: §c" + atk + "  DEF: §c" + def));
        mostrarHpEquipo(ctx, datos);
        ctx.sendMessage(Message.raw("§7Táctica [A]: §f" + datos.tacticaA + "  [B]: §f" + datos.tacticaB));
        ctx.sendMessage(Message.raw("§e▶ /digi batalla atacar §8| §edefender §8| §ehuir"));
    }

    // ── Resolver turno (atacar) ────────────────────────────────────

    private void resolverTurno(CommandContext ctx, DatosJugador datos, boolean defensivo) {
        StringBuilder log = new StringBuilder();

        // Ataques de los compañeros
        atacarConDigimon(datos.companeroA, "A", datos.tacticaA, datos, log, defensivo);
        atacarConDigimon(datos.companeroB, "B", datos.tacticaB, datos, log, defensivo);

        // ¿Murió el enemigo?
        if (datos.enemigoHp <= 0) {
            victoria(ctx, datos, log);
            return;
        }

        // Contraataque del enemigo
        contraataque(datos, log, defensivo);

        // ¿Murió el equipo?
        if (!datos.alguienVivo()) {
            derrota(ctx, datos, log);
            return;
        }

        datos.turno++;
        datos.logUltimo = "Turno " + datos.turno;
        ctx.sendMessage(Message.raw(log.toString()));
        mostrarEstadoCombate(ctx, datos);
    }

    private void resolverTurnoDefensivo(CommandContext ctx, DatosJugador datos) {
        resolverTurno(ctx, datos, true);
    }

    private void atacarConDigimon(DatoDigimon d, String slot, String tactica,
                                  DatosJugador datos, StringBuilder log, boolean modoDefensivo) {
        if (d == null || !d.vivo || d.hpCombate <= 0) return;

        int danoBase = d.calcularDanio(datos.enemigoDef);

        // Modificador de táctica
        double mult = switch (tactica) {
            case "AGRESIVO"  -> 1.4;
            case "DEFENSIVO" -> 0.7;
            default          -> 1.0;
        };
        if (modoDefensivo) mult *= 0.6;

        int dano = Math.max(1, (int)(danoBase * mult));
        datos.enemigoHp = Math.max(0, datos.enemigoHp - dano);
        log.append("§a[").append(slot).append("] ").append(d.nombre)
           .append(" §fataco por §c").append(dano).append(" dmg\n");
    }

    private void contraataque(DatosJugador datos, StringBuilder log, boolean modoDefensivo) {
        DatoDigimon[] vivos = vivosOrdenados(datos);
        for (DatoDigimon d : vivos) {
            if (d == null) continue;
            double redDef = modoDefensivo ? 0.5 : 1.0;
            // Disciplina reduce el dano recibido
            double redDisc = 1.0 - (d.disciplina / 200.0);
            int dmg = (int)(Math.max(1, datos.enemigoAtk - d.def / 2) * redDef * redDisc);
            d.hpCombate = Math.max(0, d.hpCombate - dmg);
            log.append("§c").append(datos.enemigoNombre).append(" §fataco a §e")
               .append(d.nombre).append(" §fpor §c").append(dmg).append(" dmg\n");
            if (d.hpCombate <= 0) {
                log.append("§8").append(d.nombre).append(" fue derrotado...\n");
                d.derrotas++;
            }
        }
    }

    // ── Victoria / Derrota ─────────────────────────────────────────

    private void victoria(CommandContext ctx, DatosJugador datos, StringBuilder log) {
        ctx.sendMessage(Message.raw(log.toString()));
        ctx.sendMessage(Message.raw("§6★ ¡Victoria! ★ El " + datos.enemigoNombre + " fue derrotado."));
        ctx.sendMessage(Message.raw("§e+" + datos.enemigoExp + " EXP para cada compañero vivo!"));

        if (datos.companeroA != null && datos.companeroA.vivo && datos.companeroA.hpCombate > 0) {
            datos.companeroA.victorias++;
            datos.companeroA.lazo = Math.min(100, datos.companeroA.lazo + 3);
            datos.companeroA.abi  = Math.min(99, datos.companeroA.abi  + 1);
        }
        if (datos.companeroB != null && datos.companeroB.vivo && datos.companeroB.hpCombate > 0) {
            datos.companeroB.victorias++;
            datos.companeroB.lazo = Math.min(100, datos.companeroB.lazo + 3);
            datos.companeroB.abi  = Math.min(99, datos.companeroB.abi  + 1);
        }

        datos.terminarCombate();
        ctx.sendMessage(Message.raw("§7Usa §e/digi evolucionar §7para ver condiciones de evolución."));
    }

    private void derrota(CommandContext ctx, DatosJugador datos, StringBuilder log) {
        ctx.sendMessage(Message.raw(log.toString()));
        ctx.sendMessage(Message.raw("§c✗ Ambos compañeros fueron derrotados..."));
        ctx.sendMessage(Message.raw("§7Usa §e/digi descansar §7para recuperarlos."));

        if (datos.companeroA != null) datos.companeroA.disciplina = Math.max(0, datos.companeroA.disciplina - 5);
        if (datos.companeroB != null) datos.companeroB.disciplina = Math.max(0, datos.companeroB.disciplina - 5);

        datos.terminarCombate();
    }

    // ── Huir ───────────────────────────────────────────────────────

    private void intentarHuida(CommandContext ctx, DatosJugador datos) {
        int probaHuida = 40 + promedioSpd(datos);
        probaHuida = Math.min(85, probaHuida);

        if (RNG.nextInt(100) < probaHuida) {
            ctx.sendMessage(Message.raw("§e💨 ¡Escapaste del combate!"));
            datos.terminarCombate();
        } else {
            ctx.sendMessage(Message.raw("§c¡No pudiste escapar! El enemigo contraataca..."));
            StringBuilder log = new StringBuilder();
            contraataque(datos, log, false);
            if (!datos.alguienVivo()) {
                derrota(ctx, datos, log);
            } else {
                ctx.sendMessage(Message.raw(log.toString()));
            }
        }
    }

    // ── Cambiar táctica ────────────────────────────────────────────

    private void cambiarTactica(CommandContext ctx, DatosJugador datos, String[] args) {
        if (args.length < 3) {
            ctx.sendMessage(Message.raw("§eUso: §f/digi batalla tactica <a|b> <agresivo|balanceado|defensivo>"));
            return;
        }
        String slot = args[1].toLowerCase();
        String tac  = args[2].toUpperCase();

        if (!tac.matches("AGRESIVO|BALANCEADO|DEFENSIVO")) {
            ctx.sendMessage(Message.raw("§cTáctica inválida. Usa: agresivo, balanceado, defensivo"));
            return;
        }

        if (slot.equals("a")) { datos.tacticaA = tac; ctx.sendMessage(Message.raw("§a[A] Táctica: §f" + tac)); }
        else if (slot.equals("b")) { datos.tacticaB = tac; ctx.sendMessage(Message.raw("§a[B] Táctica: §f" + tac)); }
        else ctx.sendMessage(Message.raw("§cElige §ea §co §eb"));
    }

    // ── Helpers ────────────────────────────────────────────────────

    private void mostrarEstadoCombate(CommandContext ctx, DatosJugador datos) {
        ctx.sendMessage(Message.raw("§8--- Combate Turno " + datos.turno + " ---"));
        ctx.sendMessage(Message.raw("§cEnemigo: §f" + datos.enemigoNombre +
            " §7HP: §c" + datos.enemigoHp + "/" + datos.enemigoHpMax));
        mostrarHpEquipo(ctx, datos);
    }

    private void mostrarHpEquipo(CommandContext ctx, DatosJugador datos) {
        if (datos.companeroA != null && datos.companeroA.vivo)
            ctx.sendMessage(Message.raw("§a[A] " + datos.companeroA.nombre +
                " §7HP: §a" + datos.companeroA.hpCombate + "/" + datos.companeroA.maxHp));
        if (datos.companeroB != null && datos.companeroB.vivo)
            ctx.sendMessage(Message.raw("§a[B] " + datos.companeroB.nombre +
                " §7HP: §a" + datos.companeroB.hpCombate + "/" + datos.companeroB.maxHp));
    }

    private DatoDigimon[] vivosOrdenados(DatosJugador datos) {
        return new DatoDigimon[]{ datos.companeroA, datos.companeroB };
    }

    private int promedioNivel(DatosJugador datos) {
        int sum = 0, count = 0;
        if (datos.companeroA != null) { sum += datos.companeroA.nivel; count++; }
        if (datos.companeroB != null) { sum += datos.companeroB.nivel; count++; }
        return count > 0 ? sum / count : 3;
    }

    private int promedioSpd(DatosJugador datos) {
        int sum = 0, count = 0;
        if (datos.companeroA != null && datos.companeroA.vivo) { sum += datos.companeroA.spd; count++; }
        if (datos.companeroB != null && datos.companeroB.vivo) { sum += datos.companeroB.spd; count++; }
        return count > 0 ? sum / count : 0;
    }

    private PlantillaEnemigo[] filtrarPool(int maxIdx) {
        PlantillaEnemigo[] result = new PlantillaEnemigo[maxIdx];
        System.arraycopy(ENEMIGOS, 0, result, 0, maxIdx);
        return result;
    }
}
