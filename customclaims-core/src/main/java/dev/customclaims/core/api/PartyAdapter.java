package dev.customclaims.core.api;

import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.api.model.PartyDisplayInfo;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface PartyAdapter {
    Optional<PartyId> getPlayerParty(ServerPlayer player);

    boolean isPlayerInParty(ServerPlayer player, PartyId partyId);

    Collection<ServerPlayer> getOnlinePartyMembers(MinecraftServer server, PartyId partyId);

    Optional<PartyDisplayInfo> describeParty(MinecraftServer server, PartyId partyId);
}
