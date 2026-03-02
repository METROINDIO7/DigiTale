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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Menú principal del Sapotama — ítem de mascotas.
 *
 * Botón 1 · Pasear   → Spawnea o despawnea los Digimon compañeros en el mundo.
 * Botón 2 · Batallar → Inicia combate contra un Digimon salvaje cercano.
 * Botón 3 · Equipo   → Abre el menú general de gestión de equipo (DigiMainMenuUI).
 */
public class SapotamaMenuUI extends InteractiveCustomUIPage<SapotamaMenuUI.Data> {
    private static final Logger LOGGER = Logger.getLogger(SapotamaMenuUI.class.getName());

    // ── Campos para rastrear la selección de Digimon (sin equipo) ────
    private String slot1 = "";  // primera selección
    private String slot2 = "";  // segunda selección

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

        LOGGER.log(Level.INFO, "SapotamaMenuUI.build() iniciado para: " + playerRef.getUuid());
        
        // Obtener datos del jugador
        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        LOGGER.log(Level.INFO, "Datos del jugador obtenidos: " + (datos != null ? "no nulo" : "nulo"));
        
        if (datos != null) {
            LOGGER.log(Level.INFO, "  tieneEquipo: " + datos.tieneEquipo);
        }
        
        // Si no tiene equipo, cargar la UI de selección DIRECTAMENTE sin redirigir
        if (datos == null || !datos.tieneEquipo) {
            LOGGER.log(Level.INFO, "Sin equipo - cargando UI de selección");
            uiBuilder.append("Pages/DigiStartMenu.ui");
            
            // Bind los botones de selección
            for (String bebe : new String[]{"Botamon","Punimon","Poyomon","Yuramon","Pichimon","Nyokimon"}) {
                DigiUIHelper.bindClick(eventBuilder, "#Btn" + bebe, "BtnPresionado", "elegir_" + bebe);
            }
            DigiUIHelper.bindClick(eventBuilder, "#BtnConfirmar", "BtnPresionado", "confirmar");
            return;
        }
        
        // Tiene equipo - cargar la UI normal de Sapotama
        LOGGER.log(Level.INFO, "Con equipo - cargando UI normal de Sapotama");
        
        if (datos.companeroA != null) LOGGER.log(Level.INFO, "  companeroA: " + datos.companeroA.nombre);
        if (datos.companeroB != null) LOGGER.log(Level.INFO, "  companeroB: " + datos.companeroB.nombre);
        
        uiBuilder.append("Pages/SapotamaMenu.ui");

        // Inyectar estado actual (¿paseo activo?) para que la UI lo muestre
        String labelPaseo = (datos.paseoActivo) ? "Recoger Compañeros" : "Pasear Compañeros";
        uiBuilder.set("#LblPaseo.Text", labelPaseo);

        // Nombre del equipo para mostrar en la UI
        String nomA = datos.companeroA != null ? datos.companeroA.nombre : "–";
        String nomB = datos.companeroB != null ? datos.companeroB.nombre : "–";
        uiBuilder.set("#LblEquipo.Text", nomA + "  &  " + nomB);

        // Bind botones
        DigiUIHelper.bindClick(eventBuilder, "#BtnPasear",   "BtnPresionado", "pasear");
        DigiUIHelper.bindClick(eventBuilder, "#BtnBatallar", "BtnPresionado", "batallar");
        DigiUIHelper.bindClick(eventBuilder, "#BtnEquipo",   "BtnPresionado", "equipo");
        DigiUIHelper.bindClick(eventBuilder, "#BtnCerrar",   "BtnPresionado", "cerrar");
        
        LOGGER.log(Level.INFO, "SapotamaMenuUI.build() completado exitosamente");
    }

    // ── Handle ─────────────────────────────────────────────────────
    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull Data data) {
        super.handleDataEvent(ref, store, data);

        LOGGER.log(Level.INFO, "SapotamaMenuUI.handleDataEvent - acción: " + data.btnPresionado);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) {
            LOGGER.log(Level.WARNING, "Player es null en handleDataEvent");
            return;
        }

        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());

        // ─── Manejar acciones de SELECCIÓN de Digimon (cuando no tiene equipo)
        if (data.btnPresionado.startsWith("elegir_")) {
            // Extraer el nombre del Digimon elegido
            String bebeName = data.btnPresionado.substring(7); // "elegir_Botamon" -> "Botamon"
            if (datos == null) {
                LOGGER.log(Level.INFO, "Creando datos del jugador para primera vez");
                datos = AlmacenJugadores.obtener(playerRef.getUuid());
            }
            
            // Guardar la selección
            if (slot1.isEmpty()) {
                slot1 = bebeName;
                LOGGER.log(Level.INFO, "Slot 1 = " + slot1);
            } else if (!bebeName.equals(slot1) && slot2.isEmpty()) {
                slot2 = bebeName;
                LOGGER.log(Level.INFO, "Slot 2 = " + slot2);
            } else if (bebeName.equals(slot1)) {
                // Deseleccionar slot 1
                slot1 = "";
                LOGGER.log(Level.INFO, "Deseleccionado slot 1");
            } else {
                // Deseleccionar slot 2
                slot2 = "";
                LOGGER.log(Level.INFO, "Deseleccionado slot 2");
            }
            
            // Actualizar UI para mostrar selecciones
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
            // Confirmar selección de Digimon iniciales
            LOGGER.log(Level.INFO, "Confirmando selección: " + slot1 + " + " + slot2);
            
            if (slot1.isEmpty() || slot2.isEmpty()) {
                UICommandBuilder b = new UICommandBuilder();
                b.set("#MsgInfo.Text", "¡Elige 2 companeros diferentes!");
                sendUpdate(b, null, false);
                return;
            }
            
            // Crear los Digimon y asignarlos
            if (datos == null) {
                datos = AlmacenJugadores.obtener(playerRef.getUuid());
            }
            datos.companeroA = DatoDigimon.crearInicial(slot1);
            datos.companeroB = DatoDigimon.crearInicial(slot2);
            datos.tieneEquipo = true;
            
            LOGGER.log(Level.INFO, "Equipo creado: " + slot1 + " y " + slot2);
            LOGGER.log(Level.INFO, "Reabriendo UI de Sapotama con equipo");
            
            // Recargar el UI para que muestre el SapotamaMenu.ui cuando ya tenga datos
            player.getPageManager().openCustomPage(ref, store, new SapotamaMenuUI(playerRef));
            return;
        }

        // ─── Manejar acciones NORMALES de Sapotama (cuando SÍ tiene equipo)
        if (datos == null || !datos.tieneEquipo) {
            LOGGER.log(Level.WARNING, "Acción " + data.btnPresionado + " en UI sin equipo");
            return;
        }

        switch (data.btnPresionado) {

            case "pasear" -> {
                LOGGER.log(Level.INFO, "Acción: Pasear/Recoger");
                Player playerComponent = store.getComponent(ref, Player.getComponentType());
                
                if (datos.paseoActivo) {
                    // Recogiendo compañeros
                    SistemaPaseo.recogerCompaneros(playerRef, ref, store);
                    datos.paseoActivo = false;
                    LOGGER.log(Level.INFO, "Compañeros recogidos");
                    if (playerComponent != null) {
                        playerComponent.getPageManager().setPage(ref, store, Page.None);
                    }
                } else {
                    // Intentando spawnear - prepara datos para que aparezcan tus compañeros
                    LOGGER.log(Level.INFO, "Preparando spawn de compañeros");
                    SistemaPaseo.spawnearCompaneros(playerRef, ref, store);
                    
                    // Cerrar UI - los compañeros deben aparecer automáticamente
                    if (playerComponent != null) {
                        playerComponent.getPageManager().setPage(ref, store, Page.None);
                    }
                    datos.paseoActivo = true;
                    LOGGER.log(Level.INFO, "Spawn iniciado - tus compañeros aparecerán en breve");
                }
            }

            case "batallar" -> {
                LOGGER.log(Level.INFO, "Acción: Batallar");
                if (!datos.enCombateUI()) {
                    datos.combate = com.digitale.sistema.SistemaBatalla.iniciarCombate(
                        datos.companeroA, datos.companeroB);
                }
                // Activar HUD de batalla
                DigiBatallaHudManager.mostrar(playerRef, ref, store, datos);
                player.getPageManager().openCustomPage(ref, store, new DigiBatallaMenuUI(playerRef));
            }

            case "equipo" -> {
                LOGGER.log(Level.INFO, "Acción: Ver Equipo");
                player.getPageManager().openCustomPage(ref, store, new DigiMainMenuUI(playerRef));
            }

            case "cerrar" -> {
                LOGGER.log(Level.INFO, "Acción: Cerrar UI");
                player.getPageManager().setPage(ref, store, Page.None);
            }

            default -> {
                LOGGER.log(Level.WARNING, "Acción desconocida: " + data.btnPresionado);
                sendUpdate(new UICommandBuilder(), null, false);
            }
        }
    }
}
