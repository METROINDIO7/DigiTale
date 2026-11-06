package com.digitale.sistema;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.DatoDigimon;
import com.digitale.datos.EstadoCombate;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Puesta en escena del combate por turnos.
 *
 * <ul>
 *   <li>Congela el movimiento del jugador mientras dura la batalla.</li>
 *   <li>Orienta al jugador hacia el enemigo y coloca party/enemigo en
 *       formación (spawn/teleport delegado a {@link SistemaPaseo}).</li>
 *   <li>Materializa las oleadas: cuando el motor pide un enemigo nuevo, aquí se
 *       bombea el NPC de esa especie y se retira el anterior.</li>
 *   <li>Mantiene la formación fija con un lock periódico y re-encuadra la
 *       cámara cuando algo cambia.</li>
 * </ul>
 */
public final class EscenarioBatalla {

    private static final Logger LOGGER = Logger.getLogger(EscenarioBatalla.class.getName());

    /** Cada cuánto se re-aplica la formación (ms). */
    private static final long LOCK_MS = 250L;
    private static final float MIRANDO_ENEMIGO = (float) Math.PI;
    private static final float ENEMIGO_YAW = 0f;

    private static final ScheduledExecutorService SCHED =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "digitale-batalla-lock");
            t.setDaemon(true);
            return t;
        });

    private static final Map<UUID, MovementSettings> ORIGINAL = new ConcurrentHashMap<>();
    private static final Map<UUID, ScheduledFuture<?>> LOCKS = new ConcurrentHashMap<>();

    private EscenarioBatalla() {
    }

    // ── Entrada / salida ───────────────────────────────────────────────────

    public static void iniciar(@Nonnull PlayerRef playerRef,
                               @Nonnull Ref<EntityStore> ref,
                               @Nonnull Store<EntityStore> store) {
        try {
            congelarJugador(playerRef, ref, store);
            colocarFormacion(playerRef, ref, store);
            // Primer encuadre forzado; después se refresca solo si cambia.
            CamaraCombate.entrar(playerRef);
            AnimacionesCombate.iniciar(playerRef, ref, store);
            iniciarLock(playerRef, ref, store);
            LOGGER.log(Level.INFO, "EscenarioBatalla iniciado para " + playerRef.getUsername());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "EscenarioBatalla iniciar: " + e.getMessage());
        }
    }

    public static void detener(@Nonnull PlayerRef playerRef,
                               @Nonnull Ref<EntityStore> ref,
                               @Nonnull Store<EntityStore> store) {
        try {
            UUID uuid = playerRef.getUuid();
            ScheduledFuture<?> f = LOCKS.remove(uuid);
            if (f != null) f.cancel(false);
            boolean estabaActivo = f != null || ORIGINAL.containsKey(uuid);
            AnimacionesCombate.detener(playerRef);
            SistemaPaseo.descongelarBatalla(playerRef, store);
            CamaraCombate.salir(playerRef);
            if (!estabaActivo) return; // ya estaba detenido: no repetir
            liberarJugador(playerRef, ref, store);
            LOGGER.log(Level.INFO, "EscenarioBatalla detenido para " + playerRef.getUsername());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "EscenarioBatalla detener: " + e.getMessage());
        }
    }

    // ── Formación ──────────────────────────────────────────────────────────

    /**
     * Fija al jugador mirando al enemigo, coloca party/enemigo y re-encuadra
     * la cámara. Se puede llamar en cada turno (y desde el lock periódico).
     */
    public static void colocarFormacion(@Nonnull PlayerRef playerRef,
                                        @Nonnull Ref<EntityStore> ref,
                                        @Nonnull Store<EntityStore> store) {
        try {
            AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
            if (d == null || d.combate == null || !d.combate.activa) return;
            // Durante la animación no re-colocamos nada: la escena está congelada
            // y así el hilo del mundo queda libre para la UI.
            if (d.combate.cinematicaActiva) return;

            TransformComponent jugador = store.getComponent(ref,
                TransformComponent.getComponentType());
            if (jugador == null) return;

            // El jugador queda mirando al +Z (el enemigo se coloca delante).
            jugador.teleportRotation(new Rotation3f(0f, MIRANDO_ENEMIGO, 0f));

            // prepararBatalla coloca la party y spawnear/coloca el grupo enemigo.
            SistemaPaseo.prepararBatalla(playerRef, ref, store);

            EstadoCombate c = d.combate;
            Vector3d centroEnemigos = centroDe(store, c.enemigos);
            Vector3d centroParty = centroDeParty(store, d);

            // Cada uno mira al centro del grupo rival (no todos al mismo yaw fijo).
            Rotation3f mirandoEnemigo = new Rotation3f(0f, MIRANDO_ENEMIGO, 0f);
            for (Ref<?> r : d.refsBatalla) {
                if (r == null || !r.isValid()) continue;
                if (centroEnemigos != null && mirarA(store, r, centroEnemigos)) continue;
                @SuppressWarnings("unchecked")
                TransformComponent t = store.getComponent((Ref<EntityStore>) r,
                    TransformComponent.getComponentType());
                if (t != null) t.teleportRotation(mirandoEnemigo);
            }

            for (EstadoCombate.Enemigo e : c.enemigos) {
                if (e == null || e.ref == null || !e.ref.isValid()) continue;
                if (centroParty != null && mirarA(store, e.ref, centroParty)) continue;
                @SuppressWarnings("unchecked")
                TransformComponent t = store.getComponent((Ref<EntityStore>) e.ref,
                    TransformComponent.getComponentType());
                if (t != null) t.teleportRotation(new Rotation3f(0f, ENEMIGO_YAW, 0f));
            }

            // Durante un plano de ataque la cámara la manda la cinemática.
            if (!c.cinematicaActiva) CamaraCombate.actualizar(playerRef);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "EscenarioBatalla colocarFormacion: " + e.getMessage());
        }
    }

    private static Vector3d centroDe(Store<EntityStore> store,
                                     java.util.List<EstadoCombate.Enemigo> enemigos) {
        double x = 0, y = 0, z = 0;
        int n = 0;
        for (EstadoCombate.Enemigo e : enemigos) {
            if (e == null || e.ref == null || !e.ref.isValid()) continue;
            @SuppressWarnings("unchecked")
            TransformComponent t = store.getComponent((Ref<EntityStore>) e.ref,
                TransformComponent.getComponentType());
            if (t == null) continue;
            Vector3d p = t.getPosition();
            x += p.x; y += p.y; z += p.z; n++;
        }
        return n > 0 ? new Vector3d(x / n, y / n, z / n) : null;
    }

    private static Vector3d centroDeParty(Store<EntityStore> store, AlmacenJugadores.DatosJugador d) {
        double x = 0, y = 0, z = 0;
        int n = 0;
        if (d.refsBatalla != null) {
            for (Ref<?> r : d.refsBatalla) {
                if (r == null || !r.isValid()) continue;
                @SuppressWarnings("unchecked")
                TransformComponent t = store.getComponent((Ref<EntityStore>) r,
                    TransformComponent.getComponentType());
                if (t == null) continue;
                Vector3d p = t.getPosition();
                x += p.x; y += p.y; z += p.z; n++;
            }
        }
        return n > 0 ? new Vector3d(x / n, y / n, z / n) : null;
    }

    /** Gira el NPC para mirar a un punto (solo yaw, sin inclinarlo). */
    private static boolean mirarA(Store<EntityStore> store, Ref<?> ref, Vector3d objetivo) {
        if (ref == null || !ref.isValid() || objetivo == null) return false;
        @SuppressWarnings("unchecked")
        TransformComponent t = store.getComponent((Ref<EntityStore>) ref,
            TransformComponent.getComponentType());
        if (t == null) return false;
        Vector3d p = t.getPosition();
        Vector3d dir = new Vector3d(objetivo.x - p.x, 0, objetivo.z - p.z);
        if (dir.lengthSquared() < 1.0e-4) return false;
        Rotation3f look = Rotation3f.lookAt(dir);
        if (look == null) return false;
        t.setRotation(new Rotation3f(0f, look.yaw(), 0f));
        return true;
    }

    // ── Lock periódico ─────────────────────────────────────────────────────

    private static void iniciarLock(@Nonnull PlayerRef playerRef,
                                    @Nonnull Ref<EntityStore> ref,
                                    @Nonnull Store<EntityStore> store) {
        UUID uuid = playerRef.getUuid();
        ScheduledFuture<?> anterior = LOCKS.remove(uuid);
        if (anterior != null) anterior.cancel(false);

        World world = store.getExternalData().getWorld();
        if (world == null) return;

        ScheduledFuture<?> f = SCHED.scheduleAtFixedRate(() -> {
            try {
                world.execute(() -> {
                    try {
                        if (!ref.isValid()) {
                            detener(playerRef, ref, store);
                            return;
                        }
                        AlmacenJugadores.DatosJugador d =
                            AlmacenJugadores.getDatos(playerRef.getUuid());
                        if (d == null || d.combate == null || !d.menuAbierto) {
                            detener(playerRef, ref, store);
                            return;
                        }
                        // Si el combate ya terminó, mantenemos la escena congelada
                        // hasta que el jugador cierre el menú, pero retiramos a
                        // los salvajes derrotados.
                        if (!d.combate.activa) {
                            SistemaPaseo.prepararBatalla(playerRef, ref, store);
                            CamaraCombate.actualizar(playerRef);
                            return;
                        }
                        // Fallback: si la presentación no está viva, el lock
                        // avanza el combate para que nunca se quede colgado.
                        if (!AnimacionesCombate.activa(uuid)) {
                            SistemaBatalla.procesarTurnos(d.combate);
                            return;
                        }
                        colocarFormacion(playerRef, ref, store);
                    } catch (Exception ignored) {
                    }
                });
            } catch (Exception ignored) {
            }
        }, LOCK_MS, LOCK_MS, TimeUnit.MILLISECONDS);

        LOCKS.put(uuid, f);
    }

    // ── Bloqueo de movimiento ──────────────────────────────────────────────

    /** Guarda los valores originales y deja al jugador sin velocidad/salto. */
    public static void congelarJugador(@Nonnull PlayerRef playerRef,
                                       @Nonnull Ref<EntityStore> ref,
                                       @Nonnull Store<EntityStore> store) {
        try {
            MovementManager mm = store.getComponent(ref, MovementManager.getComponentType());
            if (mm == null) return;
            MovementSettings s = mm.getSettings();
            if (s == null) return;
            ORIGINAL.putIfAbsent(playerRef.getUuid(), snapshot(s));

            s.baseSpeed = 0f;
            s.jumpForce = 0f;
            s.swimJumpForce = 0f;
            s.maxSpeedMultiplier = 0f;
            s.minSpeedMultiplier = 0f;
            s.forwardWalkSpeedMultiplier = 0f;
            s.backwardWalkSpeedMultiplier = 0f;
            s.strafeWalkSpeedMultiplier = 0f;
            s.forwardRunSpeedMultiplier = 0f;
            s.backwardRunSpeedMultiplier = 0f;
            s.strafeRunSpeedMultiplier = 0f;
            s.forwardCrouchSpeedMultiplier = 0f;
            s.backwardCrouchSpeedMultiplier = 0f;
            s.strafeCrouchSpeedMultiplier = 0f;
            s.forwardSprintSpeedMultiplier = 0f;
            s.horizontalFlySpeed = 0f;
            s.verticalFlySpeed = 0f;
            s.climbSpeed = 0f;
            s.climbSpeedLateral = 0f;
            s.climbUpSprintSpeed = 0f;
            s.climbDownSprintSpeed = 0f;
            mm.update(playerRef.getPacketHandler());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "EscenarioBatalla congelar: " + e.getMessage());
        }
    }

    /** Restaura la velocidad/salto que tenía el jugador antes del combate. */
    public static void liberarJugador(@Nonnull PlayerRef playerRef,
                                      @Nonnull Ref<EntityStore> ref,
                                      @Nonnull Store<EntityStore> store) {
        try {
            MovementSettings original = ORIGINAL.remove(playerRef.getUuid());
            if (original == null) return;
            MovementManager mm = store.getComponent(ref, MovementManager.getComponentType());
            if (mm == null) return;
            MovementSettings s = mm.getSettings();
            if (s == null) return;
            copy(original, s);
            mm.update(playerRef.getPacketHandler());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "EscenarioBatalla liberar: " + e.getMessage());
        }
    }

    /** Limpieza rápida al desconectar. */
    public static void limpiar(@Nonnull UUID uuid) {
        ScheduledFuture<?> f = LOCKS.remove(uuid);
        if (f != null) f.cancel(false);
        AnimacionesCombate.detener(uuid);
        ORIGINAL.remove(uuid);
    }

    private static MovementSettings snapshot(MovementSettings s) {
        MovementSettings c = new MovementSettings();
        copy(s, c);
        return c;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private static void copy(MovementSettings from, MovementSettings to) {
        to.baseSpeed = from.baseSpeed;
        to.acceleration = from.acceleration;
        to.jumpForce = from.jumpForce;
        to.swimJumpForce = from.swimJumpForce;
        to.maxSpeedMultiplier = from.maxSpeedMultiplier;
        to.minSpeedMultiplier = from.minSpeedMultiplier;
        to.forwardWalkSpeedMultiplier = from.forwardWalkSpeedMultiplier;
        to.backwardWalkSpeedMultiplier = from.backwardWalkSpeedMultiplier;
        to.strafeWalkSpeedMultiplier = from.strafeWalkSpeedMultiplier;
        to.forwardRunSpeedMultiplier = from.forwardRunSpeedMultiplier;
        to.backwardRunSpeedMultiplier = from.backwardRunSpeedMultiplier;
        to.strafeRunSpeedMultiplier = from.strafeRunSpeedMultiplier;
        to.forwardCrouchSpeedMultiplier = from.forwardCrouchSpeedMultiplier;
        to.backwardCrouchSpeedMultiplier = from.backwardCrouchSpeedMultiplier;
        to.strafeCrouchSpeedMultiplier = from.strafeCrouchSpeedMultiplier;
        to.forwardSprintSpeedMultiplier = from.forwardSprintSpeedMultiplier;
        to.horizontalFlySpeed = from.horizontalFlySpeed;
        to.verticalFlySpeed = from.verticalFlySpeed;
        to.climbSpeed = from.climbSpeed;
        to.climbSpeedLateral = from.climbSpeedLateral;
        to.climbUpSprintSpeed = from.climbUpSprintSpeed;
        to.climbDownSprintSpeed = from.climbDownSprintSpeed;
    }
}
