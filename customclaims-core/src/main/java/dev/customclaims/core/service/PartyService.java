package dev.customclaims.core.service;

import dev.customclaims.core.api.PartyAdapter;
import dev.customclaims.core.api.model.ClaimSideDisplayInfo;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.api.model.PartyDisplayInfo;
import dev.customclaims.core.api.model.PartyId;
import com.mojang.authlib.GameProfile;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class PartyService {
    private final PartyAdapter partyAdapter;

    public PartyService(PartyAdapter partyAdapter) {
        this.partyAdapter = partyAdapter;
    }

    public Optional<PartyId> getPlayerParty(ServerPlayer player) {
        return partyAdapter.getPlayerParty(player);
    }

    public ClaimSideId getPlayerSide(ServerPlayer player) {
        return getPlayerParty(player)
                .map(ClaimSideId::party)
                .orElseGet(() -> ClaimSideId.player(player.getUUID()));
    }

    public boolean isSameParty(ServerPlayer player, PartyId partyId) {
        return partyAdapter.isPlayerInParty(player, partyId);
    }

    public boolean isSameSide(ServerPlayer player, ClaimSideId sideId) {
        if (sideId.isPlayer()) {
            return sideId.playerUuid().filter(player.getUUID()::equals).isPresent();
        }
        return sideId.partyId().map(partyId -> isSameParty(player, partyId)).orElse(false);
    }

    public Collection<ServerPlayer> onlineMembers(MinecraftServer server, PartyId partyId) {
        return partyAdapter.getOnlinePartyMembers(server, partyId);
    }

    public Collection<ServerPlayer> onlineSideMembers(MinecraftServer server, ClaimSideId sideId) {
        if (sideId.isPlayer()) {
            return sideId.playerUuid()
                    .map(playerId -> server.getPlayerList().getPlayer(playerId))
                    .map(List::of)
                    .orElseGet(List::of);
        }
        return sideId.partyId().map(partyId -> onlineMembers(server, partyId)).orElseGet(List::of);
    }

    public Collection<UUID> memberIds(MinecraftServer server, PartyId partyId) {
        return partyAdapter.getPartyMemberIds(server, partyId);
    }

    public Collection<UUID> sideMemberIds(MinecraftServer server, ClaimSideId sideId) {
        if (sideId.isPlayer()) {
            return sideId.playerUuid().map(List::of).orElseGet(List::of);
        }
        return sideId.partyId().map(partyId -> memberIds(server, partyId)).orElseGet(List::of);
    }

    public Optional<PartyDisplayInfo> describeParty(MinecraftServer server, PartyId partyId) {
        return partyAdapter.describeParty(server, partyId);
    }

    public Optional<ClaimSideDisplayInfo> describeSide(MinecraftServer server, ClaimSideId sideId) {
        if (sideId.isParty()) {
            return sideId.partyId()
                    .flatMap(partyId -> describeParty(server, partyId))
                    .map(info -> new ClaimSideDisplayInfo(
                            sideId,
                            info.name(),
                            info.ownerName(),
                            info.onlineCount(),
                            info.memberCount()
                    ));
        }

        Optional<UUID> playerId = sideId.playerUuid();
        if (playerId.isEmpty()) {
            return Optional.empty();
        }

        ServerPlayer online = server.getPlayerList().getPlayer(playerId.get());
        String name = online == null
                ? server.getProfileCache()
                .get(playerId.get())
                .map(GameProfile::getName)
                .orElse(sideId.shortLabel())
                : online.getGameProfile().getName();
        return Optional.of(new ClaimSideDisplayInfo(
                sideId,
                name,
                name,
                online == null ? 0 : 1,
                1
        ));
    }
}
