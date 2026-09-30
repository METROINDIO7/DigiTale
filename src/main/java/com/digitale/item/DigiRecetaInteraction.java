package com.digitale.item;

import com.digitale.crianza.DigitamaDef;
import com.digitale.ui.DigiRecetaUI;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Examinar Dato Incubación: revela la receta del bebé vinculado.
 * El item es Dato_Incubacion_<Especie>; se registra como DigiTale_Dato_Receta.
 */
public class DigiRecetaInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<DigiRecetaInteraction> CODEC = BuilderCodec.builder(
        DigiRecetaInteraction.class, DigiRecetaInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    private static final Logger LOGGER = Logger.getLogger(DigiRecetaInteraction.class.getName());
    private static final String PREFIJO = "Dato_Incubacion_";

    public DigiRecetaInteraction() {
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

            String bebe = null;
            try {
                ItemStack mano = interactionContext.getHeldItem();
                if (mano != null && mano.getItemId() != null) {
                    String id = mano.getItemId();
                    String corto = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
                    if (corto.startsWith(PREFIJO)) bebe = corto.substring(PREFIJO.length());
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "DigiReceta held: " + e.getMessage());
            }
            if (bebe == null || bebe.isEmpty()) return;

            DigitamaDef def = DigitamaDef.porBebe(bebe);
            if (def == null) return;

            Ref<EntityStore> playerEntity = playerRef.getReference();
            if (playerEntity == null || !playerEntity.isValid()) return;
            Store<EntityStore> store = playerEntity.getStore();
            DigitamaDef finalDef = def;
            String finalBebe = bebe;
            store.getExternalData().getWorld().execute(() -> {
                try {
                    // Examinar MUESTRA la receta pero NO desbloquea ni consume:
                    // se desbloquea en la Mesa con el dato.
                    playerRef.sendMessage(com.digitale.util.DigiMsg.parse(
                        "§eReceta de " + finalBebe + ": llevala a la §fMesa de Crianza §epara desbloquearla."));
                    DigiRecetaUI.open(playerRef, playerEntity, store, finalDef);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "No se pudo abrir receta: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "DigiRecetaInteraction: " + e.getMessage());
        }
    }
}
