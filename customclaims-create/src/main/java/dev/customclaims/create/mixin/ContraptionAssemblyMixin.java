package dev.customclaims.create.mixin;

import com.simibubi.create.content.contraptions.Contraption;
import dev.customclaims.create.compat.CreateContraptionProtectionHooks;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Contraption.class, remap = false)
public abstract class ContraptionAssemblyMixin {
    @Shadow
    public BlockPos anchor;

    @Shadow
    public abstract Map<BlockPos, StructureTemplate.StructureBlockInfo> getBlocks();

    @Inject(method = "searchMovedStructure", at = @At("RETURN"), cancellable = true)
    private void customclaims$validateAssemblyBounds(
            Level level,
            BlockPos pos,
            net.minecraft.core.Direction forcedDirection,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!Boolean.TRUE.equals(cir.getReturnValue()) || anchor == null) {
            return;
        }

        List<BlockPos> sourcePositions = new ArrayList<>();
        for (BlockPos localPos : getBlocks().keySet()) {
            sourcePositions.add(anchor.offset(localPos));
        }
        if (CreateContraptionProtectionHooks.shouldBlockAssembly(level, sourcePositions)) {
            cir.setReturnValue(false);
        }
    }
}
