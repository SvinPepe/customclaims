package dev.customclaims.core.service;

import dev.customclaims.core.config.CoreConfig;

public final class ConfigManager {
    public boolean debugLogging() {
        return CoreConfig.DEBUG_LOGGING.get();
    }

    public boolean metricsEnabled() {
        return CoreConfig.METRICS_ENABLED.get();
    }

    public String metricsEndpoint() {
        return CoreConfig.METRICS_ENDPOINT.get();
    }
}
