package dev.customclaims.war.service;

import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.service.TerritoryStateService;
import dev.customclaims.war.config.WarConfig;
import java.time.Instant;

public final class PostWarProtectionService {
    private final TerritoryStateService territoryStateService;

    public PostWarProtectionService(TerritoryStateService territoryStateService) {
        this.territoryStateService = territoryStateService;
    }

    public void protect(ChunkPosKey chunk) {
        int seconds = WarConfig.POST_WAR_PROTECTION_SECONDS.get();
        if (seconds <= 0) {
            territoryStateService.clearStatus(chunk);
            return;
        }
        territoryStateService.markPostWarProtected(chunk, Instant.now().plusSeconds(seconds));
    }
}
