package dev.customclaims.core.service;

import dev.customclaims.core.api.PartyAdapter;
import dev.customclaims.core.api.model.PartyDisplayInfo;
import dev.customclaims.core.api.model.PartyId;
import java.util.Collection;
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

    public boolean isSameParty(ServerPlayer player, PartyId partyId) {
        return partyAdapter.isPlayerInParty(player, partyId);
    }

    public Collection<ServerPlayer> onlineMembers(MinecraftServer server, PartyId partyId) {
        return partyAdapter.getOnlinePartyMembers(server, partyId);
    }

    public Collection<UUID> memberIds(MinecraftServer server, PartyId partyId) {
        return partyAdapter.getPartyMemberIds(server, partyId);
    }

    public Optional<PartyDisplayInfo> describeParty(MinecraftServer server, PartyId partyId) {
        return partyAdapter.describeParty(server, partyId);
    }
}
