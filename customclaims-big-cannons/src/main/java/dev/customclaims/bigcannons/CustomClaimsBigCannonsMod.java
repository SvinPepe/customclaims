package dev.customclaims.bigcannons;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CustomClaimsBigCannonsMod.MOD_ID)
public final class CustomClaimsBigCannonsMod {
    public static final String MOD_ID = "customclaims_big_cannons";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CustomClaimsBigCannonsMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Custom Claims Big Cannons compat placeholder initialized");
    }
}
