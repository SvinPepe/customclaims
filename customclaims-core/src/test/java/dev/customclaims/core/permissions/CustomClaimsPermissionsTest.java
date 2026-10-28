package dev.customclaims.core.permissions;

import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomClaimsPermissionsTest {

    @Test
    @DisplayName("All opac.warfare.* nodes are registered and non-null")
    void testWarfareNodes() {
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.WAR_START));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.WAR_CANCEL));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.WAR_STATUS));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.WAR_ADMIN));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.WAR_BYPASS));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.ROLLBACK));
    }

    @Test
    @DisplayName("All opac.protection.* nodes are registered and non-null")
    void testProtectionNodes() {
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.CLAIMRULES));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.EXPLOSIONS_STATUS));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.EXPLOSIONS_TOGGLE));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.CREATE_STATUS));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.CREATE_TOGGLE));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.ASSEMBLY_STATUS));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.ASSEMBLY_TOGGLE));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.LIMITS_RESET));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.PROTECTION_BYPASS));
        assertNotNull(CustomClaimsPermissions.getNode(CustomClaimsPermissions.PROTECTION_ADMIN));
    }

    @Test
    @DisplayName("Legacy customclaims.* aliases resolve to their canonical opac.* counterparts")
    void testLegacyAliases() {
        assertSame(
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.WAR_START),
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.LEGACY_WAR_START)
        );
        assertSame(
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.WAR_STATUS),
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.LEGACY_WAR_STATUS)
        );
        assertSame(
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.CLAIMRULES),
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.LEGACY_CLAIMRULES)
        );
        assertSame(
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.EXPLOSIONS_TOGGLE),
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.LEGACY_EXPLOSIONS_TOGGLE)
        );
        assertSame(
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.CREATE_TOGGLE),
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.LEGACY_CREATE_TOGGLE)
        );
        assertSame(
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.ASSEMBLY_TOGGLE),
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.LEGACY_ASSEMBLY_TOGGLE)
        );
        assertSame(
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.ADMIN),
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.LEGACY_ADMIN)
        );
        assertSame(
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.BYPASS),
                CustomClaimsPermissions.getNode(CustomClaimsPermissions.LEGACY_BYPASS)
        );
    }
}
