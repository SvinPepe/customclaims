package dev.customclaims.core.platform;

import java.util.ServiceLoader;

public final class PlatformHelperHolder {
    private static final IPlatformHelper PLATFORM = ServiceLoader.load(IPlatformHelper.class)
            .findFirst()
            .orElseGet(NeoForgePlatformHelper::new);

    private static final IPermissionHelper PERMISSIONS = ServiceLoader.load(IPermissionHelper.class)
            .findFirst()
            .orElseGet(NeoForgePermissionHelper::new);

    private static final INetworkHelper NETWORK = ServiceLoader.load(INetworkHelper.class)
            .findFirst()
            .orElseGet(NeoForgeNetworkHelper::new);

    private PlatformHelperHolder() {
    }

    public static IPlatformHelper getPlatform() {
        return PLATFORM;
    }

    public static IPermissionHelper getPermissions() {
        return PERMISSIONS;
    }

    public static INetworkHelper getNetwork() {
        return NETWORK;
    }
}
