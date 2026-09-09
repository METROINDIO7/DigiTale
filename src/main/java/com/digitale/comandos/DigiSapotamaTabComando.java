package com.digitale.comandos;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.ui.DigiStartMenuUI;
import com.digitale.ui.SapotamaTabUI;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/** 
 * /digi_tab — abre el menú de pestañas unificado de Sapotama (Equipo/Crianza/Batalla).
 *
 * Si el jugador no tiene equipo aún → abre DigiStartMenu (selección de starter).
 * Si ya tiene equipo               → abre SapotamaTabUI (tabs unificadas).
 *
 * Usa AbstractPlayerCommand que ejecuta en WorldThread (obligatorio en Hytale).
 */
public class DigiSapotamaTabComando extends AbstractPlayerCommand {

    public DigiSapotamaTabComando(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext ctx,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {
        if (AlmacenJugadores.existe(playerRef.getUuid())) {
            SapotamaTabUI.open(playerRef, ref, store);
        } else {
            DigiStartMenuUI.open(playerRef, ref, store);
        }
    }
}
