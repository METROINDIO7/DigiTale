package com.digitale.item;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.crianza.DigitamaDef;
import com.digitale.crianza.RecetaMesa;
import com.digitale.crianza.SistemaIncubacion;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.util.DigiMsg;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Secundario en la Mesa de Crianza (bench nativo): lee los slots de
 * input del ProcessingBenchBlock y combina (huevo + dato + extras).
 * Sin mezcla válida abre la página de estado (progreso/reclamar).
 * Se registra como "DigiTale_Mesa_Procesa".
 */
public class DigiMesaProcesaInteraction extends SimpleBlockInteraction {

    public static final BuilderCodec<DigiMesaProcesaInteraction> CODEC = BuilderCodec.builder(
        DigiMesaProcesaInteraction.class, DigiMesaProcesaInteraction::new,
        SimpleBlockInteraction.CODEC
    ).build();

    private static final Logger LOGGER = Logger.getLogger(DigiMesaProcesaInteraction.class.getName());

    public DigiMesaProcesaInteraction() {
        super();
    }

    @Override
    protected void interactWithBlock(@Nonnull World world,
                                     @Nonnull CommandBuffer<EntityStore> buffer,
                                     @Nonnull InteractionType interactionType,
                                     @Nonnull InteractionContext interactionContext,
                                     @Nonnull ItemStack heldItem,
                                     @Nonnull Vector3i blockPosition,
                                     @Nonnull CooldownHandler cooldownHandler) {
        try {
            Ref<EntityStore> ent = interactionContext.getEntity();
            if (ent == null) return;
            PlayerRef playerRef = buffer.getComponent(ent, PlayerRef.getComponentType());
            if (playerRef == null) return;
            Ref<EntityStore> playerEntity = playerRef.getReference();
            if (playerEntity == null || !playerEntity.isValid()) return;
            Store<EntityStore> store = playerEntity.getStore();

            ChunkStore chunkStore = world.getChunkStore();
            if (chunkStore == null) return;
            com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock bench =
                com.digitale.crianza.BenchUtil.resolver(world, blockPosition);
            if (bench == null) {
                playerRef.sendMessage(DigiMsg.parse("§cMesa antigua o sin bench: recogela y vuelve a colocarla."));
                return;
            }
            // El horneado es 100% nativo (TURN ON + recetas). Aquí solo banco/estado.
            com.digitale.ui.DigiBancoPCUI.open(playerRef, playerEntity, store);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "DigiMesaProcesaInteraction: " + e.getMessage());
        }
    }

    @Override
    protected void simulateInteractWithBlock(@Nonnull InteractionType interactionType,
                                             @Nonnull InteractionContext interactionContext,
                                             @Nonnull ItemStack heldItem,
                                             @Nonnull World world,
                                             @Nonnull Vector3i blockPosition) {
        // Solo servidor.
    }

    private void guardar(Store<EntityStore> store, Ref<EntityStore> ref,
                         AlmacenJugadores.DatosJugador datos) {
        try {
            PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
            if (progress != null) progress.guardarDesde(datos);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo guardar procesado: " + e.getMessage());
        }
    }
}
