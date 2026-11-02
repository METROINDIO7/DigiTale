package com.digitale.ui;

import com.digitale.componentes.ComponentRegistry;
import com.digitale.componentes.PetProgressComponent;
import com.digitale.crianza.DigitamaDef;
import com.digitale.crianza.Incubacion;
import com.digitale.crianza.SistemaIncubacion;
import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.AlmacenJugadores.DatosJugador;
import com.digitale.datos.DatoDigimon;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reclamo de crías (token Huevo_Incubando_Huevo_Bebe): muestra la cría del
 * token con Equipo / Banco PC / Liberar + tab Banco PC (al equipo / liberar).
 * El token se consume al reclamar.
 */
public class DigiMesaReclamoUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(DigiMesaReclamoUI.class.getName());
    private static final Random RNG = new Random();
    private static final int MAX_BANCO = 8;

    private String msgPendiente = "";
    private boolean animado = false;
    private int tab = 0;
    /** Token en mano: "Digitama_X" + bebe + variante (receta no exacta). Null = solo ver banco. */
    private final String tokenHuevo;
    private final String tokenBebe;
    private final boolean tokenVariante;

    public DigiMesaReclamoUI(@Nonnull PlayerRef playerRef, String tokenHuevo, String tokenBebe, boolean tokenVariante) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.tokenHuevo = tokenHuevo;
        this.tokenBebe = tokenBebe;
        this.tokenVariante = tokenVariante;
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store,
                            String tokenHuevo, String tokenBebe, boolean tokenVariante) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;
            player.getPageManager().openCustomPage(ref, store,
                new DigiMesaReclamoUI(playerRef, tokenHuevo, tokenBebe, tokenVariante));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error abriendo reclamo: " + e.getMessage());
        }
    }

    /** Abrir directo en el tab banco (sin token). */
    public static void openBanco(@Nonnull PlayerRef playerRef,
                                 @Nonnull Ref<EntityStore> ref,
                                 @Nonnull Store<EntityStore> store) {
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;
            DigiMesaReclamoUI page = new DigiMesaReclamoUI(playerRef, null, null, false);
            page.tab = 1;
            player.getPageManager().openCustomPage(ref, store, page);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error abriendo banco: " + e.getMessage());
        }
    }

    /** % del token actual (100 conocida exacta; variante -25; experimento por tier). */
    private int pctToken(DatosJugador d) {
        if (tokenHuevo == null || tokenBebe == null) return 0;
        DigitamaDef def = SistemaIncubacion.porItem(tokenHuevo);
        boolean conocida = d != null && d.tieneReceta(tokenBebe);
        int pct;
        if (conocida) {
            pct = 100;
        } else {
            int base = 30;
            if (def != null) {
                String t = def.tier != null ? def.tier.trim().toLowerCase(java.util.Locale.ROOT) : "";
                if (t.startsWith("bas")) base = 60;
                else if (t.startsWith("med")) base = 40;
                else if (t.startsWith("avan")) base = 25;
                else if (t.startsWith("legen")) base = 12;
            }
            pct = Math.min(95, Math.max(5, base));
        }
        if (tokenVariante) pct = Math.max(5, pct - 25);
        return pct;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder event,
                      @Nonnull Store<EntityStore> store) {
        cmd.append("DigiMesaReclamo.ui");
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnTabListas",
            EventData.of("action", "tablistas"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnTabBanco",
            EventData.of("action", "tabbanco"), false);
        for (int i = 0; i < 1; i++) {
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnEq" + i,
                EventData.of("action", "equipo"), false);
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnBk" + i,
                EventData.of("action", "banco"), false);
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnRel" + i,
                EventData.of("action", "liberar"), false);
        }
        for (int i = 0; i < MAX_BANCO; i++) {
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnBEq" + i,
                EventData.of("action", "bequipo").append("value", String.valueOf(i)), false);
            event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnBRel" + i,
                EventData.of("action", "bliberar").append("value", String.valueOf(i)), false);
        }
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCerrar",
            EventData.of("action", "cerrar"), false);
        actualizar(cmd, ref, store);
        if (!animado) {
            animado = true;
            AnimacionUI.encender(this::enviarAnimacion, ref, store);
        } else {
            cmd.set("#CrtNegro.Visible", false);
            for (int i = 1; i <= 3; i++) cmd.set("#CrtLinea" + i + ".Visible", false);
            for (int i = 1; i <= 4; i++) cmd.set("#CrtFade" + i + ".Visible", false);
        }
    }

    /** Envia un frame de la animacion CRT al cliente. */
    public void enviarAnimacion(UICommandBuilder cmd) {
        try {
            sendUpdate(cmd);
        } catch (Exception ignored) {
        }
    }

    private void actualizar(UICommandBuilder cmd, Ref<EntityStore> ref, Store<EntityStore> store) {
        DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        cmd.set("#TabListas.Visible", tab == 0);
        cmd.set("#TabBanco.Visible", tab == 1);
        // Fila única: la cría del token.
        if (tokenHuevo != null && tokenBebe != null) {
            int pct = pctToken(d);
            cmd.set("#LblR0.Text", "Huevo " + tokenHuevo.replace('_', ' ') + " -> " + tokenBebe
                + " (" + pct + "%)" + (tokenVariante ? " variante (-25%)" : "")
                + (pct >= 100 ? " receta exacta" : " [fallo = Burpmon]"));
            cmd.set("#LblR0.Visible", true);
            String retrato = DigiImagenes.rutaPara(tokenBebe);
            if (retrato != null) {
                cmd.set("#ImgR0.AssetPath", retrato);
                cmd.set("#ImgR0.Visible", true);
            } else {
                cmd.set("#ImgR0.Visible", false);
            }
            cmd.set("#BtnEq0.Visible", true);
            cmd.set("#BtnBk0.Visible", true);
            cmd.set("#BtnRel0.Visible", true);
        } else {
            cmd.set("#LblR0.Text", "Usa un Huevo Incubando para reclamar. Banco disponible abajo.");
            cmd.set("#LblR0.Visible", true);
            cmd.set("#ImgR0.Visible", false);
            cmd.set("#BtnEq0.Visible", false);
            cmd.set("#BtnBk0.Visible", false);
            cmd.set("#BtnRel0.Visible", false);
        }
        for (int i = 1; i < 4; i++) {
            cmd.set("#LblR" + i + ".Text", "");
            cmd.set("#LblR" + i + ".Visible", false);
            cmd.set("#ImgR" + i + ".Visible", false);
            cmd.set("#BtnEq" + i + ".Visible", false);
            cmd.set("#BtnBk" + i + ".Visible", false);
            cmd.set("#BtnRel" + i + ".Visible", false);
        }
        int nb = (d != null) ? d.banco.size() : 0;
        for (int i = 0; i < MAX_BANCO; i++) {
            if (d != null && i < nb) {
                DatoDigimon b = d.banco.get(i);
                String extra = (i == MAX_BANCO - 1 && nb > MAX_BANCO)
                    ? " (+ " + (nb - MAX_BANCO) + " mas en banco " + nb + "/100)" : "";
                cmd.set("#LblB" + i + ".Text", b.nombre + " (" + b.especie + ") Nv" + b.nivel + extra);
                cmd.set("#LblB" + i + ".Visible", true);
                String retrato = DigiImagenes.rutaPara(b.especie);
                if (retrato != null) {
                    cmd.set("#ImgB" + i + ".AssetPath", retrato);
                    cmd.set("#ImgB" + i + ".Visible", true);
                } else {
                    cmd.set("#ImgB" + i + ".Visible", false);
                }
                cmd.set("#BtnBEq" + i + ".Visible", true);
                cmd.set("#BtnBRel" + i + ".Visible", true);
            } else {
                cmd.set("#LblB" + i + ".Text", i == 0 && nb == 0 ? "Banco vacio (0/100)." : "");
                cmd.set("#LblB" + i + ".Visible", i == 0 && nb == 0);
                cmd.set("#ImgB" + i + ".Visible", false);
                cmd.set("#BtnBEq" + i + ".Visible", false);
                cmd.set("#BtnBRel" + i + ".Visible", false);
            }
        }
        if (!msgPendiente.isEmpty()) {
            cmd.set("#LblMsg.Text", msgPendiente);
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
            case "tablistas" -> {
                tab = 0;
                rebuild();
            }
            case "tabbanco" -> {
                tab = 1;
                rebuild();
            }
            case "equipo", "banco" -> {
                if (tokenHuevo == null || tokenBebe == null) {
                    msgPendiente = "Usa un Huevo Incubando para reclamar.";
                    rebuild();
                    return;
                }
                boolean aBanco = "banco".equals(action);
                if (!aBanco && !datos.tieneEspacio()) {
                    msgPendiente = "Equipo lleno (6/6).";
                    rebuild();
                    return;
                }
                if (aBanco && datos.banco.size() >= DatosJugador.BANCO_MAX) {
                    msgPendiente = "Banco lleno (100/100).";
                    rebuild();
                    return;
                }
                DigitamaDef def = SistemaIncubacion.porItem(tokenHuevo);
                int pct = pctToken(datos);
                String quien = RNG.nextInt(100) < pct ? tokenBebe : "Burpmon";
                DatoDigimon bebe;
                try {
                    bebe = DatoDigimon.crearInicial(quien);
                } catch (Exception e) {
                    msgPendiente = "Reclamo fallido.";
                    rebuild();
                    return;
                }
                // Se aprende al reclamar (el dato ya se consumió en el horno).
                datos.desbloquearReceta(tokenBebe);
                // Calumon es soporte: no ocupa combate.
                if ("Calumon".equals(quien)) {
                    try {
                        bebe.noCombate = true;
                    } catch (Exception ignored) {
                    }
                }
                if (aBanco) {
                    datos.banco.add(bebe);
                    guardar(store, ref, datos);
                    consumirToken(ref, store);
                    playerRef.sendMessage(com.digitale.util.DigiMsg.parse(
                        "§b¡" + quien + " guardado en el Banco PC! (" + pct + "%)"));
                    player.getPageManager().setPage(ref, store,
                        com.hypixel.hytale.protocol.packets.interface_.Page.None);
                    return;
                } else {
                    int slot = datos.agregarAlEquipo(bebe);
                    if (slot < 0) {
                        msgPendiente = "Equipo lleno (6/6).";
                        rebuild();
                        return;
                    }
                    guardar(store, ref, datos);
                    consumirToken(ref, store);
                    playerRef.sendMessage(com.digitale.util.DigiMsg.parse(
                        !quien.equals("Burpmon")
                            ? ("§a¡Eclosiono " + quien + "! (slot " + (slot + 1) + ")")
                            : ("§cLa mezcla fallo... Burpmon al equipo. (slot " + (slot + 1) + ")")));
                    player.getPageManager().setPage(ref, store,
                        com.hypixel.hytale.protocol.packets.interface_.Page.None);
                    return;
                }
            }
            case "liberar" -> {
                if (tokenHuevo == null) {
                    rebuild();
                    return;
                }
                consumirToken(ref, store);
                guardar(store, ref, datos);
                playerRef.sendMessage(com.digitale.util.DigiMsg.parse(
                    "§7Cria liberada (el token se elimina)."));
                player.getPageManager().setPage(ref, store,
                    com.hypixel.hytale.protocol.packets.interface_.Page.None);
                return;
            }
            case "bequipo" -> {
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
                int slot = datos.agregarAlEquipo(b);
                if (slot < 0) {
                    datos.banco.add(cual, b);
                    msgPendiente = "Equipo lleno (6/6).";
                    rebuild();
                    return;
                }
                guardar(store, ref, datos);
                msgPendiente = b.nombre + " (" + b.especie + ") al equipo (slot " + (slot + 1) + ").";
                rebuild();
            }
            case "bliberar" -> {
                int cual = parseIdx(json);
                if (cual < 0 || cual >= datos.banco.size()) {
                    rebuild();
                    return;
                }
                DatoDigimon b = datos.banco.remove(cual);
                guardar(store, ref, datos);
                msgPendiente = b.nombre + " liberado del banco.";
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

    /** Elimina 1 token del reclamo del inventario (al reclamar/liberar). */
    private void consumirToken(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (tokenHuevo == null || tokenBebe == null) return;
        try {
            String suf = tokenHuevo.startsWith("Digitama_")
                ? tokenHuevo.substring("Digitama_".length()) : tokenHuevo;
            String objetivo = "Huevo_Incubando_" + suf + "_" + tokenBebe;
            java.util.List<com.digitale.util.Inventarios.Hallazgo> hallados =
                com.digitale.util.Inventarios.buscar(ref, store,
                    (id, corto) -> corto.equals(objetivo) || corto.startsWith(objetivo + "_"));
            if (hallados.isEmpty()) {
                hallados = com.digitale.util.Inventarios.buscar(ref, store,
                    (id, corto) -> corto.startsWith("Huevo_Incubando_"));
            }
            if (hallados.isEmpty()) return;
            com.digitale.util.Inventarios.Hallazgo h = hallados.get(0);
            com.hypixel.hytale.server.core.inventory.container.ItemContainer cont =
                com.digitale.util.Inventarios.contenedorDe(ref, store, h);
            if (cont != null) cont.removeItemStackFromSlot(h.slot, 1);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "consumirToken: " + e.getMessage());
        }
    }

    private void guardar(Store<EntityStore> store, Ref<EntityStore> ref, DatosJugador datos) {
        try {
            PetProgressComponent progress = ComponentRegistry.ensureAndGetProgress(store, ref);
            if (progress != null) progress.guardarDesde(datos);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo guardar reclamo: " + e.getMessage());
        }
    }
}
