package dev.customclaims.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CoreConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Enables additional server-side Custom Claims debug logging.")
            .define("debug_logging", false);

    public static final ModConfigSpec.BooleanValue SCOREBOARD_TEAM_PARTY_FALLBACK = BUILDER
            .comment("Temporary fallback: treat a player's scoreboard team as their party until Open Parties and Claims API hooks are wired.")
            .define("scoreboard_team_party_fallback", true);

    public static final ModConfigSpec.BooleanValue ALLOW_FALLBACK_CLAIM_TRANSFER = BUILDER
            .comment("Temporary fallback: store military claim ownership locally when the Open Parties and Claims transfer API is unavailable.")
            .define("allow_fallback_claim_transfer", true);

    public static final ModConfigSpec.IntValue OP_PERMISSION_LEVEL = BUILDER
            .comment("Operator level accepted by the fallback permission service.")
            .defineInRange("op_permission_level", 2, 0, 4);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CoreConfig() {
    }
}
