package com.digitale.ui;

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

/**
 * Menú principal del Sapotama — ítem de mascotas.
 *
 * Botón 1 · Pasear   → Spawnea o despawnea los Digimon compañeros en el mundo.
 * Botón 2 · Batallar → Inicia combate contra un Digimon salvaje cercano.
 * Botón 3 · Equipo   → Abre el menú general de gestión de equipo (DigiMainMenuUI).
 */
public class SapotamaMenuUI extends InteractiveCustomUIPage<SapotamaMenuUI.Data> {

    // ── Codec ──────────────────────────────────────────────────────
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

    // ── Build ──────────────────────────────────────────────────────
    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder uiBuilder,
                      @Nonnull UIEventBuilder eventBuilder,
                      @Nonnull Store<EntityStore> store) {

        uiBuilder.append("Pages/SapotamaMenu.ui");

        // Inyectar estado actual (¿paseo activo?) para que la UI lo muestre
        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        String labelPaseo = (datos != null && datos.paseoActivo) ? "Recoger Compañeros" : "Pasear Compañeros";
        uiBuilder.set("#LblPaseo.Text", labelPaseo);

        // Nombre del equipo para mostrar en la UI
        if (datos != null && datos.tieneEquipo) {
            String nomA = datos.companeroA != null ? datos.companeroA.nombre : "–";
            String nomB = datos.companeroB != null ? datos.companeroB.nombre : "–";
            uiBuilder.set("#LblEquipo.Text", nomA + "  &  " + nomB);
        } else {
            uiBuilder.set("#LblEquipo.Text", "Sin companeros aun");
        }

        // Bind botones
        DigiUIHelper.bindClick(eventBuilder, "#BtnPasear",   "BtnPresionado", "pasear");
        DigiUIHelper.bindClick(eventBuilder, "#BtnBatallar", "BtnPresionado", "batallar");
        DigiUIHelper.bindClick(eventBuilder, "#BtnEquipo",   "BtnPresionado", "equipo");
        DigiUIHelper.bindClick(eventBuilder, "#BtnCerrar",   "BtnPresionado", "cerrar");
    }

    // ── Handle ─────────────────────────────────────────────────────
    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());

        switch (data.btnPresionado) {

            case "pasear" -> {
                if (datos == null || !datos.tieneEquipo) {
                    // No tiene equipo — redirigir al menú de inicio
                    player.getPageManager().openCustomPage(ref, store, new DigiStartMenuUI(playerRef));
                    return;
                }
                // Alternar paseo on/off
                if (datos.paseoActivo) {
                    SistemaPaseo.recogerCompaneros(playerRef, ref, store);
                    datos.paseoActivo = false;
                } else {
                    SistemaPaseo.spawnearCompaneros(playerRef, ref, store);
                    datos.paseoActivo = true;
                }
                // Refrescar UI para actualizar el label del botón
                player.getPageManager().openCustomPage(ref, store, new SapotamaMenuUI(playerRef));
            }

            case "batallar" -> {
                if (datos == null || !datos.tieneEquipo) {
                    player.getPageManager().openCustomPage(ref, store, new DigiStartMenuUI(playerRef));
                    return;
                }
                if (!datos.enCombateUI()) {
                    datos.combate = com.digitale.sistema.SistemaBatalla.iniciarCombate(
                        datos.companeroA, datos.companeroB);
                }
                // Activar HUD de batalla
                DigiBatallaHudManager.mostrar(playerRef, ref, store, datos);
                player.getPageManager().openCustomPage(ref, store, new DigiBatallaMenuUI(playerRef));
            }

            case "equipo" -> {
                player.getPageManager().openCustomPage(ref, store, new DigiMainMenuUI(playerRef));
            }

            case "cerrar" -> {
                player.getPageManager().setPage(ref, store, Page.None);
            }

            default -> sendUpdate(new UICommandBuilder(), null, false);
        }
    }
}
