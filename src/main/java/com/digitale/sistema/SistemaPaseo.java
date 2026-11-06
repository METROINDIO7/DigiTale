package com.digitale.sistema;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
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
            // Convencion Hytale: yaw 0 mira a -Z -> forward = (-sin, 0, -cos).
            double fx = -Math.sin(yaw), fz = -Math.cos(yaw);
            double rx = -fz, rz = fx;

            World world = store.getExternalData().getWorld();
            if (world == null) return;

            // Party en linea (3/4 de Cyber Sleuth): 2.2 adelante y 2.2 de separacion.
            for (int i = 0; i < party.size() && i < 3; i++) {
                DatoDigimon d = party.get(i);
                if (d == null) continue;
                Vector3d pos = new Vector3d(p)
                    .add(fx * 2.2 + rx * ((i - 1) * 2.2), 0.05, fz * 2.2 + rz * ((i - 1) * 2.2));
                String rol = rolPara(d);
                Ref<?> npcRef = (i < datos.refsBatalla.length) ? datos.refsBatalla[i] : null;
                // Si el slot cambió de Digimon (Cambio), reciclamos el NPC.
                boolean especieCambio = npcRef != null
                    && !d.especie.equals(datos.refsBatallaEspecie[i]);
                if (npcRef != null && (!npcRef.isValid() || especieCambio)) {
                    if (npcRef.isValid()) intentarDespawnear(store, npcRef, "party" + i);
                    npcRef = null;
                    datos.refsBatalla[i] = null;
                }
                if (npcRef == null) {
                    npcRef = registrarSpawnearNpc(world, playerRef, d, rol, pos, store, i == 0);
                    if (npcRef != null) {
                        datos.refsBatalla[i] = npcRef;
                        datos.refsBatallaEspecie[i] = d.especie;
                    } else {
                        LOGGER.log(Level.WARNING, "[SistemaPaseo] Sin rol de batalla para: " + d.especie);
                        continue;
                    }
                } else {
                    teleportar(store, npcRef, pos);
                }
                congelar(store, npcRef);
            }

            // ── Grupo enemigo: todos a la vez, en formación ──────────────
            int n = c.enemigos.size();
            for (int i = 0; i < n; i++) {
                com.digitale.datos.EstadoCombate.Enemigo e = c.enemigos.get(i);
                if (e == null) continue;
                // Un salvaje derrotado se retira DESPUÉS de su animación de muerte.
                if (!e.vivo()) {
                    long ahora = System.currentTimeMillis();
                    if (e.despawnEn == 0) e.despawnEn = ahora + 1800L;
                    if (ahora >= e.despawnEn) {
                        if (e.ref != null) despawnearUno(store, e.ref);
                        e.ref = null;
                    }
                    continue;
                }
                Vector3d pos = posicionEnemigo(p, fx, fz, rx, rz, i, n);

                // El primer salvaje es el NPC que golpeaste (mismo modelo).
                if (i == 0 && e.ref == null && c.objetivo != null && c.objetivo.isValid()) {
                    e.ref = c.objetivo;
                    e.propio = false;
                }
                if (e.ref != null && e.ref.isValid()) {
                    teleportar(store, e.ref, pos);
                    congelar(store, e.ref);
                    continue;
                }
                if (e.spawnFallido) continue;
                Ref<?> nuevo = spawnSalvaje(store, playerRef, e.nombre, pos);
                if (nuevo != null) {
                    e.ref = nuevo;
                    e.propio = true;
                    congelar(store, nuevo);
                } else {
                    e.spawnFallido = true;
                    LOGGER.log(Level.WARNING, "[SistemaPaseo] Sin rol salvaje para: " + e.nombre);
                }
            }
            // Altura media de las hitboxes: la cámara escala con ella.
            c.alturaMedia = calcularAlturaMedia(store, datos, c);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[SistemaPaseo] prepararBatalla: " + e.getMessage());
        }
    }

    /** Altura media (y) de las hitboxes de party + enemigos. */
    private static float calcularAlturaMedia(Store<EntityStore> store,
                                             AlmacenJugadores.DatosJugador datos,
                                             com.digitale.datos.EstadoCombate c) {
        float suma = 0f;
        int n = 0;
        if (datos.refsBatalla != null) {
            for (Ref<?> r : datos.refsBatalla) {
                float h = alturaDe(store, r);
                if (h > 0.01f) { suma += h; n++; }
            }
        }
        for (com.digitale.datos.EstadoCombate.Enemigo e : c.enemigos) {
            if (e == null) continue;
            float h = alturaDe(store, e.ref);
            if (h > 0.01f) { suma += h; n++; }
        }
        return n > 0 ? suma / n : 0f;
    }

    private static float alturaDe(Store<EntityStore> store, Ref<?> ref) {
        if (ref == null || !ref.isValid()) return 0f;
        try {
            @SuppressWarnings("unchecked")
            Ref<EntityStore> r = (Ref<EntityStore>) ref;
            var bb = store.getComponent(r,
                com.hypixel.hytale.server.core.modules.entity.component.BoundingBox.getComponentType());
            if (bb != null && bb.getBoundingBox() != null) {
                var b = bb.getBoundingBox();
                return (float) Math.abs(b.max.y - b.min.y);
            }
            var mc = store.getComponent(r, ModelComponent.getComponentType());
            if (mc != null && mc.getModel() != null && mc.getModel().getBoundingBox() != null) {
                var b = mc.getModel().getBoundingBox();
                return (float) (Math.abs(b.max.y - b.min.y)
                    * Math.max(0.01f, mc.getModel().getScale()));
            }
        } catch (Exception ignored) {
        }
        return 0f;
    }

    /** Congela al NPC (la AI deja de moverlo/girarlo durante el combate). */
    private static void congelar(Store<EntityStore> store, Ref<?> ref) {
        if (ref == null || !ref.isValid()) return;
        try {
            @SuppressWarnings("unchecked")
            Ref<EntityStore> r = (Ref<EntityStore>) ref;
            store.ensureComponent(r, Frozen.getComponentType());
        } catch (Exception ignored) {
        }
    }

    private static void descongelar(Store<EntityStore> store, Ref<?> ref) {
        if (ref == null || !ref.isValid()) return;
        try {
            @SuppressWarnings("unchecked")
            Ref<EntityStore> r = (Ref<EntityStore>) ref;
            store.tryRemoveComponent(r, Frozen.getComponentType());
        } catch (Exception ignored) {
        }
    }

    /**
     * Posición de cada salvaje del grupo. El índice 0 (el que golpeaste) va al
     * centro delante; el resto se reparte en una o dos filas.
     */
    private static Vector3d posicionEnemigo(Vector3d p, double fx, double fz,
                                            double rx, double rz, int i, int n) {
        double[][] off = switch (Math.max(1, Math.min(5, n))) {
            case 1 -> new double[][]{{0, 5.2}};
            case 2 -> new double[][]{{-1.3, 5.3}, {1.3, 5.3}};
            case 3 -> new double[][]{{0, 5.5}, {-2.2, 5.0}, {2.2, 5.0}};
            case 4 -> new double[][]{{0, 5.2}, {-2.1, 5.8}, {2.1, 5.8}, {0, 6.8}};
            default -> new double[][]{{0, 5.2}, {-2.2, 5.8}, {2.2, 5.8}, {-1.2, 6.8}, {1.2, 6.8}};
        };
        double[] o = off[Math.min(i, off.length - 1)];
        return new Vector3d(p).add(fx * o[1] + rx * o[0], 0.05, fz * o[1] + rz * o[0]);
    }

    /**
     * Spawnea el NPC salvaje de una oleada (la especie cambia entre oleadas).
     * @return la ref del NPC o null si el rol no existe.
     */
    public static Ref<?> spawnSalvaje(Store<EntityStore> store, PlayerRef playerRef,
                                      String especie, Vector3d pos) {
        try {
            World world = store.getExternalData().getWorld();
            if (world == null) return null;
            String rol = com.digitale.datos.EnemigoCombate.rolSalvajeDe(especie);
            NPCPlugin npc = NPCPlugin.get();
            if (npc == null) return null;
            var result = npc.spawnNPC(store, rol, null, pos, new Rotation3f());
            if (result == null) {
                LOGGER.log(Level.WARNING, "[SistemaPaseo] No se pudo spawnear oleada: " + rol);
                return null;
            }
            try {
                Object r = result.getClass().getMethod("first").invoke(result);
                if (r instanceof Ref<?> ref) return ref;
            } catch (Exception ignored) {
            }
            if (result instanceof Ref<?> ref) return ref;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[SistemaPaseo] spawnSalvaje: " + e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void teleportar(Store<EntityStore> store, Ref<?> npcRef, Vector3d pos) {
        try {
            Ref<EntityStore> ref = (Ref<EntityStore>) npcRef;
            TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
            if (t == null) return;
            // Si ya está en su sitio, NO se re-teleporta: re-teleportar cada tick
            // hacía que el NPC se creyera en el aire y repitiera el salto.
            Vector3d actual = t.getPosition();
            if (actual != null && actual.distanceSquared(pos) < 0.04) return;
            t.teleportPosition(pos);
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

    /** Recoge los NPCs de la formación de combate y limpia los enemigos. */
    /** Descongela a todos los combatientes (al salir del combate). */
    public static void descongelarBatalla(PlayerRef playerRef, Store<EntityStore> store) {
        try {
            AlmacenJugadores.DatosJugador datos =
                AlmacenJugadores.getDatos(obtenerUuidJugador(playerRef));
            if (datos == null) return;
            if (datos.refsBatalla != null) {
                for (Ref<?> r : datos.refsBatalla) descongelar(store, r);
            }
            if (datos.combate != null) {
                for (com.digitale.datos.EstadoCombate.Enemigo e : datos.combate.enemigos) {
                    if (e != null) descongelar(store, e.ref);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void recogerBatalla(PlayerRef playerRef,
                                      Ref<EntityStore> ref,
                                      Store<EntityStore> store) {
        try {
            AlmacenJugadores.DatosJugador datos =
                AlmacenJugadores.getDatos(obtenerUuidJugador(playerRef));
            if (datos == null) return;
            com.digitale.datos.EstadoCombate c = datos.combate;

            for (int i = 0; i < datos.refsBatalla.length; i++) {
                descongelar(store, datos.refsBatalla[i]);
                despawnearUno(store, datos.refsBatalla[i]);
            }
            datos.limpiarBatalla();

            if (c != null) {
                // Los spawneados por la batalla se retiran siempre; los salvajes
                // del mundo solo si fueron derrotados (victoria).
                for (com.digitale.datos.EstadoCombate.Enemigo e : c.enemigos) {
                    if (e == null || e.ref == null) continue;
                    descongelar(store, e.ref);
                    if (e.propio || c.victoria) despawnearUno(store, e.ref);
                    e.ref = null;
                }
                c.objetivo = null;
            }
            // El combate ya terminó: liberamos la referencia para que la UI
            // no siga mostrando un combate viejo.
            datos.combate = null;
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

    @SuppressWarnings("unchecked")
    private static void intentarDespawnear(Store<EntityStore> store, Ref<?> npcRef, String slot) {
        if (npcRef == null || !npcRef.isValid()) return;
        try {
            store.removeEntity((Ref<EntityStore>) npcRef,
                com.hypixel.hytale.component.RemoveReason.REMOVE);
            LOGGER.log(Level.FINE, "[SistemaPaseo] [" + slot + "] NPC eliminado.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[SistemaPaseo] [" + slot + "] despawn: " + e.getMessage());
        }
    }
}