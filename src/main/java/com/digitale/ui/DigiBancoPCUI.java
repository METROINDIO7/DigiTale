package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
import com.digitale.sistema.SistemaPaseo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PC Digital: gestiona dónde va cada Digimon (equipo <-> caja).
 * Estética propia (cian sobre azul oscuro), distinta al Reclamo.
 */
public class DigiBancoPCUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(DigiBancoPCUI.class.getName());
    private static final int FILAS_BANCO = 8;

    private String msgPendiente = "";

    public DigiBancoPCUI(@Nonnull PlayerRef playerRef) {
        super(playerRef, CustomPageLifetime.CanDismiss);
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;
            player.getPageManager().openCustomPage(ref, store, new DigiBancoPCUI(playerRef));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error abriendo PC: " + e.getMessage());
        }
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder event,
                      @Nonnull Store<EntityStore> store) {
        cmd.append("DigiBancoPC.ui");
        for (int i = 0; i < DatosJugador.MAX_EQUIPO; i++) {
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnPDep" + i,
                EventData.of("action", "depositar").append("value", String.valueOf(i)), false);
        }
        for (int i = 0; i < FILAS_BANCO; i++) {
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnPRet" + i,
                EventData.of("action", "retirar").append("value", String.valueOf(i)), false);
        }
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnPCCerrar",
            EventData.of("action", "cerrar"), false);
        actualizar(cmd, ref, store);
    }

    private void actualizar(UICommandBuilder cmd, Ref<EntityStore> ref, Store<EntityStore> store) {
        DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        int nb = (d != null) ? d.banco.size() : 0;
        cmd.set("#LblPCCount.Text", "Equipo " + (d != null ? d.contarEquipo() : 0)
            + "/6  |  Caja " + nb + "/" + DatosJugador.BANCO_MAX);
        for (int i = 0; i < DatosJugador.MAX_EQUIPO; i++) {
            DatoDigimon digi = (d != null) ? d.getSlot(i) : null;
            if (digi != null) {
                cmd.set("#LblPEq" + i + ".Text",
                    (i + 1) + ". " + digi.nombre + " (" + digi.especie + ") Nv" + digi.nivel);
                cmd.set("#LblPEq" + i + ".Visible", true);
                String retrato = DigiImagenes.rutaPara(digi.especie);
                if (retrato != null) {
                    cmd.set("#ImgPEq" + i + ".AssetPath", retrato);
                    cmd.set("#ImgPEq" + i + ".Visible", true);
                } else {
                    cmd.set("#ImgPEq" + i + ".Visible", false);
                }
                cmd.set("#BtnPDep" + i + ".Visible", true);
            } else {
                cmd.set("#LblPEq" + i + ".Text", (i + 1) + ". -");
                cmd.set("#LblPEq" + i + ".Visible", true);
                cmd.set("#ImgPEq" + i + ".Visible", false);
                cmd.set("#BtnPDep" + i + ".Visible", false);
            }
        }
        for (int i = 0; i < FILAS_BANCO; i++) {
            if (d != null && i < nb) {
                DatoDigimon b = d.banco.get(i);
                String extra = (i == FILAS_BANCO - 1 && nb > FILAS_BANCO)
                    ? " (+ " + (nb - FILAS_BANCO) + " mas)" : "";
                cmd.set("#LblPB" + i + ".Text", b.nombre + " (" + b.especie + ") Nv" + b.nivel + extra);
                cmd.set("#LblPB" + i + ".Visible", true);
                String retrato = DigiImagenes.rutaPara(b.especie);
                if (retrato != null) {
                    cmd.set("#ImgPB" + i + ".AssetPath", retrato);
                    cmd.set("#ImgPB" + i + ".Visible", true);
                } else {
                    cmd.set("#ImgPB" + i + ".Visible", false);
                }
                cmd.set("#BtnPRet" + i + ".Visible", true);
            } else {
                cmd.set("#LblPB" + i + ".Text", "");
                cmd.set("#LblPB" + i + ".Visible", false);
                cmd.set("#ImgPB" + i + ".Visible", false);
                cmd.set("#BtnPRet" + i + ".Visible", false);
            }
        }
        if (!msgPendiente.isEmpty()) {
            cmd.set("#LblPCMsg.Text", msgPendiente);
            msgPendiente = "";
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull String jsonData) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String action = json.has("action") ? json.get("action").getAsString() : "";
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null) return;

        switch (action) {
            case "depositar" -> {
                int slot = parseIdx(json);
                DatoDigimon sale = datos.getSlot(slot);
                if (sale == null) {
                    rebuild();
                    return;
                }
                if (datos.banco.size() >= DatosJugador.BANCO_MAX) {
                    msgPendiente = "Caja llena (100/100).";
                    rebuild();
                    return;
                }
                if (datos.paseoEn(slot)) {
                    SistemaPaseo.despawnearUno(store, datos.refsPaseo[slot]);
                    datos.setRefPaseo(slot, null);
                    datos.paseoActivo = datos.algunPaseo();
                }
                datos.setSlot(slot, null);
                if (datos.activoA == slot) datos.activoA = 0;
                if (datos.activoB == slot) datos.activoB = 1;
                sale.dormido = false;
                sale.dormidoEnSeg = 0;
                datos.banco.add(sale);
                if (datos.contarEquipo() == 0) datos.tieneEquipo = false;
                guardar(store, ref, datos);
                msgPendiente = sale.nombre + " guardado en la Caja (" + datos.banco.size() + "/100).";
                rebuild();
            }
            case "retirar" -> {
                int cual = parseIdx(json);
                if (cual < 0 || cual >= datos.banco.size()) {
                    rebuild();
                    return;
                }
                if (!datos.tieneEspacio()) {
                    msgPendiente = "Equipo lleno (6/6).";
                    rebuild();
                    return;
                }
                DatoDigimon b = datos.banco.remove(cual);
                int s = datos.agregarAlEquipo(b);
                if (s < 0) {
                    datos.banco.add(cual, b);
                    msgPendiente = "Equipo lleno (6/6).";
                    rebuild();
                    return;
                }
                guardar(store, ref, datos);
                msgPendiente = b.nombre + " al equipo (slot " + (s + 1) + ").";
                rebuild();
            }
            case "cerrar" -> player.getPageManager().setPage(ref, store,
                com.hypixel.hytale.protocol.packets.interface_.Page.None);
            default -> {
                UICommandBuilder cmd = new UICommandBuilder();
                sendUpdate(cmd);
            }
        }
    }

    private static int parseIdx(JsonObject json) {
        try {
            return Integer.parseInt(json.has("value") ? json.get("value").getAsString() : "-1");
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void guardar(Store<EntityStore> store, Ref<EntityStore> ref, DatosJugador datos) {
        try {
            PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
            if (progress != null) progress.guardarDesde(datos);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo guardar PC: " + e.getMessage());
        }
    }
}
