package com.digitale.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Escaneo de inventario tolerante: primero secciones por ID y, si no hay
 * nada, fallback a los getters clásicos (deprecados pero funcionales).
 * Todo loguea para diagnosticar en el servidor.
 */
public final class Inventarios {

    private Inventarios() {
    }

    private static final Logger LOGGER = Logger.getLogger(Inventarios.class.getName());

    public static class Hallazgo {
        public int seccion;
        public short slot;
        public ItemStack stack;
        public String id;
        public String corto;
    }

    public interface Filtro {
        boolean aceptar(String idCompleto, String corto);
    }

    /** Recorre todos los stacks visibles del jugador. Nunca retorna null. */
    public static List<Hallazgo> recorrer(Ref<EntityStore> ref, Store<EntityStore> store) {
        List<Hallazgo> out = new ArrayList<>();
        try {
            for (int seccion = 0; seccion < 64; seccion++) {
                ItemContainer cont;
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
                    ItemStack stack;
                    try {
                        stack = cont.getItemStack(i);
                    } catch (Exception e) {
                        continue;
                    }
                    if (stack == null || stack.getItemId() == null) continue;
                    Hallazgo h = new Hallazgo();
                    h.seccion = seccion;
                    h.slot = i;
                    h.stack = stack;
                    h.id = stack.getItemId();
                    h.corto = h.id.contains(":") ? h.id.substring(h.id.indexOf(':') + 1) : h.id;
                    out.add(h);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Inventarios.recorrer secciones: " + e.getMessage());
        }
        if (!out.isEmpty()) {
            LOGGER.log(Level.INFO, "Inventarios: " + out.size() + " stacks por secciones.");
            return out;
        }
        // Fallback: getters clásicos.
        try {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null || player.getInventory() == null) return out;
            var inv = player.getInventory();
            @SuppressWarnings("removal")
            ItemContainer[] conts = {
                inv.getHotbar(), inv.getStorage(), inv.getBackpack(), inv.getUtility(), inv.getTools()
            };
            for (int s = 0; s < conts.length; s++) {
                ItemContainer cont = conts[s];
                if (cont == null) continue;
                short cap = cont.getCapacity();
                for (short i = 0; i < cap; i++) {
                    ItemStack stack;
                    try {
                        stack = cont.getItemStack(i);
                    } catch (Exception e) {
                        continue;
                    }
                    if (stack == null || stack.getItemId() == null) continue;
                    Hallazgo h = new Hallazgo();
                    h.seccion = 1000 + s;
                    h.slot = i;
                    h.stack = stack;
                    h.id = stack.getItemId();
                    h.corto = h.id.contains(":") ? h.id.substring(h.id.indexOf(':') + 1) : h.id;
                    out.add(h);
                }
            }
            LOGGER.log(Level.INFO, "Inventarios: " + out.size() + " stacks por fallback.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Inventarios.recorrer fallback: " + e.getMessage());
        }
        return out;
    }

    /** Busca stacks que cumplan el filtro (para mostrar/gastar). */
    public static List<Hallazgo> buscar(Ref<EntityStore> ref, Store<EntityStore> store, Filtro filtro) {
        List<Hallazgo> out = new ArrayList<>();
        for (Hallazgo h : recorrer(ref, store)) {
            try {
                if (filtro.aceptar(h.id, h.corto)) out.add(h);
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    /** Contenedor real por (seccion, slot) de un hallazgo (para gastar). */
    public static ItemContainer contenedorDe(Ref<EntityStore> ref, Store<EntityStore> store, Hallazgo h) {
        try {
            if (h.seccion >= 1000) {
                Player player = store.getComponent(ref, Player.getComponentType());
                if (player == null || player.getInventory() == null) return null;
                var inv = player.getInventory();
                @SuppressWarnings("removal")
                ItemContainer[] conts = {
                    inv.getHotbar(), inv.getStorage(), inv.getBackpack(), inv.getUtility(), inv.getTools()
                };
                int idx = h.seccion - 1000;
                if (idx < 0 || idx >= conts.length) return null;
                return conts[idx];
            }
            return com.hypixel.hytale.server.core.inventory.InventoryUtils.getSectionById(ref, h.seccion, store);
        } catch (Exception e) {
            return null;
        }
    }
}
