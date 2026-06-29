package dev.customclaims.protection.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ProtectionConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

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
            .comment("Cooldown for party-level /claimrules toggles such as explosions and Create machines.")
            .defineInRange("claimrules.toggle_cooldown_seconds", 600, 0, 86_400);

    public static final ModConfigSpec.BooleanValue CUSTOM_EXPLOSION_FILTER_ENABLED = BUILDER
            .comment("If true, CustomClaims filters block damage from explosions in claimed chunks with party explosion protection enabled.")
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

    public static final ModConfigSpec.BooleanValue ALLOW_OPEN_STORAGE_ON_FOREIGN_CLAIMS = BUILDER
            .comment("If true, players can open storage on foreign peaceful claims.")
            .define("storage_rules.allow_open_storage_on_foreign_claims", true);

    public static final ModConfigSpec.BooleanValue PROTECT_STORAGE_FROM_BREAKING_ON_PEACEFUL_CLAIMS = BUILDER
            .comment("If true, protected storage blocks cannot be broken on foreign peaceful claims.")
            .define("storage_rules.protect_storage_from_breaking_on_peaceful_claims", true);

    public static final ModConfigSpec.BooleanValue ALLOW_STORAGE_BREAKING_IN_WAR_CHUNKS = BUILDER
            .comment("If true, protected storage blocks can be broken in contested chunks.")
            .define("storage_rules.allow_storage_breaking_in_war_chunks", true);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> PROTECTED_STORAGE_BLOCKS = BUILDER
            .comment("Storage block ids protected from breaking in peaceful foreign claims.")
            .defineListAllowEmpty("storage_rules.protected_storage_blocks",
                    List.of("minecraft:chest", "minecraft:trapped_chest", "minecraft:barrel", "minecraft:shulker_box"),
                    () -> "minecraft:chest",
                    value -> value instanceof String);

    public static final ModConfigSpec.BooleanValue DISABLE_WITHER_SUMMON_IN_OVERWORLD = BUILDER
            .define("wither_rules.disable_wither_summon_in_overworld", true);

    public static final ModConfigSpec.BooleanValue DISABLE_WITHER_SUMMON_IN_END = BUILDER
            .define("wither_rules.disable_wither_summon_in_end", true);

    public static final ModConfigSpec.BooleanValue DISABLE_WITHER_SUMMON_IN_NETHER = BUILDER
            .define("wither_rules.disable_wither_summon_in_nether", false);

    public static final ModConfigSpec.BooleanValue LOG_BLOCKED_WITHER_SUMMONS = BUILDER
            .define("wither_rules.log_blocked_wither_summons", true);

    public static final ModConfigSpec.BooleanValue ENABLE_VILLAGER_PROTECTION = BUILDER
            .define("villager_protection.enable_villager_protection", true);

    public static final ModConfigSpec.BooleanValue PROTECT_VILLAGERS_EVERYWHERE = BUILDER
            .define("villager_protection.protect_villagers_everywhere", true);

    public static final ModConfigSpec.BooleanValue PROTECT_VILLAGERS_ONLY_ON_CLAIMS = BUILDER
            .define("villager_protection.protect_villagers_only_on_claims", false);

    public static final ModConfigSpec.BooleanValue PROTECT_WANDERING_TRADERS = BUILDER
            .define("villager_protection.protect_wandering_traders", true);

    public static final ModConfigSpec.BooleanValue PREVENT_PLAYER_DAMAGE = BUILDER
            .define("villager_protection.prevent_player_damage", true);

    public static final ModConfigSpec.BooleanValue PREVENT_FIRE_DAMAGE = BUILDER
            .define("villager_protection.prevent_fire_damage", true);

    public static final ModConfigSpec.BooleanValue PREVENT_LAVA_DAMAGE = BUILDER
            .define("villager_protection.prevent_lava_damage", true);

    public static final ModConfigSpec.BooleanValue PREVENT_FALL_DAMAGE = BUILDER
            .define("villager_protection.prevent_fall_damage", true);

    public static final ModConfigSpec.BooleanValue PREVENT_DROWNING_DAMAGE = BUILDER
            .define("villager_protection.prevent_drowning_damage", true);

    public static final ModConfigSpec.BooleanValue PREVENT_SUFFOCATION_DAMAGE = BUILDER
            .define("villager_protection.prevent_suffocation_damage", true);

    public static final ModConfigSpec.BooleanValue PREVENT_EXPLOSION_DAMAGE = BUILDER
            .define("villager_protection.prevent_explosion_damage", true);

    public static final ModConfigSpec.BooleanValue PREVENT_PROJECTILE_DAMAGE = BUILDER
            .define("villager_protection.prevent_projectile_damage", true);

    public static final ModConfigSpec.BooleanValue PREVENT_VOID_DAMAGE = BUILDER
            .define("villager_protection.prevent_void_damage", false);

    public static final ModConfigSpec.BooleanValue ALLOW_ZOMBIE_DAMAGE = BUILDER
            .define("villager_protection.allow_zombie_damage", true);

    public static final ModConfigSpec.BooleanValue PROTECT_VILLAGERS_IN_WAR_CHUNKS = BUILDER
            .comment("If false, villager/trader protection is disabled in contested war chunks.")
            .define("villager_protection.protect_villagers_in_war_chunks", false);

    public static final ModConfigSpec.BooleanValue ALLOW_PLAYER_DAMAGE_TO_VILLAGERS_IN_WAR_CHUNKS = BUILDER
            .comment("If true, player damage to villagers/traders is allowed in contested war chunks.")
            .define("villager_protection.allow_player_damage_to_villagers_in_war_chunks", true);

    public static final ModConfigSpec.BooleanValue ALLOW_EXPLOSION_DAMAGE_TO_VILLAGERS_IN_WAR_CHUNKS = BUILDER
            .comment("If true, explosion damage to villagers/traders is allowed in contested war chunks.")
            .define("villager_protection.allow_explosion_damage_to_villagers_in_war_chunks", true);

    public static final ModConfigSpec.BooleanValue CLEAR_FIRE_ON_BLOCKED_DAMAGE = BUILDER
            .define("villager_protection.clear_fire_on_blocked_damage", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ProtectionConfig() {
    }
}
