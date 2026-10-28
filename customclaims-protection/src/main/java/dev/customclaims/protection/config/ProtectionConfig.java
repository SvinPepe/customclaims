package dev.customclaims.protection.config;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ProtectionConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<List<? extends String>> CONTESTED_ENTITY_INTERACTION_EXCEPTIONS = BUILDER
            .comment("Entity types whose right-click interactions bypass OPaC for attacking/defending sides in the target's contested chunk.",
                    "Exact namespace:path IDs only; no wildcards. Empty disables exceptions. Mods need not be installed.",
                    "Uses a full OPaC pass until the next server tick; does not override the entity mod's own access rules.")
            .defineListAllowEmpty("entity_interaction.contested_exceptions", List.of("corpse:corpse"),
                    () -> "corpse:corpse",
                    value -> {
                        if (!(value instanceof String id) || !id.contains(":")) {
                            return false;
                        }
                        Identifier parsed = Identifier.tryParse(id);
                        return parsed != null && parsed.toString().equals(id);
                    });

    public static final ModConfigSpec.IntValue FOREIGN_BLOCK_BREAK_LIMIT = BUILDER
            .comment("Temporary MVP limit for foreign peaceful claim block breaks per player runtime session.")
            .defineInRange("foreign_interaction.block_break_limit", 0, 0, 100_000);

    public static final ModConfigSpec.IntValue FOREIGN_BLOCK_PLACE_LIMIT = BUILDER
            .comment("Temporary MVP limit for foreign peaceful claim block placements per player runtime session.")
            .defineInRange("foreign_interaction.block_place_limit", 0, 0, 100_000);

    public static final ModConfigSpec.IntValue FOREIGN_INTERACTION_LIMIT_RESET_INTERVAL_SECONDS = BUILDER
            .comment("Global runtime reset interval for foreign peaceful claim block break/place counters.")
            .defineInRange("foreign_interaction.limit_reset_interval_seconds", 3600, 1, 604_800);

    public static final ModConfigSpec.IntValue CLAIMRULES_TOGGLE_COOLDOWN_SECONDS = BUILDER
            .comment("Cooldown for side-level /claimrules toggles such as explosions and Create machines.")
            .defineInRange("claimrules.toggle_cooldown_seconds", 600, 0, 86_400);

    public static final ModConfigSpec.BooleanValue CUSTOM_EXPLOSION_FILTER_ENABLED = BUILDER
            .comment("If true, CustomClaims filters block damage from explosions in claimed chunks with side explosion protection enabled.")
            .define("explosions.custom_filter_enabled", true);

    public static final ModConfigSpec.BooleanValue ALLOW_EXPLOSIONS_IN_WAR_CHUNKS = BUILDER
            .comment("If true, CustomClaims does not filter explosion block damage in contested war chunks.")
            .define("explosions.allow_in_war_chunks", true);

    public static final ModConfigSpec.BooleanValue BLOCK_BIG_CANNON_PROJECTILE_LAUNCH_FROM_PROTECTED_CLAIMS = BUILDER
            .comment("If true, Create Big Cannons projectiles spawned from protected claimed chunks are cancelled.")
            .define("big_cannons.block_projectile_launch_from_protected_claims", true);

    public static final ModConfigSpec.BooleanValue LOG_BLOCKED_BIG_CANNON_PROJECTILES = BUILDER
            .comment("If true, log Create Big Cannons projectiles blocked by CustomClaims explosion protection.")
            .define("big_cannons.log_blocked_projectiles", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ProtectionConfig() {
    }
}
