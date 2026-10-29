package dev.customclaims.forge.platform;

import dev.customclaims.core.network.CustomPacketPayload;
import dev.customclaims.core.platform.INetworkHelper;
import dev.customclaims.xaero.CustomClaimsXaeroMod;
import dev.customclaims.xaero.network.ClientboundWarMarkersPayload;
import dev.customclaims.xaero.network.ServerboundStartWarAtPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ForgeNetworkHelper implements INetworkHelper {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CustomClaimsXaeroMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        int id = 0;
        CHANNEL.messageBuilder(ClientboundWarMarkersPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientboundWarMarkersPayload::read)
                .encoder(ClientboundWarMarkersPayload::write)
                .consumerMainThread((msg, ctxSupplier) -> {
                    ClientboundWarMarkersPayload.handle(msg);
                    ctxSupplier.get().setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(ServerboundStartWarAtPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ServerboundStartWarAtPayload::read)
                .encoder(ServerboundStartWarAtPayload::write)
                .consumerMainThread((msg, ctxSupplier) -> {
                    ServerPlayer sender = ctxSupplier.get().getSender();
                    if (sender != null) {
                        ServerboundStartWarAtPayload.handle(msg, sender);
                    }
                    ctxSupplier.get().setPacketHandled(true);
                })
                .add();
    }

    @Override
    public boolean canSendToPlayer(ServerPlayer player, String channelId) {
        return player != null && player.connection != null;
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        if (player != null && payload != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
        }
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        if (payload != null) {
            CHANNEL.sendToServer(payload);
        }
    }
}
