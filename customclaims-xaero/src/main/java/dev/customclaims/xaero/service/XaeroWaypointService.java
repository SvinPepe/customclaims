package dev.customclaims.xaero.service;

import dev.customclaims.war.model.WarMarkerDto;
import dev.customclaims.xaero.CustomClaimsXaeroMod;
import dev.customclaims.xaero.config.XaeroCompatConfig;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.fml.ModList;

public final class XaeroWaypointService {
    private static final Map<String, Instant> LAST_REFRESH_BY_MARKER = new HashMap<>();
    private static boolean missingXaeroLogged;
    private static boolean waypointErrorLogged;

    private XaeroWaypointService() {
    }

    public static void replaceWarMarkers(List<WarMarkerDto> markers) {
        if (!XaeroCompatConfig.XAERO_WAYPOINTS_ENABLED.get()) {
            LAST_REFRESH_BY_MARKER.clear();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        Instant now = Instant.now();
        LAST_REFRESH_BY_MARKER.keySet().removeIf(key -> markers.stream().noneMatch(marker -> key(marker).equals(key)));
        for (WarMarkerDto marker : markers) {
            if (!player.level().dimension().location().toString().equals(marker.dimension())) {
                continue;
            }
            String key = key(marker);
            Instant lastRefresh = LAST_REFRESH_BY_MARKER.get(key);
            if (lastRefresh != null && Duration.between(lastRefresh, now).getSeconds()
                    < XaeroCompatConfig.XAERO_WAYPOINT_REFRESH_INTERVAL_SECONDS.get()) {
                continue;
            }
            if (createTemporaryWaypoint(marker, player)) {
                LAST_REFRESH_BY_MARKER.put(key, now);
            }
        }
    }

    private static boolean createTemporaryWaypoint(WarMarkerDto marker, LocalPlayer player) {
        if (!ModList.get().isLoaded("xaerominimap") && !ModList.get().isLoaded("xaeroworldmap")) {
            logMissingXaeroOnce();
            return false;
        }

        int blockX = marker.chunkX() * 16 + 8;
        int blockZ = marker.chunkZ() * 16 + 8;
        int blockY = Math.max(-64, Math.min(320, player.blockPosition().getY()));
        Throwable failure = null;

        try {
            if (createThroughMinimapSession(blockX, blockY, blockZ)) {
                return true;
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            failure = exception;
        }

        try {
            if (createThroughWorldMapSupport(blockX, blockY, blockZ)) {
                return true;
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            if (failure == null) {
                failure = exception;
            }
        }

        if (failure != null) {
            if (!waypointErrorLogged) {
                CustomClaimsXaeroMod.LOGGER.warn("Could not create Xaero temporary war waypoint", failure);
                waypointErrorLogged = true;
            }
        } else {
            logMissingXaeroOnce();
        }
        return false;
    }

    private static boolean createThroughMinimapSession(int blockX, int blockY, int blockZ)
            throws ReflectiveOperationException {
        Class<?> sessionClass = findClass("xaero.common.XaeroMinimapSession");
        if (sessionClass == null) {
            return false;
        }
        Object session = sessionClass.getMethod("getCurrentSession").invoke(null);
        if (session == null) {
            return false;
        }

        Object waypointsManager = session.getClass().getMethod("getWaypointsManager").invoke(session);
        Object waypointWorld = waypointsManager.getClass().getMethod("getCurrentWorld").invoke(waypointsManager);
        if (waypointWorld == null) {
            return false;
        }

        Method createTemporaryWaypoints = Arrays.stream(waypointsManager.getClass().getMethods())
                .filter(method -> method.getName().equals("createTemporaryWaypoints"))
                .filter(method -> method.getParameterCount() == 6)
                .findFirst()
                .orElse(null);
        if (createTemporaryWaypoints == null) {
            return false;
        }
        createTemporaryWaypoints.invoke(waypointsManager, waypointWorld, blockX, blockY, blockZ, false, 1.0D);
        return true;
    }

    private static boolean createThroughWorldMapSupport(int blockX, int blockY, int blockZ)
            throws ReflectiveOperationException {
        Class<?> supportModsClass = findClass("xaero.map.mods.SupportMods");
        if (supportModsClass == null) {
            return false;
        }
        Object minimapSupport = supportModsClass.getField("xaeroMinimap").get(null);
        if (minimapSupport == null) {
            logMissingXaeroOnce();
            return false;
        }

        Method createTempWaypoint = minimapSupport.getClass()
                .getMethod("createTempWaypoint", int.class, int.class, int.class, double.class, boolean.class);
        createTempWaypoint.invoke(minimapSupport, blockX, blockY, blockZ, 1.0D, false);
        return true;
    }

    private static Class<?> findClass(String name) throws ReflectiveOperationException {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }

    private static void logMissingXaeroOnce() {
        if (!missingXaeroLogged) {
            CustomClaimsXaeroMod.LOGGER.info(
                    "Xaero temporary war waypoints are enabled, but Xaero Minimap integration is not available yet"
            );
            missingXaeroLogged = true;
        }
    }

    private static String key(WarMarkerDto marker) {
        return marker.dimension() + ":" + marker.chunkX() + ":" + marker.chunkZ();
    }
}
