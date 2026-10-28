package dev.customclaims.war.service;

import dev.customclaims.core.CoreServices;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.ClaimSideDisplayInfo;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.model.WarData;
import dev.customclaims.war.model.WarState;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class WarDisplayService {
    private static final int PROGRESS_BAR_WIDTH = 12;

    private final CoreServices coreServices;

    public WarDisplayService(CoreServices coreServices) {
        this.coreServices = coreServices;
    }

    public String formatWarStatus(MinecraftServer server, WarData war, Instant now) {
        return label(server, war)
                + "\nState: " + stateName(war.state()) + " | " + progressText(war)
                + "\nSector: " + war.sectorChunks().size() + " chunks"
                + "\nAttackers: " + sideSummary(server, war.attackerSide())
                + "\nDefenders: " + sideSummary(server, war.defenderSide())
                + "\nPresence: ATK " + war.lastAttackersPresent() + " DEF " + war.lastDefendersPresent()
                + " | Lives: ATK " + remainingLives(war.attackerLives()) + " DEF " + remainingLives(war.defenderLives())
                + " | Time: " + timeText(war, now);
    }

    public String formatWarListEntry(MinecraftServer server, WarData war, Instant now) {
        return "- " + label(server, war)
                + " | " + stateName(war.state())
                + " | " + Math.round(war.progress()) + "%"
                + " | Sector: " + war.sectorChunks().size() + " chunks"
                + " | " + timeText(war, now);
    }

    public String formatStart(MinecraftServer server, WarData war) {
        return "War declared: " + label(server, war)
                + "\nSector: " + war.sectorChunks().size() + " chunks"
                + "\nAttackers: " + sideSummary(server, war.attackerSide())
                + "\nDefenders: " + sideSummary(server, war.defenderSide())
                + "\nPreparation: " + formatDuration(WarConfig.PREPARATION_SECONDS.get());
    }

    public String formatAdminProgress(WarData war) {
        return "Progress set to " + Math.round(war.progress()) + "% for " + chunkLabel(war.targetChunk()) + ".";
    }

    public String label(MinecraftServer server, WarData war) {
        return sideName(server, war.attackerSide())
                + " vs " + sideName(server, war.defenderSide())
                + " [" + war.sectorChunks().size() + " chunks @ " + chunkLabel(war.targetChunk()) + "]";
    }

    public String sideName(MinecraftServer server, ClaimSideId sideId) {
        return coreServices.partyService().describeSide(server, sideId)
                .map(ClaimSideDisplayInfo::name)
                .orElse(sideId.shortLabel());
    }

    public String sideSummary(MinecraftServer server, ClaimSideId sideId) {
        return coreServices.partyService().describeSide(server, sideId)
                .map(info -> info.name()
                        + " (owner " + info.ownerName()
                        + ", online " + info.onlineCount() + "/" + info.memberCount() + ")")
                .orElse(sideId.shortLabel());
    }

    public String matchupName(MinecraftServer server, WarData war) {
        return sideName(server, war.attackerSide()) + " vs " + sideName(server, war.defenderSide());
    }

    public String chunkLabel(ChunkPosKey key) {
        return dimensionName(key.levelId()) + " [chunk " + key.x() + ", " + key.z() + "]";
    }

    public Component actionbarComponent(WarData war, ServerPlayer player) {
        int playerLives = war.lives(player.getUUID());
        int maxLives = WarConfig.STARTING_LIVES.get();
        boolean activelyContested = war.lastAttackersPresent() > 0 && war.lastDefendersPresent() > 0;
        String key = activelyContested ? "opac_warfare.actionbar.contested" : "opac_warfare.actionbar.zone";
        return Component.translatable(key, war.lastAttackersPresent(), war.lastDefendersPresent(), playerLives, maxLives);
    }

    public String actionbarText(WarData war) {
        boolean activelyContested = war.lastAttackersPresent() > 0 && war.lastDefendersPresent() > 0;
        String status = activelyContested ? "⚔ Contested!" : "⚔ Siege Zone";
        return status + " | ⚔ " + war.lastAttackersPresent() + " vs " + war.lastDefendersPresent() + " 🛡"
                + " | Capture: " + Math.round(war.progress()) + "%";
    }

    public String progressText(WarData war) {
        return "Capture " + progressBar(war.progress()) + " "
                + Math.round(war.progress()) + "% (" + signedDecimal(war.lastDeltaPerSecond()) + "/s)";
    }

    public String stateName(WarState state) {
        return switch (state) {
            case PREPARING -> "Preparing";
            case ACTIVE -> "Active";
            case FINISHED -> "Finished";
            case CANCELLED -> "Cancelled";
            case FAILED -> "Failed";
        };
    }

    public String timeText(WarData war, Instant now) {
        if (war.state() == WarState.PREPARING) {
            long elapsed = Duration.between(war.createdAt(), now).getSeconds();
            long remaining = Math.max(0L, WarConfig.PREPARATION_SECONDS.get() - elapsed);
            return "active in " + formatDuration(remaining);
        }
        if (war.state() == WarState.ACTIVE && war.activeAt().isPresent()) {
            long elapsed = Duration.between(war.activeAt().get(), now).getSeconds();
            long remaining = Math.max(0L, WarConfig.MAX_DURATION_SECONDS.get() - elapsed);
            return formatDuration(remaining) + " left";
        }
        return war.endReason().isBlank() ? "ended" : "ended: " + war.endReason();
    }

    public String dimensionName(String levelId) {
        return switch (levelId) {
            case "minecraft:overworld" -> "Overworld";
            case "minecraft:the_nether" -> "Nether";
            case "minecraft:the_end" -> "The End";
            default -> levelId;
        };
    }

    public String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    private String progressBar(double progress) {
        int filled = (int) Math.round((Math.max(0.0D, Math.min(100.0D, progress)) / 100.0D) * PROGRESS_BAR_WIDTH);
        return "[" + "#".repeat(filled) + "-".repeat(PROGRESS_BAR_WIDTH - filled) + "]";
    }

    private String signedDecimal(double value) {
        return String.format(Locale.ROOT, "%+.2f", value);
    }

    private int remainingLives(java.util.Map<java.util.UUID, Integer> lives) {
        return lives.values().stream().mapToInt(Integer::intValue).sum();
    }
}
