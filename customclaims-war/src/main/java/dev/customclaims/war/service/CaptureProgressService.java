package dev.customclaims.war.service;

import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.model.WarData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class CaptureProgressService {
    private final dev.customclaims.core.service.PartyService partyService;

    public CaptureProgressService(dev.customclaims.core.service.PartyService partyService) {
        this.partyService = partyService;
    }

    public double nextProgress(MinecraftServer server, ServerLevel level, WarData war, AfkTracker afkTracker) {
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

            if (partyService.isSameParty(player, war.attackerParty())) {
                attackers++;
            } else if (partyService.isSameParty(player, war.defenderParty())) {
                defenders++;
            }
        }

        double delta = 0.0D;
        if (attackers > 0) {
            delta += attackers * WarConfig.ATTACKER_PROGRESS_PER_SECOND.get();
        } else {
            delta -= WarConfig.EMPTY_DECAY_PER_SECOND.get();
        }

        if (defenders > 0) {
            delta -= defenders * WarConfig.DEFENDER_DECAY_PER_SECOND.get();
        }

        return clamp(war.progress() + delta);
    }

    private double clamp(double value) {
        return Math.max(0.0D, Math.min(100.0D, value));
    }
}
