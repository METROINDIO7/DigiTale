package com.digitale.ui;

import com.digitale.crianza.DigitamaDef;
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
import java.util.logging.Logger;

/**
 * Receta de incubación revelada por un Dato Incubación.
 * Muestra huevo, ingredientes, entorno, energía, ciclos y resultado.
 */
public class DigiRecetaUI extends CustomUIPage {

    private static final Logger LOGGER = Logger.getLogger(DigiRecetaUI.class.getName());

    private final DigitamaDef def;

    public DigiRecetaUI(@Nonnull PlayerRef playerRef, @Nonnull DigitamaDef def) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.def = def;
    }

    public static void open(@Nonnull PlayerRef playerRef,
                            @Nonnull Ref<EntityStore> ref,
                            @Nonnull Store<EntityStore> store,
                            @Nonnull DigitamaDef def) {
        LOGGER.info("DigiRecetaUI.open() " + def.idItem);
        DigiRecetaUI page = new DigiRecetaUI(playerRef, def);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.getPageManager().openCustomPage(ref, store, page);
        }
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref,
                      @Nonnull UICommandBuilder cmd,
                      @Nonnull UIEventBuilder event,
                      @Nonnull Store<EntityStore> store) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            Group {
                LayoutMode: Top;
                Anchor: (Height: 500, Width: 560);

                Group {
                    LayoutMode: Top;
                    Background: (Color: #0b1220);
                    Padding: (Full: 15);

                    Label {
                        Text: "DATO INCUBACION";
                        Style: (TextColor: #ffcc00, FontSize: 18, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 26);
                    }
                    Label #RecHuevo {
                        Text: "";
                        Style: (TextColor: #00d4ff, FontSize: 15, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 22);
                    }
                    Label #RecFam {
                        Text: "";
                        Style: (TextColor: #8899aa, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Group { Anchor: (Height: 4); }
                    Group { Anchor: (Height: 2); Background: (Color: #ffcc00); }
                    Group { Anchor: (Height: 4); }
                    Label {
                        Text: "INGREDIENTES";
                        Style: (TextColor: #00d4ff, FontSize: 13, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 18);
                    }
            """);
        for (int i = 0; i < 8; i++) {
            sb.append("                    Label #RecIng").append(i).append(" {\n");
            sb.append("                        Text: \"\";\n");
            sb.append("                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center);\n");
            sb.append("                        Anchor: (Height: 16);\n");
            sb.append("                    }\n");
        }
        sb.append("""
                    Group { Anchor: (Height: 4); }
                    Label #RecEntorno {
                        Text: "";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                        Anchor: (Height: 32);
                    }
                    Label #RecEnergia {
                        Text: "";
                        Style: (TextColor: #ffcc00, FontSize: 12, HorizontalAlignment: Center);
                        Anchor: (Height: 16);
                    }
                    Label #RecResultado {
                        Text: "";
                        Style: (TextColor: #00ff88, FontSize: 14, RenderBold: true, HorizontalAlignment: Center);
                        Anchor: (Height: 20);
                    }
                    Group { Anchor: (Height: 8); }
                    TextButton #BtnDesbloquear {
                        Text: "Desbloquear (gasta dato)";
                        Anchor: (Width: 220, Height: 36);
                        Style: (Default: (Background: (Color: #0f3460), LabelStyle: (FontSize: 12, TextColor: #ffcc00, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (FontSize: 12, TextColor: #ffffff, HorizontalAlignment: Center)));
                    }
                    Group { Anchor: (Height: 4); }
                    Label #LblRecMsg {
                        Text: "";
                        Style: (TextColor: #e0e8f0, FontSize: 12, HorizontalAlignment: Center, Wrap: true);
                        Anchor: (Height: 20);
                    }
                    Group { Anchor: (Height: 4); }
                    TextButton #BtnCerrar {
                        Text: "Cerrar";
                        Anchor: (Width: 160, Height: 36);
                        Style: (Default: (Background: (Color: #16213e), LabelStyle: (TextColor: #8899aa, FontSize: 13, HorizontalAlignment: Center)), Hovered: (Background: (Color: #1a5a90), LabelStyle: (TextColor: #e0e8f0, FontSize: 13, HorizontalAlignment: Center)));
                    }
                }
            }
            """);
        cmd.appendInline(null, sb.toString());

        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnDesbloquear",
            EventData.of("action", "desbloquear"), false);
        event.addEventBinding(CustomUIEventBindingType.Activating, "#BtnCerrar",
            EventData.of("action", "cerrar"), false);

        cmd.set("#RecHuevo.Text", def.nombre + " (" + def.idItem + ")");
        cmd.set("#RecFam.Text", def.familia + " | " + def.atributo + " | " + def.tier);
        for (int i = 0; i < 8; i++) {
            if (i < def.ingredientes.length) {
                cmd.set("#RecIng" + i + ".Visible", true);
                cmd.set("#RecIng" + i + ".Text", "- " + def.ingredientes[i]);
            } else {
                cmd.set("#RecIng" + i + ".Text", "");
                cmd.set("#RecIng" + i + ".Visible", false);
            }
        }
        cmd.set("#RecEntorno.Text", "Entorno: " + def.entorno);
        cmd.set("#RecEnergia.Text", "Energia " + def.energia + "/10 | Ciclos: " + def.ciclos);
        cmd.set("#RecResultado.Text", "Nace: " + String.join(", ", def.pool()));
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref,
                                @Nonnull Store<EntityStore> store,
                                @Nonnull String jsonData) {
        JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
        String action = json.has("action") ? json.get("action").getAsString() : "";
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        if ("cerrar".equals(action)) {
            player.getPageManager().setPage(ref, store,
                com.hypixel.hytale.protocol.packets.interface_.Page.None);
        } else if ("desbloquear".equals(action)) {
            com.digitale.datos.AlmacenJugadores.DatosJugador datos =
                com.digitale.datos.AlmacenJugadores.getDatos(playerRef.getUuid());
            UICommandBuilder cmd = new UICommandBuilder();
            if (datos == null) {
                sendUpdate(cmd);
                return;
            }
            // Busca cualquier dato del pool de este huevo y lo gasta.
            boolean ok = false;
            String usado = "";
            for (String esp : def.pool()) {
                if (gastarDatoDe(ref, store, esp)) {
                    ok = true;
                    usado = esp;
                    break;
                }
            }
            if (!ok) {
                cmd.set("#LblRecMsg.Text", "Necesitas un Dato Incubacion de este huevo.");
            } else if (datos.desbloquearReceta(def.especie)) {
                guardar(store, ref, datos);
                cmd.set("#LblRecMsg.Text", "¡Receta de " + def.nombre + " desbloqueada! (gasto " + usado + ")");
            } else {
                cmd.set("#LblRecMsg.Text", "Ya conocias esta receta.");
            }
            sendUpdate(cmd);
        } else {
            UICommandBuilder cmd = new UICommandBuilder();
            sendUpdate(cmd);
        }
    }

    private void guardar(Store<EntityStore> store, Ref<EntityStore> ref,
                         com.digitale.datos.AlmacenJugadores.DatosJugador datos) {
        try {
            com.digitale.componentes.PetProgressComponent progress =
                com.digitale.componentes.ComponentRegistry.ensureAndGetProgress(store, ref);
            if (progress != null) progress.guardarDesde(datos);
        } catch (Exception ignored) {
        }
    }

    /** Gasta 1 Dato_Incubacion_<bebe> de cualquier sección. */
    private static boolean gastarDatoDe(Ref<EntityStore> ref, Store<EntityStore> store, String bebe) {
        String quiero = "Dato_Incubacion_" + bebe;
        try {
            for (int seccion = 0; seccion < 32; seccion++) {
                com.hypixel.hytale.server.core.inventory.container.ItemContainer cont;
                try {
                    cont = com.hypixel.hytale.server.core.inventory.InventoryUtils.getSectionById(ref, seccion, store);
                } catch (Exception e) {
                    continue;
                }
                if (cont == null) continue;
                short cap;
                try {
                    cap = cont.getCapacity();
                } catch (Exception e) {
                    continue;
                }
                for (short i = 0; i < cap; i++) {
                    com.hypixel.hytale.server.core.inventory.ItemStack stack;
                    try {
                        stack = cont.getItemStack(i);
                    } catch (Exception e) {
                        continue;
                    }
                    if (stack == null || stack.getItemId() == null) continue;
                    String id = stack.getItemId();
                    String corto = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
                    if (!corto.equals(quiero)) continue;
                    try {
                        cont.removeItemStackFromSlot(i, 1);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
