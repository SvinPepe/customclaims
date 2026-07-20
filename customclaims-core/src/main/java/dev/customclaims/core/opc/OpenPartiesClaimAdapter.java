package dev.customclaims.core.opc;

import dev.customclaims.core.api.ClaimAdapter;
import dev.customclaims.core.api.PartyAdapter;
import dev.customclaims.core.api.model.ClaimSnapshot;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.api.model.PartyDisplayInfo;
import dev.customclaims.core.api.model.PartyId;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.claims.api.IServerClaimsManagerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;

public final class OpenPartiesClaimAdapter implements ClaimAdapter, PartyAdapter {
    private static final UUID SERVER_CLAIM_OWNER_ID = new UUID(0L, 0L);

    @Override
    public String name() {
        return "open_parties_and_claims";
    }

    @Override
    public UUID serverClaimOwnerId() {
        return SERVER_CLAIM_OWNER_ID;
    }

    @Override
    public Optional<PartyId> getClaimOwner(ServerLevel level, ChunkPos chunkPos) {
        return getClaimSideOwner(level, chunkPos).flatMap(ClaimSideId::partyId);
    }

    @Override
    public Optional<ClaimSideId> getClaimSideOwner(ServerLevel level, ChunkPos chunkPos) {
        return getClaimSideOwner(api(level.getServer()), level, chunkPos);
    }

    @Override
    public boolean transferClaim(ServerLevel level, ChunkPos chunkPos, PartyId newOwner) {
        return transferClaimToSide(level, chunkPos, ClaimSideId.party(newOwner));
    }

    @Override
    public boolean transferClaimToSide(ServerLevel level, ChunkPos chunkPos, ClaimSideId newOwner) {
        if (newOwner.isPlayer()) {
            UUID ownerUuid = newOwner.playerUuid().orElse(null);
            if (ownerUuid == null) {
                return false;
            }

            IPlayerChunkClaimAPI oldClaim = api(level.getServer()).getServerClaimsManager()
                    .get(level.dimension().location(), chunkPos);
            int subConfigIndex = oldClaim == null ? 0 : oldClaim.getSubConfigIndex();
            boolean forceload = oldClaim != null && oldClaim.isForceloadable();
            return claimFromServer(level, chunkPos, ownerUuid, subConfigIndex, forceload)
                    && getClaimSnapshot(level, chunkPos)
                    .map(snapshot -> snapshot.ownerId().equals(ownerUuid))
                    .orElse(false);
        }

        OpenPACServerAPI api = api(level.getServer());

        Optional<UUID> partyUuid = newOwner.partyId().flatMap(this::parsePartyUuid);
        if (partyUuid.isEmpty()) {
            return false;
        }

        IServerPartyAPI party = api.getPartyManager().getPartyById(partyUuid.get());
        if (party == null) {
            return false;
        }

        UUID ownerUuid = party.getOwner().getUUID();
        IServerClaimsManagerAPI claimsManager = api.getServerClaimsManager();
        IPlayerChunkClaimAPI oldClaim = claimsManager.get(level.dimension().location(), chunkPos);
        int subConfigIndex = oldClaim == null ? 0 : oldClaim.getSubConfigIndex();
        boolean forceload = oldClaim != null && oldClaim.isForceloadable();
        return claimFromServer(level, chunkPos, ownerUuid, subConfigIndex, forceload)
                && getClaimSideOwner(level, chunkPos).filter(newOwner::equals).isPresent();
    }

    @Override
    public Optional<ClaimSnapshot> getClaimSnapshot(ServerLevel level, ChunkPos chunkPos) {
        OpenPACServerAPI api = api(level.getServer());
        IPlayerChunkClaimAPI claim = api.getServerClaimsManager().get(level.dimension().location(), chunkPos);
        if (claim == null) {
            return Optional.empty();
        }
        var playerInfo = api.getServerClaimsManager().getPlayerInfo(claim.getPlayerId());
        return Optional.of(new ClaimSnapshot(
                claim.getPlayerId(),
                playerInfo != null && playerInfo.isPartyOwned(),
                claim.getSubConfigIndex(),
                claim.isForceloadable()
        ));
    }

    @Override
    public boolean claimFromServer(
            ServerLevel level,
            ChunkPos chunkPos,
            UUID ownerId,
            int subConfigIndex,
            boolean forceload
    ) {
        OpenPACServerAPI api = api(level.getServer());
        // claim(...) is OPaC's administrative mutation path. Do not replace this
        // with tryToClaim(...): that is the player request path and applies player
        // limits plus economy integrations intended for manually purchased claims.
        api.getServerClaimsManager().claim(
                level.dimension().location(),
                ownerId,
                subConfigIndex,
                chunkPos.x,
                chunkPos.z,
                forceload
        );
        return getClaimSnapshot(level, chunkPos)
                .map(snapshot -> snapshot.ownerId().equals(ownerId))
                .orElse(false);
    }

    @Override
    public Optional<PartyId> getPlayerParty(ServerPlayer player) {
        IServerPartyAPI party = api(player.server).getPartyManager().getPartyByMember(player.getUUID());
        return Optional.ofNullable(party).map(value -> PartyId.of(value.getId().toString()));
    }

    @Override
    public boolean isPlayerInParty(ServerPlayer player, PartyId partyId) {
        Optional<UUID> partyUuid = parsePartyUuid(partyId);
        if (partyUuid.isEmpty()) {
            return false;
        }

        IServerPartyAPI party = api(player.server).getPartyManager().getPartyById(partyUuid.get());
        return party != null && party.getMemberInfo(player.getUUID()) != null;
    }

    @Override
    public Collection<ServerPlayer> getOnlinePartyMembers(MinecraftServer server, PartyId partyId) {
        Optional<UUID> partyUuid = parsePartyUuid(partyId);
        if (partyUuid.isEmpty()) {
            return java.util.List.of();
        }

        IServerPartyAPI party = api(server).getPartyManager().getPartyById(partyUuid.get());
        if (party == null) {
            return java.util.List.of();
        }
        return party.getOnlineMemberStream().toList();
    }

    @Override
    public Collection<UUID> getPartyMemberIds(MinecraftServer server, PartyId partyId) {
        Optional<UUID> partyUuid = parsePartyUuid(partyId);
        if (partyUuid.isEmpty()) {
            return java.util.List.of();
        }

        IServerPartyAPI party = api(server).getPartyManager().getPartyById(partyUuid.get());
        if (party == null) {
            return java.util.List.of();
        }

        Set<UUID> memberIds = new LinkedHashSet<>();
        memberIds.add(party.getOwner().getUUID());
        party.getMemberInfoStream().map(member -> member.getUUID()).forEach(memberIds::add);
        return java.util.List.copyOf(memberIds);
    }

    @Override
    public Optional<PartyDisplayInfo> describeParty(MinecraftServer server, PartyId partyId) {
        Optional<UUID> partyUuid = parsePartyUuid(partyId);
        if (partyUuid.isEmpty()) {
            return Optional.empty();
        }

        IServerPartyAPI party = api(server).getPartyManager().getPartyById(partyUuid.get());
        if (party == null) {
            return Optional.empty();
        }

        String name = party.getDefaultName();
        String ownerName = party.getOwner().getUsername();
        int onlineCount = (int) party.getOnlineMemberStream().count();
        return Optional.of(new PartyDisplayInfo(
                partyId,
                name,
                ownerName,
                onlineCount,
                party.getMemberCount()
        ));
    }

    private Optional<ClaimSideId> getClaimSideOwner(OpenPACServerAPI api, ServerLevel level, ChunkPos chunkPos) {
        IPlayerChunkClaimAPI claim = api.getServerClaimsManager().get(level.dimension().location(), chunkPos);
        if (claim == null) {
            return Optional.empty();
        }

        IServerPartyAPI party;
        var playerInfo = api.getServerClaimsManager().getPlayerInfo(claim.getPlayerId());
        if (playerInfo != null && playerInfo.isPartyOwned()) {
            party = api.getPartyManager().getPartyByOwner(claim.getPlayerId());
        } else {
            party = api.getPartyManager().getPartyByMember(claim.getPlayerId());
        }

        if (party != null) {
            return Optional.of(ClaimSideId.party(PartyId.of(party.getId().toString())));
        }
        return Optional.of(ClaimSideId.player(claim.getPlayerId()));
    }

    private OpenPACServerAPI api(MinecraftServer server) {
        return OpenPACServerAPI.get(server);
    }

    private Optional<UUID> parsePartyUuid(PartyId partyId) {
        try {
            return Optional.of(UUID.fromString(partyId.value()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
