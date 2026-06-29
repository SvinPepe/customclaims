package dev.customclaims.protection.network;

import dev.customclaims.protection.CustomClaimsProtectionMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundOpenClaimRulesPayload() implements CustomPacketPayload {
    public static final Type<ServerboundOpenClaimRulesPayload> TYPE = new Type<>(
            ResourceLocation.parse(CustomClaimsProtectionMod.MOD_ID + ":open_claimrules")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpenClaimRulesPayload> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundOpenClaimRulesPayload::write, ServerboundOpenClaimRulesPayload::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
    }

    public static ServerboundOpenClaimRulesPayload read(RegistryFriendlyByteBuf buffer) {
        return new ServerboundOpenClaimRulesPayload();
    }

    public static void handle(ServerboundOpenClaimRulesPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            context.reply(new ClientboundClaimRulesStatePayload(
                    ClaimRulesStateDto.from(CustomClaimsProtectionMod.services().claimRulesService().stateFor(player)),
                    "",
                    true
            ));
        });
    }
}
