package dev.customclaims.core.platform;

import net.minecraft.server.level.ServerPlayer;

/**
 * Loader-agnostic permission query interface.
 */
public interface IPermissionHelper {
    /**
     * Checks if the given player has the specified permission node.
     *
     * @param player the player to check
     * @param permissionNode the permission string (e.g., "opac.warfare.start")
     * @return true if permission is granted
     */
    boolean hasPermission(ServerPlayer player, String permissionNode);
}
