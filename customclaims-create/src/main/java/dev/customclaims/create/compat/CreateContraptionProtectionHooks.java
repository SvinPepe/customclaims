package dev.customclaims.create.compat;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import dev.customclaims.protection.CustomClaimsProtectionMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import java.util.UUID;

public final class CreateContraptionProtectionHooks {
    private static final String BREAKER_ID = "BreakerId";
    private static final String BREAKING_POS = "BreakingPos";
    private static final String PROGRESS = "Progress";
    private static final String TICKS_UNTIL_NEXT_PROGRESS = "TicksUntilNextProgress";

    private CreateContraptionProtectionHooks() {
    }

    public static boolean shouldBlockBreaker(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return !CustomClaimsProtectionMod.services()
                .createMachinesProtectionService()
                .canCreateAffect(serverLevel, pos);
    }

    public static boolean shouldBlockBreaker(MovementContext context, BlockPos pos) {
        if (!(context.world instanceof ServerLevel serverLevel)) {
            return false;
        }
        return !CustomClaimsProtectionMod.services()
                .createMachinesProtectionService()
                .canCreateAffect(serverLevel, pos, controllingPlayer(context));
    }

    public static boolean shouldBlockMovement(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        return !CustomClaimsProtectionMod.services()
                .createMachinesProtectionService()
                .canCreateAffect(serverLevel, pos);
    }

    public static void clearBreakerState(Level level, CompoundTag data) {
        if (level.isClientSide() || !data.contains(BREAKING_POS)) {
            return;
        }

        int breakerId = data.getInt(BREAKER_ID);
        BlockPos breakingPos = readBlockPos(data, BREAKING_POS);
        if (breakingPos == null) {
            return;
        }
        data.remove(PROGRESS);
        data.remove(TICKS_UNTIL_NEXT_PROGRESS);
        data.remove(BREAKING_POS);
        level.destroyBlockProgress(breakerId, breakingPos, -1);
    }

    public static BlockPos readBlockPos(CompoundTag data, String key) {
        Tag tag = data.get(key);
        if (tag instanceof CompoundTag compound) {
            return new BlockPos(compound.getInt("X"), compound.getInt("Y"), compound.getInt("Z"));
        }
        if (tag instanceof IntArrayTag arrayTag) {
            int[] values = arrayTag.getAsIntArray();
            if (values.length >= 3) {
                return new BlockPos(values[0], values[1], values[2]);
            }
        }
        if (tag instanceof LongTag longTag) {
            return BlockPos.of(longTag.getAsLong());
        }
        return null;
    }

    private static UUID controllingPlayer(MovementContext context) {
        if (context.contraption == null) {
            return null;
        }

        AbstractContraptionEntity entity = context.contraption.entity;
        if (entity == null) {
            return null;
        }
        return entity.getControllingPlayer().orElse(null);
    }
}
