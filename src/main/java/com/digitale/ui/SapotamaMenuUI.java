package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.sistema.SistemaPaseo;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SapotamaMenuUI extends InteractiveCustomUIPage<SapotamaMenuUI.Data> {
    private static final Logger LOGGER = Logger.getLogger(SapotamaMenuUI.class.getName());

    private String slot1 = "";
    private String slot2 = "";

    public static class Data {
        public String btnPresionado = "";

        public static final BuilderCodec<Data> CODEC = BuilderCodec
                .builder(Data.class, Data::new)
                .append(new KeyedCodec<>("BtnPresionado", Codec.STRING),
                        (d, v) -> d.btnPresionado = v,
                        d -> d.btnPresionado)
                .add()
                .build();
    }

    private final PlayerRef playerRef;

    public SapotamaMenuUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.playerRef = playerRef;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiBuilder,
                      @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store<EntityStore> store) {

        LOGGER.log(Level.INFO, "SapotamaMenuUI.build() iniciado para: " + playerRef.getUuid());

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        LOGGER.log(Level.INFO, "Datos del jugador obtenidos: " + (datos != null ? "no nulo" : "nulo"));
        if (datos != null) LOGGER.log(Level.INFO, "  tieneEquipo: " + datos.tieneEquipo);

        if (datos == null || !datos.tieneEquipo) {
            LOGGER.log(Level.INFO, "Sin equipo - cargando UI de seleccion");
            uiBuilder.append("Pages/DigiStartMenu.ui");
            for (String bebe : new String[]{"Botamon","Punimon","Poyomon","Yuramon","Pichimon","Nyokimon"}) {
                DigiUIHelper.bindClick(eventBuilder, "#Btn" + bebe, "BtnPresionado", "elegir_" + bebe);
            }
            DigiUIHelper.bindClick(eventBuilder, "#BtnConfirmar", "BtnPresionado", "confirmar");
            return;
        }

        LOGGER.log(Level.INFO, "Con equipo - cargando UI normal de Sapotama");
        if (datos.companeroA != null) LOGGER.log(Level.INFO, "  companeroA: " + datos.companeroA.nombre);
        if (datos.companeroB != null) LOGGER.log(Level.INFO, "  companeroB: " + datos.companeroB.nombre);

        uiBuilder.append("Pages/SapotamaMenu.ui");

        String labelPaseo = datos.paseoActivo ? "Recoger Companeros" : "Pasear Companeros";
        uiBuilder.set("#LblPaseo.Text", labelPaseo);

        String nomA = datos.companeroA != null ? datos.companeroA.nombre : "-";
        String nomB = datos.companeroB != null ? datos.companeroB.nombre : "-";
        uiBuilder.set("#LblEquipo.Text", nomA + "  &  " + nomB);

        DigiUIHelper.bindClick(eventBuilder, "#BtnPasear",   "BtnPresionado", "pasear");
        DigiUIHelper.bindClick(eventBuilder, "#BtnBatallar", "BtnPresionado", "batallar");
        DigiUIHelper.bindClick(eventBuilder, "#BtnEquipo",   "BtnPresionado", "equipo");
        DigiUIHelper.bindClick(eventBuilder, "#BtnCerrar",   "BtnPresionado", "cerrar");

        LOGGER.log(Level.INFO, "SapotamaMenuUI.build() completado exitosamente");
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);

        LOGGER.log(Level.INFO, "SapotamaMenuUI.handleDataEvent - accion: " + data.btnPresionado);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            LOGGER.log(Level.WARNING, "Player es null en handleDataEvent");
            return;
        }

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());

        // ── Seleccion de Digimon (sin equipo) ──────────────────────
        if (data.btnPresionado.startsWith("elegir_")) {
            String bebeName = data.btnPresionado.substring(7);
            if (datos == null) datos = AlmacenJugadores.obtener(playerRef.getUuid());

            if (slot1.isEmpty()) {
                slot1 = bebeName;
                LOGGER.log(Level.INFO, "Slot 1 = " + slot1);
            } else if (!bebeName.equals(slot1) && slot2.isEmpty()) {
                slot2 = bebeName;
                LOGGER.log(Level.INFO, "Slot 2 = " + slot2);
            } else if (bebeName.equals(slot1)) {
                slot1 = "";
            } else {
                slot2 = "";
            }

            UICommandBuilder b = new UICommandBuilder();
            String msgInfo = slot1.isEmpty() ? "Elige tu primer companero"
                    : slot2.isEmpty() ? "Elegiste: " + slot1 + " - ahora elige el segundo"
                    : "Equipo: " + slot1 + " + " + slot2 + " - pulsa Confirmar";
            b.set("#MsgInfo.Text", msgInfo);
            b.set("#Seleccion1.Text", slot1.isEmpty() ? "-" : slot1);
            b.set("#Seleccion2.Text", slot2.isEmpty() ? "-" : slot2);
            sendUpdate(b, null, false);
            return;
        }

        if (data.btnPresionado.equals("confirmar")) {
            LOGGER.log(Level.INFO, "Confirmando seleccion: " + slot1 + " + " + slot2);

            if (slot1.isEmpty() || slot2.isEmpty()) {
                UICommandBuilder b = new UICommandBuilder();
                b.set("#MsgInfo.Text", "Elige 2 companeros diferentes!");
                sendUpdate(b, null, false);
                return;
            }

            if (datos == null) datos = AlmacenJugadores.obtener(playerRef.getUuid());
            datos.companeroA = DatoDigimon.crearInicial(slot1);
            datos.companeroB = DatoDigimon.crearInicial(slot2);
            datos.tieneEquipo = true;

            // ── GUARDAR en el componente persistente ──────────────
            PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
            progress.guardarDesde(datos);
            LOGGER.log(Level.INFO, "Equipo guardado en componente persistente: " + slot1 + " + " + slot2);

            player.getPageManager().openCustomPage(ref, store, new SapotamaMenuUI(playerRef));
            return;
        }

        // ── Acciones normales (con equipo) ─────────────────────────
        if (datos == null || !datos.tieneEquipo) {
            LOGGER.log(Level.WARNING, "Accion " + data.btnPresionado + " en UI sin equipo");
            return;
        }

        switch (data.btnPresionado) {

            case "pasear" -> {
                LOGGER.log(Level.INFO, "Accion: Pasear/Recoger");
                Player playerComponent = store.getComponent(ref, Player.getComponentType());

                if (datos.paseoActivo) {
                    SistemaPaseo.recogerCompaneros(playerRef, ref, store);
                    datos.paseoActivo = false;
                    LOGGER.log(Level.INFO, "Companeros recogidos");
                } else {
                    LOGGER.log(Level.INFO, "Preparando spawn de companeros");
                    SistemaPaseo.spawnearCompaneros(playerRef, ref, store);
                    datos.paseoActivo = true;
                    LOGGER.log(Level.INFO, "Spawn iniciado");
                }

                // ── GUARDAR estado de paseo ───────────────────────
                PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
                progress.guardarDesde(datos);

                if (playerComponent != null) {
                    playerComponent.getPageManager().setPage(ref, store, Page.None);
                }
            }

            case "batallar" -> {
                LOGGER.log(Level.INFO, "Accion: Batallar");
                if (!datos.enCombateUI()) {
                    datos.combate = com.digitale.sistema.SistemaBatalla.iniciarCombate(
                            datos.companeroA, datos.companeroB);
                }

                player.getPageManager().openCustomPage(ref, store, new DigiBatallaMenuUI(playerRef));
            }

            case "equipo" -> {
                LOGGER.log(Level.INFO, "Accion: Ver Equipo");
                player.getPageManager().openCustomPage(ref, store, new DigiMainMenuUI(playerRef));
            }

            case "cerrar" -> {
                LOGGER.log(Level.INFO, "Accion: Cerrar UI");
                player.getPageManager().setPage(ref, store, Page.None);
            }

            default -> {
                LOGGER.log(Level.WARNING, "Accion desconocida: " + data.btnPresionado);
                sendUpdate(new UICommandBuilder(), null, false);
            }
        }
    }
}