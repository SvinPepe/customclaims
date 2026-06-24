package dev.customclaims.core;

import com.mojang.logging.LogUtils;
import dev.customclaims.core.config.CoreConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(CustomClaimsCoreMod.MOD_ID)
public final class CustomClaimsCoreMod {
    public static final String MOD_ID = "customclaims_core";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static CoreServices services;

    public CustomClaimsCoreMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, CoreConfig.SPEC);
        services = CoreServices.create();
        LOGGER.info("Custom Claims Core initialized with {} adapter", services.claimAdapter().name());
    }

    public static CoreServices services() {
        if (services == null) {
            throw new IllegalStateException("Custom Claims Core services are not initialized yet");
        }
        return services;
    }
}
