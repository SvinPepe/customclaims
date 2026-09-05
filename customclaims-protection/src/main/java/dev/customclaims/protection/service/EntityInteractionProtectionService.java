package dev.customclaims.protection.service;

import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.protection.config.ProtectionConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class EntityInteractionProtectionService {
    private final TerritoryService territoryService;

    public EntityInteractionProtectionService(TerritoryService territoryService) {
        this.territoryService = territoryService;
    }

    public boolean shouldBypassOpenPartiesProtection(ServerPlayer player, Entity target) {
        if (!(target.level() instanceof ServerLevel level) || player.serverLevel() != level) {
            return false;
        }

        // Read the current config so reloads and an empty list take effect immediately.
        String entityType = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString();
        return ProtectionConfig.CONTESTED_ENTITY_INTERACTION_EXCEPTIONS.get().contains(entityType)
                && territoryService.getInteractionStatus(player, level, target.chunkPosition())
                == TerritoryStatus.WAR_CONTESTED;
    }
}
