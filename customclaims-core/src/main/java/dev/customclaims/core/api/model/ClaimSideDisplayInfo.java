package dev.customclaims.core.api.model;

public record ClaimSideDisplayInfo(
        ClaimSideId id,
        String name,
        String ownerName,
        int onlineCount,
        int memberCount
) {
    public ClaimSideDisplayInfo {
        if (name == null || name.isBlank()) {
            name = id == null ? "Unknown" : id.shortLabel();
        }
        if (ownerName == null || ownerName.isBlank()) {
            ownerName = "unknown";
        }
    }
}
