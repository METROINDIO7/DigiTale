package com.digitale.sistema;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("deprecation")
public class SistemaPaseo {
    private static final Logger LOGGER = Logger.getLogger(SistemaPaseo.class.getName());

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

        LOGGER.log(Level.INFO, "[SistemaPaseo] ========== SPAWN COMPANIONS REQUEST ==========");
        LOGGER.log(Level.INFO, "[SistemaPaseo] Jugador: " + playerRef.getUsername() + " @ " + posJugador);

        world.execute(() -> {
            try {
                if (datos.companeroA != null) {
                    String rolA = datos.companeroA.especie + "_Companero";
                    Vector3d posA = new Vector3d(posJugador).add(2.5, 1.0, 0.0);
                    Ref<?> refA = registrarSpawnearNpc(world, playerRef, datos.companeroA, rolA, posA, store, true);
                    if (refA != null) {
                        datos.refPaseoA = refA;
                        LOGGER.log(Level.INFO, "[SistemaPaseo] refPaseoA guardada correctamente");
                    } else {
                        LOGGER.log(Level.SEVERE, "[SistemaPaseo] refPaseoA es null");
                    }
                }

                if (datos.companeroB != null) {
                    String rolB = datos.companeroB.especie + "_Companero";
                    Vector3d posB = new Vector3d(posJugador).add(-2.5, 1.0, 0.0);
                    Ref<?> refB = registrarSpawnearNpc(world, playerRef, datos.companeroB, rolB, posB, store, false);
                    if (refB != null) {
                        datos.refPaseoB = refB;
                        LOGGER.log(Level.INFO, "[SistemaPaseo] refPaseoB guardada correctamente");
                    } else {
                        LOGGER.log(Level.SEVERE, "[SistemaPaseo] refPaseoB es null");
                    }
                }

                LOGGER.log(Level.INFO, "[SistemaPaseo] ========== SPAWN REQUEST COMPLETED ==========");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "[SistemaPaseo] Error in world.execute: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static Ref<?> registrarSpawnearNpc(World world,
                                               PlayerRef playerRef,
                                               DatoDigimon digimon,
                                               String roleId,
                                               Vector3d posicion,
                                               Store<EntityStore> store,
                                               boolean esA) {
        try {
            LOGGER.log(Level.INFO, "[SistemaPaseo] >>> SPAWN Request [" + (esA ? "A" : "B") + "] Role: " + roleId);

            NPCPlugin npcPlugin = NPCPlugin.get();
            if (npcPlugin == null) {
                LOGGER.log(Level.WARNING, "[SistemaPaseo] NPCPlugin no disponible");
                return null;
            }

            Vector3f rotation = new Vector3f(0, 0, 0);
            var result = npcPlugin.spawnNPC(store, roleId, null, posicion, rotation);
            if (result == null) {
                LOGGER.log(Level.WARNING, "[SistemaPaseo] spawnNPC retorno null");
                return null;
            }

            Ref<?> npcRef = null;

            try {
                npcRef = (Ref<?>) result.getClass().getMethod("first").invoke(result);
                if (npcRef != null) LOGGER.log(Level.INFO, "[SistemaPaseo] Ref via first() | isValid: " + npcRef.isValid());
            } catch (Exception e1) {
                LOGGER.log(Level.WARNING, "[SistemaPaseo] first() fallo: " + e1.getMessage());
            }

            if (npcRef == null && result instanceof Ref) {
                npcRef = (Ref<?>) result;
                LOGGER.log(Level.INFO, "[SistemaPaseo] Resultado directo es Ref | isValid: " + npcRef.isValid());
            }

            if (npcRef == null) {
                LOGGER.log(Level.SEVERE, "[SistemaPaseo] No se pudo obtener Ref");
                return null;
            }

            UUID playerUuid = obtenerUuidJugador(playerRef);
            AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerUuid);
            if (datos != null) {
                datos.posSpawnX = posicion.x;
                datos.posSpawnY = posicion.y;
                datos.posSpawnZ = posicion.z;
                if (esA) datos.rolSpawnA = roleId;
                else datos.rolSpawnB = roleId;
            }

            LOGGER.log(Level.INFO, "[SistemaPaseo] NPC spawneado y Ref guardada para [" + (esA ? "A" : "B") + "]");
            return npcRef;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[SistemaPaseo] Error en spawn NPC: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void recogerCompaneros(PlayerRef playerRef,
                                         Ref<EntityStore> ref,
                                         Store<EntityStore> store) {
        UUID playerUuid = obtenerUuidJugador(playerRef);
        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerUuid);
        if (datos == null) return;

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        LOGGER.log(Level.INFO, "[SistemaPaseo] ========== DESPAWN COMPANIONS REQUEST ==========");
        LOGGER.log(Level.INFO, "[SistemaPaseo] Jugador: " + playerRef.getUsername());
        LOGGER.log(Level.INFO, "[SistemaPaseo] refPaseoA: " + datos.refPaseoA + " | refPaseoB: " + datos.refPaseoB);

        world.execute(() -> {
            try {
                if (datos.refPaseoA != null && datos.refPaseoA.isValid()) {
                    intentarDespawnear(store, datos.refPaseoA, "A");
                } else {
                    LOGGER.log(Level.WARNING, "[SistemaPaseo] refPaseoA es null o invalida");
                }

                if (datos.refPaseoB != null && datos.refPaseoB.isValid()) {
                    intentarDespawnear(store, datos.refPaseoB, "B");
                } else {
                    LOGGER.log(Level.WARNING, "[SistemaPaseo] refPaseoB es null o invalida");
                }

                datos.refPaseoA = null;
                datos.refPaseoB = null;
                datos.paseoActivo = false;
                datos.rolSpawnA = "";
                datos.rolSpawnB = "";

                LOGGER.log(Level.INFO, "[SistemaPaseo] Estado limpiado. paseoActivo: false");
                LOGGER.log(Level.INFO, "[SistemaPaseo] ========== DESPAWN REQUEST COMPLETED ==========");

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "[SistemaPaseo] Error in world.execute (despawn): " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void intentarDespawnear(Store<EntityStore> store, Ref<?> npcRef, String slot) {

        // PASO 1: Loguear todos los valores del enum para diagnostico definitivo
        try {
            Class<?> removeReasonClass = Class.forName("com.hypixel.hytale.component.RemoveReason");
            Object[] constants = removeReasonClass.getEnumConstants();
            if (constants != null && constants.length > 0) {
                StringBuilder sb = new StringBuilder("[SistemaPaseo] Valores de RemoveReason: ");
                for (Object c : constants) sb.append(c.toString()).append(" | ");
                LOGGER.log(Level.INFO, sb.toString());
            } else {
                LOGGER.log(Level.WARNING, "[SistemaPaseo] RemoveReason no tiene constantes");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "[SistemaPaseo] No se pudo inspeccionar RemoveReason: " + ex.getMessage());
        }

        // PASO 2: Probar candidatos de nombre conocidos
        String[] candidatos = {"DESPAWN", "KILLED", "REMOVE", "UNLOAD", "DELETE", "NONE", "DEFAULT", "FORCE", "PLUGIN"};
        for (String candidato : candidatos) {
            try {
                Class<?> removeReasonClass = Class.forName("com.hypixel.hytale.component.RemoveReason");
                Object reason = Enum.valueOf((Class<Enum>) removeReasonClass, candidato);
                store.getClass()
                        .getMethod("removeEntity", Ref.class, removeReasonClass)
                        .invoke(store, npcRef, reason);
                LOGGER.log(Level.INFO, "[SistemaPaseo] [" + slot + "] Despawneado con RemoveReason." + candidato);
                return;
            } catch (IllegalArgumentException ignored) {
                // nombre no existe, siguiente
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "[SistemaPaseo] Fallo con " + candidato + ": " + e.getMessage());
            }
        }

        // PASO 3: Usar el primer valor del enum disponible
        try {
            Class<?> removeReasonClass = Class.forName("com.hypixel.hytale.component.RemoveReason");
            Object[] constants = removeReasonClass.getEnumConstants();
            if (constants != null && constants.length > 0) {
                Object primerValor = constants[0];
                store.getClass()
                        .getMethod("removeEntity", Ref.class, removeReasonClass)
                        .invoke(store, npcRef, primerValor);
                LOGGER.log(Level.INFO, "[SistemaPaseo] [" + slot + "] Despawneado con primer valor: " + primerValor);
                return;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[SistemaPaseo] Fallo con primer valor del enum: " + e.getMessage());
        }

        LOGGER.log(Level.SEVERE, "[SistemaPaseo] [" + slot + "] Todos los intentos de despawn fallaron.");
    }
}