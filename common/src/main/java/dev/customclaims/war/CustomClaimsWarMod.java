package dev.customclaims.war;

import com.mojang.logging.LogUtils;
import dev.customclaims.core.CustomClaimsCoreMod;
import org.slf4j.Logger;

public final class CustomClaimsWarMod {
    public static final String MOD_ID = "customclaims_war";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static WarServices services;

    public static synchronized void init() {
        if (services != null) {
            return;
        }
        services = WarServices.create(CustomClaimsCoreMod.services());
        LOGGER.info("Custom Claims War initialized");
    }

    public static WarServices services() {
        if (services == null) {
            throw new IllegalStateException("Custom Claims War services are not initialized yet");
        }
        return services;
    }
}
