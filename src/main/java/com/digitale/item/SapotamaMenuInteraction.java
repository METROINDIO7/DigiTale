package com.digitale.item;

import com.digitale.comandos.DigiMenuComando;
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
 * Interacción del ítem Sapotama (clic derecho / tecla F).
 * Abre la misma UI que /digi_menu. Se registra como
 * "DigiTale_Sapotama_Menu" y se referencia desde Sapotama.json.
 */
public class SapotamaMenuInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<SapotamaMenuInteraction> CODEC = BuilderCodec.builder(
        SapotamaMenuInteraction.class, SapotamaMenuInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    private static final Logger LOGGER = Logger.getLogger(SapotamaMenuInteraction.class.getName());

    public SapotamaMenuInteraction() {
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
                    DigiMenuComando.abrir(playerRef, playerEntity, store);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error abriendo menú Sapotama: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "SapotamaMenuInteraction: " + e.getMessage());
        }
    }
}
