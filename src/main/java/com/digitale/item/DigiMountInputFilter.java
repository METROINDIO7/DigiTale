package com.digitale.item;

import com.digitale.sistema.MountMovementSupport;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.packets.player.ClientMovement;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class DigiMountInputFilter implements PlayerPacketFilter {

    @Override
    @SuppressWarnings("nullness")
    public boolean test(PlayerRef playerRef, Packet packet) {
        if (!(packet instanceof ClientMovement movement)) {
            return false;
        }

        Ref<EntityStore> playerEntityRef = playerRef.getReference();
        if (playerEntityRef == null || !playerEntityRef.isValid()) {
            return false;
        }

        Store<EntityStore> store = playerEntityRef.getStore();
        store.getExternalData().getWorld().execute(() ->
                MountMovementSupport.onClientMovement(playerRef, playerEntityRef, store, movement.mountedTo)
        );

        return false;
    }
}
