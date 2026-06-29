package dev.customclaims.protection.network;

import dev.customclaims.protection.CustomClaimsProtectionMod;
import dev.customclaims.protection.client.ClaimRulesClientPacketHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundClaimRulesStatePayload(
        ClaimRulesStateDto state,
        String message,
        boolean openScreen
) implements CustomPacketPayload {
    public static final Type<ClientboundClaimRulesStatePayload> TYPE = new Type<>(
            ResourceLocation.parse(CustomClaimsProtectionMod.MOD_ID + ":claimrules_state")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundClaimRulesStatePayload> STREAM_CODEC =
            StreamCodec.ofMember(ClientboundClaimRulesStatePayload::write, ClientboundClaimRulesStatePayload::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        state.write(buffer);
        buffer.writeUtf(message);
        buffer.writeBoolean(openScreen);
    }

    public static ClientboundClaimRulesStatePayload read(RegistryFriendlyByteBuf buffer) {
        return new ClientboundClaimRulesStatePayload(
                ClaimRulesStateDto.read(buffer),
                buffer.readUtf(),
                buffer.readBoolean()
        );
    }

    public static void handle(ClientboundClaimRulesStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClaimRulesClientPacketHandler.handle(payload));
    }
}
