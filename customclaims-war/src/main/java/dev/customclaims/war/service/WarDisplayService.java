package dev.customclaims.war.service;

import dev.customclaims.core.CoreServices;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.PartyDisplayInfo;
import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.model.WarData;
import dev.customclaims.war.model.WarState;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import net.minecraft.server.MinecraftServer;

public final class WarDisplayService {
    private static final int PROGRESS_BAR_WIDTH = 12;

    private final CoreServices coreServices;

    public WarDisplayService(CoreServices coreServices) {
        this.coreServices = coreServices;
    }

    public String formatWarStatus(MinecraftServer server, WarData war, Instant now) {
        return label(server, war)
                + "\nState: " + stateName(war.state()) + " | " + progressText(war)
                + "\nAttackers: " + partySummary(server, war.attackerParty())
                + "\nDefenders: " + partySummary(server, war.defenderParty())
                + "\nPresence: ATK " + war.lastAttackersPresent() + " DEF " + war.lastDefendersPresent()
                + " | Lives: ATK " + remainingLives(war.attackerLives()) + " DEF " + remainingLives(war.defenderLives())
                + " | Time: " + timeText(war, now);
    }

    public String formatWarListEntry(MinecraftServer server, WarData war, Instant now) {
        return "- " + label(server, war)
                + " | " + stateName(war.state())
                + " | " + Math.round(war.progress()) + "%"
                + " | " + timeText(war, now);
    }

    public String formatStart(MinecraftServer server, WarData war) {
        return "War started: " + label(server, war)
                + "\nAttackers: " + partySummary(server, war.attackerParty())
                + "\nDefenders: " + partySummary(server, war.defenderParty())
                + "\nPreparation: " + formatDuration(WarConfig.PREPARATION_SECONDS.get());
    }

    public String formatAdminProgress(WarData war) {
        return "Progress set to " + Math.round(war.progress()) + "% for " + chunkLabel(war.targetChunk()) + ".";
    }

    public String label(MinecraftServer server, WarData war) {
        return partyName(server, war.attackerParty())
                + " vs " + partyName(server, war.defenderParty())
                + " @ " + chunkLabel(war.targetChunk());
    }

    public String partyName(MinecraftServer server, PartyId partyId) {
        return coreServices.partyService().describeParty(server, partyId)
                .map(PartyDisplayInfo::name)
                .orElse("Unknown party");
    }

    public String partySummary(MinecraftServer server, PartyId partyId) {
        return coreServices.partyService().describeParty(server, partyId)
                .map(info -> info.name()
                        + " (owner " + info.ownerName()
                        + ", online " + info.onlineCount() + "/" + info.memberCount() + ")")
                .orElse("Unknown party");
    }

    public String chunkLabel(ChunkPosKey key) {
        return dimensionName(key.levelId()) + " [chunk " + key.x() + ", " + key.z() + "]";
    }

    public String actionbarText(WarData war) {
        return "Capture " + Math.round(war.progress()) + "% (" + signedDecimal(war.lastDeltaPerSecond())
                + "/s) | ATK " + war.lastAttackersPresent() + " DEF " + war.lastDefendersPresent()
                + " | Lives A " + remainingLives(war.attackerLives()) + " D " + remainingLives(war.defenderLives());
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
