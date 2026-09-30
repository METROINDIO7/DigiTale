package com.digitale.item;

import com.digitale.crianza.Incubacion;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.ui.DigiMesaReclamoUI;
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
 * Usar el Huevo Incubando (token del horno): si hay alguna incubación
 * lista abre la página de Reclamo (equipo / banco / liberar).
 * Se registra como "DigiTale_Huevo_Incubando".
 */
public class DigiHuevoIncubandoInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<DigiHuevoIncubandoInteraction> CODEC = BuilderCodec.builder(
        DigiHuevoIncubandoInteraction.class, DigiHuevoIncubandoInteraction::new,
        SimpleInstantInteraction.CODEC
    ).build();

    private static final Logger LOGGER = Logger.getLogger(DigiHuevoIncubandoInteraction.class.getName());

    public DigiHuevoIncubandoInteraction() {
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
                    String[] token = parseToken(interactionContext);
                    if (token == null) {
                        playerRef.sendMessage(DigiMsg.parse("§eSosten el Huevo Incubando para reclamar."));
                        return;
                    }
                    DigiMesaReclamoUI.open(playerRef, playerEntity, store,
                        token[0], token[1], token.length > 2);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error abriendo reclamo: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "DigiHuevoIncubandoInteraction: " + e.getMessage());
        }
    }

    /**
     * Parsea el token en mano: Huevo_Incubando_&lt;SufHuevo&gt;_&lt;Bebe&gt;[_X|_S]
     * -> {"Digitama_&lt;Suf&gt;", "&lt;Bebe&gt;"} (+ tercer elemento si variante) o null.
     */
    static String[] parseToken(InteractionContext ctx) {
        try {
            com.hypixel.hytale.server.core.inventory.ItemStack mano = ctx.getHeldItem();
            if (mano == null || mano.getItemId() == null) return null;
            String id = mano.getItemId();
            String corto = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
            String pref = "Huevo_Incubando_";
            if (!corto.startsWith(pref)) return null;
            String resto = corto.substring(pref.length());
            boolean variante = false;
            if (resto.endsWith("_X") || resto.endsWith("_S")) {
                variante = true;
                resto = resto.substring(0, resto.length() - 2);
            }
            if (resto.startsWith("Desconocido_")) {
                String bebe = resto.substring("Desconocido_".length());
                if (bebe.isEmpty()) return null;
                if (variante) return new String[]{"Digitama_Desconocido", bebe, "X"};
                return new String[]{"Digitama_Desconocido", bebe};
            }
            for (com.digitale.crianza.DigitamaDef d : com.digitale.crianza.DigitamaDef.TODOS) {
                String suf = d.idItem.startsWith("Digitama_")
                    ? d.idItem.substring("Digitama_".length()) : null;
                if (suf == null || suf.isEmpty()) continue;
                if (resto.startsWith(suf + "_")) {
                    String bebe = resto.substring(suf.length() + 1);
                    if (bebe.isEmpty()) continue;
                    if (variante) return new String[]{d.idItem, bebe, "X"};
                    return new String[]{d.idItem, bebe};
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
