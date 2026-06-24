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
            .defineInRange("starting_progress", 1.0D, 0.0D, 100.0D);

    public static final ModConfigSpec.DoubleValue ATTACKER_PROGRESS_PER_SECOND = BUILDER
            .comment("Progress gained per non-AFK attacker standing in the contested chunk each second.")
            .defineInRange("attacker_progress_per_second", 0.35D, 0.0D, 100.0D);

    public static final ModConfigSpec.DoubleValue EMPTY_DECAY_PER_SECOND = BUILDER
            .comment("Progress decay per second when no non-AFK attackers are in the contested chunk.")
            .defineInRange("empty_decay_per_second", 0.20D, 0.0D, 100.0D);

    public static final ModConfigSpec.DoubleValue DEFENDER_DECAY_PER_SECOND = BUILDER
            .comment("Additional progress decay per non-AFK defender in the contested chunk each second.")
            .defineInRange("defender_decay_per_second", 0.25D, 0.0D, 100.0D);

    public static final ModConfigSpec.IntValue MAX_ACTIVE_WARS_PER_PARTY = BUILDER
            .comment("Maximum simultaneous active/preparing wars started by one party.")
            .defineInRange("max_active_wars_per_party", 3, 1, 128);

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

    public static final ModConfigSpec SPEC = BUILDER.build();

    private WarConfig() {
    }
}
