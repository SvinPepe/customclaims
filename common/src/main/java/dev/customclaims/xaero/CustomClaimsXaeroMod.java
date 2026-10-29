package dev.customclaims.xaero;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class CustomClaimsXaeroMod {
    public static final String MOD_ID = "customclaims_xaero";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        LOGGER.info("Custom Claims Xaero fair-play overlay initialized");
    }
}
