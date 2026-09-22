package com.digitale.comandos;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
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
 * /digi_reset — Reinicia el equipo del jugador borrando todos los digimons.
 * Vuelve al estado inicial para elegir nuevos compañeros.
 */
public class DigiResetComando extends AbstractPlayerCommand {

    public DigiResetComando(@NonNullDecl String name, @NonNullDecl String description) {
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
            ctx.sendMessage(DigiMsg.parse("§cNo tienes equipo. Nada que reiniciar."));
            ctx.sendMessage(DigiMsg.parse("§7Usa §e/digi_start §7para elegir companeros."));
            return;
        }

        // Guardar cuántos había antes de borrar
        int total = datos.contarEquipo();

        // Limpiar todo
        for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) datos.setSlot(i, null);
        datos.activoA = 0;
        datos.activoB = 1;
        datos.tieneEquipo = false;
        datos.enCombate = false;
        datos.enemigoNombre = "";
        datos.enemigoHp = 0;
        datos.enemigoHpMax = 0;
        datos.enemigoAtk = 0;
        datos.enemigoDef = 0;
        datos.enemigoExp = 0;
        datos.turno = 0;
        datos.logUltimo = "";
        datos.paseoActivo = false;
        datos.combate = null;
        datos.desafioAbierto = false;
        datos.modoDesafio = "Amistoso";
        datos.tacticaA = "BALANCEADO";
        datos.tacticaB = "BALANCEADO";

        // Recoger NPCs del paseo si estaban fuera.
        try {
            com.digitale.sistema.SistemaPaseo.recogerCompaneros(playerRef, ref, store);
        } catch (Exception e) {
            // No bloquear el comando si no había nada que recoger.
        }

        // Persistir el borrado de inmediato (si no, los slots viejos reviven al reloguear).
        try {
            com.digitale.componentes.PetProgressComponent progress =
                ComponentRegistry.ensureAndGetProgress(store, ref);
            if (progress != null) progress.guardarDesde(datos);
        } catch (Exception e) {
            // No bloquear el comando si el guardado falla.
        }

        ctx.sendMessage(DigiMsg.parse("§c===================================="));
        ctx.sendMessage(DigiMsg.parse("§c== §fREINICIO DE EQUIPO"));
        ctx.sendMessage(DigiMsg.parse("§c== §7Digimon liberados: §f" + total));
        ctx.sendMessage(DigiMsg.parse("§c===================================="));
        ctx.sendMessage(DigiMsg.parse(""));
        ctx.sendMessage(DigiMsg.parse("§e> Equipo reiniciado correctamente."));
        ctx.sendMessage(DigiMsg.parse("§7Usa §e/digi_start §7o §e/digi_tab §7para elegir nuevos companeros."));
    }
}
