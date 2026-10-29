package dev.customclaims.core.platform;

import java.nio.file.Path;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;

public final class NeoForgePlatformHelper implements IPlatformHelper {
    @Override
    public String getPlatformName() {
        return "neoforge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get() != null && ModList.get().isLoaded(modId);
    }

    @Override
    public String getModVersion(String modId) {
        if (ModList.get() == null) {
            return "unknown";
        }
        return ModList.get().getModContainerById(modId)
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("unknown");
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        //? if <26.0 {
        return !FMLLoader.isProduction();
        //?} else {
        /*return !FMLLoader.getCurrent().isProduction();
        *///?}
    }

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
