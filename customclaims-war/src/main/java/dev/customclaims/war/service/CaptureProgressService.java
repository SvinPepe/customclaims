package dev.customclaims.war.service;

import dev.customclaims.core.service.PartyService;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.model.WarData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class CaptureProgressService {
    private final PartyService partyService;
    private final WarLivesService livesService;

    public CaptureProgressService(PartyService partyService, WarLivesService livesService) {
        this.partyService = partyService;
        this.livesService = livesService;
    }

    public CaptureTickResult nextProgress(MinecraftServer server, ServerLevel level, WarData war, AfkTracker afkTracker) {
        int attackers = 0;
        int defenders = 0;
        List<ServerPlayer> presentPlayers = new ArrayList<>();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.level().dimension().equals(level.dimension())) {
                continue;
            }
            if (!war.isInSector(player.chunkPosition())) {
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
                presentPlayers.add(player);
            } else if (partyService.isSameSide(player, war.defenderSide())) {
                defenders++;
                presentPlayers.add(player);
            }
        }

        double delta = calculateDelta(attackers, defenders);

        // Anti-hide feature: apply Glowing effect to all combatants when zone is contested
        if (attackers > 0 && defenders > 0 && WarConfig.getSafe(WarConfig.GLOWING_EFFECT_ENABLED)) {
            for (ServerPlayer player : presentPlayers) {
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false));
            }
        }

        return new CaptureTickResult(clamp(war.progress() + delta), delta, attackers, defenders);
    }

    public static double calculateDelta(int attackers, int defenders) {
        // Anti-zerg capture calculation:
        // Base rate with 1 attacker: +0.40%/s
        // Micro-bonus per extra attacker: +0.05%/s
        // Hard ceiling cap: +0.60%/s
        double baseRate = WarConfig.getSafe(WarConfig.BASE_CAPTURE_RATE_PER_SECOND);
        double extraBonus = WarConfig.getSafe(WarConfig.EXTRA_ATTACKER_BONUS_PER_SECOND);
        double maxRate = WarConfig.getSafe(WarConfig.MAX_CAPTURE_RATE_PER_SECOND);
        double defenderWeight = WarConfig.getSafe(WarConfig.DEFENDER_WEIGHT_PER_SECOND);
        double emptyDecay = WarConfig.getSafe(WarConfig.EMPTY_CHUNK_DECAY_PER_SECOND);

        if (attackers > 0) {
            double attackerGain = Math.min(maxRate, baseRate + Math.max(0, attackers - 1) * extraBonus);
            return attackerGain - (defenders * defenderWeight);
        } else if (defenders > 0) {
            return -(defenders * defenderWeight);
        } else {
            return -emptyDecay;
        }
    }

    public static double clamp(double value) {
        return Math.max(0.0D, Math.min(100.0D, value));
    }
}
