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

/**
 * UI de entrenamiento. Un click = 1 sesión en ese stat para A+B.
 */
public class DigiEntrenarMenuUI extends InteractiveCustomUIPage<DigiEntrenarMenuUI.Data> {

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

    public DigiEntrenarMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiBuilder,
                      @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store<EntityStore> store) {

        uiBuilder.append("DigiEntrenarMenu.ui");
        actualizarEnergias(uiBuilder);

        // Cada botón manda su stat como accion
        for (String stat : new String[]{"atk", "def", "spd", "wis", "hp"}) {
            DigiUIHelper.bindClick(eventBuilder, "#Btn" + capitalizar(stat), "@Accion", "stat_" + stat);
        }
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
            return;
        }

        if (data.accion.startsWith("stat_")) {
            String stat = data.accion.substring(5);
            DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
            StringBuilder msg = new StringBuilder();

            if (datos.companeroA != null && datos.companeroA.vivo) {
                boolean ok = datos.companeroA.entrenar(stat, 1);
                msg.append(ok ? datos.companeroA.nombre + " entrenó " + stat.toUpperCase() + "  "
                              : datos.companeroA.nombre + " sin energía  ");
            }
            if (datos.companeroB != null && datos.companeroB.vivo) {
                boolean ok = datos.companeroB.entrenar(stat, 1);
                msg.append(ok ? datos.companeroB.nombre + " entrenó " + stat.toUpperCase()
                              : datos.companeroB.nombre + " sin energía");
            }

            // Actualizar resultado y energías
            UICommandBuilder b = new UICommandBuilder();
            b.set("#Resultado.TextSpans", Message.raw(msg.toString()));
            actualizarEnergias(b);
            sendUpdate(b, false);
        } else {
            sendUpdate(new UICommandBuilder(), false);
        }
    }

    private void actualizarEnergias(UICommandBuilder b) {
        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        String enA = datos.companeroA != null ? String.valueOf(datos.companeroA.energia) : "?";
        String enB = datos.companeroB != null ? String.valueOf(datos.companeroB.energia) : "?";
        b.set("#LabelEnergias.TextSpans",
            Message.raw("Energia A: " + enA + "/100  |  Energia B: " + enB + "/100"));
    }

    private String capitalizar(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
