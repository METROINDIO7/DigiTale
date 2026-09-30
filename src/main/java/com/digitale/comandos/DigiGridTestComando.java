package com.digitale.comandos;

import com.digitale.ui.DigiGridTestUI;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

/** /digi_gridtest — TEST temporal de ItemGrid con arrastre (ver log GRIDTEST). */
public class DigiGridTestComando extends AbstractPlayerCommand {

    public DigiGridTestComando(@NonNullDecl String name, @NonNullDecl String description) {
        super(name, description);
    }

    @Override
    protected void execute(@NonNullDecl CommandContext ctx,
                           @NonNullDecl Store<EntityStore> store,
                           @NonNullDecl Ref<EntityStore> ref,
                           @NonNullDecl PlayerRef playerRef,
                           @NonNullDecl World world) {
        DigiGridTestUI.open(playerRef, ref, store);
    }
}
