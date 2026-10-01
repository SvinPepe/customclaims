package dev.customclaims.core.service;

import dev.customclaims.core.config.CoreConfig;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.Entity;

public final class PermissionService {
    public boolean hasPermission(ServerPlayer player, String permission) {
        // In Minecraft 26.1+, Entity#getTags() (scoreboard string tags) was removed.
        // Fall back to config-based permissions and OP check.
        return CoreConfig.DEFAULT_PLAYER_PERMISSIONS.get().contains(permission)
                || CoreConfig.DEFAULT_PLAYER_PERMISSIONS.get().contains(CustomClaimsPermissions.BYPASS)
                || isOp(player);
    }

    private boolean isOp(ServerPlayer player) {
        var server = player.level().getServer();
        if (server == null) return false;
        // In 26.1+ PlayerList.isOp() takes NameAndId instead of GameProfile
        return server.getPlayerList().isOp(new NameAndId(player.getUUID(), player.getScoreboardName()));
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