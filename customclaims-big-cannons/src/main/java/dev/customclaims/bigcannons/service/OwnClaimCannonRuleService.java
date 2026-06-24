package dev.customclaims.bigcannons.service;

public final class OwnClaimCannonRuleService {
    public boolean canShootFromOwnClaim(boolean explosionProtectionEnabled) {
        return !explosionProtectionEnabled;
    }
}
