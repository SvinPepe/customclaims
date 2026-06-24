package dev.customclaims.protection.service;

import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.core.service.PermissionService;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.protection.config.ProtectionConfig;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public final class ForeignInteractionLimitService {
    private final TerritoryService territoryService;
    private final PermissionService permissionService;
    private final Map<UUID, Integer> foreignBreaks = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> foreignPlacements = new ConcurrentHashMap<>();

    public ForeignInteractionLimitService(TerritoryService territoryService, PermissionService permissionService) {
        this.territoryService = territoryService;
        this.permissionService = permissionService;
    }

    public boolean canBreak(ServerPlayer player, ServerLevel level, BlockPos pos) {
        if (permissionService.hasPermission(player, CustomClaimsPermissions.BYPASS)) {
            return true;
        }

        TerritoryStatus status = territoryService.getInteractionStatus(player, level, new ChunkPos(pos));
        if (status == TerritoryStatus.WAR_CONTESTED || status == TerritoryStatus.UNCLAIMED) {
            return true;
        }
        if (status != TerritoryStatus.FOREIGN_LIMITED_INTERACTION) {
            return true;
        }

        return incrementIfBelowLimit(foreignBreaks, player.getUUID(), ProtectionConfig.FOREIGN_BLOCK_BREAK_LIMIT.get());
    }

    public boolean canPlace(ServerPlayer player, ServerLevel level, BlockPos pos) {
        if (permissionService.hasPermission(player, CustomClaimsPermissions.BYPASS)) {
            return true;
        }

        TerritoryStatus status = territoryService.getInteractionStatus(player, level, new ChunkPos(pos));
        if (status == TerritoryStatus.WAR_CONTESTED || status == TerritoryStatus.UNCLAIMED) {
            return true;
        }
        if (status != TerritoryStatus.FOREIGN_LIMITED_INTERACTION) {
            return true;
        }

        return incrementIfBelowLimit(foreignPlacements, player.getUUID(), ProtectionConfig.FOREIGN_BLOCK_PLACE_LIMIT.get());
    }

    public String limitsStatus(ServerPlayer player) {
        int breakLimit = ProtectionConfig.FOREIGN_BLOCK_BREAK_LIMIT.get();
        int placeLimit = ProtectionConfig.FOREIGN_BLOCK_PLACE_LIMIT.get();
        int breaks = foreignBreaks.getOrDefault(player.getUUID(), 0);
        int places = foreignPlacements.getOrDefault(player.getUUID(), 0);
        return "Foreign claim limits: breaks " + breaks + "/" + breakLimit + ", placements " + places + "/" + placeLimit + ".";
    }

    private boolean incrementIfBelowLimit(Map<UUID, Integer> usage, UUID playerId, int limit) {
        if (limit <= 0) {
            return false;
        }
        int current = usage.getOrDefault(playerId, 0);
        if (current >= limit) {
            return false;
        }
        usage.put(playerId, current + 1);
        return true;
    }
}
