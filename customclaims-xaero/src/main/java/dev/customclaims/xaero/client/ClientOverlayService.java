package dev.customclaims.xaero.client;

import dev.customclaims.war.model.WarMarkerDto;
import dev.customclaims.xaero.config.XaeroCompatConfig;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class ClientOverlayService {
    private static final int MARKER_TTL_TICKS = 60;
    private static final int MAX_RENDERED_MARKERS = 5;
    private static final Map<String, MarkerView> MARKERS = new LinkedHashMap<>();

    private ClientOverlayService() {
    }

    public static void replaceMarkers(List<WarMarkerDto> markers) {
        if (!XaeroCompatConfig.CUSTOM_OVERLAY_ENABLED.get()) {
            MARKERS.clear();
            return;
        }
        MARKERS.clear();
        for (WarMarkerDto marker : markers) {
            MARKERS.put(key(marker), new MarkerView(marker, MARKER_TTL_TICKS));
        }
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (!XaeroCompatConfig.CUSTOM_OVERLAY_ENABLED.get()) {
            MARKERS.clear();
            return;
        }
        MARKERS.entrySet().removeIf(entry -> entry.getValue().tickAndExpired());
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (!XaeroCompatConfig.CUSTOM_OVERLAY_ENABLED.get()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || MARKERS.isEmpty()) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = minecraft.font;
        int width = graphics.guiWidth();
        renderNearestMarkerBanner(graphics, font, player, width);

        int x = Math.max(8, width - 228);
        int y = 24;

        graphics.fill(x - 4, y - 4, width - 8, y + 16 + Math.min(MARKERS.size(), MAX_RENDERED_MARKERS) * 21, 0x77000000);
        graphics.drawString(font, "CustomClaims Wars", x, y, 0xFFE7E7E7, true);
        y += 13;

        List<WarMarkerDto> sorted = MARKERS.values().stream()
                .map(MarkerView::marker)
                .sorted(Comparator.comparingInt(marker -> distanceChunks(player, marker)))
                .limit(MAX_RENDERED_MARKERS)
                .toList();

        for (WarMarkerDto marker : sorted) {
            int color = stateColor(marker);
            int pulse = pulseColor(marker);
            String firstLine = trim(marker.state() + " " + distanceText(player, marker) + " " + marker.label(), 35);
            String secondLine = trim(Math.round(marker.progress()) + "% "
                    + signedDelta(marker.deltaPerSecond())
                    + "/s ATK " + marker.attackerCount()
                    + " DEF " + marker.defenderCount()
                    + " " + marker.viewerRelation(), 35);
            graphics.fill(x - 3, y, x - 1, y + 18, pulse);
            graphics.drawString(font, firstLine, x, y, color, true);
            graphics.drawString(font, secondLine, x, y + 10, 0xFFD6D6D6, true);
            y += 21;
        }
    }

    private static void renderNearestMarkerBanner(GuiGraphics graphics, Font font, LocalPlayer player, int width) {
        WarMarkerDto nearest = MARKERS.values().stream()
                .map(MarkerView::marker)
                .filter(marker -> distanceChunks(player, marker) != Integer.MAX_VALUE)
                .min(Comparator.comparingInt(marker -> distanceChunks(player, marker)))
                .orElse(null);
        if (nearest == null) {
            return;
        }

        int color = stateColor(nearest);
        int pulse = pulseColor(nearest);
        String text = trim("WAR TARGET " + distanceText(player, nearest) + " " + nearest.label(), 48);
        int textWidth = font.width(text);
        int x = Math.max(8, (width - textWidth) / 2);
        int y = 8;
        graphics.fill(x - 8, y - 4, x + textWidth + 8, y + 12, 0xAA000000);
        graphics.fill(x - 8, y - 4, x + textWidth + 8, y - 2, pulse);
        graphics.drawString(font, text, x, y, color, true);
    }

    private static String key(WarMarkerDto marker) {
        return marker.dimension() + ":" + marker.chunkX() + ":" + marker.chunkZ();
    }

    private static String distanceText(LocalPlayer player, WarMarkerDto marker) {
        int distance = distanceChunks(player, marker);
        if (distance == Integer.MAX_VALUE) {
            return "other-dim";
        }
        return distance + "ch";
    }

    private static int distanceChunks(LocalPlayer player, WarMarkerDto marker) {
        if (!player.level().dimension().location().toString().equals(marker.dimension())) {
            return Integer.MAX_VALUE;
        }
        ChunkPos playerChunk = player.chunkPosition();
        return Math.max(Math.abs(playerChunk.x - marker.chunkX()), Math.abs(playerChunk.z - marker.chunkZ()));
    }

    private static int stateColor(WarMarkerDto marker) {
        boolean blink = (System.currentTimeMillis() / 500L) % 2L == 0L;
        if ("Active".equals(marker.state())) {
            return switch (marker.viewerRelation()) {
                case "attacker" -> blink ? 0xFFFF5C5C : 0xFFFFB36B;
                case "defender" -> blink ? 0xFF69D2FF : 0xFFB8F3FF;
                default -> blink ? 0xFFFF6B6B : 0xFFFFD166;
            };
        }
        return switch (marker.state()) {
            case "Preparing" -> blink ? 0xFFFFD166 : 0xFFFFFFFF;
            default -> 0xFFBDBDBD;
        };
    }

    private static int pulseColor(WarMarkerDto marker) {
        boolean blink = (System.currentTimeMillis() / 300L) % 2L == 0L;
        if (!blink) {
            return 0x00000000;
        }
        return switch (marker.viewerRelation()) {
            case "attacker" -> 0xFFFF3333;
            case "defender" -> 0xFF33AAFF;
            case "admin" -> 0xFFFFD166;
            default -> 0xFFFF7A59;
        };
    }

    private static String signedDelta(double value) {
        if (value > 0.0D) {
            return "+" + String.format(java.util.Locale.ROOT, "%.2f", value);
        }
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private static String trim(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static final class MarkerView {
        private final WarMarkerDto marker;
        private int ttlTicks;

        private MarkerView(WarMarkerDto marker, int ttlTicks) {
            this.marker = marker;
            this.ttlTicks = ttlTicks;
        }

        private WarMarkerDto marker() {
            return marker;
        }

        private boolean tickAndExpired() {
            ttlTicks--;
            return ttlTicks <= 0;
        }
    }
}
