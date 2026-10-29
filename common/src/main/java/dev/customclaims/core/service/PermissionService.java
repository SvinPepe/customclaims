package dev.customclaims.core.service;

import dev.customclaims.core.config.CoreConfig;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.core.platform.IPermissionHelper;
import dev.customclaims.core.platform.PlatformHelperHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class PermissionService {
    public boolean hasPermission(ServerPlayer player, String permission) {
        if (player == null) {
            return false;
        }

        // 1. Check direct node via platform permission provider (NeoForge / Fabric LuckPerms)
        IPermissionHelper permHelper = PlatformHelperHolder.getPermissions();
        if (permHelper.hasPermission(player, permission)) {
            return true;
        }

        // 2. Check admin or bypass permission node
        if (isBypassOrAdmin(player, permission)) {
            return true;
        }

        // 3. Fallback to scoreboard tags, OP check, and default list only for unregistered legacy permissions
        //? if <26.0 {
        if (player.getTags().contains(permission)
                || player.getTags().contains(CustomClaimsPermissions.BYPASS)
                || isOp(player)) {
            return true;
        }
        //?} else {
        /*if (player.entityTags().contains(permission)
                || player.entityTags().contains(CustomClaimsPermissions.BYPASS)
                || isOp(player)) {
            return true;
        }
        *///?}

        return CoreConfig.DEFAULT_PLAYER_PERMISSIONS.get().contains(permission);
    }

    private boolean isBypassOrAdmin(ServerPlayer player, String permission) {
        IPermissionHelper permHelper = PlatformHelperHolder.getPermissions();
        if (permHelper.hasPermission(player, CustomClaimsPermissions.BYPASS)
                || permHelper.hasPermission(player, CustomClaimsPermissions.ADMIN)) {
            return true;
        }
        if (permission.startsWith("opac.warfare.") || permission.contains(".war.") || permission.contains(".rollback")) {
            if (permHelper.hasPermission(player, CustomClaimsPermissions.WAR_BYPASS)
                    || permHelper.hasPermission(player, CustomClaimsPermissions.WAR_ADMIN)) {
                return true;
            }
        }
        if (permission.startsWith("opac.protection.") || permission.contains(".explosions.") || permission.contains(".create.") || permission.contains(".assembly.")) {
            if (permHelper.hasPermission(player, CustomClaimsPermissions.PROTECTION_BYPASS)
                    || permHelper.hasPermission(player, CustomClaimsPermissions.PROTECTION_ADMIN)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOp(ServerPlayer player) {
        //? if <26.0 {
        return player.createCommandSourceStack().hasPermission(CoreConfig.OP_PERMISSION_LEVEL.get());
        //?} else {
        /*var server = player.level().getServer();
        if (server == null) return false;
        return server.getPlayerList().isOp(new net.minecraft.server.players.NameAndId(player.getUUID(), player.getScoreboardName()));
        *///?}
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
