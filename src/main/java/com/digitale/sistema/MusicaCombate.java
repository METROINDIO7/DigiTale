package com.digitale.sistema;

import com.hypixel.hytale.protocol.packets.world.UpdateForcedMusic;
import com.hypixel.hytale.server.core.asset.type.musiccontainer.config.MusicContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Musica de combate: fuerza el contenedor {@code DigiTale_Combat} (Random con
 * los temas de Common/Music/Combat) al iniciar y lo quita al terminar.
 */
public final class MusicaCombate {

    private static final Logger LOGGER = Logger.getLogger(MusicaCombate.class.getName());
    private static final String ID = "DigiTale_Combat";

    private MusicaCombate() {
    }

    public static void iniciar(PlayerRef playerRef) {
        int indice = indice();
        if (indice == MusicContainer.EMPTY_ID) {
            LOGGER.log(Level.WARNING, "No se encontro el MusicContainer " + ID);
            return;
        }
        enviar(playerRef, indice);
    }

    public static void detener(PlayerRef playerRef) {
        enviar(playerRef, MusicContainer.EMPTY_ID);
    }

    private static int indice() {
        try {
            return MusicContainer.getAssetMap().getIndexOrDefault(ID, MusicContainer.EMPTY_ID);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "MusicaCombate indice: " + e.getMessage());
            return MusicContainer.EMPTY_ID;
        }
    }

    private static void enviar(PlayerRef playerRef, int indice) {
        if (playerRef == null) return;
        try {
            playerRef.getPacketHandler().writeNoCache(new UpdateForcedMusic(indice));
            LOGGER.log(Level.INFO, "MusicaCombate -> " + indice);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "MusicaCombate enviar: " + e.getMessage());
        }
    }
}
