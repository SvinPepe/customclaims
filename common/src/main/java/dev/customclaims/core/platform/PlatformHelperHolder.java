package dev.customclaims.core.platform;

import java.nio.file.Path;
import java.nio.file.Paths;
//? if >=1.20.2 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else {
/*import dev.customclaims.core.network.CustomPacketPayload;
*///?}
import net.minecraft.server.level.ServerPlayer;

public final class PlatformHelperHolder {
    private static IPlatformHelper platform;
    private static IPermissionHelper permissions;
    private static INetworkHelper network;

    private PlatformHelperHolder() {
    }

    public static synchronized void init(IPlatformHelper platformHelper, IPermissionHelper permissionHelper, INetworkHelper networkHelper) {
        platform = platformHelper;
        permissions = permissionHelper;
        network = networkHelper;
    }

    public static IPlatformHelper get() {
        return platform != null ? platform : FallbackPlatformHelper.INSTANCE;
    }

    public static IPlatformHelper getPlatform() {
        return get();
    }

    public static IPermissionHelper getPermissions() {
        return permissions != null ? permissions : (player, node) -> false;
    }

    public static INetworkHelper getNetwork() {
        return network != null ? network : FallbackNetworkHelper.INSTANCE;
    }

    private static final class FallbackPlatformHelper implements IPlatformHelper {
        private static final FallbackPlatformHelper INSTANCE = new FallbackPlatformHelper();
        @Override public String getPlatformName() { return "unknown"; }
        @Override public boolean isModLoaded(String modId) { return false; }
        @Override public String getModVersion(String modId) { return "unknown"; }
        @Override public boolean isDevelopmentEnvironment() { return false; }
        @Override public Path getConfigDirectory() { return Paths.get("config"); }
    }

    private static final class FallbackNetworkHelper implements INetworkHelper {
        private static final FallbackNetworkHelper INSTANCE = new FallbackNetworkHelper();
        @Override public boolean canSendToPlayer(ServerPlayer p, String id) { return false; }
        @Override public void sendToPlayer(ServerPlayer p, CustomPacketPayload pl) {}
        @Override public void sendToServer(CustomPacketPayload pl) {}
    }
}
