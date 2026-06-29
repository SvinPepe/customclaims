package dev.customclaims.core.service;

import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.api.model.TerritoryStatus;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class TerritoryStateService {
    private record ContestedSides(ClaimSideId attacker, ClaimSideId defender) {
        private boolean contains(ClaimSideId sideId) {
            return attacker.equals(sideId) || defender.equals(sideId);
        }
    }

    private final Map<ChunkPosKey, TerritoryStatus> statusOverrides = new ConcurrentHashMap<>();
    private final Map<ChunkPosKey, ClaimSideId> ownerOverrides = new ConcurrentHashMap<>();
    private final Map<ChunkPosKey, ContestedSides> contestedParties = new ConcurrentHashMap<>();
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

    public Optional<ClaimSideId> ownerOverride(ChunkPosKey key) {
        return Optional.ofNullable(ownerOverrides.get(key));
    }

    public void markContested(ChunkPosKey key, ClaimSideId attacker, ClaimSideId defender) {
        statusOverrides.put(key, TerritoryStatus.WAR_CONTESTED);
        contestedParties.put(key, new ContestedSides(attacker, defender));
        postWarProtectionUntil.remove(key);
    }

    public boolean isContestedParticipant(ChunkPosKey key, ClaimSideId sideId) {
        ContestedSides sides = contestedParties.get(key);
        return sides != null && sides.contains(sideId);
    }

    public void markPostWarProtected(ChunkPosKey key, Instant until) {
        statusOverrides.put(key, TerritoryStatus.POST_WAR_PROTECTED);
        postWarProtectionUntil.put(key, until);
        contestedParties.remove(key);
    }

    public void clearStatus(ChunkPosKey key) {
        statusOverrides.remove(key);
        postWarProtectionUntil.remove(key);
        contestedParties.remove(key);
    }

    public void setOwnerOverride(ChunkPosKey key, ClaimSideId owner) {
        ownerOverrides.put(key, owner);
    }

    public void clearOwnerOverride(ChunkPosKey key) {
        ownerOverrides.remove(key);
    }
}
