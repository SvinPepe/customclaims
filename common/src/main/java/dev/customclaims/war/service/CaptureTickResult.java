package dev.customclaims.war.service;

public record CaptureTickResult(
        double progress,
        double deltaPerSecond,
        int attackersPresent,
        int defendersPresent
) {
}
