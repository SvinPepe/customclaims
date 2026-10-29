package dev.customclaims.core.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//? if <26.0 {
import net.minecraft.resources.ResourceLocation;
//?} else {
/*import net.minecraft.resources.Identifier;
*///?}
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public final class NeoForgeNetworkHelper implements INetworkHelper {
    @Override
    public boolean canSendToPlayer(ServerPlayer player, String channelId) {
        if (player == null || channelId == null) {
            return false;
        }
        //? if <26.0 {
        ResourceLocation id = ResourceLocation.parse(channelId);
        //?} else {
        /*Identifier id = Identifier.parse(channelId);
        *///?}
        return NetworkRegistry.hasChannel(player.connection, id);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        if (player != null && payload != null) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        if (payload != null) {
            //? if <26.0 {
            PacketDistributor.sendToServer(payload);
            //?} else {
            /*net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(payload);
            *///?}
        }
    }
}
