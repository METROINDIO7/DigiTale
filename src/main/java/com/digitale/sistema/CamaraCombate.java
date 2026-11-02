package com.digitale.sistema;

import com.digitale.datos.AlmacenJugadores;
import com.hypixel.hytale.protocol.ClientCameraView;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MouseInputTargetType;
import com.hypixel.hytale.protocol.MouseInputType;
import com.hypixel.hytale.protocol.MovementForceRotationType;
import com.hypixel.hytale.protocol.PositionDistanceOffsetType;
import com.hypixel.hytale.protocol.RotationType;
import com.hypixel.hytale.protocol.ServerCameraSettings;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.joml.Vector3f;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cámara fija de combate (receta copiada del comando vainilla Topdown:
 * vista Custom + writeNoCache + rotación cenital). Entrar al abrir batalla,
 * salir al terminar/cerrar/desconectar. Idempotente por flag en RAM.
 */
public final class CamaraCombate {

    private CamaraCombate() {
    }

    private static final Logger LOGGER = Logger.getLogger(CamaraCombate.class.getName());

    /**
     * Camara de batalla tipo Cyber Sleuth: vista 3/4 alta y algo alejada para
     * encuadrar al jugador abajo, la party en linea y el enemigo al fondo.
     */
    public static final float DISTANCIA = 8.0f;
    public static final float PITCH = -0.45f;

    public static void entrar(PlayerRef playerRef) {
        entrar(playerRef, DISTANCIA, PITCH);
    }

    public static void entrar(PlayerRef playerRef, float distancia) {
        entrar(playerRef, distancia, PITCH);
    }

    public static void entrar(PlayerRef playerRef, float distancia, float pitch) {
        if (playerRef == null) return;
        try {
            AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
            if (d != null && d.camaraCombate) return;
            ServerCameraSettings s = new ServerCameraSettings();
            s.positionLerpSpeed = 0.35f;
            s.rotationLerpSpeed = 0.35f;
            s.distance = distancia;
            s.baseFov = 75f;
            s.displayCursor = true;
            s.isFirstPerson = false;
            s.movementForceRotationType = MovementForceRotationType.Custom;
            s.eyeOffset = true;
            s.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
            s.rotationType = RotationType.Custom;
            s.rotation = new Direction(0f, pitch, 0f);
            s.mouseInputType = MouseInputType.LookAtPlane;
            s.planeNormal = new Vector3f(0f, 1f, 0f);
            s.mouseInputTargetType = MouseInputTargetType.None;
            s.sendMouseMotion = false;
            s.allowPitchControls = false;
            s.displayReticle = false;
            // OJO: skipCharacterPhysics=true atraviesa el suelo. Freeze solo por filtro.
            s.skipCharacterPhysics = false;
            SetServerCamera pkt = new SetServerCamera(ClientCameraView.Custom, true, s);
            playerRef.getPacketHandler().writeNoCache(pkt);
            if (d != null) d.camaraCombate = true;
            LOGGER.log(Level.INFO, "CamaraCombate entrar: " + playerRef.getUsername());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "CamaraCombate entrar: " + e.getMessage());
        }
    }

    public static void salir(PlayerRef playerRef) {
        if (playerRef == null) return;
        try {
            AlmacenJugadores.DatosJugador d = AlmacenJugadores.getDatos(playerRef.getUuid());
            if (d != null && !d.camaraCombate) return;
            SetServerCamera pkt = new SetServerCamera(ClientCameraView.Custom, false, null);
            playerRef.getPacketHandler().writeNoCache(pkt);
            if (d != null) d.camaraCombate = false;
            LOGGER.log(Level.INFO, "CamaraCombate salir: " + playerRef.getUsername());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "CamaraCombate salir: " + e.getMessage());
        }
    }
}
