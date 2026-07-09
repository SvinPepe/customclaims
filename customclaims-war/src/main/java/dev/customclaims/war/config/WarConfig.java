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

    public static final ModConfigSpec.DoubleValue PLAYER_WEIGHT_PER_SECOND = BUILDER
            .comment("Progress weight per non-AFK participant standing in the contested chunk each second.")
            .defineInRange("war.capture.player_weight_per_second", 0.35D, 0.0D, 100.0D);

    public static final ModConfigSpec.DoubleValue ATTACKER_PRESENCE_BONUS_PER_SECOND = BUILDER
            .comment("Flat bonus progress per second when at least one non-AFK attacker is in the contested chunk.")
            .defineInRange("war.capture.attacker_presence_bonus_per_second", 0.25D, 0.0D, 100.0D);

    public static final ModConfigSpec.DoubleValue EMPTY_CHUNK_DECAY_PER_SECOND = BUILDER
            .comment("Progress decay per second when no non-AFK attackers or defenders are in the contested chunk.")
            .defineInRange("war.capture.empty_chunk_decay_per_second", 0.50D, 0.0D, 100.0D);

    public static final ModConfigSpec.IntValue MAX_ACTIVE_WARS_PER_PARTY = BUILDER
            .comment("Maximum simultaneous active/preparing wars involving one side.")
            .defineInRange("max_active_wars_per_party", 1, 1, 128);

    public static final ModConfigSpec.IntValue MAX_STARTED_CHUNKS_PER_ATTACKER_SIDE_PER_DAY = BUILDER
            .comment("Maximum successful war target chunk starts per attacking side per configured day. Set to 0 to disable.")
            .defineInRange("war.daily_start_limit.max_started_chunks_per_attacker_side", 5, 0, 1024);

    public static final ModConfigSpec.IntValue MAX_ACCEPTED_CHUNKS_PER_DEFENDER_SIDE_PER_DAY = BUILDER
            .comment("Maximum successful incoming war target chunk starts per defending side per configured day. Set to 0 to disable.")
            .defineInRange("war.daily_start_limit.max_accepted_chunks_per_defender_side", 10, 0, 1024);

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

    public static final ModConfigSpec.IntValue POST_WAR_PROTECTION_SECONDS = BUILDER
            .comment("Seconds to mark a won/lost target chunk as post-war protected.")
            .defineInRange("post_war_protection_seconds", 1800, 0, 604_800);

    public static final ModConfigSpec.IntValue WAR_UI_BOSSBAR_VISIBLE_RADIUS_CHUNKS = BUILDER
            .comment("Chunk radius around a war target where players see the war bossbar.")
            .defineInRange("war_ui.bossbar_visible_radius_chunks", 3, 0, 64);

    public static final ModConfigSpec.ConfigValue<String> CONTESTED_OWNER_UUID = BUILDER
            .comment("Fake player UUID that temporarily owns OPC claims while a war chunk is contested.")
            .define("war.contested_owner_uuid", "00000000-0000-0000-0000-00000000cc01");

    public static final ModConfigSpec.ConfigValue<String> CONTESTED_OWNER_NAME = BUILDER
            .comment("Display name used in logs/messages for the fake contested claim owner.")
            .define("war.contested_owner_name", "Contested War");

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

    private WarConfig() {
    }
}
