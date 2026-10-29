package dev.customclaims.core.api.model;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record ClaimSideId(Kind kind, String id) {
    public enum Kind {
        PARTY("party"),
        PLAYER("player");

        private final String prefix;

        Kind(String prefix) {
            this.prefix = prefix;
        }

        public String prefix() {
            return prefix;
        }
    }

    public ClaimSideId {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Claim side id cannot be blank");
        }
        id = id.trim();
    }

    public static ClaimSideId party(PartyId partyId) {
        return new ClaimSideId(Kind.PARTY, partyId.value());
    }

    public static ClaimSideId player(UUID playerId) {
        return new ClaimSideId(Kind.PLAYER, playerId.toString());
    }

    public static ClaimSideId parse(String value) {
        String trimmed = Objects.requireNonNull(value, "value").trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Claim side id cannot be blank");
        }
        int separator = trimmed.indexOf(':');
        if (separator < 0) {
            return new ClaimSideId(Kind.PARTY, trimmed);
        }

        String prefix = trimmed.substring(0, separator).toLowerCase(Locale.ROOT);
        String id = trimmed.substring(separator + 1);
        return switch (prefix) {
            case "party" -> new ClaimSideId(Kind.PARTY, id);
            case "player" -> new ClaimSideId(Kind.PLAYER, id);
            default -> throw new IllegalArgumentException("Unknown claim side type: " + prefix);
        };
    }

    public String storageKey() {
        return kind.prefix() + ":" + id;
    }

    public boolean isParty() {
        return kind == Kind.PARTY;
    }

    public boolean isPlayer() {
        return kind == Kind.PLAYER;
    }

    public Optional<PartyId> partyId() {
        return isParty() ? Optional.of(PartyId.of(id)) : Optional.empty();
    }

    public Optional<UUID> playerUuid() {
        if (!isPlayer()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public String shortLabel() {
        String prefix = kind == Kind.PARTY ? "Party" : "Player";
        return prefix + " " + id.substring(0, Math.min(8, id.length()));
    }

    @Override
    public String toString() {
        return storageKey();
    }
}
