package dev.customclaims.war.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class WarConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue PREPARATION_SECONDS = BUILDER
            .comment("Seconds between /war start and the chunk becoming contested.")
            .defineInRange("preparation_seconds", 300, 5, 86_400);

    public static final ModConfigSpec.IntValue MAX_DURATION_SECONDS = BUILDER
            .comment("Maximum active war duration in seconds.")
            .defineInRange("max_duration_seconds", 3600, 60, 604_800);

    public static final ModConfigSpec.DoubleValue STARTING_PROGRESS = BUILDER
            .comment("Capture progress assigned when the active phase starts.")
            .defineInRange("war.capture.starting_progress", 50.0D, 0.0D, 100.0D);

    public static final ModConfigSpec.IntValue MAX_SECTOR_CHUNKS = BUILDER
            .comment("Maximum number of connected chunks included in a sector conquest wedge.")
            .defineInRange("war.sector.max_chunks", 4, 1, 6);

    public static final ModConfigSpec.IntValue MAX_CONQUEST_DEPTH_CHUNKS = BUILDER
            .comment("Maximum depth of chunks from the border eligible for conquest in a single siege.")
            .defineInRange("war.sector.max_conquest_depth_chunks", 2, 1, 4);

    public static final ModConfigSpec.DoubleValue BASE_CAPTURE_RATE_PER_SECOND = BUILDER
            .comment("Base capture progress per second with 1 attacker present in the sector.")
            .defineInRange("war.capture.base_rate_per_second", 0.40D, 0.0D, 100.0D);

    public static final ModConfigSpec.DoubleValue EXTRA_ATTACKER_BONUS_PER_SECOND = BUILDER
            .comment("Additional capture progress per second for each extra attacker beyond the first.")
            .defineInRange("war.capture.extra_attacker_bonus_per_second", 0.05D, 0.0D, 100.0D);

    public static final ModConfigSpec.DoubleValue MAX_CAPTURE_RATE_PER_SECOND = BUILDER
            .comment("Hard ceiling cap on attacker capture progress gain per second (anti-zerg cap).")
            .defineInRange("war.capture.max_capture_rate_per_second", 0.60D, 0.0D, 100.0D);

    public static final ModConfigSpec.DoubleValue DEFENDER_WEIGHT_PER_SECOND = BUILDER
            .comment("Progress reduction per second per defending player inside the sector.")
            .defineInRange("war.capture.defender_weight_per_second", 0.35D, 0.0D, 100.0D);

    public static final ModConfigSpec.BooleanValue GLOWING_EFFECT_ENABLED = BUILDER
            .comment("If true, all players in an actively contested sector receive the Glowing effect to prevent hiding.")
            .define("war.capture.glowing_effect_enabled", true);

    public static final ModConfigSpec.DoubleValue PLAYER_WEIGHT_PER_SECOND = BUILDER
            .comment("Legacy progress weight per non-AFK participant.")
            .defineInRange("war.capture.player_weight_per_second", 0.35D, 0.0D, 100.0D);

    public static final ModConfigSpec.DoubleValue ATTACKER_PRESENCE_BONUS_PER_SECOND = BUILDER
            .comment("Legacy flat bonus progress per second.")
            .defineInRange("war.capture.attacker_presence_bonus_per_second", 0.25D, 0.0D, 100.0D);

    public static final ModConfigSpec.DoubleValue EMPTY_CHUNK_DECAY_PER_SECOND = BUILDER
            .comment("Progress decay per second when no non-AFK attackers or defenders are in the contested chunk.")
            .defineInRange("war.capture.empty_chunk_decay_per_second", 0.50D, 0.0D, 100.0D);

    public static final ModConfigSpec.ConfigValue<String> DISCORD_WEBHOOK_URL = BUILDER
            .comment("Discord webhook URL for siege start, active, and finish embed notifications. Empty disables.")
            .define("war.discord.webhook_url", "");

    public static final ModConfigSpec.ConfigValue<String> DISCORD_WEBHOOK_LANGUAGE = BUILDER
            .comment("Language for Discord webhook embed notifications: 'ru_ru' or 'en_us'.")
            .define("war.discord.webhook_language", "ru_ru");

    public static final ModConfigSpec.IntValue ATTACKER_COOLDOWN_SECONDS = BUILDER
            .comment("Fixed cooldown window for war starts by one attacking side. Set to 0 to disable the time window.")
            .defineInRange("war.cooldown.attacker_seconds", 7_200, 0, 604_800);

    public static final ModConfigSpec.IntValue DEFENDER_COOLDOWN_SECONDS = BUILDER
            .comment("Fixed protection cooldown window after a side accepts its first incoming war. Set to 0 to disable the time window.")
            .defineInRange("war.cooldown.defender_seconds", 3_600, 0, 604_800);

    public static final ModConfigSpec.IntValue MAX_STARTED_CHUNKS_PER_ATTACKER_SIDE_PER_COOLDOWN = BUILDER
            .comment("Maximum concurrent and successful war target starts for one attacking side during its cooldown window.")
            .defineInRange("war.cooldown.max_started_chunks_per_attacker_side", 1, 1, 1024);

    public static final ModConfigSpec.IntValue MAX_ACCEPTED_CHUNKS_PER_DEFENDER_SIDE_PER_COOLDOWN = BUILDER
            .comment("Maximum concurrent and successful incoming war target starts for one defending side during its protection cooldown window.")
            .defineInRange("war.cooldown.max_accepted_chunks_per_defender_side", 1, 1, 1024);

    public static final ModConfigSpec.IntValue MAX_STARTED_CHUNKS_PER_ATTACKER_SIDE_PER_DAY = BUILDER
            .comment("Maximum successful war target chunk starts per attacking side per configured day. Set to 0 to disable.")
            .defineInRange("war.daily_start_limit.max_started_chunks_per_attacker_side", 0, 0, 1024);

    public static final ModConfigSpec.IntValue MAX_ACCEPTED_CHUNKS_PER_DEFENDER_SIDE_PER_DAY = BUILDER
            .comment("Maximum successful incoming war target chunk starts per defending side per configured day. Set to 0 to disable.")
            .defineInRange("war.daily_start_limit.max_accepted_chunks_per_defender_side", 0, 0, 1024);

    public static final ModConfigSpec.BooleanValue ALLOW_DIAGONAL_BORDER_CHUNKS = BUILDER
            .comment("Whether diagonal adjacency counts when checking if a target chunk is a border chunk.")
            .define("allow_diagonal_border_chunks", false);

    public static final ModConfigSpec.IntValue AFK_SECONDS = BUILDER
            .comment("Players with no tracked interaction for this many seconds are treated as AFK.")
            .defineInRange("afk_seconds", 300, 30, 86_400);

    public static final ModConfigSpec.BooleanValue ENABLE_RAID_WINDOW = BUILDER
            .comment("If enabled, /war start is blocked inside configured raid windows.")
            .define("raid_window.enable_raid_window", true);

    public static final ModConfigSpec.ConfigValue<String> RAID_WINDOW_TIMEZONE = BUILDER
            .comment("Timezone for raid windows.")
            .define("raid_window.timezone", "Europe/Moscow");

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_WINDOWS = BUILDER
            .comment("Blocked raid windows in HH:mm-HH:mm form.")
            .defineListAllowEmpty("raid_window.blocked_windows", List.of("04:00-08:00"), () -> "04:00-08:00", value -> value instanceof String);

    public static final ModConfigSpec.BooleanValue ALLOW_ONGOING_WARS_TO_CONTINUE_AFTER_WINDOW_START = BUILDER
            .comment("If false, active wars are failed when a blocked raid window starts.")
            .define("raid_window.allow_ongoing_wars_to_continue_after_window_start", true);

    public static final ModConfigSpec.IntValue WAR_UI_BOSSBAR_VISIBLE_RADIUS_CHUNKS = BUILDER
            .comment("Chunk radius around a war target where players see the war bossbar.")
            .defineInRange("war_ui.bossbar_visible_radius_chunks", 3, 0, 64);

    public static final ModConfigSpec.BooleanValue BROADCAST_NOTIFICATIONS_TO_ALL_PLAYERS = BUILDER
            .comment("If true, war chat notifications are sent to every online player instead of only the involved sides.")
            .define("war.notifications.broadcast_to_all_players", true);

    public static final ModConfigSpec.BooleanValue BOSSBAR_VISIBLE_TO_ALL_PLAYERS = BUILDER
            .comment("If true, every online player sees every active or preparing war bossbar regardless of distance or dimension.")
            .define("war_ui.bossbar_visible_to_all_players", true);

    public static final ModConfigSpec.IntValue STARTING_LIVES = BUILDER
            .comment("Personal war lives assigned to each current attacker/defender side member when a war becomes active.")
            .defineInRange("war.lives.starting_lives", 3, 1, 100);

    public static final ModConfigSpec.BooleanValue SCOREBOARD_SIDEBAR_ENABLED = BUILDER
            .comment("If true, active war lives are shown in the vanilla sidebar scoreboard.")
            .define("war.lives.scoreboard_sidebar_enabled", true);

    public static final ModConfigSpec.ConfigValue<String> SCOREBOARD_OBJECTIVE = BUILDER
            .comment("Objective name used for the war lives sidebar scoreboard.")
            .define("war.lives.scoreboard_objective", "cc_war_lives");

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static <T> T getSafe(ModConfigSpec.ConfigValue<T> value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    private WarConfig() {
    }
}
