package dev.customclaims.core.permissions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CustomClaimsPermissions {
    // Warfare nodes
    public static final String WAR_START = "opac.warfare.start";
    public static final String WAR_CANCEL = "opac.warfare.cancel";
    public static final String WAR_STATUS = "opac.warfare.status";
    public static final String WAR_ADMIN = "opac.warfare.admin";
    public static final String WAR_BYPASS = "opac.warfare.bypass";
    public static final String ROLLBACK = "opac.warfare.rollback";

    // Protection nodes
    public static final String CLAIMRULES = "opac.protection.claimrules";
    public static final String EXPLOSIONS_STATUS = "opac.protection.explosions.status";
    public static final String EXPLOSIONS_TOGGLE = "opac.protection.explosions.toggle";
    public static final String CREATE_STATUS = "opac.protection.create.status";
    public static final String CREATE_TOGGLE = "opac.protection.create.toggle";
    public static final String ASSEMBLY_STATUS = "opac.protection.assembly.status";
    public static final String ASSEMBLY_TOGGLE = "opac.protection.assembly.toggle";
    public static final String LIMITS_RESET = "opac.protection.limits.reset";
    public static final String PROTECTION_BYPASS = "opac.protection.bypass";
    public static final String PROTECTION_ADMIN = "opac.protection.admin";

    // Global nodes
    public static final String ADMIN = "opac.admin";
    public static final String BYPASS = "opac.bypass";

    // Legacy aliases
    public static final String LEGACY_ADMIN = "customclaims.admin";
    public static final String LEGACY_BYPASS = "customclaims.bypass";
    public static final String LEGACY_WAR_START = "customclaims.war.start";
    public static final String LEGACY_WAR_CANCEL = "customclaims.war.cancel";
    public static final String LEGACY_WAR_STATUS = "customclaims.war.status";
    public static final String LEGACY_WAR_ADMIN = "customclaims.war.admin";
    public static final String LEGACY_ROLLBACK = "customclaims.rollback";
    public static final String LEGACY_CLAIMRULES = "customclaims.claimrules";
    public static final String LEGACY_LIMITS_RESET = "customclaims.limits.reset";
    public static final String LEGACY_EXPLOSIONS_STATUS = "customclaims.explosions.status";
    public static final String LEGACY_EXPLOSIONS_TOGGLE = "customclaims.explosions.toggle";
    public static final String LEGACY_CREATE_STATUS = "customclaims.create.status";
    public static final String LEGACY_CREATE_TOGGLE = "customclaims.create.toggle";
    public static final String LEGACY_ASSEMBLY_STATUS = "customclaims.assembly.status";
    public static final String LEGACY_ASSEMBLY_TOGGLE = "customclaims.assembly.toggle";

    private static final Map<String, String> ALIAS_MAP = new HashMap<>();

    static {
        alias(LEGACY_ADMIN, ADMIN);
        alias(LEGACY_BYPASS, BYPASS);
        alias(LEGACY_WAR_START, WAR_START);
        alias(LEGACY_WAR_CANCEL, WAR_CANCEL);
        alias(LEGACY_WAR_STATUS, WAR_STATUS);
        alias(LEGACY_WAR_ADMIN, WAR_ADMIN);
        alias(LEGACY_ROLLBACK, ROLLBACK);
        alias(LEGACY_CLAIMRULES, CLAIMRULES);
        alias(LEGACY_LIMITS_RESET, LIMITS_RESET);
        alias(LEGACY_EXPLOSIONS_STATUS, EXPLOSIONS_STATUS);
        alias(LEGACY_EXPLOSIONS_TOGGLE, EXPLOSIONS_TOGGLE);
        alias(LEGACY_CREATE_STATUS, CREATE_STATUS);
        alias(LEGACY_CREATE_TOGGLE, CREATE_TOGGLE);
        alias(LEGACY_ASSEMBLY_STATUS, ASSEMBLY_STATUS);
        alias(LEGACY_ASSEMBLY_TOGGLE, ASSEMBLY_TOGGLE);
    }

    private static void alias(String legacy, String target) {
        ALIAS_MAP.put(legacy, target);
    }

    public static String resolveCanonical(String permission) {
        return ALIAS_MAP.getOrDefault(permission, permission);
    }

    public static Map<String, String> getAliases() {
        return Collections.unmodifiableMap(ALIAS_MAP);
    }

    private CustomClaimsPermissions() {
    }
}
