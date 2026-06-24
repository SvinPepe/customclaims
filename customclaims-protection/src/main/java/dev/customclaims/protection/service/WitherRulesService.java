package dev.customclaims.protection.service;

import dev.customclaims.protection.config.ProtectionConfig;
import net.minecraft.world.level.Level;

public final class WitherRulesService {
    public boolean isWitherBlocked(Level level) {
        String dimension = level.dimension().location().toString();
        return ("minecraft:overworld".equals(dimension) && ProtectionConfig.DISABLE_WITHER_SUMMON_IN_OVERWORLD.get())
                || ("minecraft:the_end".equals(dimension) && ProtectionConfig.DISABLE_WITHER_SUMMON_IN_END.get())
                || ("minecraft:the_nether".equals(dimension) && ProtectionConfig.DISABLE_WITHER_SUMMON_IN_NETHER.get());
    }
}
