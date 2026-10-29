package dev.customclaims.fabric.platform;

import dev.customclaims.core.platform.IPermissionHelper;
import java.lang.reflect.Method;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public final class FabricPermissionHelper implements IPermissionHelper {
    private final boolean hasPermissionsApi = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
    private Method checkMethod;

    public FabricPermissionHelper() {
        if (hasPermissionsApi) {
            try {
                Class<?> clazz = Class.forName("me.lucko.fabric.api.permissions.v0.Permissions");
                checkMethod = clazz.getMethod("check", ServerPlayer.class, String.class);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public boolean hasPermission(ServerPlayer player, String permissionNode) {
        if (player == null || permissionNode == null) {
            return false;
        }
        if (checkMethod != null) {
            try {
                return (boolean) checkMethod.invoke(null, player, permissionNode);
            } catch (Throwable ignored) {
            }
        }
        return false;
    }
}
