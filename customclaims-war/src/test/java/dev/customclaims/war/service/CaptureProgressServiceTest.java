package dev.customclaims.war.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CaptureProgressServiceTest {

    private static final double EPSILON = 0.0001D;

    @Test
    @DisplayName("Single attacker gets exact base capture rate (+0.40%/s)")
    void testSingleAttackerBaseRate() {
        double delta = CaptureProgressService.calculateDelta(1, 0);
        assertEquals(0.40D, delta, EPSILON);
    }

    @Test
    @DisplayName("Extra attackers give micro-bonus (+0.05%/s per extra attacker)")
    void testExtraAttackerBonus() {
        double delta2 = CaptureProgressService.calculateDelta(2, 0);
        assertEquals(0.45D, delta2, EPSILON);

        double delta3 = CaptureProgressService.calculateDelta(3, 0);
        assertEquals(0.50D, delta3, EPSILON);

        double delta4 = CaptureProgressService.calculateDelta(4, 0);
        assertEquals(0.55D, delta4, EPSILON);
    }

    @Test
    @DisplayName("Anti-zerg hard ceiling cap (+0.60%/s) is strictly enforced regardless of attacker count")
    void testAntiZergHardCap() {
        double delta5 = CaptureProgressService.calculateDelta(5, 0);
        assertEquals(0.60D, delta5, EPSILON);

        double delta10 = CaptureProgressService.calculateDelta(10, 0);
        assertEquals(0.60D, delta10, EPSILON, "10 attackers must be capped at 0.60%/s");

        double delta50 = CaptureProgressService.calculateDelta(50, 0);
        assertEquals(0.60D, delta50, EPSILON, "50 attackers must be capped at 0.60%/s");
    }

    @Test
    @DisplayName("Defenders actively counter capture progress (-0.35%/s per defender)")
    void testDefenderResistance() {
        // 1 attacker (+0.40), 1 defender (-0.35) -> +0.05
        double contested1v1 = CaptureProgressService.calculateDelta(1, 1);
        assertEquals(0.05D, contested1v1, EPSILON);

        // 1 attacker (+0.40), 2 defenders (-0.70) -> -0.30
        double contested1v2 = CaptureProgressService.calculateDelta(1, 2);
        assertEquals(-0.30D, contested1v2, EPSILON);

        // 0 attackers, 1 defender -> -0.35
        double defendersOnly = CaptureProgressService.calculateDelta(0, 1);
        assertEquals(-0.35D, defendersOnly, EPSILON);
    }

    @Test
    @DisplayName("Empty chunk decays at configured rate (-0.50%/s)")
    void testEmptyChunkDecay() {
        double emptyDelta = CaptureProgressService.calculateDelta(0, 0);
        assertEquals(-0.50D, emptyDelta, EPSILON);
    }

    @Test
    @DisplayName("Clamp keeps progress strictly within [0.0, 100.0]")
    void testProgressClamping() {
        assertEquals(0.0D, CaptureProgressService.clamp(-10.0D));
        assertEquals(0.0D, CaptureProgressService.clamp(0.0D));
        assertEquals(55.5D, CaptureProgressService.clamp(55.5D));
        assertEquals(100.0D, CaptureProgressService.clamp(100.0D));
        assertEquals(100.0D, CaptureProgressService.clamp(115.0D));
    }
}
