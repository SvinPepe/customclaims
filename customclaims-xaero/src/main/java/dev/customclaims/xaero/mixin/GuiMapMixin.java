package dev.customclaims.xaero.mixin;

import dev.customclaims.xaero.client.XaeroMapWarStartOption;
import java.util.ArrayList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

@Mixin(targets = "xaero.map.gui.GuiMap", remap = false)
public abstract class GuiMapMixin {
    @Shadow
    private int rightClickX;

    @Shadow
    private int rightClickZ;

    @Shadow
    private ResourceKey<Level> rightClickDim;

    @Inject(method = "getRightClickOptions", at = @At("RETURN"))
    private void customclaims$addStartWarOption(CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
        ArrayList<RightClickOption> options = cir.getReturnValue();
        if (options == null || rightClickDim == null) {
            return;
        }
        if (options.stream().anyMatch(XaeroMapWarStartOption.class::isInstance)) {
            return;
        }

        options.add(new XaeroMapWarStartOption(
                options.size(),
                (IRightClickableElement) (Object) this,
                rightClickDim,
                rightClickX,
                rightClickZ
        ));
    }
}
