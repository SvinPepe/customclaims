package dev.customclaims.xaero.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class XaeroCompatConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue VISIBLE_RADIUS_CHUNKS;
    public static final ModConfigSpec.BooleanValue CUSTOM_OVERLAY_ENABLED;
    public static final ModConfigSpec.BooleanValue XAERO_WAYPOINTS_ENABLED;
    public static final ModConfigSpec.IntValue XAERO_WAYPOINT_REFRESH_INTERVAL_SECONDS;
    public static final ModConfigSpec.BooleanValue MAP_WAR_START_ENABLED;
    public static final ModConfigSpec.IntValue MAP_WAR_START_MAX_DISTANCE_CHUNKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        VISIBLE_RADIUS_CHUNKS = builder
                .comment("War markers are sent only for wars visible to the player within this chunk radius.")
                .defineInRange("xaero_overlay.visible_radius_chunks", 8, 0, 32);

        CUSTOM_OVERLAY_ENABLED = builder
                .comment("If true, render the old CustomClaims client HUD overlay. Disabled by default while Xaero waypoints are used.")
                .define("xaero_overlay.custom_overlay_enabled", false);

        XAERO_WAYPOINTS_ENABLED = builder
                .comment("If true, visible war markers create Xaero temporary waypoints on compatible clients.")
                .define("xaero_waypoints.enabled", true);

        XAERO_WAYPOINT_REFRESH_INTERVAL_SECONDS = builder
                .comment("Minimum seconds between refreshing the same Xaero temporary war waypoint.")
                .defineInRange("xaero_waypoints.refresh_interval_seconds", 5, 1, 300);

        MAP_WAR_START_ENABLED = builder
                .comment("If true, players with war start permission can start wars from the Xaero World Map right-click menu.")
                .define("xaero_map_war_start.enabled", true);

        MAP_WAR_START_MAX_DISTANCE_CHUNKS = builder
                .comment("Maximum Chebyshev chunk distance for starting a war from the Xaero World Map. Zero limits it to the player's current chunk.")
                .defineInRange("xaero_map_war_start.max_distance_chunks", 32, 0, 32);

        SPEC = builder.build();
    }

    private XaeroCompatConfig() {
    }
}
