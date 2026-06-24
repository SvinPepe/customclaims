package dev.customclaims.protection.service;

import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.core.api.model.TerritoryStatus;
import dev.customclaims.core.service.TerritoryService;
import dev.customclaims.protection.config.ProtectionConfig;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public final class ExplosionProtectionService {
    private final TerritoryService territoryService;
    private final Map<PartyId, Boolean> partyExplosionProtection = new ConcurrentHashMap<>();

    public ExplosionProtectionService(TerritoryService territoryService) {
        this.territoryService = territoryService;
    }

    public boolean canExplosionAffect(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        TerritoryStatus status = territoryService.getStatus(level, chunkPos);
        if (status == TerritoryStatus.WAR_CONTESTED) {
            return ProtectionConfig.ALLOW_EXPLOSIONS_IN_WAR_CHUNKS.get();
        }
        if (status == TerritoryStatus.PEACEFUL_CLAIMED || status == TerritoryStatus.POST_WAR_PROTECTED) {
            boolean ownerDisabledProtection = territoryService.getClaimOwner(level, chunkPos)
                    .map(owner -> !isPartyExplosionProtectionEnabled(owner))
                    .orElse(false);
            if (ownerDisabledProtection) {
                return true;
            }
            return !ProtectionConfig.PROTECT_PEACEFUL_CLAIMS_FROM_EXPLOSIONS.get();
        }
        return true;
    }

    public boolean isPartyExplosionProtectionEnabled(PartyId partyId) {
        return partyExplosionProtection.getOrDefault(partyId, true);
    }

    public void setPartyExplosionProtection(PartyId partyId, boolean enabled) {
        partyExplosionProtection.put(partyId, enabled);
    }
}
