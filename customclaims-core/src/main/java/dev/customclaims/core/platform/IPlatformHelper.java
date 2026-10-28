package dev.customclaims.core.platform;

import java.nio.file.Path;

/**
 * Loader-agnostic platform query interface.
 */
public interface IPlatformHelper {
    /**
     * @return platform identifier (e.g., "neoforge", "fabric").
     */
    String getPlatformName();

    /**
     * @param modId the mod identifier to check.
     * @return true if the mod is present in the runtime environment.
     */
    boolean isModLoaded(String modId);

    /**
     * @return true if running in a development / non-production environment.
     */
    boolean isDevelopmentEnvironment();

    /**
     * @return the game configuration directory.
     */
    Path getConfigDirectory();
}
