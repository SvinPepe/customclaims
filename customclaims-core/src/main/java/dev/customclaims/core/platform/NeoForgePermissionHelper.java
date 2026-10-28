package dev.customclaims.core.platform;

import dev.customclaims.core.permissions.CustomClaimsPermissions;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

public final class NeoForgePermissionHelper implements IPermissionHelper {
    @Override
    public boolean hasPermission(ServerPlayer player, String permissionNode) {
        if (player == null || permissionNode == null) {
            return false;
        }
        PermissionNode<Boolean> node = CustomClaimsPermissions.getNode(permissionNode);
        if (node != null) {
            try {
                return PermissionAPI.getPermission(player, node);
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
