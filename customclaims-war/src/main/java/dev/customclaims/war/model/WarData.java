package dev.customclaims.war.model;

import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.PartyId;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public final class WarData {
    private final UUID id;
    private final PartyId attackerParty;
    private final PartyId defenderParty;
    private final ChunkPosKey targetChunk;
    private final Instant createdAt;
    private WarState state;
    private double progress;
    private Instant activeAt;
    private Instant endedAt;
    private String endReason;

    public WarData(UUID id, PartyId attackerParty, PartyId defenderParty, ChunkPosKey targetChunk, Instant createdAt) {
        this.id = id;
        this.attackerParty = attackerParty;
        this.defenderParty = defenderParty;
        this.targetChunk = targetChunk;
        this.createdAt = createdAt;
        this.state = WarState.PREPARING;
        this.progress = 0.0D;
        this.endReason = "";
    }

    public UUID id() {
        return id;
    }

    public PartyId attackerParty() {
        return attackerParty;
    }

    public PartyId defenderParty() {
        return defenderParty;
    }

    public ChunkPosKey targetChunk() {
        return targetChunk;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public WarState state() {
        return state;
    }

    public void setState(WarState state) {
        this.state = state;
    }

    public double progress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = clamp(progress);
    }

    public Optional<Instant> activeAt() {
        return Optional.ofNullable(activeAt);
    }

    public void setActiveAt(Instant activeAt) {
        this.activeAt = activeAt;
    }

    public Optional<Instant> endedAt() {
        return Optional.ofNullable(endedAt);
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public String endReason() {
        return endReason;
    }

    public void setEndReason(String endReason) {
        this.endReason = endReason;
    }

    public boolean isTerminal() {
        return state == WarState.FINISHED || state == WarState.CANCELLED || state == WarState.FAILED;
    }

    public String toStorageLine() {
        return String.join("|",
                id.toString(),
                encode(attackerParty.value()),
                encode(defenderParty.value()),
                encode(targetChunk.levelId()),
                Integer.toString(targetChunk.x()),
                Integer.toString(targetChunk.z()),
                state.name(),
                Double.toString(progress),
                createdAt.toString(),
                activeAt == null ? "" : activeAt.toString(),
                endedAt == null ? "" : endedAt.toString(),
                encode(endReason)
        );
    }

    public static Optional<WarData> fromStorageLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 12) {
            return Optional.empty();
        }

        try {
            WarData data = new WarData(
                    UUID.fromString(parts[0]),
                    PartyId.of(decode(parts[1])),
                    PartyId.of(decode(parts[2])),
                    new ChunkPosKey(decode(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5])),
                    Instant.parse(parts[8])
            );
            data.setState(WarState.valueOf(parts[6]));
            data.setProgress(Double.parseDouble(parts[7]));
            if (!parts[9].isBlank()) {
                data.setActiveAt(Instant.parse(parts[9]));
            }
            if (!parts[10].isBlank()) {
                data.setEndedAt(Instant.parse(parts[10]));
            }
            data.setEndReason(decode(parts[11]));
            return Optional.of(data);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String value) {
        if (value.isBlank()) {
            return "";
        }
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(100.0D, value));
    }
}
