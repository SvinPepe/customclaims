package dev.customclaims.protection.compat;

import dev.customclaims.api.war.IWarfareBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public final class WarfareBridgeHolder {
    public static void register(IWarfareBridge bridge) {
        dev.customclaims.api.war.WarfareBridgeHolder.register(bridge);
    }

    public static boolean isContestedWarChunk(ServerLevel level, ChunkPos chunkPos) {
        return dev.customclaims.api.war.WarfareBridgeHolder.isContestedWarChunk(level, chunkPos);
    }

    private WarfareBridgeHolder() {
    }
}
