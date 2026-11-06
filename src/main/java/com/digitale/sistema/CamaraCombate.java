package com.digitale.sistema;

import com.digitale.datos.AlmacenJugadores;
import com.digitale.datos.EstadoCombate;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.AttachedToType;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MouseInputTargetType;
import com.hypixel.hytale.protocol.MouseInputType;
import com.hypixel.hytale.protocol.MovementForceRotationType;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.PositionType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cámara de combate estilo Digimon Story: vista 3/4 desde detrás del jugador,
 * elevada y ligeramente desplazada a un lado, apuntando al centro de la escena
 * (party + enemigo).
 *
 * <p><b>Por qué antes no enfocaba</b>: la pose se calculaba con
 * {@code atan2(-dx, dz)} cuando la convención real de Hytale (ver
 * {@code Rotation3f.lookAt}, que hace {@code atan2(-dx, -dz)}) es la opuesta en
 * Z. El resultado era una cámara girada ~140° que dejaba la escena fuera de
 * pantalla. Aquí usamos directamente {@link Rotation3f#lookAt(Vector3d)} para
 * que el encuadre sea siempre el correcto.</p>
 *
 * <p>La cámara es una pose absoluta ({@code Custom}) y se re-encuadra sola
 * cuando la escena cambia (oleadas, cambio de líder, muerte del enemigo) sin
 * reenviar el paquete si nada se movió.</p>
 */
public final class CamaraCombate {

    private CamaraCombate() {
    }

    private static final Logger LOGGER = Logger.getLogger(CamaraCombate.class.getName());

    // ── Perfil por defecto (ajustable con /digi_cam) ────────────────────
    /** Distancia horizontal detrás del jugador. */
    public static final float DISTANCIA   = 6.6f;
    /** Desplazamiento lateral (hombro) para dar la vista 3/4. */
    public static final float LATERAL     = 2.0f;
    /** Altura sobre el suelo de la escena. */
    public static final float ALTURA      = 3.3f;
    /** FOV base del combate. */
    public static final float FOV         = 70f;
    /** Altura a la que se apunta (pecho de los Digimon). */
    public static final float ALTURA_MIRA = 0.8f;

    // ── Perfil de foco cinematográfico (ataque/daño) ────────────────────
    /** Distancia al objetivo enfocado. */
    public static final float DIST_FOCO   = 2.6f;
    /** Desplazamiento lateral del foco (evita tapar a la party). */
    public static final float LATERAL_FOCO = 2.3f;
    /** Altura de cámara del foco. */
    public static final float ALTURA_FOCO = 2.1f;
    /** FOV cerrado para el plano de acción. */
    public static final float FOV_FOCO    = 60f;

    // ── Escalado por tamaño de los Digimon ──────────────────────────────
    /** Altura media de referencia (Adult/Champion) con la que se calibró el perfil. */
    public static final float ALTURA_REF     = 2.0f;
    /** La cámara no se acerca más de esto (bebés diminutos). */
    public static final float ESCALA_MIN     = 0.5f;
    /** Tope para no alejarse sin fin con colosos. */
    public static final float ESCALA_MAX     = 3.0f;
    /** Escala mínima del plano de foco (evita meterse dentro del modelo). */
    public static final float ESCALA_FOCO_MIN = 0.7f;

    /** Umbrales para no spamear paquetes de cámara. */
    private static final double EPS_POS = 0.02;
    private static final double EPS_ROT = 0.004;

    private static final Map<UUID, Pose> ULTIMA = new ConcurrentHashMap<>();

    /** Pose calculada, para diagnóstico. */
    private record Pose(double x, double y, double z, float yaw, float pitch) {
        boolean casiIgual(Pose o) {
            if (o == null) return false;
            return Math.abs(x - o.x) < EPS_POS
                && Math.abs(y - o.y) < EPS_POS
                && Math.abs(z - o.z) < EPS_POS
                && Math.abs(yaw - o.yaw) < EPS_ROT
                && Math.abs(pitch - o.pitch) < EPS_ROT;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // API
    // ══════════════════════════════════════════════════════════════════

    public static void entrar(PlayerRef playerRef) {
        LOGGER.log(Level.INFO, "CamaraCombate entrar: "
            + (playerRef != null ? playerRef.getUsername() : "-"));
        float k = escala(playerRef);
        float h = alturaMedia(playerRef);
        aplicar(playerRef, DISTANCIA * k, ALTURA * k, LATERAL * k,
            Math.max(ALTURA_MIRA * k, h * 0.40f), k, true);
    }

    public static void entrar(PlayerRef playerRef, float distancia) {
        float k = escala(playerRef);
        float h = alturaMedia(playerRef);
        aplicar(playerRef, distancia, ALTURA * k, LATERAL * k,
            Math.max(ALTURA_MIRA * k, h * 0.40f), k, true);
    }

    /** Entrada de diagnóstico: distancia y altura personalizadas. */
    public static void entrar(PlayerRef playerRef, float distancia, float altura) {
        float k = escala(playerRef);
        float h = alturaMedia(playerRef);
        aplicar(playerRef, distancia, altura, LATERAL * k,
            Math.max(ALTURA_MIRA * k, h * 0.40f), k, true);
    }

    /** Factor de escala de la cámara según la altura media de las hitboxes. */
    private static float escala(PlayerRef playerRef) {
        float h = alturaMedia(playerRef);
        if (h <= 0.02f) h = ALTURA_REF;
        return Math.max(ESCALA_MIN, Math.min(ESCALA_MAX, h / ALTURA_REF));
    }

    private static float alturaMedia(PlayerRef playerRef) {
        if (playerRef == null) return 0f;
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        return d != null && d.combate != null ? d.combate.alturaMedia : 0f;
    }

    /**
     * Plano cinematográfico sobre un combatiente: cámara cercana mirando su
     * frente (el aliado se enfoca desde el lado enemigo y viceversa).
     */
    public static void enfocar(PlayerRef playerRef, Ref<?> objetivo, boolean aliado) {
        if (playerRef == null || objetivo == null || !objetivo.isValid()) return;
        try {
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null || !ref.isValid()) return;
            Store<EntityStore> store = ref.getStore();
            TransformComponent tp = store.getComponent(ref, TransformComponent.getComponentType());
            @SuppressWarnings("unchecked")
            TransformComponent tt = store.getComponent((Ref<EntityStore>) objetivo,
                TransformComponent.getComponentType());
            if (tp == null || tt == null) return;

            float yawJugador = tp.getRotation() != null ? tp.getRotation().y : 0f;
            Vector3d forward = new Vector3d(-Math.sin(yawJugador), 0, -Math.cos(yawJugador));
            Vector3d right = new Vector3d(-forward.z, 0, forward.x);
            Vector3d pos = tt.getPosition();
            double lado = aliado ? 1.0 : -1.0;

            float k = escala(playerRef);
            float kf = Math.max(ESCALA_FOCO_MIN, Math.min(ESCALA_MAX, k));
            float h = alturaMedia(playerRef);
            double dist = DIST_FOCO * kf;
            double lat = LATERAL_FOCO * kf;
            double alt = ALTURA_FOCO * kf;
            double miraY = Math.max(1.1f * kf, h * 0.45f);

            double camX = pos.x + forward.x * lado * dist + right.x * lat;
            double camY = pos.y + alt;
            double camZ = pos.z + forward.z * lado * dist + right.z * lat;
            double tx = pos.x, ty = pos.y + miraY, tz = pos.z;

            Rotation3f mira = Rotation3f.lookAt(new Vector3d(tx - camX, ty - camY, tz - camZ));
            float yaw = mira != null ? mira.yaw() : 0f;
            float pitch = mira != null ? mira.pitch() : 0f;

            ServerCameraSettings s = new ServerCameraSettings();
            s.positionLerpSpeed = 0.20f;
            s.rotationLerpSpeed = 0.20f;
            s.baseFov = FOV_FOCO;
            s.distance = 0f;
            s.displayCursor = true;
            s.isFirstPerson = false;
            s.allowPitchControls = false;
            s.displayReticle = false;
            s.sendMouseMotion = false;
            s.eyeOffset = false;
            s.skipCharacterPhysics = false;
            s.hideHeldItem = true;
            s.attachedToType = AttachedToType.None;
            s.followAttachedEntity = false;
            s.positionType = PositionType.Custom;
            s.position = new Position(camX, camY, camZ);
            s.positionDistanceOffsetType = PositionDistanceOffsetType.None;
            s.rotationType = RotationType.Custom;
            s.rotation = new Direction(yaw, pitch, 0f);
            s.movementForceRotationType = MovementForceRotationType.Custom;
            s.movementForceRotation = new Direction(yawJugador, 0f, 0f);
            s.mouseInputType = MouseInputType.LookAtPlane;
            s.planeNormal = new Vector3f(0f, 1f, 0f);
            s.mouseInputTargetType = MouseInputTargetType.None;

            playerRef.getPacketHandler().writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, true, s));
            ULTIMA.put(playerRef.getUuid(), new Pose(camX, camY, camZ, yaw, pitch));
            AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
            if (d != null) d.camaraCombate = true;
            LOGGER.log(Level.FINE, "CamaraCombate foco " + (aliado ? "aliado" : "enemigo")
                + " cam=(" + camX + "," + camY + "," + camZ + ")");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "CamaraCombate enfocar: " + e.getMessage());
        }
    }

    /**
     * Re-encuadra la cámara si la escena cambió. Barato: no manda paquete si
     * la pose nueva es prácticamente igual a la anterior.
     */
    public static void actualizar(PlayerRef playerRef) {
        float k = escala(playerRef);
        float h = alturaMedia(playerRef);
        aplicar(playerRef, DISTANCIA * k, ALTURA * k, LATERAL * k,
            Math.max(ALTURA_MIRA * k, h * 0.40f), k, false);
    }

    /** ¿El jugador tiene la cámara de combate activa? */
    public static boolean activa(PlayerRef playerRef) {
        if (playerRef == null) return false;
        AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
        return d != null && d.camaraCombate;
    }

    /** Texto de diagnóstico para /digi_cam. */
    public static String describir(PlayerRef playerRef) {
        if (playerRef == null) return "sin jugador";
        Pose p = ULTIMA.get(playerRef.getUuid());
        if (p == null) return "sin pose calculada";
        return String.format(java.util.Locale.ROOT,
            "cam=(%.2f, %.2f, %.2f) yaw=%.3f pitch=%.3f",
            p.x, p.y, p.z, p.yaw, p.pitch);
    }

    // ══════════════════════════════════════════════════════════════════
    // CÁLCULO DE LA POSE
    // ══════════════════════════════════════════════════════════════════

    private static void aplicar(PlayerRef playerRef, float distancia, float altura,
                                float lateral, float miraY, float k, boolean forzar) {
        if (playerRef == null) return;
        try {
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null || !ref.isValid()) return;
            Store<EntityStore> store = ref.getStore();
            TransformComponent t = store.getComponent(ref, TransformComponent.getComponentType());
            if (t == null) return;

            AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());

            Vector3d p = t.getPosition();
            Rotation3f rot = t.getRotation();
            float yawJugador = rot != null ? rot.y : 0f;

            // Convención Hytale: forward = (-sin yaw, 0, -cos yaw).
            Vector3d forward = new Vector3d(-Math.sin(yawJugador), 0, -Math.cos(yawJugador));
            // right = forward × up = (-fz, 0, fx)
            Vector3d right = new Vector3d(-forward.z, 0, forward.x);

            // ── Centro de la escena: party y enemigo reales si existen ──
            double sx = 0, sy = 0, sz = 0;
            int n = 0;
            double suelo = p.y;
            int enemigosVisibles = 0;
            if (d != null && d.combate != null) {
                EstadoCombate c = d.combate;
                if (d.refsBatalla != null) {
                    for (Ref<?> r : d.refsBatalla) {
                        Vector3d q = posicionDe(store, r);
                        if (q == null) continue;
                        sx += q.x; sy += q.y; sz += q.z; n++;
                        if (q.y < suelo) suelo = q.y;
                    }
                }
                // Todo el grupo enemigo entra en el encuadre (no solo el primero).
                for (EstadoCombate.Enemigo e : c.enemigos) {
                    Vector3d q = posicionDe(store, e.ref);
                    if (q == null) continue;
                    sx += q.x; sy += q.y; sz += q.z; n++;
                    enemigosVisibles++;
                    if (q.y < suelo) suelo = q.y;
                }
            }
            // Con más enemigos, la cámara se aleja un poco para que quepan.
            double distCam = Math.min(distancia + 1.6 * k,
                distancia + 0.35 * k * Math.max(0, enemigosVisibles - 1));

            double tx, ty, tz;
            if (n > 0) {
                tx = sx / n;
                ty = suelo + miraY;
                tz = sz / n;
            } else {
                // Sin escena montada: mira al frente del jugador.
                tx = p.x + forward.x * 3.0;
                ty = p.y + miraY;
                tz = p.z + forward.z * 3.0;
            }

            // ── Posición de cámara: detrás + hombro + altura ────────────
            double camX = p.x - forward.x * distCam + right.x * lateral;
            double camY = suelo + altura;
            double camZ = p.z - forward.z * distCam + right.z * LATERAL;

            // ── Apuntado exacto con la utilidad del motor ───────────────
            double dx = tx - camX, dy = ty - camY, dz = tz - camZ;
            Rotation3f mira = Rotation3f.lookAt(new Vector3d(dx, dy, dz));
            float yaw = mira != null ? mira.yaw() : 0f;
            float pitch = mira != null ? mira.pitch() : 0f;

            Pose pose = new Pose(camX, camY, camZ, yaw, pitch);
            Pose anterior = ULTIMA.get(playerRef.getUuid());
            boolean igual = pose.casiIgual(anterior);
            boolean yaActiva = d != null && d.camaraCombate;
            if (!forzar && yaActiva && igual) return;

            ServerCameraSettings s = new ServerCameraSettings();
            s.positionLerpSpeed = 0.18f;
            s.rotationLerpSpeed = 0.18f;
            s.baseFov = FOV;
            s.distance = 0f;
            s.displayCursor = true;
            s.isFirstPerson = false;
            s.allowPitchControls = false;
            s.displayReticle = false;
            s.sendMouseMotion = false;
            s.eyeOffset = false;
            s.skipCharacterPhysics = false;
            s.hideHeldItem = true;

            // Pose absoluta (sin entidad ancla: más estable y sin NPCs extra).
            s.attachedToType = AttachedToType.None;
            s.followAttachedEntity = false;
            s.positionType = PositionType.Custom;
            s.position = new Position(camX, camY, camZ);
            s.positionDistanceOffsetType = PositionDistanceOffsetType.None;
            s.rotationType = RotationType.Custom;
            s.rotation = new Direction(yaw, pitch, 0f);

            // El personaje queda encarado al enemigo aunque esté congelado.
            s.movementForceRotationType = MovementForceRotationType.Custom;
            s.movementForceRotation = new Direction(yawJugador, 0f, 0f);

            // El ratón se proyecta al plano del suelo (cursor de la UI).
            s.mouseInputType = MouseInputType.LookAtPlane;
            s.planeNormal = new Vector3f(0f, 1f, 0f);
            s.mouseInputTargetType = MouseInputTargetType.None;

            playerRef.getPacketHandler().writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, true, s));

            ULTIMA.put(playerRef.getUuid(), pose);
            if (d != null) d.camaraCombate = true;

            LOGGER.log(Level.FINE, "CamaraCombate " + playerRef.getUsername()
                + " cam=(" + camX + "," + camY + "," + camZ + ")"
                + " mira=(" + tx + "," + ty + "," + tz + ")"
                + " yaw=" + yaw + " pitch=" + pitch);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "CamaraCombate: " + e.getMessage());
        }
    }

    private static Vector3d posicionDe(Store<EntityStore> store, Ref<?> ref) {
        if (store == null || ref == null || !ref.isValid()) return null;
        try {
            @SuppressWarnings("unchecked")
            Ref<EntityStore> r = (Ref<EntityStore>) ref;
            TransformComponent tr = store.getComponent(r, TransformComponent.getComponentType());
            return tr != null ? tr.getPosition() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // SALIDA
    // ══════════════════════════════════════════════════════════════════

    /** Devuelve la cámara al jugador y limpia anclas antiguas si las hubiera. */
    public static void salir(PlayerRef playerRef) {
        if (playerRef == null) return;
        try {
            AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
            ULTIMA.remove(playerRef.getUuid());

            // Compatibilidad: versiones anteriores spawneaban un NPC ancla.
            if (d != null && d.refCamara != null) {
                try {
                    Ref<EntityStore> ref = playerRef.getReference();
                    if (ref != null && ref.isValid()) {
                        SistemaPaseo.despawnearUno(ref.getStore(), d.refCamara);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.FINE, "CamaraCombate despawnAncla: " + e.getMessage());
                }
                d.refCamara = null;
            }

            if (d != null && !d.camaraCombate) return;
            playerRef.getPacketHandler().writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, false, null));
            if (d != null) d.camaraCombate = false;
            LOGGER.log(Level.INFO, "CamaraCombate salir: " + playerRef.getUsername());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "CamaraCombate salir: " + e.getMessage());
        }
    }
}
