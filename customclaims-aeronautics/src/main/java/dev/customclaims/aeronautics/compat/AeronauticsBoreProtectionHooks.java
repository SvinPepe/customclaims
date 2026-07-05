package dev.customclaims.aeronautics.compat;

import dev.customclaims.aeronautics.CustomClaimsAeronauticsMod;
import dev.customclaims.protection.CustomClaimsProtectionMod;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class AeronauticsBoreProtectionHooks {
    private static final String TICK_RESULT_CLASS = "dev.ryanhcode.offroad.handlers.MultiminingDataTickResult";
    private static final String STOP_RESULT = "STOP";

    private static Object stopTickResult;
    private static boolean stopTickResultLookupFailed;
    private static boolean stallReflectionWarningLogged;

    private AeronauticsBoreProtectionHooks() {
    }

    public static boolean shouldBlockBoreMining(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return !CustomClaimsProtectionMod.services()
                .createMachinesProtectionService()
                .canCreateAffect(serverLevel, pos);
    }

    public static void stallSupplier(Object supplier) {
        if (supplier == null) {
            return;
        }

        try {
            Method setStalled = supplier.getClass().getMethod("setStalled", boolean.class);
            setStalled.invoke(supplier, true);
        } catch (NoSuchMethodException ignored) {
            // Some Offroad MultiMiningSupplier implementations are not Borehead bearings.
        } catch (ReflectiveOperationException | LinkageError exception) {
            if (!stallReflectionWarningLogged) {
                CustomClaimsAeronauticsMod.LOGGER.warn(
                        "Could not stall protected Aeronautics/Offroad bore supplier",
                        exception
                );
                stallReflectionWarningLogged = true;
            }
        }
    }

    public static Object stopTickResult() {
        if (stopTickResult != null || stopTickResultLookupFailed) {
            return stopTickResult;
        }

        try {
            Class<?> resultClass = Class.forName(TICK_RESULT_CLASS);
            Object[] constants = resultClass.getEnumConstants();
            if (constants != null) {
                for (Object constant : constants) {
                    if (constant instanceof Enum<?> enumConstant && STOP_RESULT.equals(enumConstant.name())) {
                        stopTickResult = constant;
                        return stopTickResult;
                    }
                }
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            CustomClaimsAeronauticsMod.LOGGER.warn(
                    "Could not resolve Aeronautics/Offroad multi-mining STOP result",
                    exception
            );
        }

        stopTickResultLookupFailed = true;
        return null;
    }
}