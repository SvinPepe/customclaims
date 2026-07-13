package dev.customclaims.create;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CustomClaimsCreateMod.MOD_ID)
public final class CustomClaimsCreateMod {
    public static final String MOD_ID = "customclaims_create";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CustomClaimsCreateMod(IEventBus modEventBus, ModContainer modContainer) {
        if (ModList.get().isLoaded("create")) {
            LOGGER.info("Custom Claims Create compat active");
        } else {
            LOGGER.info("Custom Claims Create compat inactive because Create is not loaded");
        }
    }
}
