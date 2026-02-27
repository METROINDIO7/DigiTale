package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class DigiStatusMenuUI extends InteractiveCustomUIPage<DigiStatusMenuUI.Data> {

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec
            .builder(Data.class, Data::new)
            .append(new KeyedCodec<>("Accion", Codec.STRING),
                    (data, v) -> data.accion = v,
                    data -> data.accion)
            .add()
            .build();
        public String accion = "";
    }

    private final PlayerRef playerRef;

    public DigiStatusMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiBuilder,
                      @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store<EntityStore> store) {

        uiBuilder.append("Pages/DigiStatusMenu.ui");

        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        rellenarDatos(uiBuilder, datos);

        eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#BtnVolver",
                new EventData().append("Accion", "volver"),
                false
        );
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);

        if ("volver".equals(data.accion)) {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null)
                player.getPageManager().openCustomPage(ref, store, new DigiMainMenuUI(playerRef));
        } else {
            sendUpdate(new UICommandBuilder(), null, false);
        }
    }

    private void rellenarDatos(UICommandBuilder uiBuilder, DatosJugador datos) {
        setDigimon(uiBuilder, datos.companeroA, "A");
        setDigimon(uiBuilder, datos.companeroB, "B");
    }

    private void setDigimon(UICommandBuilder b, DatoDigimon d, String slot) {
        if (d == null || !d.vivo) {
            b.set("#Nombre" + slot + ".Text", "Sin companero");
            b.set("#Especie" + slot + ".Text", "---");
            b.set("#Elemento" + slot + ".Text", "---");
            b.set("#Hp" + slot + ".Text", "HP: ---");
            b.set("#Atk" + slot + ".Text", "ATK: ---");
            b.set("#Def" + slot + ".Text", "DEF: ---");
            b.set("#Spd" + slot + ".Text", "SPD: ---");
            b.set("#Wis" + slot + ".Text", "WIS: ---");
            b.set("#Lazo" + slot + ".Text", "Lazo: ---");
            b.set("#Abi" + slot + ".Text", "ABI: ---");
            b.set("#Vic" + slot + ".Text", "Victorias: ---");
            b.set("#Energia" + slot + ".Text", "Energia: ---");
            return;
        }
        b.set("#Nombre" + slot + ".Text", d.nombre);
        b.set("#Especie" + slot + ".Text", d.especie + " - " + d.nombreNivel());
        b.set("#Elemento" + slot + ".Text", d.elemento);
        b.set("#Hp" + slot + ".Text", "HP: " + d.hp + "/" + d.maxHp);
        b.set("#Atk" + slot + ".Text", "ATK: " + d.atk);
        b.set("#Def" + slot + ".Text", "DEF: " + d.def);
        b.set("#Spd" + slot + ".Text", "SPD: " + d.spd);
        b.set("#Wis" + slot + ".Text", "WIS: " + d.wis);
        b.set("#Lazo" + slot + ".Text", "Lazo: " + d.lazo + "/100");
        b.set("#Abi" + slot + ".Text", "ABI: " + d.abi);
        b.set("#Vic" + slot + ".Text", "Victorias: " + d.victorias);
        b.set("#Energia" + slot + ".Text", "Energia: " + d.energia + "/100");
    }
}
