package com.digitale.sistema;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Location;
import org.joml.Vector3d;

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
                if (datos.getA() != null && !datos.getA().noCombate) {
                    String rolA = rolPara(datos.getA());
                    Vector3d posA = new Vector3d(posJugador).add(2.5, 1.0, 0.0);
                    Ref<?> refA = registrarSpawnearNpc(world, playerRef, datos.getA(), rolA, posA, store, true);
                    if (refA != null) {
                        datos.setRefPaseo(datos.activoA, refA);
                        datos.paseoActivo = true;
                        LOGGER.log(Level.INFO, "[SistemaPaseo] refPaseoA guardada correctamente");
                    } else {
                        LOGGER.log(Level.SEVERE, "[SistemaPaseo] refPaseoA es null");
                    }
                }

                if (datos.getB() != null) {
                    String rolB = rolPara(datos.getB());
                    Vector3d posB = new Vector3d(posJugador).add(-2.5, 1.0, 0.0);
                    Ref<?> refB = registrarSpawnearNpc(world, playerRef, datos.getB(), rolB, posB, store, false);
                    if (refB != null) {
                        datos.setRefPaseo(datos.activoB, refB);
                        datos.paseoActivo = true;
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

            Rotation3f rotation = new Rotation3f();
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

    /**
     * Jugar = sacar a pasear: alterna el spawn del Digimon del slot.
     * Si ya está fuera lo recoge; si no, lo spawnea junto al jugador.
     * Retorna el mensaje para la UI (o null si el slot está vacío).
     * Debe llamarse desde el hilo del mundo (los handlers de UI ya lo están).
     */
    public static String alternarPaseo(PlayerRef playerRef,
                                       Ref<EntityStore> ref,
                                       Store<EntityStore> store,
                                       int slot) {
        UUID playerUuid = obtenerUuidJugador(playerRef);
        AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerUuid);
        if (datos == null) return null;
        DatoDigimon digimon = datos.getSlot(slot);
        if (digimon == null) return null;
        if (digimon.noCombate) return digimon.nombre + " es soporte y no pasea.";

        World world = store.getExternalData().getWorld();
        if (world == null) return null;

        // Ya está paseando → recogerlo.
        if (datos.paseoEn(slot)) {
            Ref<?> npcRef = datos.refsPaseo[slot];
            final String nombre = digimon.nombre;
            world.execute(() -> {
                try {
                    intentarDespawnear(store, npcRef, String.valueOf(slot));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "[SistemaPaseo] Fallo al recoger slot " + slot + ": " + e.getMessage());
                }
            });
            datos.setRefPaseo(slot, null);
            datos.paseoActivo = datos.algunPaseo();
            LOGGER.log(Level.INFO, "[SistemaPaseo] " + nombre + " volvió a descansar.");
            return nombre + " volvio a descansar.";
        }

        // Sacarlo a pasear junto al jugador.
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) return null;
        Vector3d posJugador = transform.getPosition();
        // Pequeño desplazamiento lateral según slot para que no se apilen.
        Vector3d pos = new Vector3d(posJugador).add(2.0 + (slot % 3), 1.0, (slot / 3) * 2.0 - 1.0);
        String rol = rolPara(digimon);
        if (rol == null) {
            LOGGER.log(Level.WARNING, "[SistemaPaseo] Sin rol de paseo para especie: " + digimon.especie);
            return digimon.nombre + " aun no tiene rol de paseo (falta " + digimon.especie + "_Companero).";
        }

        final String rolFinal = rol;
        final String nombre = digimon.nombre;
        world.execute(() -> {
            Ref<?> npcRef = registrarSpawnearNpc(world, playerRef, digimon, rolFinal, pos, store, slot == datos.activoA);
            if (npcRef != null) {
                datos.setRefPaseo(slot, npcRef);
                datos.paseoActivo = true;
                LOGGER.log(Level.INFO, "[SistemaPaseo] " + nombre + " salio a pasear (slot " + slot + ").");
            }
        });
        digimon.interactuar(); // pasear juntos sube el lazo
        return nombre + " salio a pasear contigo.";
    }

    /**
     * Rol de paseo para la especie. Prueba "<Especie>_Companero" y como
     * alternativa "Test_<Especie>". Null si no existe ninguno (hay que modelarlo).
     */
    private static String rolPara(String especie) {
        // Los roles se resuelven en el spawn; aquí solo construimos candidatos.
        // registrarSpawnearNpc devuelve null si el rol no existe.
        return especie + "_Companero";
    }

    /** Rol según tamaño: Burpmon >=3x usa el modelo Grande. */
    static String rolPara(com.digitale.datos.DatoDigimon digimon) {
        if (digimon != null && digimon.esGrande()) return "Burpmon_Grande_Companero";
        if (digimon == null) return null;
        return rolPara(digimon.especie);
    }

    /**
     * Puesta en escena del combate: recoge el paseo, spawnea la party
     * (máx 3) en formación junto al jugador y coloca al salvaje enfrente.
     * Llamar al abrir la UI y tras cada acción (el salvaje deambula).
     */
    public static void prepararBatalla(PlayerRef playerRef,
                                       Ref<EntityStore> ref,
                                       Store<EntityStore> store) {
        try {
            UUID playerUuid = obtenerUuidJugador(playerRef);
            AlmacenJugadores.DatosJugador datos = AlmacenJugadores.getDatos(playerUuid);
            if (datos == null || datos.combate == null) return;
            EstadoCombate c = datos.combate;
            java.util.List<DatoDigimon> party =
                (c.party != null && !c.party.isEmpty()) ? c.party : datos.equipoCombate();
            if (party.isEmpty()) return;

            // Recoger paseo (síncrono: ya estamos en el hilo del mundo).
            for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
                Ref<?> r = datos.refsPaseo[i];
                if (r != null && r.isValid()) {
                    try {
                        intentarDespawnear(store, r, "paseo" + i);
                    } catch (Exception ignored) {
                    }
                }
                datos.setRefPaseo(i, null);
            }
            datos.paseoActivo = false;

            TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
            if (transform == null) return;
            Vector3d p = transform.getPosition();
            Rotation3f rot = transform.getRotation();
            float yaw = rot != null ? rot.y : 0f;
            double fx = -Math.sin(yaw), fz = Math.cos(yaw);
            double rx = -Math.cos(yaw), rz = -Math.sin(yaw);

            World world = store.getExternalData().getWorld();
            if (world == null) return;

            // Salvaje enfrente (4.8 bloques): mas cerca y visible.
            if (c.objetivo != null && c.objetivo.isValid()) {
                teleportar(store, c.objetivo,
                    new Vector3d(p).add(fx * 4.8, 0.5, fz * 4.8));
            }

            // Party en linea (3/4 de Cyber Sleuth): 2.2 adelante y 2.2 de separacion.
            for (int i = 0; i < party.size() && i < 3; i++) {
                DatoDigimon d = party.get(i);
                if (d == null) continue;
                Vector3d pos = new Vector3d(p)
                    .add(fx * 2.2 + rx * ((i - 1) * 2.2), 0.5, fz * 2.2 + rz * ((i - 1) * 2.2));
                Ref<?> npcRef = (i < datos.refsBatalla.length) ? datos.refsBatalla[i] : null;
                if (npcRef == null || !npcRef.isValid()) {
                    npcRef = registrarSpawnearNpc(world, playerRef, d,
                        rolPara(d), pos, store, i == 0);
                    if (npcRef != null && i < datos.refsBatalla.length) {
                        datos.refsBatalla[i] = npcRef;
                    } else if (npcRef == null) {
                        LOGGER.log(Level.WARNING, "[SistemaPaseo] Sin rol de batalla para: " + d.especie);
                        continue;
                    }
                } else {
                    teleportar(store, npcRef, pos);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[SistemaPaseo] prepararBatalla: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void teleportar(Store<EntityStore> store, Ref<?> npcRef, Vector3d pos) {
        try {
            Ref<EntityStore> ref = (Ref<EntityStore>) npcRef;
            TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
            if (t != null) t.teleportPosition(pos);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[SistemaPaseo] teleport: " + e.getMessage());
        }
    }

    /** Despawn de un NPC con log mínimo. */
    public static void despawnearUno(Store<EntityStore> store, Ref<?> npcRef) {
        try {
            if (npcRef != null && npcRef.isValid()) intentarDespawnear(store, npcRef, "batalla");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[SistemaPaseo] despawnearUno: " + e.getMessage());
        }
    }

    /** Recoge los NPCs de la formación de combate. */
    public static void recogerBatalla(PlayerRef playerRef,
                                      Ref<EntityStore> ref,
                                      Store<EntityStore> store) {
        try {
            AlmacenJugadores.DatosJugador datos =
                AlmacenJugadores.getDatos(obtenerUuidJugador(playerRef));
            if (datos == null) return;
            for (int i = 0; i < datos.refsBatalla.length; i++) {
                despawnearUno(store, datos.refsBatalla[i]);
            }
            datos.limpiarBatalla();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[SistemaPaseo] recogerBatalla: " + e.getMessage());
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

        world.execute(() -> {
            try {
                for (int i = 0; i < AlmacenJugadores.DatosJugador.MAX_EQUIPO; i++) {
                    Ref<?> npcRef = datos.refsPaseo[i];
                    if (npcRef != null && npcRef.isValid()) {
                        intentarDespawnear(store, npcRef, String.valueOf(i));
                    }
                    datos.setRefPaseo(i, null);
                }

                datos.limpiarPaseo();
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