package com.digitale.sistema;

import com.hypixel.hytale.builtin.mounts.NPCMountComponent;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class MountMovementSupport {
    private static final Logger LOGGER = Logger.getLogger(MountMovementSupport.class.getName());

    private static final ConcurrentHashMap<UUID, MountMode> PLAYER_MODES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, MountMode> PLAYER_APPLIED_MODE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, MovementSettings> PLAYER_ORIGINAL_SETTINGS = new ConcurrentHashMap<>();

    private static final Set<String> FLYING_ROLE_IDS = ConcurrentHashMap.newKeySet();
    private static final Set<String> SWIMMING_ROLE_IDS = ConcurrentHashMap.newKeySet();

    private static final Pattern RE_REFERENCE = Pattern.compile("\\\"Reference\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");

    public enum MountMode {
        DEFAULT,
        AUTO,
        FLY,
        SWIM
    }

    private MountMovementSupport() {
    }

    public static void initializeRoleProfiles() {
        FLYING_ROLE_IDS.clear();
        SWIMMING_ROLE_IDS.clear();

        loadFromPath(Path.of("src/main/resources/Server/NPC/Roles"));
        loadFromPath(Path.of("build/resources/main/Server/NPC/Roles"));

        LOGGER.log(Level.INFO, "[MountSupport] Roles cargados | Fly=" + FLYING_ROLE_IDS.size() + " Swim=" + SWIMMING_ROLE_IDS.size());
    }

    private static void loadFromPath(Path root) {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(p -> p.toString().endsWith(".json")).forEach(MountMovementSupport::indexRoleFile);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[MountSupport] No se pudo leer roles en " + root + " -> " + e.getMessage());
        }
    }

    private static void indexRoleFile(Path file) {
        try {
            String json = Files.readString(file, StandardCharsets.UTF_8);
            Matcher m = RE_REFERENCE.matcher(json);
            if (!m.find()) {
                return;
            }
            String reference = m.group(1);
            String fileName = file.getFileName().toString();
            String roleId = fileName.substring(0, fileName.length() - 5);
            String normalizedRoleId = normalizeRoleName(roleId);

            if (reference.contains("Template_Digimon_Combat_Volador_Companero")) {
                FLYING_ROLE_IDS.add(normalizedRoleId);
            } else if (reference.contains("Template_Digimon_Combat_Nadador_Companero")) {
                SWIMMING_ROLE_IDS.add(normalizedRoleId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "[MountSupport] Ignorando role file " + file + " -> " + e.getMessage());
        }
    }

    public static MountMode getMode(UUID uuid) {
        return PLAYER_MODES.getOrDefault(uuid, MountMode.AUTO);
    }

    public static void setMode(UUID uuid, MountMode mode) {
        PLAYER_MODES.put(uuid, mode);
        if (mode == MountMode.DEFAULT) {
            PLAYER_APPLIED_MODE.remove(uuid);
        }
    }

    public static void clearPlayer(UUID uuid) {
        PLAYER_MODES.remove(uuid);
        PLAYER_APPLIED_MODE.remove(uuid);
        PLAYER_ORIGINAL_SETTINGS.remove(uuid);
    }

    @SuppressWarnings("nullness")
    public static void onClientMovement(PlayerRef playerRef,
                                        Ref<EntityStore> playerEntityRef,
                                        Store<EntityStore> store,
                                        int mountedEntityId) {
        UUID uuid = playerRef.getUuid();
        MountMode selectedMode = getMode(uuid);

        if (mountedEntityId <= 0 || selectedMode == MountMode.DEFAULT) {
            restoreOriginalIfNeeded(playerRef, playerEntityRef, store, uuid);
            return;
        }

        Ref<EntityStore> mountRef = new Ref<>(store, mountedEntityId);
        if (!mountRef.isValid()) {
            restoreOriginalIfNeeded(playerRef, playerEntityRef, store, uuid);
            return;
        }

        NPCMountComponent mountComponent = store.getComponent(mountRef, NPCMountComponent.getComponentType());
        if (mountComponent == null) {
            restoreOriginalIfNeeded(playerRef, playerEntityRef, store, uuid);
            return;
        }

        MountMode effectiveMode = selectedMode;
        String roleName = resolveRoleName(mountComponent.getOriginalRoleIndex());
        String normalizedRoleName = normalizeRoleName(roleName);
        if (selectedMode == MountMode.AUTO) {
            if (normalizedRoleName != null && FLYING_ROLE_IDS.contains(normalizedRoleName)) {
                effectiveMode = MountMode.FLY;
            } else if (normalizedRoleName != null && SWIMMING_ROLE_IDS.contains(normalizedRoleName)) {
                effectiveMode = MountMode.SWIM;
            } else {
                LOGGER.log(Level.FINE, "[MountSupport] AUTO sin perfil para role=" + roleName + " (norm=" + normalizedRoleName + ")");
                restoreOriginalIfNeeded(playerRef, playerEntityRef, store, uuid);
                return;
            }
        }

        applyMode(playerRef, playerEntityRef, store, uuid, effectiveMode, roleName);
    }

    @SuppressWarnings("nullness")
    private static void applyMode(PlayerRef playerRef,
                                  Ref<EntityStore> playerEntityRef,
                                  Store<EntityStore> store,
                                  UUID uuid,
                                  MountMode mode,
                                  String roleName) {
        MovementManager manager = store.getComponent(playerEntityRef, MovementManager.getComponentType());
        PhysicsValues physics = store.getComponent(playerEntityRef, PhysicsValues.getComponentType());
        Player player = store.getComponent(playerEntityRef, Player.getComponentType());
        if (manager == null || physics == null || player == null) {
            return;
        }

        PLAYER_ORIGINAL_SETTINGS.putIfAbsent(uuid, new MovementSettings(manager.getDefaultSettings()));

        MountMode alreadyApplied = PLAYER_APPLIED_MODE.get(uuid);
        if (alreadyApplied == mode) {
            return;
        }

        MovementSettings tuned = new MovementSettings(PLAYER_ORIGINAL_SETTINGS.get(uuid));
        switch (mode) {
            case FLY -> {
                tuned.canFly = true;
                tuned.horizontalFlySpeed = Math.max(tuned.horizontalFlySpeed, 16.0f);
                tuned.verticalFlySpeed = Math.max(tuned.verticalFlySpeed, 11.0f);
                tuned.airSpeedMultiplier = Math.max(tuned.airSpeedMultiplier, 1.40f);
                tuned.baseSpeed = Math.max(tuned.baseSpeed, 8.0f);
                tuned.forwardRunSpeedMultiplier = Math.max(tuned.forwardRunSpeedMultiplier, 1.15f);
            }
            case SWIM -> {
                // En V4, permitir movimiento vertical ayuda a sumergirse al montar nadadores.
                tuned.canFly = true;
                tuned.baseSpeed = Math.max(tuned.baseSpeed, 9.5f);
                tuned.horizontalFlySpeed = Math.max(tuned.horizontalFlySpeed, 8.0f);
                tuned.verticalFlySpeed = Math.max(tuned.verticalFlySpeed, 6.5f);
                tuned.swimJumpForce = Math.max(tuned.swimJumpForce, tuned.jumpForce * 1.85f);
                tuned.jumpForce = Math.max(2.8f, tuned.jumpForce * 0.55f);
                tuned.velocityResistance = Math.max(0.06f, tuned.velocityResistance * 0.78f);
                tuned.forwardRunSpeedMultiplier = Math.max(tuned.forwardRunSpeedMultiplier, 1.20f);
            }
            default -> {
                return;
            }
        }

        manager.setDefaultSettings(tuned, physics, player.getGameMode());
        manager.applyDefaultSettings();
        manager.update(playerRef.getPacketHandler());
        PLAYER_APPLIED_MODE.put(uuid, mode);

        LOGGER.log(Level.INFO, "[MountSupport] Aplicado modo " + mode + " a " + playerRef.getUsername() +
                (roleName == null ? "" : " sobre " + roleName));
    }

    @SuppressWarnings("nullness")
    public static void restoreOriginalIfNeeded(PlayerRef playerRef,
                                               Ref<EntityStore> playerEntityRef,
                                               Store<EntityStore> store,
                                               UUID uuid) {
        MountMode applied = PLAYER_APPLIED_MODE.remove(uuid);
        MovementSettings original = PLAYER_ORIGINAL_SETTINGS.remove(uuid);
        if (applied == null || original == null) {
            return;
        }

        MovementManager manager = store.getComponent(playerEntityRef, MovementManager.getComponentType());
        PhysicsValues physics = store.getComponent(playerEntityRef, PhysicsValues.getComponentType());
        Player player = store.getComponent(playerEntityRef, Player.getComponentType());
        if (manager == null || physics == null || player == null) {
            return;
        }

        manager.setDefaultSettings(original, physics, player.getGameMode());
        manager.applyDefaultSettings();
        manager.update(playerRef.getPacketHandler());
        LOGGER.log(Level.INFO, "[MountSupport] Restaurado movement default para " + playerRef.getUsername());
    }

    private static String resolveRoleName(int roleIndex) {
        NPCPlugin plugin = NPCPlugin.get();
        if (plugin == null || roleIndex < 0) {
            return null;
        }
        try {
            return plugin.getName(roleIndex);
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return null;
        }

        String normalized = roleName.replace('\\', '/').trim();
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash + 1 < normalized.length()) {
            normalized = normalized.substring(lastSlash + 1);
        }
        if (normalized.endsWith(".json")) {
            normalized = normalized.substring(0, normalized.length() - 5);
        }

        return normalized.toLowerCase(Locale.ROOT);
    }
}
