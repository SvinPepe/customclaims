package dev.customclaims.bigcannons;

import com.mojang.logging.LogUtils;
import dev.customclaims.bigcannons.event.BigCannonIntegration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CustomClaimsBigCannonsMod.MOD_ID)
public final class CustomClaimsBigCannonsMod {
    public static final String MOD_ID = "customclaims_big_cannons";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CustomClaimsBigCannonsMod(IEventBus modEventBus, ModContainer modContainer) {
        if (ModList.get().isLoaded("createbigcannons")) {
            BigCannonIntegration.register();
            LOGGER.info("Custom Claims Big Cannons compat initialized with Create Big Cannons integration active");
        } else {
            LOGGER.info("Custom Claims Big Cannons compat initialized; Create Big Cannons is not loaded, integration inactive");
        }
    }
}
