package dev.customclaims.core.service;

import dev.customclaims.core.config.CoreConfig;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class PermissionService {
    public boolean hasPermission(ServerPlayer player, String permission) {
        return player.tags().contains(permission)
                || player.tags().contains(CustomClaimsPermissions.BYPASS)
                || CoreConfig.DEFAULT_PLAYER_PERMISSIONS.get().contains(permission)
                || player.hasPermissions(CoreConfig.OP_PERMISSION_LEVEL.get());
    }

    public boolean hasPermission(CommandSourceStack source, String permission) {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayer player) {
            return hasPermission(player, permission);
        }
        return true;
    }
}