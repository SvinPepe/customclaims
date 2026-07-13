package dev.customclaims.aeronautics.compat;

import dev.customclaims.aeronautics.CustomClaimsAeronauticsMod;
import dev.customclaims.protection.CustomClaimsProtectionMod;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class SableAssemblyProtectionHooks {
    private static boolean boundsReflectionWarningLogged;

    private SableAssemblyProtectionHooks() {
    }

    public static boolean shouldBlockAssembly(ServerLevel level, Iterable<BlockPos> blocks, Object bounds) {
        List<BlockPos> corners = cornersOf(bounds);
        if (corners.isEmpty()) {
            return !CustomClaimsProtectionMod.services()
                    .createMachinesProtectionService()
                    .canCreateAssembly(level, blocks);
        }
        return !CustomClaimsProtectionMod.services()
                .createMachinesProtectionService()
                .canCreateAssembly(level, blocks, corners);
    }

    private static List<BlockPos> cornersOf(Object bounds) {
        if (bounds == null) {
            logBoundsReflectionFailure(new IllegalArgumentException("Sable assembly bounds were null"));
            return List.of();
        }

        try {
            int minX = coordinate(bounds, "minX");
            int minY = coordinate(bounds, "minY");
            int minZ = coordinate(bounds, "minZ");
            int maxX = coordinate(bounds, "maxX");
            int maxY = coordinate(bounds, "maxY");
            int maxZ = coordinate(bounds, "maxZ");
            return List.of(
                    new BlockPos(minX, minY, minZ), new BlockPos(minX, minY, maxZ),
                    new BlockPos(minX, maxY, minZ), new BlockPos(minX, maxY, maxZ),
                    new BlockPos(maxX, minY, minZ), new BlockPos(maxX, minY, maxZ),
                    new BlockPos(maxX, maxY, minZ), new BlockPos(maxX, maxY, maxZ)
            );
        } catch (ReflectiveOperationException | LinkageError exception) {
            logBoundsReflectionFailure(exception);
            return List.of();
        }
    }

    private static int coordinate(Object bounds, String methodName) throws ReflectiveOperationException {
        Method method = bounds.getClass().getMethod(methodName);
        Object value = method.invoke(bounds);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalStateException("Sable assembly bound " + methodName + " was not numeric");
    }

    private static void logBoundsReflectionFailure(Throwable exception) {
        if (boundsReflectionWarningLogged) {
            return;
        }
        CustomClaimsAeronauticsMod.LOGGER.warn(
                "Could not read Sable assembly bounds; falling back to assembled block positions",
                exception
        );
        boundsReflectionWarningLogged = true;
    }
}

