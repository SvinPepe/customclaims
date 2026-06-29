package dev.customclaims.protection.network;

import dev.customclaims.protection.service.ClaimRulesState;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record ClaimRulesStateDto(
        boolean hasSide,
        String sideLabel,
        boolean explosionProtectionEnabled,
        boolean createMachinesEnabled,
        long explosionCooldownSeconds,
        long createCooldownSeconds,
        boolean canToggleExplosions,
        boolean canToggleCreate
) {
    public static ClaimRulesStateDto from(ClaimRulesState state) {
        return new ClaimRulesStateDto(
                state.hasSide(),
                state.sideLabel(),
                state.explosionProtectionEnabled(),
                state.createMachinesEnabled(),
                state.explosionCooldownSeconds(),
                state.createCooldownSeconds(),
                state.canToggleExplosions(),
                state.canToggleCreate()
        );
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(hasSide);
        buffer.writeUtf(sideLabel);
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
