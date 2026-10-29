package dev.customclaims.core.permissions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomClaimsPermissionsTest {

    @Test
    @DisplayName("All opac.warfare.* node constants are non-null and valid")
    void testWarfareNodes() {
        assertNotNull(CustomClaimsPermissions.WAR_START);
        assertNotNull(CustomClaimsPermissions.WAR_CANCEL);
        assertNotNull(CustomClaimsPermissions.WAR_STATUS);
        assertNotNull(CustomClaimsPermissions.WAR_ADMIN);
        assertNotNull(CustomClaimsPermissions.WAR_BYPASS);
        assertNotNull(CustomClaimsPermissions.ROLLBACK);
    }

    @Test
    @DisplayName("All opac.protection.* node constants are non-null and valid")
    void testProtectionNodes() {
        assertNotNull(CustomClaimsPermissions.CLAIMRULES);
        assertNotNull(CustomClaimsPermissions.EXPLOSIONS_STATUS);
        assertNotNull(CustomClaimsPermissions.EXPLOSIONS_TOGGLE);
        assertNotNull(CustomClaimsPermissions.CREATE_STATUS);
        assertNotNull(CustomClaimsPermissions.CREATE_TOGGLE);
        assertNotNull(CustomClaimsPermissions.ASSEMBLY_STATUS);
        assertNotNull(CustomClaimsPermissions.ASSEMBLY_TOGGLE);
        assertNotNull(CustomClaimsPermissions.LIMITS_RESET);
        assertNotNull(CustomClaimsPermissions.PROTECTION_BYPASS);
        assertNotNull(CustomClaimsPermissions.PROTECTION_ADMIN);
    }

    @Test
    @DisplayName("Legacy customclaims.* aliases resolve to their canonical opac.* counterparts")
    void testLegacyAliases() {
        assertEquals(
                CustomClaimsPermissions.WAR_START,
                CustomClaimsPermissions.resolveCanonical(CustomClaimsPermissions.LEGACY_WAR_START)
        );
        assertEquals(
                CustomClaimsPermissions.WAR_STATUS,
                CustomClaimsPermissions.resolveCanonical(CustomClaimsPermissions.LEGACY_WAR_STATUS)
        );
        assertEquals(
                CustomClaimsPermissions.CLAIMRULES,
                CustomClaimsPermissions.resolveCanonical(CustomClaimsPermissions.LEGACY_CLAIMRULES)
        );
        assertEquals(
                CustomClaimsPermissions.EXPLOSIONS_TOGGLE,
                CustomClaimsPermissions.resolveCanonical(CustomClaimsPermissions.LEGACY_EXPLOSIONS_TOGGLE)
        );
        assertEquals(
                CustomClaimsPermissions.CREATE_TOGGLE,
                CustomClaimsPermissions.resolveCanonical(CustomClaimsPermissions.LEGACY_CREATE_TOGGLE)
        );
        assertEquals(
                CustomClaimsPermissions.ASSEMBLY_TOGGLE,
                CustomClaimsPermissions.resolveCanonical(CustomClaimsPermissions.LEGACY_ASSEMBLY_TOGGLE)
        );
        assertEquals(
                CustomClaimsPermissions.ADMIN,
                CustomClaimsPermissions.resolveCanonical(CustomClaimsPermissions.LEGACY_ADMIN)
        );
        assertEquals(
                CustomClaimsPermissions.BYPASS,
                CustomClaimsPermissions.resolveCanonical(CustomClaimsPermissions.LEGACY_BYPASS)
        );
    }
}
