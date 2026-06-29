package dev.customclaims.war.service;

import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.model.WarData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class CaptureProgressService {
    private final dev.customclaims.core.service.PartyService partyService;
    private final WarLivesService livesService;

    public CaptureProgressService(dev.customclaims.core.service.PartyService partyService, WarLivesService livesService) {
        this.partyService = partyService;
        this.livesService = livesService;
    }

    public CaptureTickResult nextProgress(MinecraftServer server, ServerLevel level, WarData war, AfkTracker afkTracker) {
        int attackers = 0;
        int defenders = 0;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(level.dimension())) {
                continue;
            }
            if (!player.chunkPosition().equals(war.targetChunk().toChunkPos())) {
                continue;
            }
            if (afkTracker.isAfk(player)) {
                continue;
            }
            if (!livesService.canContribute(player, war)) {
                continue;
            }

            if (partyService.isSameSide(player, war.attackerSide())) {
                attackers++;
            } else if (partyService.isSameSide(player, war.defenderSide())) {
                defenders++;
            }
        }

        double playerWeight = WarConfig.PLAYER_WEIGHT_PER_SECOND.get();
        double delta = (attackers - defenders) * playerWeight;
        if (attackers > 0) {
            delta += WarConfig.ATTACKER_PRESENCE_BONUS_PER_SECOND.get();
        }
        if (attackers == 0 && defenders == 0) {
            delta -= WarConfig.EMPTY_CHUNK_DECAY_PER_SECOND.get();
        }

        return new CaptureTickResult(clamp(war.progress() + delta), delta, attackers, defenders);
    }

    private double clamp(double value) {
        return Math.max(0.0D, Math.min(100.0D, value));
    }
}
