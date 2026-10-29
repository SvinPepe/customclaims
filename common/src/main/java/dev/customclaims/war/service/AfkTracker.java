package dev.customclaims.war.service;

import dev.customclaims.war.config.WarConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;

public final class AfkTracker {
    private final Map<UUID, Instant> lastActivity = new ConcurrentHashMap<>();

    public void markActive(ServerPlayer player) {
        lastActivity.put(player.getUUID(), Instant.now());
    }

    public boolean isAfk(ServerPlayer player) {
        Instant lastSeen = lastActivity.get(player.getUUID());
        if (lastSeen == null) {
            markActive(player);
            return false;
        }
        return Duration.between(lastSeen, Instant.now()).getSeconds() >= WarConfig.AFK_SECONDS.get();
    }
}
