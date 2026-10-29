package dev.customclaims.core.platform;

//? if >=1.20.2 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else {
/*import dev.customclaims.core.network.CustomPacketPayload;
*///?}
import net.minecraft.server.level.ServerPlayer;

public interface INetworkHelper {
    boolean canSendToPlayer(ServerPlayer player, String channelId);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToServer(CustomPacketPayload payload);
}
