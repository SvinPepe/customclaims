package dev.customclaims.bigcannons.compat;

import dev.customclaims.bigcannons.CustomClaimsBigCannonsMod;
import dev.customclaims.protection.CustomClaimsProtectionMod;
import dev.customclaims.protection.config.ProtectionConfig;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

public final class BigCannonLaunchProtectionHooks {
    private BigCannonLaunchProtectionHooks() {
    }

    public static boolean shouldBlockLaunch(
            ServerLevel level,
            AbstractMountedCannonContraption cannon,
            PitchOrientedContraptionEntity entity
    ) {
        if (!ProtectionConfig.BLOCK_BIG_CANNON_PROJECTILE_LAUNCH_FROM_PROTECTED_CLAIMS.get()) {
            return false;
        }

        BlockPos launchPos = launchPosition(cannon, entity);
        boolean allowed = CustomClaimsProtectionMod.services()
                .explosionProtectionService()
                .canExplosionAffect(level, launchPos);
        if (allowed) {
            return false;
        }

        if (ProtectionConfig.LOG_BLOCKED_BIG_CANNON_PROJECTILES.get()) {
            CustomClaimsBigCannonsMod.LOGGER.info(
                    "Blocked Create Big Cannons launch before consuming munition at {} {}",
                    level.dimension().location(),
                    launchPos.toShortString()
            );
        }
        return true;
    }

    private static BlockPos launchPosition(AbstractMountedCannonContraption cannon, PitchOrientedContraptionEntity entity) {
        try {
            BlockPos localMuzzle = findLocalMuzzle(cannon);
            Vec3 globalMuzzle = entity.toGlobalVector(Vec3.atCenterOf(localMuzzle), 1.0F);
            return BlockPos.containing(globalMuzzle);
        } catch (RuntimeException exception) {
            CustomClaimsBigCannonsMod.LOGGER.debug(
                    "Falling back to mounted cannon entity position for launch protection",
                    exception
            );
            return entity.blockPosition();
        }
    }

    private static BlockPos findLocalMuzzle(AbstractMountedCannonContraption cannon) {
        Direction orientation = cannon.initialOrientation();
        if (orientation == null) {
            return cannon.getStartPos();
        }

        Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks = cannon.getBlocks();
        BlockPos local = cannon.getStartPos().immutable();
        int remaining = Math.max(64, blocks.size() + 4);
        while (remaining-- > 0) {
            StructureTemplate.StructureBlockInfo block = blocks.get(local);
            if (block == null || block.state().isAir()) {
                return local;
            }
            local = local.relative(orientation);
        }

        return local;
    }
}
