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

/**
 * UI de bienvenida para elegir los 2 Digimon baby iniciales.
 */
public class DigiStartMenuUI extends InteractiveCustomUIPage<DigiStartMenuUI.Data> {

    public static class Data {
        public static final BuilderCodec<Data> CODEC = BuilderCodec
            .builder(Data.class, Data::new)
            .append(new KeyedCodec<>("@Seleccion", Codec.STRING),
                    (data, v) -> data.seleccion = v,
                    data -> data.seleccion)
            .add()
            .build();
        public String seleccion = "";
    }

    private final PlayerRef playerRef;
    private String slot1 = "";  // primera elección
    private String slot2 = "";  // segunda elección

    public DigiStartMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CantClose, Data.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiBuilder,
                      @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store<EntityStore> store) {

        uiBuilder.append("Pages/DigiStartMenu.ui");

        // 6 bebés disponibles
        for (String bebe : new String[]{"Botamon","Punimon","Poyomon","Yuramon","Pichimon","Nyokimon"}) {
            DigiUIHelper.bindClick(eventBuilder, "#Btn" + bebe, "@Seleccion", bebe);
        }
        DigiUIHelper.bindClick(eventBuilder, "#BtnConfirmar", "@Seleccion", "confirmar");
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);

        if ("confirmar".equals(data.seleccion)) {
            if (slot1.isEmpty() || slot2.isEmpty()) {
                UICommandBuilder b = new UICommandBuilder();
                b.set("#MsgInfo.TextSpans", Message.raw("Elige 2 compañeros diferentes."));
                sendUpdate(b);
                return;
            }

            // Crear el equipo
            DatosJugador datos = AlmacenJugadores.obtener(playerRef.getUuid());
            datos.companeroA = DatoDigimon.crearInicial(slot1);
            datos.companeroB = DatoDigimon.crearInicial(slot2);
            datos.tieneEquipo = true;

            // Abrir menú principal
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player != null)
                player.getPageManager().openCustomPage(ref, store,
                    new DigiMainMenuUI(playerRef));
            return;
        }

        // Seleccionar bebé
        String bebe = data.seleccion;
        if (!bebe.isEmpty()) {
            if (slot1.isEmpty()) {
                slot1 = bebe;
            } else if (!bebe.equals(slot1)) {
                slot2 = bebe;
            }

            UICommandBuilder b = new UICommandBuilder();
            String msg = slot1.isEmpty() ? "Elige tu primer compañero"
                       : slot2.isEmpty() ? "Elegiste: " + slot1 + " — ahora elige el segundo"
                       : "Equipo: " + slot1 + " + " + slot2 + " — pulsa Confirmar";
            b.set("#MsgInfo.TextSpans", Message.raw(msg));
            b.set("#Seleccion1.TextSpans", Message.raw(slot1.isEmpty() ? "—" : slot1));
            b.set("#Seleccion2.TextSpans", Message.raw(slot2.isEmpty() ? "—" : slot2));
            sendUpdate(b);
        } else {
            sendUpdate();
        }
    }
}
