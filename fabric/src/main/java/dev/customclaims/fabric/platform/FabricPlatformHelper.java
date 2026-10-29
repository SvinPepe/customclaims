package dev.customclaims.fabric.platform;

import dev.customclaims.core.platform.IPlatformHelper;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricPlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public String getModVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
