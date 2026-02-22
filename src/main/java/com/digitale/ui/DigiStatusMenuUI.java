package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.digitale.ui.DigiUIHelper;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
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
            .append(new KeyedCodec<>("@Accion", Codec.STRING),
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

        uiBuilder.append("DigiStatusMenu.ui");

        // Rellenar datos dinámicos al abrir
        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        rellenarDatos(uiBuilder, datos);

        DigiUIHelper.bindClick(eventBuilder, "#BtnVolver", "@Accion", "volver");
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
            sendUpdate(new UICommandBuilder(), false);
        }
    }

    // ── Actualizar labels dinámicamente ───────────────────────────
    private void rellenarDatos(UICommandBuilder uiBuilder, DatosJugador datos) {
        setDigimon(uiBuilder, datos.companeroA, "A");
        setDigimon(uiBuilder, datos.companeroB, "B");
    }

    private void setDigimon(UICommandBuilder b, DatoDigimon d, String slot) {
        if (d == null || !d.vivo) {
            b.set("#Nombre" + slot + ".TextSpans", Message.raw("Sin compañero"));
            b.set("#Especie" + slot + ".TextSpans", Message.raw("—"));
            b.set("#Elemento" + slot + ".TextSpans", Message.raw("—"));
            b.set("#Hp" + slot + ".TextSpans", Message.raw("HP: —"));
            b.set("#Atk" + slot + ".TextSpans", Message.raw("ATK: —"));
            b.set("#Def" + slot + ".TextSpans", Message.raw("DEF: —"));
            b.set("#Spd" + slot + ".TextSpans", Message.raw("SPD: —"));
            b.set("#Wis" + slot + ".TextSpans", Message.raw("WIS: —"));
            b.set("#Lazo" + slot + ".TextSpans", Message.raw("Lazo: —"));
            b.set("#Abi" + slot + ".TextSpans", Message.raw("ABI: —"));
            b.set("#Vic" + slot + ".TextSpans", Message.raw("Victorias: —"));
            b.set("#Energia" + slot + ".TextSpans", Message.raw("Energia: —"));
            return;
        }
        b.set("#Nombre" + slot + ".TextSpans", Message.raw(d.nombre));
        b.set("#Especie" + slot + ".TextSpans", Message.raw(d.especie + " · " + d.nombreNivel()));
        b.set("#Elemento" + slot + ".TextSpans", Message.raw(d.elemento));
        b.set("#Hp" + slot + ".TextSpans", Message.raw("HP: " + d.hp + "/" + d.maxHp));
        b.set("#Atk" + slot + ".TextSpans", Message.raw("ATK: " + d.atk));
        b.set("#Def" + slot + ".TextSpans", Message.raw("DEF: " + d.def));
        b.set("#Spd" + slot + ".TextSpans", Message.raw("SPD: " + d.spd));
        b.set("#Wis" + slot + ".TextSpans", Message.raw("WIS: " + d.wis));
        b.set("#Lazo" + slot + ".TextSpans", Message.raw("Lazo: " + d.lazo + "/100"));
        b.set("#Abi" + slot + ".TextSpans", Message.raw("ABI: " + d.abi));
        b.set("#Vic" + slot + ".TextSpans", Message.raw("Victorias: " + d.victorias));
        b.set("#Energia" + slot + ".TextSpans", Message.raw("Energia: " + d.energia + "/100"));
    }

}
