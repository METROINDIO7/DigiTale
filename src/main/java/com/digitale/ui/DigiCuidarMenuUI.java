package com.digitale.ui;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.digitale.ui.DigiUIHelper;
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

public class DigiCuidarMenuUI extends InteractiveCustomUIPage<DigiCuidarMenuUI.Data> {

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

    public DigiCuidarMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiBuilder,
                      @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store<EntityStore> store) {

        uiBuilder.append("Pages/DigiCuidarMenu.ui");
        actualizarEstados(uiBuilder);

        DigiUIHelper.bindClick(eventBuilder, "#BtnAlimentarA", "@Accion", "alimentar_a");
        DigiUIHelper.bindClick(eventBuilder, "#BtnAlimentarB", "@Accion", "alimentar_b");
        DigiUIHelper.bindClick(eventBuilder, "#BtnMimarA", "@Accion", "mimar_a");
        DigiUIHelper.bindClick(eventBuilder, "#BtnMimarB", "@Accion", "mimar_b");
        DigiUIHelper.bindClick(eventBuilder, "#BtnDescansarAB", "@Accion", "descansar_ab");
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

        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        String resultado = aplicarCuidado(data.accion, datos);

        UICommandBuilder b = new UICommandBuilder();
        b.set("#Resultado.TextSpans", Message.raw(resultado));
        actualizarEstados(b);
        sendUpdate(b);
    }

    private String aplicarCuidado(String accion, DatosJugador datos) {
        return switch (accion) {
            case "alimentar_a" -> cuidar(datos.companeroA, "alimentar");
            case "alimentar_b" -> cuidar(datos.companeroB, "alimentar");
            case "mimar_a"     -> cuidar(datos.companeroA, "mimar");
            case "mimar_b"     -> cuidar(datos.companeroB, "mimar");
            case "descansar_ab" -> {
                String r = cuidar(datos.companeroA, "descansar") + "  " +
                           cuidar(datos.companeroB, "descansar");
                yield r;
            }
            default -> "";
        };
    }

    private String cuidar(DatoDigimon d, String accion) {
        if (d == null || !d.vivo) return "Sin compañero";
        return switch (accion) {
            case "alimentar" -> {
                d.alimentar(30);
                yield d.nombre + ": Hambre " + d.hambre + "/100";
            }
            case "mimar" -> {
                d.interactuar();
                yield d.nombre + ": Lazo " + d.lazo + "/100";
            }
            case "descansar" -> {
                d.descansar();
                yield d.nombre + ": HP " + d.hp + "/" + d.maxHp + " EN " + d.energia;
            }
            default -> "";
        };
    }

    private void actualizarEstados(UICommandBuilder b) {
        DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
        String textA = datos.companeroA != null
            ? "A - Hambre: " + datos.companeroA.hambre + "/100  |  Energia: " + datos.companeroA.energia + "/100"
            : "A - Sin compañero";
        String textB = datos.companeroB != null
            ? "B - Hambre: " + datos.companeroB.hambre + "/100  |  Energia: " + datos.companeroB.energia + "/100"
            : "B - Sin compañero";
        b.set("#EstadoA.TextSpans", Message.raw(textA));
        b.set("#EstadoB.TextSpans", Message.raw(textB));
    }
}
