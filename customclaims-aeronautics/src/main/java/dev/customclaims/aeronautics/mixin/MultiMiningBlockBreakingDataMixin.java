package dev.customclaims.aeronautics.mixin;

import dev.customclaims.aeronautics.compat.AeronauticsBoreProtectionHooks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "dev.ryanhcode.offroad.handlers.server.MultiMiningServerManager$BlockBreakingData", remap = false)
public abstract class MultiMiningBlockBreakingDataMixin {
    @Shadow
    @Final
    private List<?> suppliers;

    @Shadow
    public abstract BlockPos getBreakingPos();

    @Shadow
    public abstract void setDestroyProgress(float destroyProgress);

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void customclaims$tick(Level level, CallbackInfoReturnable cir) {
        BlockPos breakingPos = getBreakingPos();
        if (!AeronauticsBoreProtectionHooks.shouldBlockBoreMining(level, breakingPos)) {
            return;
        }

        setDestroyProgress(-1.0F);
        suppliers.forEach(AeronauticsBoreProtectionHooks::stallSupplier);

        Object stopResult = AeronauticsBoreProtectionHooks.stopTickResult();
        if (stopResult != null) {
            cir.setReturnValue(stopResult);
        }
    }
}