package dev.customclaims.aeronautics.mixin;

import dev.customclaims.aeronautics.compat.AeronauticsBoreProtectionHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.ryanhcode.offroad.handlers.server.MultiMiningServerManager", remap = false)
public abstract class MultiMiningServerManagerMixin {
    @Inject(
            method = "addOrRefreshPos(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ldev/ryanhcode/offroad/handlers/server/MultiMiningSupplier;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void customclaims$addOrRefreshPos(
            Level level,
            BlockPos pos,
            @Coerce Object supplier,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (AeronauticsBoreProtectionHooks.shouldBlockBoreMining(level, pos)) {
            AeronauticsBoreProtectionHooks.stallSupplier(supplier);
            cir.setReturnValue(false);
        }
    }
}