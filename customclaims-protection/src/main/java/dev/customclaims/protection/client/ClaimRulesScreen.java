package dev.customclaims.protection.client;

import dev.customclaims.protection.network.ClaimRulesStateDto;
import dev.customclaims.protection.network.ServerboundSetClaimRulePayload;
import dev.customclaims.protection.service.ClaimRulesService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClaimRulesScreen extends Screen {
    private ClaimRulesStateDto state;
    private String message;
    private long explosionCooldownSeconds;
    private long createCooldownSeconds;
    private int cooldownTickCounter;

    private ClaimRulesScreen(ClaimRulesStateDto state, String message) {
        super(Component.literal("CustomClaims Rules"));
        this.state = state;
        this.message = message == null ? "" : message;
        resetCooldowns(state);
    }

    public static void applyState(ClaimRulesStateDto state, String message, boolean openScreen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof ClaimRulesScreen screen) {
            screen.state = state;
            screen.message = message == null ? "" : message;
            screen.resetCooldowns(state);
            screen.rebuildWidgets();
            return;
        }
        if (openScreen) {
            minecraft.setScreen(new ClaimRulesScreen(state, message));
        }
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
        int center = width / 2;
        int y = Math.max(84, height / 2 - 36);

        addRenderableWidget(Button.builder(explosionButtonLabel(), button ->
                        PacketDistributor.sendToServer(new ServerboundSetClaimRulePayload(
                                ClaimRulesService.RULE_EXPLOSIONS,
                                !state.explosionProtectionEnabled()
                        )))
                .bounds(center - 105, y, 210, 20)
                .build()).active = state.hasSide()
                && state.canToggleExplosions()
                && explosionCooldownSeconds <= 0L;

        addRenderableWidget(Button.builder(createButtonLabel(), button ->
                        PacketDistributor.sendToServer(new ServerboundSetClaimRulePayload(
                                ClaimRulesService.RULE_CREATE,
                                !state.createMachinesEnabled()
                        )))
                .bounds(center - 105, y + 24, 210, 20)
                .build()).active = state.hasSide()
                && state.canToggleCreate()
                && createCooldownSeconds <= 0L;

        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
                .bounds(center - 50, y + 62, 100, 20)
                .build());
    }

    @Override
    public void tick() {
        if (explosionCooldownSeconds <= 0L && createCooldownSeconds <= 0L) {
            return;
        }

        cooldownTickCounter++;
        if (cooldownTickCounter < 20) {
            return;
        }

        cooldownTickCounter = 0;
        boolean wasBlocked = explosionCooldownSeconds > 0L || createCooldownSeconds > 0L;
        explosionCooldownSeconds = Math.max(0L, explosionCooldownSeconds - 1L);
        createCooldownSeconds = Math.max(0L, createCooldownSeconds - 1L);
        boolean isBlocked = explosionCooldownSeconds > 0L || createCooldownSeconds > 0L;
        if (wasBlocked != isBlocked || explosionCooldownSeconds == 0L || createCooldownSeconds == 0L) {
            rebuildWidgets();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int center = width / 2;
        int y = Math.max(30, height / 2 - 92);
        graphics.drawCenteredString(font, title, center, y, 0xFFFFFFFF);
        graphics.drawCenteredString(font, state.sideLabel(), center, y + 18, state.hasSide() ? 0xFFD6F4FF : 0xFFFFAAAA);

        String explosionCooldown = cooldownLine("Explosions", explosionCooldownSeconds);
        String createCooldown = cooldownLine("Create", createCooldownSeconds);
        graphics.drawCenteredString(font, explosionCooldown, center, y + 40, 0xFFBDBDBD);
        graphics.drawCenteredString(font, createCooldown, center, y + 52, 0xFFBDBDBD);

        if (!message.isBlank()) {
            graphics.drawCenteredString(font, trim(message, 60), center, height - 36, 0xFFFFF0A6);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Component explosionButtonLabel() {
        String status = state.explosionProtectionEnabled() ? "enabled" : "disabled";
        if (explosionCooldownSeconds > 0L) {
            return Component.literal("Explosions: " + status + " | Cooldown " + ClaimRulesService.formatDuration(explosionCooldownSeconds));
        }
        String action = state.explosionProtectionEnabled() ? "Disable" : "Enable";
        return Component.literal("Explosions: " + status + " | " + action);
    }

    private Component createButtonLabel() {
        String status = state.createMachinesEnabled() ? "allowed" : "blocked";
        if (createCooldownSeconds > 0L) {
            return Component.literal("Create machines: " + status + " | Cooldown " + ClaimRulesService.formatDuration(createCooldownSeconds));
        }
        String action = state.createMachinesEnabled() ? "Block" : "Allow";
        return Component.literal("Create machines: " + status + " | " + action);
    }

    private void resetCooldowns(ClaimRulesStateDto state) {
        explosionCooldownSeconds = Math.max(0L, state.explosionCooldownSeconds());
        createCooldownSeconds = Math.max(0L, state.createCooldownSeconds());
        cooldownTickCounter = 0;
    }

    private static String cooldownLine(String label, long seconds) {
        if (seconds <= 0L) {
            return label + " toggle ready";
        }
        return label + " cooldown: " + ClaimRulesService.formatDuration(seconds);
    }

    private static String trim(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }
}
