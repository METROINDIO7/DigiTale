package com.digitale.ui;

import com.digitale.util.Inventarios;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TEST temporal: verifica que ItemGrid renderiza y qué payloads llegan
 * en Dropped / SlotClicking / SlotMouseDragCompleted. Todo va al log.
 */
public class DigiGridTestUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(DigiGridTestUI.class.getName());
    private String ultimo = "Sin eventos aun.";

    public DigiGridTestUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;
            player.getPageManager().openCustomPage(ref, store, new DigiGridTestUI(playerRef));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error abriendo grid test: " + e.getMessage());
        }
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder event,
                      @Nonnull Store<EntityStore> store) {
        cmd.append("DigiGridTest.ui");
        List<ItemGridSlot> slots = new ArrayList<>();
        try {
            List<Inventarios.Hallazgo> hallados = Inventarios.recorrer(ref, store);
            for (int i = 0; i < 6; i++) {
                ItemGridSlot gs = new ItemGridSlot();
                if (i < hallados.size() && hallados.get(i).stack != null) {
                    try {
                        gs.setItemStack(hallados.get(i).stack);
                        gs.setName(hallados.get(i).corto);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "slot " + i + ": " + e.getMessage());
                    }
                }
                slots.add(gs);
            }
            cmd.set("#GridTest.Slots", slots);
            LOGGER.info("GRIDTEST: grid alimentada con " + slots.size() + " slots.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "GRIDTEST set Slots fallo: " + e.getMessage());
        }
        event.addEventBinding(CustomUIEventBindingType.Dropped, "#GridTest",
            EventData.of("action", "drop"), false);
        event.addEventBinding(CustomUIEventBindingType.SlotClicking, "#GridTest",
            EventData.of("action", "click"), false);
        event.addEventBinding(CustomUIEventBindingType.SlotMouseDragCompleted, "#GridTest",
            EventData.of("action", "drag"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCerrarTest",
            EventData.of("action", "cerrar"), false);
        cmd.set("#LblTest.Text", ultimo);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull String jsonData) {
        LOGGER.severe("GRIDTEST raw: " + jsonData);
        try {
            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
            String action = json.has("action") ? json.get("action").getAsString() : "?";
            if ("cerrar".equals(action)) {
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player != null) {
                    player.getPageManager().setPage(ref, store,
                        com.hypixel.hytale.protocol.packets.interface_.Page.None);
                }
                return;
            }
            ultimo = "Evento " + action + ": " + jsonData;
            UICommandBuilder cmd = new UICommandBuilder();
            cmd.set("#LblTest.Text", ultimo.length() > 200 ? ultimo.substring(0, 200) : ultimo);
            sendUpdate(cmd);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "GRIDTEST evento: " + e.getMessage());
        }
    }
}
