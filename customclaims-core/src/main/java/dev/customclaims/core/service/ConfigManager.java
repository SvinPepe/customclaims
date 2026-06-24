package dev.customclaims.core.service;

import dev.customclaims.core.config.CoreConfig;

public final class ConfigManager {
    public boolean debugLogging() {
        return CoreConfig.DEBUG_LOGGING.get();
    }
}
