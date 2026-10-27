package dev.customclaims.betterprotection;

import dev.customclaims.aeronautics.CustomClaimsAeronauticsMod;
import dev.customclaims.bigcannons.CustomClaimsBigCannonsMod;
import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.create.CustomClaimsCreateMod;
import dev.customclaims.protection.CustomClaimsProtectionMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(OpacBetterProtectionMod.MOD_ID)
public final class OpacBetterProtectionMod {
    public static final String MOD_ID = "opac_better_protection";

    public OpacBetterProtectionMod(IEventBus modEventBus, ModContainer modContainer) {
        CustomClaimsCoreMod.init(modEventBus, modContainer);
        CustomClaimsProtectionMod.init(modEventBus, modContainer);
        CustomClaimsCreateMod.init(modEventBus, modContainer);
        CustomClaimsBigCannonsMod.init(modEventBus, modContainer);
        CustomClaimsAeronauticsMod.init(modEventBus, modContainer);
    }
}
