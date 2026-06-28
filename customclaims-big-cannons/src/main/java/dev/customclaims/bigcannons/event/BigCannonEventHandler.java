package dev.customclaims.bigcannons.event;

import dev.customclaims.bigcannons.CustomClaimsBigCannonsMod;
import dev.customclaims.protection.CustomClaimsProtectionMod;
import dev.customclaims.protection.config.ProtectionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import rbasamoyai.createbigcannons.events.ProjectileDamageEvent;
import rbasamoyai.createbigcannons.munitions.AbstractCannonProjectile;

public final class BigCannonEventHandler {
    private BigCannonEventHandler() {
    }

    public static void onProjectileDamage(ProjectileDamageEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (!CustomClaimsProtectionMod.services()
                .explosionProtectionService()
                .canExplosionAffect(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!ProtectionConfig.BLOCK_BIG_CANNON_PROJECTILE_LAUNCH_FROM_PROTECTED_CLAIMS.get()) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof AbstractCannonProjectile projectile)) {
            return;
        }

        BlockPos pos = projectile.blockPosition();
        if (CustomClaimsProtectionMod.services()
                .explosionProtectionService()
                .canExplosionAffect(level, pos)) {
            return;
        }

        event.setCanceled(true);
        projectile.discard();

        if (ProtectionConfig.LOG_BLOCKED_BIG_CANNON_PROJECTILES.get()) {
            CustomClaimsBigCannonsMod.LOGGER.info(
                    "Blocked Create Big Cannons projectile {} from protected claim at {} {}",
                    BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType()),
                    level.dimension().location(),
                    pos.toShortString()
            );
        }
    }
}
