package dev.customclaims.bigcannons.mixin;

import dev.customclaims.bigcannons.compat.BigCannonLaunchProtectionHooks;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rbasamoyai.createbigcannons.cannon_control.contraption.AbstractMountedCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.MountedBigCannonContraption;
import rbasamoyai.createbigcannons.cannon_control.contraption.PitchOrientedContraptionEntity;

@Mixin(value = MountedBigCannonContraption.class, remap = false)
public abstract class MountedBigCannonContraptionMixin {
    @Inject(method = "fireShot", at = @At("HEAD"), cancellable = true)
    private void customclaims$blockProtectedClaimShot(
            ServerLevel level,
            PitchOrientedContraptionEntity entity,
            CallbackInfo ci
    ) {
        if (BigCannonLaunchProtectionHooks.shouldBlockLaunch(
                level,
                (AbstractMountedCannonContraption) (Object) this,
                entity
        )) {
            ci.cancel();
        }
    }

    @Inject(method = "actuallyFireDropMortar", at = @At("HEAD"), cancellable = true)
    private void customclaims$blockProtectedDropMortar(CallbackInfo ci) {
        AbstractMountedCannonContraption cannon = (AbstractMountedCannonContraption) (Object) this;
        if (!(cannon.entity instanceof PitchOrientedContraptionEntity entity)) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if (BigCannonLaunchProtectionHooks.shouldBlockLaunch(level, cannon, entity)) {
            ci.cancel();
        }
    }
}
