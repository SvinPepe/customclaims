package dev.customclaims.aeronautics;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CustomClaimsAeronauticsMod.MOD_ID)
public final class CustomClaimsAeronauticsMod {
    public static final String MOD_ID = "customclaims_aeronautics";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CustomClaimsAeronauticsMod(IEventBus modEventBus, ModContainer modContainer) {
        boolean offroadLoaded = ModList.get().isLoaded("offroad");
        boolean sableLoaded = ModList.get().isLoaded("sable");
        if (offroadLoaded) {
            LOGGER.info("Custom Claims Aeronautics compat active for Offroad bore mining");
        }
        if (sableLoaded) {
            LOGGER.info("Custom Claims Aeronautics compat active for Sable contraption assembly");
        }
        if (!offroadLoaded && !sableLoaded && ModList.get().isLoaded("aeronautics")) {
            LOGGER.info("Custom Claims Aeronautics compat initialized; Offroad and Sable integrations inactive");
        } else if (!offroadLoaded && !sableLoaded) {
            LOGGER.info("Custom Claims Aeronautics compat inactive because Aeronautics/Offroad/Sable is not loaded");
        }
    }
}
