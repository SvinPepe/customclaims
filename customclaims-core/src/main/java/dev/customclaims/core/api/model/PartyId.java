package dev.customclaims.core.api.model;

import java.util.Objects;

public record PartyId(String value) {
    public PartyId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Party id cannot be blank");
        }
        value = value.trim();
    }

    public static PartyId of(String value) {
        return new PartyId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
