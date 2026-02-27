package com.digitale.item;

import com.digitale.ui.SapotamaMenuUI;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * Intercepta el uso del ítem Sapotama (tecla F / Use).
 * Abre el menú de mascotas en lugar del comportamiento normal del ítem.
 */
public class SapotamaInputFilter implements PlayerPacketFilter {

    private static final String SAPOTAMA_ID = "Sapotama";

    @Override
    public boolean test(@Nonnull PlayerRef playerRef, @Nonnull Packet packet) {
        if (!(packet instanceof SyncInteractionChains syncPacket)) {
            return false;
        }

        for (SyncInteractionChain chain : syncPacket.updates) {
            if (chain.interactionType != InteractionType.Use || !chain.initial) {
                continue;
            }

            if (!tieneSapotamaEnMano(playerRef)) {
                continue;
            }

            Ref<EntityStore> entityRef = playerRef.getReference();
            if (entityRef == null || !entityRef.isValid()) return false;

            Store<EntityStore> store = entityRef.getStore();
            store.getExternalData().getWorld().execute(() -> {
                Player player = store.getComponent(entityRef, Player.getComponentType());
                if (player == null) return;
                player.getPageManager().openCustomPage(
                        entityRef, store, new SapotamaMenuUI(playerRef));
            });

            return true;
        }

        return false;
    }

    private boolean tieneSapotamaEnMano(@Nonnull PlayerRef playerRef) {
        try {
            Ref<EntityStore> entityRef = playerRef.getReference();
            if (entityRef == null || !entityRef.isValid()) return false;

            Store<EntityStore> store = entityRef.getStore();
            Player player = store.getComponent(entityRef, Player.getComponentType());
            if (player == null) return false;

            Inventory inv = player.getInventory();
            if (inv == null) return false;

            ItemStack mano = inv.getItemInHand();
            return mano != null
                    && mano.getItemId() != null
                    && SAPOTAMA_ID.equals(mano.getItemId());
        } catch (Exception e) {
            return false;
        }
    }
}
