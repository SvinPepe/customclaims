package dev.customclaims.core;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class CustomClaimsCoreMod {
    public static final String MOD_ID = "customclaims_core";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static CoreServices services;

    public static synchronized void init() {
        if (services != null) {
            return;
        }
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
