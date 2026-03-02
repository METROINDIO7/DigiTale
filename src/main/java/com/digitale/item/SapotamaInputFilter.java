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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Intercepta el uso del ítem Sapotama (tecla F / Use).
 * Abre el menú de mascotas en lugar del comportamiento normal del ítem.
 */
public class SapotamaInputFilter implements PlayerPacketFilter {

    private static final String SAPOTAMA_ID = "Sapotama";
    private static final Logger LOGGER = Logger.getLogger(SapotamaInputFilter.class.getName());

    @Override
    public boolean test(@Nonnull PlayerRef playerRef, @Nonnull Packet packet) {
        if (!(packet instanceof SyncInteractionChains syncPacket)) {
            return false;
        }

        for (SyncInteractionChain chain : syncPacket.updates) {
            // SOLO interceptar Primary (click derecho al usar item)
            if (chain.interactionType != InteractionType.Primary || !chain.initial) {
                continue;  // No es Primary - dejar pasar
            }
            
            LOGGER.log(Level.INFO, "Primary detectado");

            Ref<EntityStore> entityRef = playerRef.getReference();
            if (entityRef == null || !entityRef.isValid()) {
                return false;  // Ref inválida - no hacer nada
            }

            Store<EntityStore> store = entityRef.getStore();
            
            // IMPORTANTE: Ejecutar TODO en WorldThread (incluyendo verificación de items)
            // Los accesos a Store deben ocurrir SIEMPRE en el WorldThread
            store.getExternalData().getWorld().execute(() -> {
                // Verificar si tiene Sapotama EN el WorldThread
                if (!tieneSapotamaEnMano(store, entityRef)) {
                    LOGGER.log(Level.INFO, "No es Sapotama - permitiendo default");
                    return;  // No es Sapotama, dejar que Hytale lo maneje
                }
                
                // ES SAPOTAMA - abrir UI
                LOGGER.log(Level.INFO, "¡Sapotama Primary! Abriendo UI...");
                Player player = store.getComponent(entityRef, Player.getComponentType());
                if (player != null) {
                    try {
                        player.getPageManager().openCustomPage(
                                entityRef, store, new SapotamaMenuUI(playerRef));
                        LOGGER.log(Level.INFO, "SapotamaMenuUI abierto");
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Error abriendo SapotamaMenuUI: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    LOGGER.log(Level.WARNING, "Player component es null");
                }
            });
            
            // Retornar false de cualquier forma (no bloquear mientras se procesa)
            return false;
        }

        return false;
    }

    private boolean tieneSapotamaEnMano(@Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> entityRef) {
        try {
            Player player = store.getComponent(entityRef, Player.getComponentType());
            if (player == null) {
                LOGGER.log(Level.WARNING, "Player component es null");
                return false;
            }

            Inventory inv = player.getInventory();
            if (inv == null) {
                LOGGER.log(Level.WARNING, "Inventario es null");
                return false;
            }

            ItemStack mano = inv.getItemInHand();
            if (mano == null) {
                LOGGER.log(Level.INFO, "Mano vacía");
                return false;
            }

            String itemId = mano.getItemId();
            LOGGER.log(Level.INFO, "Item en mano: {" + itemId + "}");
            
            boolean tieneItem = SAPOTAMA_ID.equals(itemId);
            LOGGER.log(Level.INFO, "¿Es Sapotama?: {" + tieneItem + "}");
            return tieneItem;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en tieneSapotamaEnMano: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}


