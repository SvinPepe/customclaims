package dev.customclaims.protection.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ProtectionNetwork {
    private ProtectionNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(
                ClientboundClaimRulesStatePayload.TYPE,
                ClientboundClaimRulesStatePayload.STREAM_CODEC,
                ClientboundClaimRulesStatePayload::handle
        );
        registrar.playToServer(
                ServerboundSetClaimRulePayload.TYPE,
                ServerboundSetClaimRulePayload.STREAM_CODEC,
                ServerboundSetClaimRulePayload::handle
        );
        registrar.playToServer(
                ServerboundOpenClaimRulesPayload.TYPE,
                ServerboundOpenClaimRulesPayload.STREAM_CODEC,
                ServerboundOpenClaimRulesPayload::handle
        );
    }
}
