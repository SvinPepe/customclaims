package dev.customclaims.core.service;

import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.api.model.TerritoryStatus;
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

    public Optional<TerritoryStatus> statusOverride(ChunkPosKey key) {
        return Optional.ofNullable(statusOverrides.get(key));
    }

    public Optional<ClaimSideId> ownerOverride(ChunkPosKey key) {
        return Optional.ofNullable(ownerOverrides.get(key));
    }

    public void markContested(ChunkPosKey key, ClaimSideId attacker, ClaimSideId defender) {
        statusOverrides.put(key, TerritoryStatus.WAR_CONTESTED);
        contestedParties.put(key, new ContestedSides(attacker, defender));
    }

    public boolean isContestedParticipant(ChunkPosKey key, ClaimSideId sideId) {
        ContestedSides sides = contestedParties.get(key);
        return sides != null && sides.contains(sideId);
    }

    public void clearStatus(ChunkPosKey key) {
        statusOverrides.remove(key);
        contestedParties.remove(key);
    }

    public void setOwnerOverride(ChunkPosKey key, ClaimSideId owner) {
        ownerOverrides.put(key, owner);
    }

    public void clearOwnerOverride(ChunkPosKey key) {
        ownerOverrides.remove(key);
    }
}
