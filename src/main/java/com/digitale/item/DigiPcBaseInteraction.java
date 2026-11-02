package com.digitale.item;

import com.digitale.ui.DigiBancoPCUI;
import com.digitale.ui.VuetaleBancoPCUI;
import com.digitale.util.DigiMsg;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uso base del PC Digital (todavía sin granja): abre el Banco PC.
 * Se registra como "DigiTale_PC_Base".
 */
public class DigiPcBaseInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<DigiPcBaseInteraction> CODEC = BuilderCodec.builder(
        DigiPcBaseInteraction.class, DigiPcBaseInteraction::new,
        SimpleInstantInteraction.CODEC
    ).build();

    private static final Logger LOGGER = Logger.getLogger(DigiPcBaseInteraction.class.getName());

    public DigiPcBaseInteraction() {
        super();
    }

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType,
                            @Nonnull InteractionContext interactionContext,
                            @Nonnull CooldownHandler cooldownHandler) {
        try {
            Ref<EntityStore> ref = interactionContext.getEntity();
            if (ref == null) return;
            var buffer = interactionContext.getCommandBuffer();
            if (buffer == null) return;
            PlayerRef playerRef = buffer.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef == null) return;
            Ref<EntityStore> playerEntity = playerRef.getReference();
            if (playerEntity == null || !playerEntity.isValid()) return;
            Store<EntityStore> store = playerEntity.getStore();
            store.getExternalData().getWorld().execute(() -> {
                try {
                    playerRef.sendMessage(DigiMsg.parse("§bPC Digital: gestiona tu equipo y tu caja."));
                    if (!VuetaleBancoPCUI.abrir(playerRef, playerEntity, store)) {
                        DigiBancoPCUI.open(playerRef, playerEntity, store);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error abriendo PC: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "DigiPcBaseInteraction: " + e.getMessage());
        }
    }
}
