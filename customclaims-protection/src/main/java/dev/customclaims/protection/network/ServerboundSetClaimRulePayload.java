package dev.customclaims.protection.network;

import dev.customclaims.protection.CustomClaimsProtectionMod;
import dev.customclaims.protection.service.ClaimRuleUpdateResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSetClaimRulePayload(String ruleId, boolean enabled) implements CustomPacketPayload {
    public static final Type<ServerboundSetClaimRulePayload> TYPE = new Type<>(
            ResourceLocation.parse(CustomClaimsProtectionMod.MOD_ID + ":set_claimrule")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetClaimRulePayload> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundSetClaimRulePayload::write, ServerboundSetClaimRulePayload::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(ruleId);
        buffer.writeBoolean(enabled);
    }

    public static ServerboundSetClaimRulePayload read(RegistryFriendlyByteBuf buffer) {
        return new ServerboundSetClaimRulePayload(buffer.readUtf(), buffer.readBoolean());
    }

    public static void handle(ServerboundSetClaimRulePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            ClaimRuleUpdateResult result = CustomClaimsProtectionMod.services()
                    .claimRulesService()
                    .setRule(player.createCommandSourceStack(), payload.ruleId(), payload.enabled());
            context.reply(new ClientboundClaimRulesStatePayload(
                    ClaimRulesStateDto.from(result.state()),
                    result.message(),
                    false
            ));
        });
    }
}
