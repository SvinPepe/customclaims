package dev.customclaims.xaero;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CustomClaimsXaeroMod.MOD_ID)
public final class CustomClaimsXaeroMod {
    public static final String MOD_ID = "customclaims_xaero";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CustomClaimsXaeroMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Custom Claims Xaero compat placeholder initialized");
    }
}
