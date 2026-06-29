package dev.customclaims.core.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class CoreConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Enables additional server-side Custom Claims debug logging.")
            .define("debug_logging", false);

    public static final ModConfigSpec.IntValue OP_PERMISSION_LEVEL = BUILDER
            .comment("Operator level accepted by the permission service.")
            .defineInRange("op_permission_level", 2, 0, 4);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> DEFAULT_PLAYER_PERMISSIONS = BUILDER
            .comment("Permission nodes granted to every player without requiring OP or tags.")
            .defineListAllowEmpty("permissions.default_player_permissions",
                    List.of(
                            "customclaims.war.start",
                            "customclaims.war.status",
                            "customclaims.explosions.status",
                            "customclaims.explosions.toggle",
                            "customclaims.create.status",
                            "customclaims.create.toggle"
                    ),
                    () -> "customclaims.war.start",
                    value -> value instanceof String);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CoreConfig() {
    }
}
