package dev.customclaims.create.mixin;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import dev.customclaims.create.compat.CreateContraptionProtectionHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockBreakingMovementBehaviour.class, remap = false)
public abstract class BlockBreakingMovementBehaviourMixin {
    @Inject(method = "visitNewPosition", at = @At("HEAD"), cancellable = true)
    private void customclaims$visitNewPosition(MovementContext context, BlockPos pos, CallbackInfo ci) {
        if (CreateContraptionProtectionHooks.shouldBlockBreaker(context, pos)) {
            context.stall = false;
            ci.cancel();
        }
    }

    @Inject(method = "canBreak", at = @At("HEAD"), cancellable = true)
    private void customclaims$canBreak(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (CreateContraptionProtectionHooks.shouldBlockBreaker(level, pos)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tickBreaker", at = @At("HEAD"), cancellable = true)
    private void customclaims$tickBreaker(MovementContext context, CallbackInfo ci) {
        if (!context.data.contains("BreakingPos")) {
            return;
        }

        BlockPos breakingPos = CreateContraptionProtectionHooks.readBlockPos(context.data, "BreakingPos");
        if (breakingPos == null) {
            return;
        }
        if (!CreateContraptionProtectionHooks.shouldBlockBreaker(context, breakingPos)) {
            return;
        }

        CreateContraptionProtectionHooks.clearBreakerState(context.world, context.data);
        context.stall = false;
        ci.cancel();
    }
}
