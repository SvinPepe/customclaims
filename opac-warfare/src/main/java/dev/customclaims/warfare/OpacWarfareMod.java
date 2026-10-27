package dev.customclaims.warfare;

import dev.customclaims.war.CustomClaimsWarMod;
import dev.customclaims.xaero.CustomClaimsXaeroMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(OpacWarfareMod.MOD_ID)
public final class OpacWarfareMod {
    public static final String MOD_ID = "opac_warfare";

    public OpacWarfareMod(IEventBus modEventBus, ModContainer modContainer) {
        CustomClaimsWarMod.init(modEventBus, modContainer);
        CustomClaimsXaeroMod.init(modEventBus, modContainer);
    }
}
