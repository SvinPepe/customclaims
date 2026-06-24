package dev.customclaims.protection.event;

import dev.customclaims.protection.CustomClaimsProtectionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.level.ExplosionEvent;

public final class ExplosionEventHandler {
    private ExplosionEventHandler() {
    }

    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        event.getAffectedBlocks().removeIf(object ->
                object instanceof BlockPos pos
                        && !CustomClaimsProtectionMod.services().explosionProtectionService().canExplosionAffect(level, pos));
    }
}
