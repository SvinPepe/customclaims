package dev.customclaims.protection.compat;

import dev.customclaims.api.war.IWarfareBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.lang.reflect.Method;

public final class WarfareBridgeHolder {
    private static volatile IWarfareBridge localBridge = (level, chunkPos) -> false;
    private static volatile Boolean warfareAvailable = null;
    private static volatile Method remoteCheckMethod = null;

    public static void register(IWarfareBridge bridge) {
        if (bridge != null) {
            localBridge = bridge;
        }
    }

    public static boolean isContestedWarChunk(ServerLevel level, ChunkPos chunkPos) {
        if (warfareAvailable == null) {
            initBridge();
        }
        if (Boolean.TRUE.equals(warfareAvailable) && remoteCheckMethod != null) {
            try {
                return (boolean) remoteCheckMethod.invoke(null, level, chunkPos);
            } catch (Throwable ignored) {
                // Fall back to localBridge on error
            }
        }
        return localBridge != null && localBridge.isContestedWarChunk(level, chunkPos);
    }

    private static synchronized void initBridge() {
        if (warfareAvailable != null) {
            return;
        }
        try {
            Class<?> holderClass = Class.forName("dev.customclaims.api.war.WarfareBridgeHolder");
            remoteCheckMethod = holderClass.getMethod("isContestedWarChunk", ServerLevel.class, ChunkPos.class);
            warfareAvailable = true;
        } catch (Throwable t) {
            warfareAvailable = false;
        }
    }

    private WarfareBridgeHolder() {
    }
}
