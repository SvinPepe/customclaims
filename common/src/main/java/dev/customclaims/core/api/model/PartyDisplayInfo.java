package dev.customclaims.core.api.model;

public record PartyDisplayInfo(
        PartyId id,
        String name,
        String ownerName,
        int onlineCount,
        int memberCount
) {
    public PartyDisplayInfo {
        if (name == null || name.isBlank()) {
            name = "Unnamed party";
        }
        if (ownerName == null || ownerName.isBlank()) {
            ownerName = "unknown";
        }
    }
}
