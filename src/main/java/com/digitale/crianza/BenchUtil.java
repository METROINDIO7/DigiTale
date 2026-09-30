package com.digitale.crianza;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.joml.Vector3i;

/** Localiza el ProcessingBenchBlock de una mesa por posición de bloque. */
public final class BenchUtil {

    private BenchUtil() {
    }

    public static com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock resolver(
        World world, Vector3i pos) {
        if (world == null || pos == null) return null;
        try {
            ChunkStore chunkStore = world.getChunkStore();
            if (chunkStore == null) return null;
            Ref<ChunkStore> section =
                chunkStore.getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
            if (section == null || !section.isValid()) return null;
            Store<ChunkStore> cs = chunkStore.getStore();
            Ref<ChunkStore> blockEntity =
                BlockModule.getBlockEntity(cs, section, pos.x, pos.y, pos.z);
            if (blockEntity == null || !blockEntity.isValid()) return null;
            return cs.getComponent(blockEntity,
                com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock.getComponentType());
        } catch (Exception e) {
            return null;
        }
    }

    /** "x,y,z" -> Vector3i o null. */
    public static Vector3i parsePos(String clave) {
        if (clave == null) return null;
        try {
            String[] p = clave.split(",");
            if (p.length != 3) return null;
            return new Vector3i(Integer.parseInt(p[0].trim()),
                Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim()));
        } catch (Exception e) {
            return null;
        }
    }
}
