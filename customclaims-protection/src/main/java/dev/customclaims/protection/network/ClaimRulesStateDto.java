package dev.customclaims.protection.network;

import dev.customclaims.protection.service.ClaimRulesState;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record ClaimRulesStateDto(
        boolean inParty,
        String partyLabel,
        boolean explosionProtectionEnabled,
        boolean createMachinesEnabled,
        long explosionCooldownSeconds,
        long createCooldownSeconds,
        boolean canToggleExplosions,
        boolean canToggleCreate
) {
    public static ClaimRulesStateDto from(ClaimRulesState state) {
        return new ClaimRulesStateDto(
                state.inParty(),
                state.partyLabel(),
                state.explosionProtectionEnabled(),
                state.createMachinesEnabled(),
                state.explosionCooldownSeconds(),
                state.createCooldownSeconds(),
                state.canToggleExplosions(),
                state.canToggleCreate()
        );
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(inParty);
        buffer.writeUtf(partyLabel);
        buffer.writeBoolean(explosionProtectionEnabled);
        buffer.writeBoolean(createMachinesEnabled);
        buffer.writeVarLong(explosionCooldownSeconds);
        buffer.writeVarLong(createCooldownSeconds);
        buffer.writeBoolean(canToggleExplosions);
        buffer.writeBoolean(canToggleCreate);
    }

    public static ClaimRulesStateDto read(RegistryFriendlyByteBuf buffer) {
        return new ClaimRulesStateDto(
                buffer.readBoolean(),
                buffer.readUtf(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readVarLong(),
                buffer.readVarLong(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }
}
