package dev.customclaims.war.config;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WarConfigTest {

    @Test
    @DisplayName("Default contested claim owner UUID matches configured constant")
    void testContestedOwnerUuid() {
        UUID expected = UUID.fromString("a9d1645e-b816-3b60-b6d4-d5581ff24009");
        assertEquals(expected, WarConfig.DEFAULT_CONTESTED_OWNER_UUID);
        assertEquals(expected, WarConfig.getContestedClaimOwnerUuid());
    }

    @Test
    @DisplayName("Contested claim config defaults are present and valid")
    void testContestedClaimConfigDefaults() {
        assertNotNull(WarConfig.CONTESTED_CLAIM_OWNER_UUID.getDefault());
        assertEquals("a9d1645e-b816-3b60-b6d4-d5581ff24009", WarConfig.CONTESTED_CLAIM_OWNER_UUID.getDefault());
        assertEquals("Contested Territory", WarConfig.CONTESTED_CLAIM_NAME.getDefault());
        assertEquals(-3342336, WarConfig.CONTESTED_CLAIM_COLOR.getDefault());
    }
}
