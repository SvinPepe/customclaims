package dev.customclaims.protection.service;

import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.core.service.PermissionService;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.protection.config.ProtectionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

public final class StorageProtectionService {
    private final TerritoryService territoryService;
    private final PermissionService permissionService;

    public StorageProtectionService(TerritoryService territoryService, PermissionService permissionService) {
        this.territoryService = territoryService;
        this.permissionService = permissionService;
    }

    public boolean canOpen(ServerPlayer player, ServerLevel level, BlockPos pos) {
        if (permissionService.hasPermission(player, CustomClaimsPermissions.BYPASS)) {
            return true;
        }
        TerritoryStatus status = territoryService.getInteractionStatus(player, level, new ChunkPos(pos));
        if (status == TerritoryStatus.WAR_CONTESTED) {
            return true;
        }
        if (status == TerritoryStatus.FOREIGN_LIMITED_INTERACTION) {
            return ProtectionConfig.ALLOW_OPEN_STORAGE_ON_FOREIGN_CLAIMS.get();
        }
        return true;
    }

    public boolean canBreak(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state) {
        if (!isProtectedStorage(state)) {
            return true;
        }
        if (permissionService.hasPermission(player, CustomClaimsPermissions.BYPASS)) {
            return true;
        }

        TerritoryStatus status = territoryService.getInteractionStatus(player, level, new ChunkPos(pos));
        if (status == TerritoryStatus.WAR_CONTESTED) {
            return ProtectionConfig.ALLOW_STORAGE_BREAKING_IN_WAR_CHUNKS.get();
        }
        if (status == TerritoryStatus.FOREIGN_LIMITED_INTERACTION) {
            return !ProtectionConfig.PROTECT_STORAGE_FROM_BREAKING_ON_PEACEFUL_CLAIMS.get();
        }
        return true;
    }

    private boolean isProtectedStorage(BlockState state) {
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        return ProtectionConfig.PROTECTED_STORAGE_BLOCKS.get().contains(blockId);
    }
}
