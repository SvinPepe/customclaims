package dev.customclaims.war.model;

import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.ClaimSnapshot;
import dev.customclaims.core.api.model.PartyId;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
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
    private UUID originalClaimOwnerId;
    private boolean originalClaimPartyOwned;
    private int originalClaimSubConfigIndex;
    private boolean originalClaimForceload;
    private double lastDeltaPerSecond;
    private int lastAttackersPresent;
    private int lastDefendersPresent;
    private boolean preparationWarning60Sent;
    private boolean preparationWarning10Sent;
    private int highestNotifiedMilestone;
    private Instant lastEmptyDecayNotificationAt;
    private boolean livesInitialized;
    private final Map<UUID, Integer> attackerLives = new LinkedHashMap<>();
    private final Map<UUID, Integer> defenderLives = new LinkedHashMap<>();

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

    public double lastDeltaPerSecond() {
        return lastDeltaPerSecond;
    }

    public int lastAttackersPresent() {
        return lastAttackersPresent;
    }

    public int lastDefendersPresent() {
        return lastDefendersPresent;
    }

    public void setCaptureSnapshot(double deltaPerSecond, int attackersPresent, int defendersPresent) {
        this.lastDeltaPerSecond = deltaPerSecond;
        this.lastAttackersPresent = attackersPresent;
        this.lastDefendersPresent = defendersPresent;
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

    public Optional<ClaimSnapshot> originalClaimSnapshot() {
        if (originalClaimOwnerId == null) {
            return Optional.empty();
        }
        return Optional.of(new ClaimSnapshot(
                originalClaimOwnerId,
                originalClaimPartyOwned,
                originalClaimSubConfigIndex,
                originalClaimForceload
        ));
    }

    public void setOriginalClaimSnapshot(ClaimSnapshot snapshot) {
        this.originalClaimOwnerId = snapshot.ownerId();
        this.originalClaimPartyOwned = snapshot.partyOwned();
        this.originalClaimSubConfigIndex = snapshot.subConfigIndex();
        this.originalClaimForceload = snapshot.forceload();
    }

    public boolean preparationWarning60Sent() {
        return preparationWarning60Sent;
    }

    public void setPreparationWarning60Sent() {
        this.preparationWarning60Sent = true;
    }

    public boolean preparationWarning10Sent() {
        return preparationWarning10Sent;
    }

    public void setPreparationWarning10Sent() {
        this.preparationWarning10Sent = true;
    }

    public int highestNotifiedMilestone() {
        return highestNotifiedMilestone;
    }

    public void setHighestNotifiedMilestone(int highestNotifiedMilestone) {
        this.highestNotifiedMilestone = highestNotifiedMilestone;
    }

    public Optional<Instant> lastEmptyDecayNotificationAt() {
        return Optional.ofNullable(lastEmptyDecayNotificationAt);
    }

    public void setLastEmptyDecayNotificationAt(Instant lastEmptyDecayNotificationAt) {
        this.lastEmptyDecayNotificationAt = lastEmptyDecayNotificationAt;
    }

    public boolean livesInitialized() {
        return livesInitialized;
    }

    public void initializeLives(Collection<UUID> attackerMemberIds, Collection<UUID> defenderMemberIds, int startingLives) {
        attackerLives.clear();
        defenderLives.clear();
        attackerMemberIds.forEach(playerId -> attackerLives.put(playerId, startingLives));
        defenderMemberIds.forEach(playerId -> defenderLives.put(playerId, startingLives));
        livesInitialized = true;
    }

    public boolean hasLivesRemaining(UUID playerId) {
        Integer attackerValue = attackerLives.get(playerId);
        if (attackerValue != null) {
            return attackerValue > 0;
        }
        Integer defenderValue = defenderLives.get(playerId);
        return defenderValue != null && defenderValue > 0;
    }

    public int lives(UUID playerId) {
        Integer attackerValue = attackerLives.get(playerId);
        if (attackerValue != null) {
            return attackerValue;
        }
        return defenderLives.getOrDefault(playerId, 0);
    }

    public Optional<Integer> decrementLives(UUID playerId) {
        if (attackerLives.containsKey(playerId)) {
            int current = attackerLives.get(playerId);
            if (current <= 0) {
                return Optional.empty();
            }
            int updated = current - 1;
            attackerLives.put(playerId, updated);
            return Optional.of(updated);
        }
        if (defenderLives.containsKey(playerId)) {
            int current = defenderLives.get(playerId);
            if (current <= 0) {
                return Optional.empty();
            }
            int updated = current - 1;
            defenderLives.put(playerId, updated);
            return Optional.of(updated);
        }
        return Optional.empty();
    }

    public Map<UUID, Integer> attackerLives() {
        return Collections.unmodifiableMap(attackerLives);
    }

    public Map<UUID, Integer> defenderLives() {
        return Collections.unmodifiableMap(defenderLives);
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
                encode(endReason),
                originalClaimOwnerId == null ? "" : originalClaimOwnerId.toString(),
                Boolean.toString(originalClaimPartyOwned),
                Integer.toString(originalClaimSubConfigIndex),
                Boolean.toString(originalClaimForceload),
                Boolean.toString(livesInitialized),
                encode(livesMapToString(attackerLives)),
                encode(livesMapToString(defenderLives))
        );
    }

    public static Optional<WarData> fromStorageLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 12 && parts.length != 16 && parts.length != 19) {
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
            if (parts.length == 16 && !parts[12].isBlank()) {
                data.originalClaimOwnerId = UUID.fromString(parts[12]);
                data.originalClaimPartyOwned = Boolean.parseBoolean(parts[13]);
                data.originalClaimSubConfigIndex = Integer.parseInt(parts[14]);
                data.originalClaimForceload = Boolean.parseBoolean(parts[15]);
            }
            if (parts.length == 19) {
                if (!parts[12].isBlank()) {
                    data.originalClaimOwnerId = UUID.fromString(parts[12]);
                    data.originalClaimPartyOwned = Boolean.parseBoolean(parts[13]);
                    data.originalClaimSubConfigIndex = Integer.parseInt(parts[14]);
                    data.originalClaimForceload = Boolean.parseBoolean(parts[15]);
                }
                data.livesInitialized = Boolean.parseBoolean(parts[16]);
                data.attackerLives.putAll(parseLivesMap(decode(parts[17])));
                data.defenderLives.putAll(parseLivesMap(decode(parts[18])));
            }
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

    private static String livesMapToString(Map<UUID, Integer> lives) {
        return lives.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private static Map<UUID, Integer> parseLivesMap(String value) {
        Map<UUID, Integer> lives = new LinkedHashMap<>();
        if (value.isBlank()) {
            return lives;
        }
        for (String entry : value.split(",")) {
            String[] pair = entry.split("=", 2);
            if (pair.length == 2) {
                lives.put(UUID.fromString(pair[0]), Math.max(0, Integer.parseInt(pair[1])));
            }
        }
        return lives;
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(100.0D, value));
    }
}
