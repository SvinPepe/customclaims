package dev.customclaims.protection.client;

import dev.customclaims.protection.network.ClientboundClaimRulesStatePayload;

public final class ClaimRulesClientPacketHandler {
    private ClaimRulesClientPacketHandler() {
    }

    public static void handle(ClientboundClaimRulesStatePayload payload) {
        ClaimRulesScreen.applyState(payload.state(), payload.message(), payload.openScreen());
    }
}
