package dev.customclaims.protection.network;

import dev.customclaims.protection.service.ClaimRulesState;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record ClaimRulesStateDto(
        boolean hasSide,
        String sideLabel,
        boolean explosionProtectionEnabled,
        boolean createMachinesEnabled,
        boolean assemblyEnabled,
        long explosionCooldownSeconds,
        long createCooldownSeconds,
        long assemblyCooldownSeconds,
        boolean canToggleExplosions,
        boolean canToggleCreate,
        boolean canToggleAssembly
) {
    public static ClaimRulesStateDto from(ClaimRulesState state) {
        return new ClaimRulesStateDto(
                state.hasSide(),
                state.sideLabel(),
                state.explosionProtectionEnabled(),
                state.createMachinesEnabled(),
                state.assemblyEnabled(),
                state.explosionCooldownSeconds(),
                state.createCooldownSeconds(),
                state.assemblyCooldownSeconds(),
                state.canToggleExplosions(),
                state.canToggleCreate(),
                state.canToggleAssembly()
        );
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(hasSide);
        buffer.writeUtf(sideLabel);
        buffer.writeBoolean(explosionProtectionEnabled);
        buffer.writeBoolean(createMachinesEnabled);
        buffer.writeBoolean(assemblyEnabled);
        buffer.writeVarLong(explosionCooldownSeconds);
        buffer.writeVarLong(createCooldownSeconds);
        buffer.writeVarLong(assemblyCooldownSeconds);
        buffer.writeBoolean(canToggleExplosions);
        buffer.writeBoolean(canToggleCreate);
        buffer.writeBoolean(canToggleAssembly);
    }

    public static ClaimRulesStateDto read(RegistryFriendlyByteBuf buffer) {
        return new ClaimRulesStateDto(
                buffer.readBoolean(),
                buffer.readUtf(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readVarLong(),
                buffer.readVarLong(),
                buffer.readVarLong(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean()
        );
    }
}
