package dev.customclaims.core.service;

import dev.customclaims.core.config.CoreConfig;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;

public final class PermissionService {
    public boolean hasPermission(ServerPlayer player, String permission) {
        if (player == null) {
            return false;
        }

        // 1. Check direct node via NeoForge PermissionAPI (integrates with LuckPerms)
        PermissionNode<Boolean> node = CustomClaimsPermissions.getNode(permission);
        if (node != null) {
            try {
                if (PermissionAPI.getPermission(player, node)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        // 2. Check admin or bypass permission node
        if (isBypassOrAdmin(player, permission)) {
            return true;
        }

        // 3. Fallback to scoreboard tags, default permissions from config, and OP check
        return player.getTags().contains(permission)
                || player.getTags().contains(CustomClaimsPermissions.BYPASS)
                || CoreConfig.DEFAULT_PLAYER_PERMISSIONS.get().contains(permission)
                || isOp(player);
    }

    private boolean isBypassOrAdmin(ServerPlayer player, String permission) {
        try {
            if (PermissionAPI.getPermission(player, CustomClaimsPermissions.NODE_BYPASS)
                    || PermissionAPI.getPermission(player, CustomClaimsPermissions.NODE_ADMIN)) {
                return true;
            }
            if (permission.startsWith("opac.warfare.") || permission.contains(".war.") || permission.contains(".rollback")) {
                if (PermissionAPI.getPermission(player, CustomClaimsPermissions.NODE_WAR_BYPASS)
                        || PermissionAPI.getPermission(player, CustomClaimsPermissions.NODE_WAR_ADMIN)) {
                    return true;
                }
            }
            if (permission.startsWith("opac.protection.") || permission.contains(".explosions.") || permission.contains(".create.") || permission.contains(".assembly.")) {
                if (PermissionAPI.getPermission(player, CustomClaimsPermissions.NODE_PROTECTION_BYPASS)
                        || PermissionAPI.getPermission(player, CustomClaimsPermissions.NODE_PROTECTION_ADMIN)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean isOp(ServerPlayer player) {
        return player.createCommandSourceStack().hasPermission(CoreConfig.OP_PERMISSION_LEVEL.get());
    }

    public boolean hasPermission(CommandSourceStack source, String permission) {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayer player) {
            return hasPermission(player, permission);
        }
        // Non-player sources (console, command blocks) are trusted
        return true;
    }
}
