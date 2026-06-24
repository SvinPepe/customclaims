package dev.customclaims.war.service;

public record WarOperationResult(boolean success, String message) {
    public static WarOperationResult ok(String message) {
        return new WarOperationResult(true, message);
    }

    public static WarOperationResult fail(String message) {
        return new WarOperationResult(false, message);
    }
}
