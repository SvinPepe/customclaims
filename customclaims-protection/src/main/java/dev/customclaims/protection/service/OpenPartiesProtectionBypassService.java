package dev.customclaims.protection.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import xaero.pac.common.server.api.OpenPACServerAPI;

public final class OpenPartiesProtectionBypassService {
    private final Map<UUID, MinecraftServer> activeFullPasses = new ConcurrentHashMap<>();

    public void grantUntilNextServerTick(ServerLevel level, Entity entity) {
        grantUntilNextServerTick(level.getServer(), entity.getUUID());
    }

    public void grantUntilNextServerTick(ServerLevel level, UUID entityId) {
        grantUntilNextServerTick(level.getServer(), entityId);
    }

    public void grantUntilNextServerTick(MinecraftServer server, UUID entityId) {
        OpenPACServerAPI.get(server).getChunkProtection().giveFullPass(entityId);
        activeFullPasses.put(entityId, server);
    }

    public void clearFullPasses() {
        activeFullPasses.forEach((entityId, server) ->
                OpenPACServerAPI.get(server).getChunkProtection().removeFullPass(entityId));
        activeFullPasses.clear();
    }
}
