package dev.customclaims.aeronautics.mixin;

import dev.customclaims.aeronautics.compat.SableAssemblyProtectionHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.ryanhcode.sable.api.SubLevelAssemblyHelper", remap = false)
public abstract class SableAssemblyMixin {
    @Inject(method = "assembleBlocks", at = @At("HEAD"), cancellable = true)
    private static void customclaims$validateProtectedAssembly(
            ServerLevel level,
            BlockPos anchor,
            Iterable<BlockPos> blocks,
            @Coerce Object bounds,
            CallbackInfoReturnable<?> cir
    ) {
        if (SableAssemblyProtectionHooks.shouldBlockAssembly(level, blocks, bounds)) {
            // Simulated treats a null sub-level as a clean assembly refusal. Cancelling here is
            // essential: Sable allocates the sub-level before moveBlocks, which assumes at least
            // one block and crashes when protection replaces the iterable with an empty one.
            cir.setReturnValue(null);
        }
    }
}
