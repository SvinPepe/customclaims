package dev.customclaims.core.api.model;

import java.util.Objects;

public record ClaimId(String value) {
    public ClaimId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Claim id cannot be blank");
        }
        value = value.trim();
    }

    public static ClaimId of(String value) {
        return new ClaimId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
