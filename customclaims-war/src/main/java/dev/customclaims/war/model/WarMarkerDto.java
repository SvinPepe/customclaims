package dev.customclaims.war.model;

public record WarMarkerDto(
        String label,
        String state,
        String dimension,
        int chunkX,
        int chunkZ,
        String attackerName,
        String defenderName,
        double progress,
        double deltaPerSecond,
        int attackerCount,
        int defenderCount,
        String viewerRelation
) {
}
