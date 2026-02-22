package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class DigiMainMenuUI extends InteractiveCustomUIPage<DigiMainMenuUI.Data> {

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec
            .builder(Data.class, Data::new)
            .append(new KeyedCodec<>("@BtnPresionado", Codec.STRING),
                    (data, v) -> data.btnPresionado = v,
                    data -> data.btnPresionado)
            .add()
            .build();
        public String btnPresionado = "";
    }

    private final PlayerRef playerRef;

    public DigiMainMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiBuilder,
                      @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store<EntityStore> store) {

        uiBuilder.append("DigiMainMenu.ui");

        // TODO: sustituir BINDING_TYPE con la constante real cuando tengamos el javap
        // Patrón de la doc: eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#id", EventData.of(...), false)
        DigiUIHelper.bindClick(eventBuilder, "#BtnEstado",      "@BtnPresionado", "estado");
        DigiUIHelper.bindClick(eventBuilder, "#BtnEvolucionar", "@BtnPresionado", "evolucionar");
        DigiUIHelper.bindClick(eventBuilder, "#BtnEntrenar",    "@BtnPresionado", "entrenar");
        DigiUIHelper.bindClick(eventBuilder, "#BtnCuidar",      "@BtnPresionado", "cuidar");
        DigiUIHelper.bindClick(eventBuilder, "#BtnCerrar",      "@BtnPresionado", "cerrar");
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) { sendUpdate(new UICommandBuilder(), false); return; }

        switch (data.btnPresionado) {
            case "estado"      -> player.getPageManager().openCustomPage(ref, store, new DigiStatusMenuUI(playerRef));
            case "evolucionar" -> player.getPageManager().openCustomPage(ref, store, new DigiEvolucionMenuUI(playerRef, "a"));
            case "entrenar"    -> player.getPageManager().openCustomPage(ref, store, new DigiEntrenarMenuUI(playerRef));
            case "cuidar"      -> player.getPageManager().openCustomPage(ref, store, new DigiCuidarMenuUI(playerRef));
            case "cerrar"      -> player.getPageManager().closePage(ref, store);
            default            -> sendUpdate(new UICommandBuilder(), false);
        }
    }
}
