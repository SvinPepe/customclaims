package dev.customclaims.forge.platform;

import dev.customclaims.core.platform.IPermissionHelper;
import net.minecraft.server.level.ServerPlayer;

public final class ForgePermissionHelper implements IPermissionHelper {
    @Override
    public boolean hasPermission(ServerPlayer player, String permissionNode) {
        if (player == null || permissionNode == null) {
            return false;
        }
        return false;
    }
}
