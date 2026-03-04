package com.digitale.componentes;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class ComponentRegistry {

    // CORREGIDO: tipo generico completo en lugar de wildcard
    private static ComponentType<EntityStore, PetProgressComponent> petProgressType = null;

    public static void registerPetProgressType(ComponentType<EntityStore, PetProgressComponent> type) {
        petProgressType = type;
    }

    public static ComponentType<EntityStore, PetProgressComponent> getPetProgressType() {
        if (petProgressType == null) {
            throw new IllegalStateException("PetProgressComponent no ha sido registrado todavia");
        }
        return petProgressType;
    }

    // CORREGIDO: sin @SuppressWarnings ni cast porque el tipo ya es correcto
    public static PetProgressComponent getProgress(Store<EntityStore> store, Ref<EntityStore> ref) {
        return store.getComponent(ref, getPetProgressType());
    }

    public static PetProgressComponent ensureAndGetProgress(Store<EntityStore> store, Ref<EntityStore> ref) {
        return store.ensureAndGetComponent(ref, getPetProgressType());
    }
}