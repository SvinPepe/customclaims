package dev.customclaims.api.war;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public final class WarfareBridgeHolder {
    private static volatile IWarfareBridge bridge = (level, chunkPos) -> false;

    public static void register(IWarfareBridge newBridge) {
        if (newBridge != null) {
            bridge = newBridge;
        }
    }

    public static boolean isContestedWarChunk(ServerLevel level, ChunkPos chunkPos) {
        return bridge != null && bridge.isContestedWarChunk(level, chunkPos);
    }

    private WarfareBridgeHolder() {
    }
}
