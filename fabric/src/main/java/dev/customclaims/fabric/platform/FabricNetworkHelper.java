package dev.customclaims.fabric.platform;

import dev.customclaims.core.platform.INetworkHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//? if >=1.20.5 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else {
/*import dev.customclaims.core.network.CustomPacketPayload;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
*///?}
//? if <26.0 {
import net.minecraft.resources.ResourceLocation;
//?} else {
/*import net.minecraft.resources.Identifier;
*///?}
import net.minecraft.server.level.ServerPlayer;

public final class FabricNetworkHelper implements INetworkHelper {
    @Override
    public boolean canSendToPlayer(ServerPlayer player, String channelId) {
        if (player == null || channelId == null) {
            return false;
        }
        try {
            //? if <1.20.5 {
            /*ResourceLocation id = new ResourceLocation(channelId);
            return ServerPlayNetworking.canSend(player, id);
            *///?} else {
            ResourceLocation id = ResourceLocation.parse(channelId);
            return ServerPlayNetworking.getSendable(player).contains(id);
            //?}
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        if (player != null && payload != null) {
            try {
                //? if >=1.20.5 {
                ServerPlayNetworking.send(player, payload);
                //?} else {
                /*FriendlyByteBuf buf = PacketByteBufs.create();
                payload.write(buf);
                ServerPlayNetworking.send(player, payload.id(), buf);
                *///?}
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        if (payload != null) {
            try {
                //? if >=1.20.5 {
                ClientPlayNetworking.send(payload);
                //?} else {
                /*FriendlyByteBuf buf = PacketByteBufs.create();
                payload.write(buf);
                ClientPlayNetworking.send(payload.id(), buf);
                *///?}
            } catch (Throwable ignored) {
            }
        }
    }
}
