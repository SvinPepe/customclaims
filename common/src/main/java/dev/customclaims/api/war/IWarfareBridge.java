package dev.customclaims.api.war;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public interface IWarfareBridge {
    /**
     * Checks if the given chunk in the given level is part of an active war/siege.
     */
    boolean isContestedWarChunk(ServerLevel level, ChunkPos chunkPos);
}
