package dev.customclaims.core.config;

import java.util.List;
//? if <1.20.5 {
/*import net.minecraftforge.common.ForgeConfigSpec;
*///?} else {
import net.neoforged.neoforge.common.ModConfigSpec;
//?}

public final class CoreConfig {
    //? if <1.20.5 {
    /*private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Enables additional server-side Custom Claims debug logging.")
            .define("debug_logging", false);

    public static final ForgeConfigSpec.IntValue OP_PERMISSION_LEVEL = BUILDER
            .comment("Operator level accepted by the permission service.")
            .defineInRange("op_permission_level", 2, 0, 4);

    public static final ForgeConfigSpec.BooleanValue METRICS_ENABLED = BUILDER
            .comment("Enables anonymous dedicated-server heartbeat metrics.")
            .define("metrics.enabled", true);

    public static final ForgeConfigSpec.ConfigValue<String> METRICS_ENDPOINT = BUILDER
            .comment("HTTP(S) endpoint that receives anonymous metrics heartbeats. Empty means no heartbeat is sent.")
            .define("metrics.endpoint",
                    "https://customclaims-metrics.svinpepe.workers.dev/heartbeat");

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DEFAULT_PLAYER_PERMISSIONS = BUILDER
            .comment("Permission nodes granted to every player without requiring OP or tags.")
            .defineList("permissions.default_player_permissions",
                    List.of(
                            "opac.warfare.start",
                            "opac.warfare.cancel",
                            "opac.warfare.status",
                            "opac.protection.claimrules",
                            "opac.protection.explosions.status",
                            "opac.protection.explosions.toggle",
                            "opac.protection.create.status",
                            "opac.protection.create.toggle",
                            "opac.protection.assembly.status",
                            "opac.protection.assembly.toggle"
                    ),
                    entry -> entry instanceof String);

    public static final ForgeConfigSpec SPEC = BUILDER.build();
    *///?} else {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Enables additional server-side Custom Claims debug logging.")
            .define("debug_logging", false);

    public static final ModConfigSpec.IntValue OP_PERMISSION_LEVEL = BUILDER
            .comment("Operator level accepted by the permission service.")
            .defineInRange("op_permission_level", 2, 0, 4);

    public static final ModConfigSpec.BooleanValue METRICS_ENABLED = BUILDER
            .comment("Enables anonymous dedicated-server heartbeat metrics.")
            .define("metrics.enabled", true);

    public static final ModConfigSpec.ConfigValue<String> METRICS_ENDPOINT = BUILDER
            .comment("HTTP(S) endpoint that receives anonymous metrics heartbeats. Empty means no heartbeat is sent.")
            .define("metrics.endpoint",
                    "https://customclaims-metrics.svinpepe.workers.dev/heartbeat");

    public static final ModConfigSpec.ConfigValue<List<? extends String>> DEFAULT_PLAYER_PERMISSIONS = BUILDER
            .comment("Permission nodes granted to every player without requiring OP or tags.")
            .defineListAllowEmpty("permissions.default_player_permissions",
                    List.of(
                            "opac.warfare.start",
                            "opac.warfare.cancel",
                            "opac.warfare.status",
                            "opac.protection.claimrules",
                            "opac.protection.explosions.status",
                            "opac.protection.explosions.toggle",
                            "opac.protection.create.status",
                            "opac.protection.create.toggle",
                            "opac.protection.assembly.status",
                            "opac.protection.assembly.toggle"
                    ),
                    entry -> entry instanceof String);

    public static final ModConfigSpec SPEC = BUILDER.build();
    //?}

    private CoreConfig() {
    }
}
