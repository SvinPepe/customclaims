package dev.customclaims.xaero.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class XaeroCompatConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue VISIBLE_RADIUS_CHUNKS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        VISIBLE_RADIUS_CHUNKS = builder
                .comment("War markers are sent only for wars visible to the player within this chunk radius.")
                .defineInRange("xaero_overlay.visible_radius_chunks", 8, 0, 32);

        SPEC = builder.build();
    }

    private XaeroCompatConfig() {
    }
}
