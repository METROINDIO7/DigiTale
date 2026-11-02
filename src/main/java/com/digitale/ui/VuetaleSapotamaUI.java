package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import li.kelp.vuetale.app.PlayerUi;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Puente entre DigiTale y Vuetale.
 *
 * La pagina Vue vive en {@code src/ui/lib/pages/SapotamaPage.vue} y se compila a
 * {@code src/main/resources/vuetale/digitale/}. Este controlador empuja el estado a
 * Vue con {@link PlayerUi#setData(String, Object)} y recibe las acciones mediante un
 * host callback para ejecutar la logica en el WorldThread.
 */
public final class VuetaleSapotamaUI {

    private static final Logger LOGGER = Logger.getLogger(VuetaleSapotamaUI.class.getName());

    /** Debe coincidir con src/ui/lib/vuetale-plugin.json y ModuleRegistry.registerModule(...). */
    public static final String MODULE = "digitale";
    /** Nombre del componente en src/ui/lib/pages/SapotamaPage.vue. */
    public static final String PAGE = "SapotamaPage";
    /** Nombre del componente en src/ui/lib/pages/BatallaPage.vue. */
    public static final String PAGE_BATALLA = "BatallaPage";

    private VuetaleSapotamaUI() {
    }

    /**
     * Intenta abrir el menu de Sapotama con Vuetale.
     *
     * @return {@code true} si la pagina se abrio; {@code false} si Vuetale no esta
     *         disponible o el jugador aun no tiene equipo (para que el llamador use
     *         la UI nativa como fallback).
     */
    public static boolean abrir(@Nonnull PlayerRef playerRef,
                                @Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null || !datos.tieneEquipo) return false;

        try {
            PlayerUi ui = VuetaleNavegacion.abrir(playerRef, ref, store, MODULE, PAGE);
            new Controlador(playerRef, ref, store, ui).iniciar();
            LOGGER.log(Level.INFO, "Sapotama Vuetale abierto para " + playerRef.getUsername());
            return true;
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING,
                "No se pudo abrir la UI de Vuetale; se usara la UI nativa: " + t, t);
            return false;
        }
    }

    /**
     * Abre la seccion de batalla (cuarta seccion del menu).
     *
     * @return {@code true} si se abrio; {@code false} si Vuetale no esta
     *         disponible o el jugador aun no tiene equipo.
     */
    public static boolean abrirBatalla(@Nonnull PlayerRef playerRef,
                                       @Nonnull Ref<EntityStore> ref,
                                       @Nonnull Store<EntityStore> store) {
        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
        if (datos == null || !datos.tieneEquipo) return false;

        try {
            PlayerUi ui = VuetaleNavegacion.abrir(playerRef, ref, store, MODULE, PAGE_BATALLA);
            new Controlador(playerRef, ref, store, ui).iniciar();
            LOGGER.log(Level.INFO, "Batalla Vuetale abierta para " + playerRef.getUsername());
            return true;
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING,
                "No se pudo abrir la UI de batalla en Vuetale: " + t, t);
            return false;
        }
    }

    private static final class Controlador {

        private final PlayerRef playerRef;
        private final Ref<EntityStore> ref;
        private final Store<EntityStore> store;
        private final World world;
        private final PlayerUi ui;

        Controlador(@Nonnull PlayerRef playerRef,
                    @Nonnull Ref<EntityStore> ref,
                    @Nonnull Store<EntityStore> store,
                    @Nonnull PlayerUi ui) {
            this.playerRef = playerRef;
            this.ref = ref;
            this.store = store;
            this.world = store.getExternalData().getWorld();
            this.ui = ui;
        }

        void iniciar() {
            // Vuetale detecta este Consumer y lo expone a Vue como funcion host.
            ui.setData("onAction", (Consumer<String>) this::onAction);
            empujarTodo();
        }

        /** Se ejecuta en el hilo de V8: nunca tocar la API de Hytale aqui. */
        private void onAction(String payload) {
            try {
                world.execute(() -> procesar(payload));
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "No se pudo despachar la accion de Vuetale: " + t, t);
            }
        }

        /** Se ejecuta en el WorldThread. */
        private void procesar(String payload) {
            try {
                JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
                String action = json.has("action") ? json.get("action").getAsString() : "";
                int slot = 0;
                if (json.has("value") && !json.get("value").isJsonNull()) {
                    try {
                        slot = Integer.parseInt(json.get("value").getAsString());
                    } catch (NumberFormatException ignored) {
                        slot = 0;
                    }
                }
                if (!AlmacenJugadores.DatosJugador.slotValido(slot)) slot = 0;

                AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
                if (datos == null) return;

                switch (action) {
                    case "cerrar" -> {
                        VuetaleNavegacion.cerrar(ref, store);
                        return;
                    }
                    case "ver" -> {
                        DigiPerfilDigimonUI.open(playerRef, ref, store, slot);
                        return;
                    }
                    case "crianza", "ir_crianza" -> {
                        DigiCuidarMenuUI.open(playerRef, ref, store);
                        return;
                    }
                    case "ir_perfil" -> {
                        int activo = AlmacenJugadores.DatosJugador.slotValido(datos.activoA)
                            ? datos.activoA : 0;
                        DigiPerfilDigimonUI.open(playerRef, ref, store, activo);
                        return;
                    }
                    case "ir_batalla" -> {
                        abrirBatalla(playerRef, ref, store);
                        return;
                    }
                    case "volver", "ir_equipo" -> {
                        SapotamaTabUI.open(playerRef, ref, store);
                        return;
                    }
                    case "acta" -> {
                        if (datos.elegirActivoA(slot)) guardar(datos);
                    }
                    case "actb" -> {
                        if (datos.elegirActivoB(slot)) guardar(datos);
                    }
                    case "desafio" -> {
                        datos.desafioAbierto = !datos.desafioAbierto;
                        guardar(datos);
                    }
                    case "modo" -> {
                        datos.modoDesafio = "Amistoso".equals(datos.modoDesafio) ? "Serio" : "Amistoso";
                        guardar(datos);
                    }
                    default -> {
                        // accion desconocida: solo refrescar
                    }
                }

                empujarTodo();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error procesando accion de Vuetale: " + e.getMessage(), e);
            }
        }

        private void guardar(@Nonnull AlmacenJugadores.DatosJugador datos) {
            try {
                PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
                if (progress != null) progress.guardarDesde(datos);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "No se pudo guardar el estado del Digimon: " + e.getMessage());
            }
        }

        private void empujarTodo() {
            AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerRef.getUuid());
            if (datos == null) return;

            ui.setData("titulo", "DIGIMON COMPANEROS");
            ui.setData("slots", construirSlots(datos));
            ui.setData("crianza", construirCrianza(datos));
            ui.setData("crianzaAlerta", calcularAlerta(datos));
            ui.setData("batalla", construirBatalla(datos));
        }

        private static List<Map<String, Object>> construirSlots(
            @Nonnull AlmacenJugadores.DatosJugador datos) {
            List<Map<String, Object>> out = new ArrayList<>(AlmacenJugadores.DatosJugador.MAX_EQUIPO);
            for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
                DatoDigimon digi = datos.getSlot(i);
                boolean ocupado = digi != null;
                boolean esA = i == datos.activoA;
                boolean esB = i == datos.activoB;

                Map<String, Object> slot = new LinkedHashMap<>();
                slot.put("index", i);
                slot.put("ocupado", ocupado);
                slot.put("esA", esA);
                slot.put("esB", esB);
                if (ocupado) {
                    String marca = esA ? " [A]" : esB ? " [B]" : "";
                    slot.put("nombre", (i + 1) + ". " + digi.nombre + marca);
                    slot.put("info", digi.especie + " Nv" + digi.nivel
                        + " | HP " + digi.hp + "/" + digi.maxHp
                        + (digi.dormido ? " [Zzz]" : ""));
                    slot.put("imagen", DigiImagenes.rutaPara(digi.especie));
                } else {
                    slot.put("nombre", (i + 1) + ". ---");
                    slot.put("info", "vacio");
                    slot.put("imagen", null);
                }
                out.add(slot);
            }
            return out;
        }

        private static List<Map<String, Object>> construirCrianza(
            @Nonnull AlmacenJugadores.DatosJugador datos) {
            List<Map<String, Object>> out = new ArrayList<>(AlmacenJugadores.DatosJugador.MAX_EQUIPO);
            for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
                DatoDigimon digi = datos.getSlot(i);
                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("index", i);
                fila.put("vacio", digi == null);
                if (digi != null) {
                    fila.put("nombre", digi.nombre);
                    fila.put("hambre", digi.hambre);
                    fila.put("energia", digi.energia);
                    fila.put("lazo", digi.lazo);
                } else {
                    fila.put("nombre", "---");
                    fila.put("hambre", 0);
                    fila.put("energia", 0);
                    fila.put("lazo", 0);
                }
                out.add(fila);
            }
            return out;
        }

        private static String calcularAlerta(@Nonnull AlmacenJugadores.DatosJugador datos) {
            String alerta = null;
            int peorHambre = Integer.MAX_VALUE;
            for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
                DatoDigimon digi = datos.getSlot(i);
                if (digi != null && digi.hambre < peorHambre) {
                    peorHambre = digi.hambre;
                    alerta = digi.nombre;
                }
            }
            if (alerta != null && peorHambre < 30) {
                return "¡" + alerta + " necesita comida!";
            }
            return "";
        }

        private static Map<String, Object> construirBatalla(
            @Nonnull AlmacenJugadores.DatosJugador datos) {
            DatoDigimon a = datos.getA();
            DatoDigimon b = datos.getB();

            String activos = "Activos: A=" + (a != null ? a.nombre : "-")
                + "  B=" + (b != null ? b.nombre : "-");

            String info = "A: " + (a != null ? a.nombre + " (HP:" + a.hp + "/" + a.maxHp + ")" : "-")
                + "  |  B: " + (b != null ? b.nombre + " (HP:" + b.hp + "/" + b.maxHp + ")" : "-");

            String reto = datos.desafioDeNombre == null ? "" : datos.desafioDeNombre;
            if (!reto.isEmpty()) {
                info = "¡RETO de " + reto + "! Golpealo para aceptar.  |  " + info;
            }

            Map<String, Object> batalla = new LinkedHashMap<>();
            batalla.put("desafioAbierto", datos.desafioAbierto);
            batalla.put("modo", datos.modoDesafio);
            batalla.put("activos", activos);
            batalla.put("info", info);
            batalla.put("retoDe", reto);
            return batalla;
        }
    }
}
