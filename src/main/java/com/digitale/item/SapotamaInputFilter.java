package com.digitale.item;

import com.digitale.ui.SapotamaTabUI;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Intercepta el uso del ítem Sapotama (clic izquierdo/derecho, tecla F/Uso o habilidades).
 * Abre el menú de tabs de Sapotama en lugar del comportamiento normal del ítem.
 *
 * No bloquea el paquete (siempre devuelve false): solo dispara la apertura de la UI.
 * El ítem Sapotama.json define una interacción "Simple" en TODOS los slots de uso
 * (Primary, Secondary, Use, Ability1-3) para que el cliente siempre envíe el paquete
 * SyncInteractionChains sin importar el botón/tecla que use el jugador.
 */
public class SapotamaInputFilter implements PlayerPacketFilter {

    private static final String SAPOTAMA_ID = "Sapotama";
    private static final Logger LOGGER = Logger.getLogger(SapotamaInputFilter.class.getName());

    @Override
    public boolean test(PlayerRef playerRef, Packet packet) {
        if (!(packet instanceof SyncInteractionChains syncPacket)) {
            return false;
        }

        for (SyncInteractionChain chain : syncPacket.updates) {
            if (chain == null || !chain.initial) {
                continue;
            }

            InteractionType tipo = chain.interactionType;
            // Detectamos CUALQUIER tipo de interacción (Primary, Secondary, Use, Abilities)
            // El F key debe generar InteractionType.Use en Hytale
            boolean esUso = tipo == InteractionType.Primary
                    || tipo == InteractionType.Secondary
                    || tipo == InteractionType.Use
                    || tipo == InteractionType.Ability1
                    || tipo == InteractionType.Ability2
                    || tipo == InteractionType.Ability3;
            if (!esUso) {
                continue;
            }

            // Prioridad 1: El paquete trae el id del ítem en mano
            if (chain.itemInHandId != null && !chain.itemInHandId.isEmpty()) {
                if (SAPOTAMA_ID.equals(chain.itemInHandId)) {
                    LOGGER.log(Level.INFO, "Sapotama detectado vía itemInHandId (tipo=" + tipo + ")");
                    abrirMenu(playerRef);
                }
                return false;
            }

            // Prioridad 2: Verificar inventario directamente (más fiable para F key)
            LOGGER.log(Level.INFO, "Interacción detectada (tipo=" + tipo + ") - verificando inventario");
            abrirMenuSiTieneSapotama(playerRef);
            return false;
        }

        return false;
    }

    /** Abre el menú si el paquete ya confirmó que el ítem en mano es el Sapotama. */
    private void abrirMenu(PlayerRef playerRef) {
        Ref<EntityStore> entityRef = playerRef.getReference();
        if (entityRef == null || !entityRef.isValid()) {
            return;
        }
        Store<EntityStore> store = entityRef.getStore();
        store.getExternalData().getWorld().execute(() -> {
            Player player = store.getComponent(entityRef, Player.getComponentType());
            if (player == null) {
                return;
            }
            try {
                SapotamaTabUI.open(playerRef, entityRef, store);
                LOGGER.log(Level.INFO, "SapotamaTabUI abierto");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error abriendo SapotamaTabUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /** Verifica en el inventario (dentro del WorldThread) antes de abrir el menú. */
    private void abrirMenuSiTieneSapotama(PlayerRef playerRef) {
        Ref<EntityStore> entityRef = playerRef.getReference();
        if (entityRef == null || !entityRef.isValid()) {
            return;
        }
        Store<EntityStore> store = entityRef.getStore();
        store.getExternalData().getWorld().execute(() -> {
            if (!tieneSapotamaEnMano(store, entityRef)) {
                return;
            }
            Player player = store.getComponent(entityRef, Player.getComponentType());
            if (player == null) {
                return;
            }
            try {
                SapotamaTabUI.open(playerRef, entityRef, store);
                LOGGER.log(Level.INFO, "SapotamaTabUI abierto");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error abriendo SapotamaTabUI: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private boolean tieneSapotamaEnMano(Store<EntityStore> store, Ref<EntityStore> entityRef) {
        try {
            Player player = store.getComponent(entityRef, Player.getComponentType());
            if (player == null) {
                LOGGER.log(Level.WARNING, "Player component es null");
                return false;
            }

            var inv = player.getInventory();
            if (inv == null) {
                LOGGER.log(Level.WARNING, "Inventario es null");
                return false;
            }

            @SuppressWarnings("removal")
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
