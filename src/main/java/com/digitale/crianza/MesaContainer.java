package com.digitale.crianza;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.SlotFilter;
import com.hypixel.hytale.server.core.inventory.transaction.ClearTransaction;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Contenedor en memoria de la Mesa de Crianza (10 slots, modelo horno):
 * 0 = INPUT Huevo (Digitama_*), 1-9 = FUEL (slot 1 Dato Dato_Incubacion_*,
 * slots 2-9 minerales, comida, ingredientes, otros huevos).
 * Vive mientras la mesa está abierta; al cerrar se devuelve todo al jugador.
 */
public class MesaContainer extends ItemContainer {

    public static final short CAPACIDAD = 10;
    public static final short SLOT_HUEVO = 0;
    public static final short SLOT_DATO = 1;
    public static final short SLOT_EXTRA_A = 2;
    public static final short SLOT_EXTRA_B = 3;
    public static final short SLOT_EXTRA_C = 4;
    public static final short SLOT_EXTRA_D = 5;
    public static final short SLOT_EXTRA_E = 6;
    public static final short SLOT_EXTRA_F = 7;
    public static final short SLOT_EXTRA_G = 8;
    public static final short SLOT_EXTRA_H = 9;

    private final ItemStack[] slots = new ItemStack[CAPACIDAD];
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public short getCapacity() {
        return CAPACIDAD;
    }

    @Override
    public void setGlobalFilter(FilterType filterType) {
        // Sin filtro global: se valida al combinar.
    }

    @Override
    public void setSlotFilter(FilterActionType actionType, short slot, SlotFilter filter) {
        // Sin filtros por slot: se valida al combinar.
    }

    @Override
    public ItemContainer clone() {
        MesaContainer copia = new MesaContainer();
        for (short i = 0; i < CAPACIDAD; i++) {
            if (slots[i] != null) {
                try {
                    copia.slots[i] = slots[i].cleanCopy();
                } catch (Exception ignored) {
                }
            }
        }
        return copia;
    }

    @Override
    protected <V> V readAction(java.util.function.Supplier<V> action) {
        lock.readLock().lock();
        try {
            return action.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    protected <X, V> V readAction(java.util.function.Function<X, V> action, X input) {
        lock.readLock().lock();
        try {
            return action.apply(input);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    protected <V> V writeAction(java.util.function.Supplier<V> action) {
        lock.writeLock().lock();
        try {
            return action.get();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected <X, V> V writeAction(java.util.function.Function<X, V> action, X input) {
        lock.writeLock().lock();
        try {
            return action.apply(input);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    protected void lockForRead() {
        lock.readLock().lock();
    }

    @Override
    protected void unlockForRead() {
        lock.readLock().unlock();
    }

    @Override
    protected void lockForWrite() {
        lock.writeLock().lock();
    }

    @Override
    protected void unlockForWrite() {
        lock.writeLock().unlock();
    }

    private boolean valido(short slot) {
        return slot >= 0 && slot < CAPACIDAD;
    }

    @Override
    protected ClearTransaction internal_clear() {
        ItemStack[] antes = slots.clone();
        for (short i = 0; i < CAPACIDAD; i++) slots[i] = null;
        return new ClearTransaction(true, CAPACIDAD, antes);
    }

    @Override
    protected ItemStack internal_getSlot(short slot) {
        if (!valido(slot)) return null;
        return slots[slot];
    }

    @Override
    protected ItemStack internal_setSlot(short slot, ItemStack stack) {
        if (!valido(slot)) return null;
        ItemStack previo = slots[slot];
        slots[slot] = stack;
        return previo;
    }

    @Override
    protected ItemStack internal_removeSlot(short slot) {
        if (!valido(slot)) return null;
        ItemStack previo = slots[slot];
        slots[slot] = null;
        return previo;
    }

    @Override
    protected boolean cantAddToSlot(short slot, ItemStack existing, ItemStack adding) {
        return false;
    }

    @Override
    protected boolean cantRemoveFromSlot(short slot) {
        return false;
    }

    @Override
    protected boolean cantDropFromSlot(short slot) {
        return false;
    }

    @Override
    protected boolean cantMoveToSlot(ItemContainer other, short slot) {
        return false;
    }

    /** Retira todo el stack de un slot (para devolución al cerrar). */
    public ItemStack retirarTodo(short slot) {
        if (!valido(slot)) return null;
        lock.writeLock().lock();
        try {
            ItemStack s = slots[slot];
            slots[slot] = null;
            return s;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** Id corto (sin namespace) del stack en un slot, o null. */
    public static String cortoDe(ItemStack stack) {
        if (stack == null) return null;
        String id;
        try {
            id = stack.getItemId();
        } catch (Exception e) {
            return null;
        }
        if (id == null) return null;
        return id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
    }

    public static int cantidadDe(ItemStack stack) {
        if (stack == null) return 0;
        try {
            return Math.max(0, stack.getQuantity());
        } catch (Exception e) {
            return 1;
        }
    }
}
