package dev.customclaims.aeronautics.mixin;

import dev.customclaims.aeronautics.compat.SableAssemblyProtectionHooks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "dev.ryanhcode.sable.api.SubLevelAssemblyHelper", remap = false)
public abstract class SableAssemblyMixin {
    @ModifyVariable(method = "assembleBlocks", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static Iterable<BlockPos> customclaims$filterProtectedAssembly(
            Iterable<BlockPos> blocks,
            ServerLevel level,
            BlockPos anchor,
            @Coerce Object bounds
    ) {
        List<BlockPos> sourcePositions = new ArrayList<>();
        blocks.forEach(sourcePositions::add);
        if (SableAssemblyProtectionHooks.shouldBlockAssembly(level, sourcePositions, bounds)) {
            return List.of();
        }
        return sourcePositions;
    }
}
