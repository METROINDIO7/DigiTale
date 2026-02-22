package com.digitale.comandos;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.ui.DigiMainMenuUI;
import com.digitale.ui.DigiStartMenuUI;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/**
 * /digi_menu — abre la interfaz principal de DigiTale
 */
public class DigiMenuComando extends AbstractPlayerCommand {

    public DigiMenuComando(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext ctx,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            ctx.sendMessage(Message.raw("§cError: no se pudo acceder al jugador."));
            return;
        }

        boolean tieneEquipo = AlmacenJugadores.existe(playerRef.getUuid());

        if (tieneEquipo) {
            // Abrir menú principal
            player.getPageManager().openCustomPage(ref, store,
                new DigiMainMenuUI(playerRef));
        } else {
            // Sin equipo: abrir selector de starter
            player.getPageManager().openCustomPage(ref, store,
                new DigiStartMenuUI(playerRef));
        }
    }
}
