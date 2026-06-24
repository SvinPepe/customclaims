package dev.customclaims.core.service;

import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.api.model.TerritoryStatus;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class TerritoryStateService {
    private final Map<ChunkPosKey, TerritoryStatus> statusOverrides = new ConcurrentHashMap<>();
    private final Map<ChunkPosKey, PartyId> ownerOverrides = new ConcurrentHashMap<>();
    private final Map<ChunkPosKey, Instant> postWarProtectionUntil = new ConcurrentHashMap<>();

    public Optional<TerritoryStatus> statusOverride(ChunkPosKey key) {
        Instant protectedUntil = postWarProtectionUntil.get(key);
        if (protectedUntil != null && Instant.now().isBefore(protectedUntil)) {
            return Optional.of(TerritoryStatus.POST_WAR_PROTECTED);
        }
        if (protectedUntil != null) {
            postWarProtectionUntil.remove(key);
            statusOverrides.remove(key, TerritoryStatus.POST_WAR_PROTECTED);
        }
        return Optional.ofNullable(statusOverrides.get(key));
    }

    public Optional<PartyId> ownerOverride(ChunkPosKey key) {
        return Optional.ofNullable(ownerOverrides.get(key));
    }

    public void markContested(ChunkPosKey key) {
        statusOverrides.put(key, TerritoryStatus.WAR_CONTESTED);
    }

    public void markPostWarProtected(ChunkPosKey key, Instant until) {
        statusOverrides.put(key, TerritoryStatus.POST_WAR_PROTECTED);
        postWarProtectionUntil.put(key, until);
    }

    public void clearStatus(ChunkPosKey key) {
        statusOverrides.remove(key);
        postWarProtectionUntil.remove(key);
    }

    public void setOwnerOverride(ChunkPosKey key, PartyId owner) {
        ownerOverrides.put(key, owner);
    }
}
