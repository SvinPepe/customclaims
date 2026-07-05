package dev.customclaims.xaero.service;

import dev.customclaims.war.model.WarMarkerDto;
import dev.customclaims.xaero.CustomClaimsXaeroMod;
import dev.customclaims.xaero.config.XaeroCompatConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.fml.ModList;

public final class XaeroWaypointService {
    private static final String WAYPOINT_SYMBOL = "W";
    private static final Map<String, CreatedWaypoint> CREATED_WAYPOINTS_BY_MARKER = new HashMap<>();
    private static final Map<String, Instant> LAST_REFRESH_BY_MARKER = new HashMap<>();
    private static boolean missingXaeroLogged;
    private static boolean namedWaypointUnavailableLogged;
    private static boolean waypointErrorLogged;
    private static boolean waypointRemovalErrorLogged;

    private XaeroWaypointService() {
    }

    public static void replaceWarMarkers(List<WarMarkerDto> markers) {
        if (!XaeroCompatConfig.XAERO_WAYPOINTS_ENABLED.get()) {
            removeAllCreatedWaypoints();
            LAST_REFRESH_BY_MARKER.clear();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        String currentDimension = player.level().dimension().location().toString();
        Map<String, WarMarkerDto> visibleMarkers = new HashMap<>();
        for (WarMarkerDto marker : markers) {
            if (currentDimension.equals(marker.dimension())) {
                visibleMarkers.put(key(marker), marker);
            }
        }

        removeStaleWaypoints(visibleMarkers.keySet());

        Instant now = Instant.now();
        for (WarMarkerDto marker : visibleMarkers.values()) {
            String key = key(marker);
            CreatedWaypoint desired = waypoint(marker, player);
            CreatedWaypoint existing = CREATED_WAYPOINTS_BY_MARKER.get(key);
            if (existing != null && !existing.sameTarget(desired)) {
                removeCreatedWaypoint(existing);
                CREATED_WAYPOINTS_BY_MARKER.remove(key);
                LAST_REFRESH_BY_MARKER.remove(key);
                existing = null;
            }

            Instant lastRefresh = LAST_REFRESH_BY_MARKER.get(key);
            if (existing != null && lastRefresh != null && Duration.between(lastRefresh, now).getSeconds()
                    < XaeroCompatConfig.XAERO_WAYPOINT_REFRESH_INTERVAL_SECONDS.get()) {
                continue;
            }
            if (createTemporaryWaypoint(desired)) {
                CREATED_WAYPOINTS_BY_MARKER.put(key, desired);
                LAST_REFRESH_BY_MARKER.put(key, now);
            }
        }
    }

    private static CreatedWaypoint waypoint(WarMarkerDto marker, LocalPlayer player) {
        return new CreatedWaypoint(
                key(marker),
                marker.dimension(),
                marker.chunkX() * 16 + 8,
                Math.max(-64, Math.min(320, player.blockPosition().getY())),
                marker.chunkZ() * 16 + 8,
                trimWaypointName(marker.waypointName())
        );
    }

    private static void removeStaleWaypoints(Set<String> activeMarkerKeys) {
        Iterator<Map.Entry<String, CreatedWaypoint>> iterator = CREATED_WAYPOINTS_BY_MARKER.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CreatedWaypoint> entry = iterator.next();
            if (activeMarkerKeys.contains(entry.getKey())) {
                continue;
            }
            removeCreatedWaypoint(entry.getValue());
            iterator.remove();
            LAST_REFRESH_BY_MARKER.remove(entry.getKey());
        }
        LAST_REFRESH_BY_MARKER.keySet().removeIf(key -> !activeMarkerKeys.contains(key));
    }

    private static void removeAllCreatedWaypoints() {
        for (CreatedWaypoint waypoint : CREATED_WAYPOINTS_BY_MARKER.values()) {
            removeCreatedWaypoint(waypoint);
        }
        CREATED_WAYPOINTS_BY_MARKER.clear();
    }

    private static boolean createTemporaryWaypoint(CreatedWaypoint waypoint) {
        if (!ModList.get().isLoaded("xaerominimap") && !ModList.get().isLoaded("xaeroworldmap")) {
            logMissingXaeroOnce();
            return false;
        }

        Throwable failure = null;
        try {
            if (createNamedThroughWorldMapSupport(waypoint)) {
                return true;
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            failure = exception;
        }

        try {
            if (createNamedThroughMinimapSession(waypoint)) {
                return true;
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            if (failure == null) {
                failure = exception;
            }
        }

        if (failure != null) {
            logWaypointFailureOnce("Could not create Xaero temporary war waypoint", failure);
        } else {
            logNamedWaypointUnavailableOnce();
        }
        return false;
    }

    private static boolean removeCreatedWaypoint(CreatedWaypoint waypoint) {
        boolean removed = false;
        Throwable failure = null;
        try {
            removed = removeNamedThroughWorldMapSupport(waypoint);
        } catch (ReflectiveOperationException | LinkageError exception) {
            failure = exception;
        }

        try {
            removed = removeNamedThroughMinimapSession(waypoint) || removed;
        } catch (ReflectiveOperationException | LinkageError exception) {
            if (failure == null) {
                failure = exception;
            }
        }

        if (failure != null) {
            logWaypointRemovalFailureOnce(failure);
        }
        return removed;
    }

    private static boolean createNamedThroughWorldMapSupport(CreatedWaypoint waypoint)
            throws ReflectiveOperationException {
        Class<?> supportModsClass = findClass("xaero.map.mods.SupportMods");
        Class<?> waypointClass = findClass("xaero.map.mods.gui.Waypoint");
        if (supportModsClass == null || waypointClass == null) {
            return false;
        }

        Object minimapSupport = supportModsClass.getField("xaeroMinimap").get(null);
        if (minimapSupport == null) {
            return false;
        }

        Object xaeroWaypoint = newWorldMapWaypoint(waypointClass, minimapSupport, waypoint);
        try {
            waypointClass.getMethod("setTemporary", boolean.class).invoke(xaeroWaypoint, true);
        } catch (NoSuchMethodException ignored) {
            // Older compatible APIs can still accept the waypoint through toggleTemporaryWaypoint.
        }
        try {
            Method waypointExists = minimapSupport.getClass().getMethod("waypointExists", waypointClass);
            Object exists = waypointExists.invoke(minimapSupport, xaeroWaypoint);
            if (Boolean.TRUE.equals(exists)) {
                return true;
            }
        } catch (NoSuchMethodException ignored) {
            // If the compatibility API cannot check existence, fall through to the old toggle path.
        }

        Method toggleTemporaryWaypoint = minimapSupport.getClass()
                .getMethod("toggleTemporaryWaypoint", waypointClass);
        toggleTemporaryWaypoint.invoke(minimapSupport, xaeroWaypoint);
        return true;
    }

    private static boolean removeNamedThroughWorldMapSupport(CreatedWaypoint waypoint)
            throws ReflectiveOperationException {
        Class<?> supportModsClass = findClass("xaero.map.mods.SupportMods");
        Class<?> waypointClass = findClass("xaero.map.mods.gui.Waypoint");
        if (supportModsClass == null || waypointClass == null) {
            return false;
        }

        Object minimapSupport = supportModsClass.getField("xaeroMinimap").get(null);
        if (minimapSupport == null) {
            return false;
        }

        Method waypointExists;
        try {
            waypointExists = minimapSupport.getClass().getMethod("waypointExists", waypointClass);
        } catch (NoSuchMethodException ignored) {
            return false;
        }

        Object xaeroWaypoint = newWorldMapWaypoint(waypointClass, minimapSupport, waypoint);
        Object exists = waypointExists.invoke(minimapSupport, xaeroWaypoint);
        if (!Boolean.TRUE.equals(exists)) {
            return false;
        }

        Method toggleTemporaryWaypoint = minimapSupport.getClass()
                .getMethod("toggleTemporaryWaypoint", waypointClass);
        toggleTemporaryWaypoint.invoke(minimapSupport, xaeroWaypoint);
        return true;
    }

    private static Object newWorldMapWaypoint(Class<?> waypointClass, Object minimapSupport, CreatedWaypoint waypoint)
            throws ReflectiveOperationException {
        Constructor<?> constructor = waypointClass.getConstructor(
                Object.class,
                int.class,
                int.class,
                int.class,
                String.class,
                String.class,
                int.class,
                int.class,
                boolean.class,
                String.class,
                boolean.class,
                double.class
        );
        return constructor.newInstance(
                null,
                waypoint.blockX(),
                waypoint.blockY(),
                waypoint.blockZ(),
                waypoint.name(),
                WAYPOINT_SYMBOL,
                0,
                0,
                false,
                "",
                false,
                dimDiv(minimapSupport)
        );
    }

    private static boolean createNamedThroughMinimapSession(CreatedWaypoint waypoint)
            throws ReflectiveOperationException {
        Class<?> waypointClass = findClass("xaero.common.minimap.waypoints.Waypoint");
        MinimapWaypointAccess access = currentMinimapWaypointAccess();
        if (waypointClass == null || access == null) {
            return false;
        }

        if (containsMatchingMinimapWaypoint(access.waypoints(), waypoint)) {
            return true;
        }

        Constructor<?> constructor = waypointClass.getConstructor(
                int.class,
                int.class,
                int.class,
                String.class,
                String.class,
                int.class
        );
        Object xaeroWaypoint = constructor.newInstance(
                waypoint.blockX(),
                waypoint.blockY(),
                waypoint.blockZ(),
                waypoint.name(),
                WAYPOINT_SYMBOL,
                0
        );
        setOptionalBoolean(xaeroWaypoint, "setTemporary", true);
        setOptionalBoolean(xaeroWaypoint, "setYIncluded", true);
        addWaypoint(access.waypoints(), xaeroWaypoint);
        updateWaypoints(access);
        return true;
    }

    private static boolean removeNamedThroughMinimapSession(CreatedWaypoint waypoint)
            throws ReflectiveOperationException {
        MinimapWaypointAccess access = currentMinimapWaypointAccess();
        if (access == null) {
            return false;
        }

        boolean removed = false;
        Iterator<?> iterator = access.waypoints().iterator();
        while (iterator.hasNext()) {
            Object candidate = iterator.next();
            if (matchesMinimapWaypoint(candidate, waypoint)) {
                iterator.remove();
                removed = true;
            }
        }
        if (removed) {
            updateWaypoints(access);
        }
        return removed;
    }

    private static MinimapWaypointAccess currentMinimapWaypointAccess() throws ReflectiveOperationException {
        Class<?> sessionClass = findClass("xaero.common.XaeroMinimapSession");
        if (sessionClass == null) {
            return null;
        }
        Object session = sessionClass.getMethod("getCurrentSession").invoke(null);
        if (session == null) {
            return null;
        }

        Object waypointsManager = session.getClass().getMethod("getWaypointsManager").invoke(session);
        Object waypointWorld = waypointsManager.getClass().getMethod("getCurrentWorld").invoke(waypointsManager);
        if (waypointWorld == null) {
            return null;
        }
        Object waypointSet = waypointWorld.getClass().getMethod("getCurrentSet").invoke(waypointWorld);
        if (waypointSet == null) {
            return null;
        }
        Object list = waypointSet.getClass().getMethod("getList").invoke(waypointSet);
        if (list instanceof List<?> waypoints) {
            return new MinimapWaypointAccess(waypointsManager, waypoints);
        }
        return null;
    }

    private static boolean containsMatchingMinimapWaypoint(List<?> waypoints, CreatedWaypoint waypoint)
            throws ReflectiveOperationException {
        for (Object candidate : waypoints) {
            if (matchesMinimapWaypoint(candidate, waypoint)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesMinimapWaypoint(Object candidate, CreatedWaypoint waypoint)
            throws ReflectiveOperationException {
        if (!booleanValue(candidate, "isTemporary", false)) {
            return false;
        }
        return intValue(candidate, "getX") == waypoint.blockX()
                && intValue(candidate, "getY") == waypoint.blockY()
                && intValue(candidate, "getZ") == waypoint.blockZ()
                && waypoint.name().equals(stringValue(candidate, "getName"));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addWaypoint(List<?> waypoints, Object waypoint) {
        ((List) waypoints).add(waypoint);
    }

    private static void updateWaypoints(MinimapWaypointAccess access) throws ReflectiveOperationException {
        try {
            access.waypointsManager().getClass().getMethod("updateWaypoints").invoke(access.waypointsManager());
        } catch (NoSuchMethodException ignored) {
            // The in-memory list was already updated.
        }
    }

    private static void setOptionalBoolean(Object target, String methodName, boolean value)
            throws ReflectiveOperationException {
        try {
            target.getClass().getMethod(methodName, boolean.class).invoke(target, value);
        } catch (NoSuchMethodException ignored) {
            // Older APIs can still show a named waypoint without the optional flag.
        }
    }

    private static boolean booleanValue(Object target, String methodName, boolean fallback)
            throws ReflectiveOperationException {
        try {
            Object value = target.getClass().getMethod(methodName).invoke(target);
            if (value instanceof Boolean booleanValue) {
                return booleanValue;
            }
        } catch (NoSuchMethodException ignored) {
            return fallback;
        }
        return fallback;
    }

    private static int intValue(Object target, String methodName) throws ReflectiveOperationException {
        Object value = target.getClass().getMethod(methodName).invoke(target);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.MIN_VALUE;
    }

    private static String stringValue(Object target, String methodName) throws ReflectiveOperationException {
        Object value = target.getClass().getMethod(methodName).invoke(target);
        return value instanceof String string ? string : "";
    }

    private static Class<?> findClass(String name) throws ReflectiveOperationException {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }

    private static double dimDiv(Object minimapSupport) {
        try {
            Object value = minimapSupport.getClass().getMethod("getDimDiv").invoke(minimapSupport);
            if (value instanceof Number number) {
                return number.doubleValue();
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Fallback to overworld scale if the optional method is unavailable.
        }
        return 1.0D;
    }

    private static String trimWaypointName(String value) {
        String name = value == null || value.isBlank() ? "CustomClaims War" : value.trim();
        if (name.length() <= 32) {
            return name;
        }
        return name.substring(0, 29) + "...";
    }

    private static void logMissingXaeroOnce() {
        if (!missingXaeroLogged) {
            CustomClaimsXaeroMod.LOGGER.info(
                    "Xaero temporary war waypoints are enabled, but Xaero integration is not available yet"
            );
            missingXaeroLogged = true;
        }
    }

    private static void logNamedWaypointUnavailableOnce() {
        if (!namedWaypointUnavailableLogged) {
            CustomClaimsXaeroMod.LOGGER.info(
                    "Xaero temporary war waypoints are enabled, but no named waypoint API is available; skipping war waypoint creation"
            );
            namedWaypointUnavailableLogged = true;
        }
    }

    private static void logWaypointFailureOnce(String message, Throwable failure) {
        if (!waypointErrorLogged) {
            CustomClaimsXaeroMod.LOGGER.warn(message, failure);
            waypointErrorLogged = true;
        }
    }

    private static void logWaypointRemovalFailureOnce(Throwable failure) {
        if (!waypointRemovalErrorLogged) {
            CustomClaimsXaeroMod.LOGGER.warn("Could not remove Xaero temporary war waypoint", failure);
            waypointRemovalErrorLogged = true;
        }
    }

    private static String key(WarMarkerDto marker) {
        return marker.dimension() + ":" + marker.chunkX() + ":" + marker.chunkZ();
    }

    private record CreatedWaypoint(
            String markerKey,
            String dimension,
            int blockX,
            int blockY,
            int blockZ,
            String name
    ) {
        private boolean sameTarget(CreatedWaypoint other) {
            return dimension.equals(other.dimension)
                    && blockX == other.blockX
                    && blockY == other.blockY
                    && blockZ == other.blockZ
                    && name.equals(other.name);
        }
    }

    private record MinimapWaypointAccess(Object waypointsManager, List<?> waypoints) {
    }
}