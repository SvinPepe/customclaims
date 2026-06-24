package dev.customclaims.core.opc;

import dev.customclaims.core.api.ClaimAdapter;
import dev.customclaims.core.api.PartyAdapter;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.config.CoreConfig;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.scores.Team;

public final class OpenPartiesClaimAdapter implements ClaimAdapter, PartyAdapter {
    private final Map<ChunkPosKey, PartyId> fallbackClaimOwners = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return "open_parties_and_claims_fallback";
    }

    @Override
    public Optional<PartyId> getClaimOwner(ServerLevel level, ChunkPos chunkPos) {
        // TODO: Replace this fallback map with Open Parties and Claims claim lookup API.
        return Optional.ofNullable(fallbackClaimOwners.get(ChunkPosKey.from(level, chunkPos)));
    }

    @Override
    public boolean transferClaim(ServerLevel level, ChunkPos chunkPos, PartyId newOwner) {
        // TODO: Prefer a real Open Parties and Claims transfer/unclaim+claim integration here.
        if (!CoreConfig.ALLOW_FALLBACK_CLAIM_TRANSFER.get()) {
            return false;
        }

        fallbackClaimOwners.put(ChunkPosKey.from(level, chunkPos), newOwner);
        return true;
    }

    @Override
    public Optional<PartyId> getPlayerParty(ServerPlayer player) {
        // TODO: Replace scoreboard fallback with Open Parties and Claims party lookup API.
        if (!CoreConfig.SCOREBOARD_TEAM_PARTY_FALLBACK.get()) {
            return Optional.empty();
        }

        Team team = player.getTeam();
        if (team == null) {
            return Optional.empty();
        }
        return Optional.of(PartyId.of(team.getName()));
    }

    @Override
    public boolean isPlayerInParty(ServerPlayer player, PartyId partyId) {
        return getPlayerParty(player).filter(partyId::equals).isPresent();
    }

    @Override
    public Collection<ServerPlayer> getOnlinePartyMembers(MinecraftServer server, PartyId partyId) {
        return server.getPlayerList().getPlayers().stream()
                .filter(player -> isPlayerInParty(player, partyId))
                .toList();
    }

    public void recordFallbackClaim(ServerLevel level, ChunkPos chunkPos, PartyId owner) {
        fallbackClaimOwners.put(ChunkPosKey.from(level, chunkPos), owner);
    }
}
