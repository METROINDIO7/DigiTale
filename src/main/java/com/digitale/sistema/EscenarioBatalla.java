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
 * Puesta en escena del combate por turnos:
 *
 * <ul>
 *   <li>Congela el movimiento del jugador mientras dura la batalla (ajusta
 *       {@link MovementSettings} a cero y lo restaura al salir).</li>
 *   <li>Orienta al jugador hacia el enemigo y coloca la party/enemigo en
 *       formaci&oacute;n (delegando el spawn/teleport a {@link SistemaPaseo}).</li>
 *   <li>Mantiene la formaci&oacute;n fija con un peque&ntilde;o lockperi&oacute;dico
 *       para que la IA de los compa&ntilde;eros no se los lleve de paseo.</li>
 * </ul>
 *
 * Todo se ejecuta en el WorldThread (el lock se encola con {@code world.execute}).
 */
public final class EscenarioBatalla {

    private static final Logger LOGGER = Logger.getLogger(EscenarioBatalla.class.getName());

    /** Cada cu&aacute;nto se re-aplica la formaci&oacute;n (ms). */
    private static final long LOCK_MS = 250L;
    private static final float ENEMIGO_YAW = (float) Math.PI;

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
            CamaraCombate.entrar(playerRef);
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
            ScheduledFuture<?> f = LOCKS.remove(playerRef.getUuid());
            if (f != null) f.cancel(false);
            liberarJugador(playerRef, ref, store);
            CamaraCombate.salir(playerRef);
            LOGGER.log(Level.INFO, "EscenarioBatalla detenido para " + playerRef.getUsername());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "EscenarioBatalla detener: " + e.getMessage());
        }
    }

    // ── Formación ──────────────────────────────────────────────────────────

    /**
     * Fija al jugador mirando al +Z y recoloca party/enemigo con
     * {@link SistemaPaseo#prepararBatalla}. Se puede llamar en cada turno.
     */
    public static void colocarFormacion(@Nonnull PlayerRef playerRef,
                                        @Nonnull Ref<EntityStore> ref,
                                        @Nonnull Store<EntityStore> store) {
        try {
            AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
            if (d == null || d.combate == null || !d.combate.activa) return;

            TransformComponent jugador = store.getComponent(ref, TransformComponent.getComponentType());
            if (jugador == null) return;

            // El jugador queda mirando al +Z (el enemigo se coloca delante).
            jugador.teleportRotation(new Rotation3f(0f, 0f, 0f));

            // Spawn/teleport de la party y del salvaje delante del jugador.
            SistemaPaseo.prepararBatalla(playerRef, ref, store);

            Rotation3f mirandoEnemigo = new Rotation3f(0f, 0f, 0f);
            for (Ref<?> r : d.refsBatalla) {
                if (r != null && r.isValid()) {
                    TransformComponent t = store.getComponent(
                        (Ref<EntityStore>) r, TransformComponent.getComponentType());
                    if (t != null) t.teleportRotation(mirandoEnemigo);
                }
            }

            EstadoCombate c = d.combate;
            if (c.objetivo != null && c.objetivo.isValid()) {
                TransformComponent t = store.getComponent(
                    (Ref<EntityStore>) c.objetivo, TransformComponent.getComponentType());
                if (t != null) t.teleportRotation(new Rotation3f(0f, ENEMIGO_YAW, 0f));
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "EscenarioBatalla colocarFormacion: " + e.getMessage());
        }
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
                        // Si el combate ya termino, mantenemos la escena congelada
                        // hasta que el jugador cierre el menu con Q.
                        if (!d.combate.activa) return;
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

            // Sin traslacion ni salto; los comandos van por botones (Page).
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

    /** Restaura la velocidad/salto que ten&iacute;a el jugador antes del combate. */
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

    /** Limpieza r&aacute;pida al desconectar. */
    public static void limpiar(@Nonnull UUID uuid) {
        ScheduledFuture<?> f = LOCKS.remove(uuid);
        if (f != null) f.cancel(false);
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
