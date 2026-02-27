package com.digitale.comandos;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.ui.DigiMainMenuUI;
import com.digitale.ui.DigiStartMenuUI;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import java.awt.Color;
import java.util.concurrent.CompletableFuture;

/**
 * /digi_menu — abre el menú principal de DigiTale.
 *
 * Si el jugador no tiene equipo aún → abre DigiStartMenu (selección de starter).
 * Si ya tiene equipo               → abre DigiMainMenu (menú principal).
 *
 * Usa AbstractAsyncCommand + CompletableFuture.runAsync(world) para
 * ejecutar operaciones de UI en el WorldThread (obligatorio en Hytale).
 */
public class DigiMenuComando extends AbstractAsyncCommand {

    public DigiMenuComando(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    @NonNullDecl
    @Override
    protected CompletableFuture<Void> executeAsync(CommandContext context) {
        CommandSender sender = context.sender();

        if (!(sender instanceof Player player)) {
            context.sendMessage(Message.raw("Solo los jugadores pueden usar este comando."));
            return CompletableFuture.completedFuture(null);
        }

        // context.senderAsPlayerRef() devuelve Ref<EntityStore> del jugador
        Ref<EntityStore> ref = context.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) {
            return CompletableFuture.completedFuture(null);
        }

        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        // Despachar al WorldThread — obligatorio para operaciones de UI y ECS
        return CompletableFuture.runAsync(() -> {
            if (!ref.isValid()) return;

            // Extraer PlayerRef desde el store (patrón correcto en Hytale)
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef == null) return;

            // Decidir qué menú abrir
            boolean tieneEquipo = AlmacenJugadores.existe(playerRef.getUuid());

            if (!tieneEquipo) {
                player.getPageManager().openCustomPage(ref, store, new DigiStartMenuUI(playerRef));
            } else {
                player.getPageManager().openCustomPage(ref, store, new DigiMainMenuUI(playerRef));
            }

        }, world); // world implementa Executor → ejecuta en WorldThread
    }
}
