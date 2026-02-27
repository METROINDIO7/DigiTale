package com.digitale.sistema;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.math.vector.Vector3d;

import java.util.UUID;

@SuppressWarnings("deprecation")
public class SistemaPaseo {

    private static UUID obtenerUuidJugador(PlayerRef playerRef) {
        return playerRef.getUuid();
    }

    public static void spawnearCompaneros(PlayerRef playerRef,
                                          Ref<EntityStore> ref,
                                          Store<EntityStore> store) {
        UUID playerUuid = obtenerUuidJugador(playerRef);
        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerUuid);
        if (datos == null || !datos.tieneEquipo) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return;
        Vector3d posJugador = transform.getPosition();

        if (datos.companeroA != null) {
            String rolA = datos.companeroA.especie + "_Companero";
            Vector3d posA = new Vector3d(posJugador).add(2.5, 1.0, 0.0);
            ejecutarComando(world, playerRef, "npc spawn " + rolA + " " + posA.x + " " + posA.y + " " + posA.z);
            buscarNpcYGuardar(world, posA, rolA, true, datos);
        }

        if (datos.companeroB != null) {
            String rolB = datos.companeroB.especie + "_Companero";
            Vector3d posB = new Vector3d(posJugador).add(-2.5, 1.0, 0.0);
            ejecutarComando(world, playerRef, "npc spawn " + rolB + " " + posB.x + " " + posB.y + " " + posB.z);
            buscarNpcYGuardar(world, posB, rolB, false, datos);
        }
    }

    private static void ejecutarComando(World world, PlayerRef playerRef, String comando) {
        // TODO: Implementar usando CommandManager
        System.out.println("Ejecutando comando: " + comando);
    }

    private static void buscarNpcYGuardar(World world,
                                          Vector3d pos,
                                          String rolId,
                                          boolean esA,
                                          AlmacenJugadores.DatosJugador datos) {
        // TODO: Implementar búsqueda de entidades cercanas
    }

    public static void recogerCompaneros(PlayerRef playerRef,
                                         Ref<EntityStore> ref,
                                         Store<EntityStore> store) {
        UUID playerUuid = obtenerUuidJugador(playerRef);
        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerUuid);
        if (datos == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        if (datos.refPaseoA != null && datos.refPaseoA.isValid()) {
            // world.removeEntity(datos.refPaseoA);
        }
        if (datos.refPaseoB != null && datos.refPaseoB.isValid()) {
            // world.removeEntity(datos.refPaseoB);
        }
        datos.refPaseoA = null;
        datos.refPaseoB = null;
    }
}
