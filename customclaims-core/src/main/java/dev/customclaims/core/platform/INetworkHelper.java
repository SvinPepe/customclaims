package dev.customclaims.core.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Loader-agnostic network dispatch interface.
 */
public interface INetworkHelper {
    /**
     * Checks if the player can receive packets on the given channel ID.
     *
     * @param player destination player
     * @param channelId payload type ID
     * @return true if player client has registered the channel
     */
    boolean canSendToPlayer(ServerPlayer player, ResourceLocation channelId);

    /**
     * Sends a packet payload to a specific player on the server.
     *
     * @param player destination player
     * @param payload custom packet payload
     */
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    /**
     * Sends a packet payload to the server from a client.
     *
     * @param payload custom packet payload
     */
    void sendToServer(CustomPacketPayload payload);
}
