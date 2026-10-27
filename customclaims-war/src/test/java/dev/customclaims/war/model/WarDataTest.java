package dev.customclaims.war.model;

import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.api.model.PartyId;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WarDataTest {

    @Test
    @DisplayName("decrementLives decrements down to 0 and returns empty once exhausted")
    void testDecrementLivesExhaustion() {
        UUID warId = UUID.randomUUID();
        ClaimSideId attackerSide = ClaimSideId.party(PartyId.of("attacker"));
        ClaimSideId defenderSide = ClaimSideId.party(PartyId.of("defender"));
        ChunkPosKey targetChunk = new ChunkPosKey("minecraft:overworld", 10, 20);
        Set<ChunkPosKey> sector = Set.of(targetChunk, new ChunkPosKey("minecraft:overworld", 10, 21));

        WarData war = new WarData(warId, attackerSide, defenderSide, targetChunk, sector, Instant.now());

        UUID attackerPlayer = UUID.randomUUID();
        UUID defenderPlayer = UUID.randomUUID();
        UUID strangerPlayer = UUID.randomUUID();

        // Assign lives to attacker and defender
        war.initializeLives(java.util.List.of(attackerPlayer), java.util.List.of(defenderPlayer), 3);

        assertTrue(war.hasLivesRemaining(attackerPlayer));
        assertEquals(3, war.lives(attackerPlayer));

        // Decrement attacker: 3 -> 2
        Optional<Integer> rem1 = war.decrementLives(attackerPlayer);
        assertTrue(rem1.isPresent());
        assertEquals(2, rem1.get());
        assertTrue(war.hasLivesRemaining(attackerPlayer));

        // Decrement attacker: 2 -> 1
        Optional<Integer> rem2 = war.decrementLives(attackerPlayer);
        assertTrue(rem2.isPresent());
        assertEquals(1, rem2.get());

        // Decrement attacker: 1 -> 0
        Optional<Integer> rem3 = war.decrementLives(attackerPlayer);
        assertTrue(rem3.isPresent());
        assertEquals(0, rem3.get());
        assertFalse(war.hasLivesRemaining(attackerPlayer));
        assertEquals(0, war.lives(attackerPlayer));

        // Decrement attacker when already 0: must return empty (not Optional.of(0))
        Optional<Integer> rem4 = war.decrementLives(attackerPlayer);
        assertTrue(rem4.isEmpty(), "Subsequent death when already at 0 lives must return empty");

        // Defender: 3 -> 2 -> 1 -> 0
        assertEquals(2, war.decrementLives(defenderPlayer).orElseThrow());
        assertEquals(1, war.decrementLives(defenderPlayer).orElseThrow());
        Optional<Integer> defRem1 = war.decrementLives(defenderPlayer);
        assertTrue(defRem1.isPresent());
        assertEquals(0, defRem1.get());
        assertFalse(war.hasLivesRemaining(defenderPlayer));

        // Subsequent defender death: must return empty
        Optional<Integer> defRem2 = war.decrementLives(defenderPlayer);
        assertTrue(defRem2.isEmpty());

        // Stranger not in war: must return empty
        Optional<Integer> strangerRem = war.decrementLives(strangerPlayer);
        assertTrue(strangerRem.isEmpty());
        assertFalse(war.hasLivesRemaining(strangerPlayer));
        assertEquals(0, war.lives(strangerPlayer));
    }

    @Test
    @DisplayName("isInSector correctly checks chunk positions against sector")
    void testIsInSector() {
        UUID warId = UUID.randomUUID();
        ClaimSideId attackerSide = ClaimSideId.party(PartyId.of("attacker"));
        ClaimSideId defenderSide = ClaimSideId.party(PartyId.of("defender"));
        ChunkPosKey targetChunk = new ChunkPosKey("minecraft:overworld", 5, 5);
        Set<ChunkPosKey> sector = Set.of(
                targetChunk,
                new ChunkPosKey("minecraft:overworld", 5, 6),
                new ChunkPosKey("minecraft:overworld", 6, 5)
        );

        WarData war = new WarData(warId, attackerSide, defenderSide, targetChunk, sector, Instant.now());

        assertTrue(war.isInSector("minecraft:overworld", 5, 5));
        assertTrue(war.isInSector("minecraft:overworld", 5, 6));
        assertTrue(war.isInSector("minecraft:overworld", 6, 5));
        assertFalse(war.isInSector("minecraft:overworld", 7, 5));
        assertFalse(war.isInSector("minecraft:overworld", 0, 0));
        assertFalse(war.isInSector("minecraft:the_nether", 5, 5));

        assertTrue(war.isInSector(new ChunkPosKey("minecraft:overworld", 5, 5)));
        assertFalse(war.isInSector(new ChunkPosKey("minecraft:the_nether", 5, 5)));
    }
}
