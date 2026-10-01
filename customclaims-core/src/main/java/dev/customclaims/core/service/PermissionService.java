package dev.customclaims.core.service;

import dev.customclaims.core.config.CoreConfig;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class PermissionService {
    public boolean hasPermission(ServerPlayer player, String permission) {
        return player.getScoreboardTags().contains(permission)
                || player.getScoreboardTags().contains(CustomClaimsPermissions.BYPASS)
                || CoreConfig.DEFAULT_PLAYER_PERMISSIONS.get().contains(permission)
                || isOp(player);
    }

    private boolean isOp(ServerPlayer player) {
        var server = player.level().getServer();
        return server != null && server.getPlayerList().isOp(player.getGameProfile());
    }

    public boolean hasPermission(CommandSourceStack source, String permission) {
        Entity entity = source.getEntity();
        if (entity instanceof ServerPlayer player) {
            return hasPermission(player, permission);
        }
        return true;
    }
}