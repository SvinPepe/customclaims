package dev.customclaims.protection.service;

public record ClaimRuleUpdateResult(
        boolean success,
        boolean changed,
        String message,
        ClaimRulesState state
) {
    public static ClaimRuleUpdateResult success(boolean changed, String message, ClaimRulesState state) {
        return new ClaimRuleUpdateResult(true, changed, message, state);
    }

    public static ClaimRuleUpdateResult failure(String message, ClaimRulesState state) {
        return new ClaimRuleUpdateResult(false, false, message, state);
    }
}
