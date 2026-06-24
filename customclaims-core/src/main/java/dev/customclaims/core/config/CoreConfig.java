package dev.customclaims.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CoreConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Enables additional server-side Custom Claims debug logging.")
            .define("debug_logging", false);

    public static final ModConfigSpec.IntValue OP_PERMISSION_LEVEL = BUILDER
            .comment("Operator level accepted by the permission service.")
            .defineInRange("op_permission_level", 2, 0, 4);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CoreConfig() {
    }
}
