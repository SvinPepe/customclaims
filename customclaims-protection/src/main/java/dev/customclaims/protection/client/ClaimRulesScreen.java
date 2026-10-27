package dev.customclaims.protection.client;

import dev.customclaims.protection.network.ClaimRulesStateDto;
import dev.customclaims.protection.network.ServerboundSetClaimRulePayload;
import dev.customclaims.protection.service.ClaimRulesService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

public final class ClaimRulesScreen extends Screen {
    private ClaimRulesStateDto state;
    private String message;
    private long explosionCooldownSeconds;
    private long createCooldownSeconds;
    private long assemblyCooldownSeconds;
    private int cooldownTickCounter;

    private ClaimRulesScreen(ClaimRulesStateDto state, String message) {
        super(Component.translatable("screen.opac_better_protection.claimrules.title"));
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
        int y = Math.max(84, height / 2 - 48);

        addRenderableWidget(Button.builder(explosionButtonLabel(), button ->
                        sendSetRule(ClaimRulesService.RULE_EXPLOSIONS, !state.explosionProtectionEnabled()))
                .bounds(center - 105, y, 210, 20)
                .build()).active = state.hasSide()
                && state.canToggleExplosions()
                && explosionCooldownSeconds <= 0L;

        addRenderableWidget(Button.builder(createButtonLabel(), button ->
                        sendSetRule(ClaimRulesService.RULE_CREATE, !state.createMachinesEnabled()))
                .bounds(center - 105, y + 24, 210, 20)
                .build()).active = state.hasSide()
                && state.canToggleCreate()
                && createCooldownSeconds <= 0L;

        addRenderableWidget(Button.builder(assemblyButtonLabel(), button ->
                        sendSetRule(ClaimRulesService.RULE_ASSEMBLY, !state.assemblyEnabled()))
                .bounds(center - 105, y + 48, 210, 20)
                .build()).active = state.hasSide()
                && state.canToggleAssembly()
                && assemblyCooldownSeconds <= 0L;

        addRenderableWidget(Button.builder(Component.translatable("screen.opac_better_protection.claimrules.done"), button -> onClose())
                .bounds(center - 50, y + 86, 100, 20)
                .build());
    }

    private void sendSetRule(String ruleId, boolean enabled) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            minecraft.getConnection().send(new ServerboundCustomPayloadPacket(
                    new ServerboundSetClaimRulePayload(ruleId, enabled)
            ));
        }
    }

    @Override
    public void tick() {
        if (explosionCooldownSeconds <= 0L && createCooldownSeconds <= 0L && assemblyCooldownSeconds <= 0L) {
            return;
        }

        cooldownTickCounter++;
        if (cooldownTickCounter < 20) {
            return;
        }

        cooldownTickCounter = 0;
        boolean wasBlocked = hasActiveCooldown();
        explosionCooldownSeconds = Math.max(0L, explosionCooldownSeconds - 1L);
        createCooldownSeconds = Math.max(0L, createCooldownSeconds - 1L);
        assemblyCooldownSeconds = Math.max(0L, assemblyCooldownSeconds - 1L);
        boolean isBlocked = hasActiveCooldown();
        if (wasBlocked != isBlocked
                || explosionCooldownSeconds == 0L
                || createCooldownSeconds == 0L
                || assemblyCooldownSeconds == 0L) {
            rebuildWidgets();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractBackground(graphics, mouseX, mouseY, partialTick);
        int center = width / 2;
        int y = Math.max(22, height / 2 - 104);
        graphics.centeredText(font, title, center, y, 0xFFFFFFFF);
        graphics.centeredText(font, state.sideLabel(), center, y + 18, state.hasSide() ? 0xFFD6F4FF : 0xFFFFAAAA);

        graphics.centeredText(font, cooldownLine("screen.opac_better_protection.claimrules.label.explosions", explosionCooldownSeconds), center, y + 40, 0xFFBDBDBD);
        graphics.centeredText(font, cooldownLine("screen.opac_better_protection.claimrules.label.mining", createCooldownSeconds), center, y + 52, 0xFFBDBDBD);
        graphics.centeredText(font, cooldownLine("screen.opac_better_protection.claimrules.label.assembly", assemblyCooldownSeconds), center, y + 64, 0xFFBDBDBD);

        if (!message.isBlank()) {
            graphics.centeredText(font, trim(message, 60), center, height - 36, 0xFFFFF0A6);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Component explosionButtonLabel() {
        Component status = Component.translatable(state.explosionProtectionEnabled()
                ? "screen.opac_better_protection.claimrules.status.enabled"
                : "screen.opac_better_protection.claimrules.status.disabled");
        if (explosionCooldownSeconds > 0L) {
            return Component.translatable(
                    "screen.opac_better_protection.claimrules.explosions.cooldown",
                    status,
                    ClaimRulesService.formatDuration(explosionCooldownSeconds)
            );
        }
        Component action = Component.translatable(state.explosionProtectionEnabled()
                ? "screen.opac_better_protection.claimrules.action.disable"
                : "screen.opac_better_protection.claimrules.action.enable");
        return Component.translatable(
                "screen.opac_better_protection.claimrules.explosions.status",
                status,
                action
        );
    }

    private Component createButtonLabel() {
        Component status = Component.translatable(state.createMachinesEnabled()
                ? "screen.opac_better_protection.claimrules.status.allowed"
                : "screen.opac_better_protection.claimrules.status.blocked");
        if (createCooldownSeconds > 0L) {
            return Component.translatable(
                    "screen.opac_better_protection.claimrules.create.cooldown",
                    status,
                    ClaimRulesService.formatDuration(createCooldownSeconds)
            );
        }
        Component action = Component.translatable(state.createMachinesEnabled()
                ? "screen.opac_better_protection.claimrules.action.block"
                : "screen.opac_better_protection.claimrules.action.allow");
        return Component.translatable(
                "screen.opac_better_protection.claimrules.create.status",
                status,
                action
        );
    }

    private Component assemblyButtonLabel() {
        Component status = Component.translatable(state.assemblyEnabled()
                ? "screen.opac_better_protection.claimrules.status.allowed"
                : "screen.opac_better_protection.claimrules.status.blocked");
        if (assemblyCooldownSeconds > 0L) {
            return Component.translatable(
                    "screen.opac_better_protection.claimrules.assembly.cooldown",
                    status,
                    ClaimRulesService.formatDuration(assemblyCooldownSeconds)
            );
        }
        Component action = Component.translatable(state.assemblyEnabled()
                ? "screen.opac_better_protection.claimrules.action.block"
                : "screen.opac_better_protection.claimrules.action.allow");
        return Component.translatable(
                "screen.opac_better_protection.claimrules.assembly.status",
                status,
                action
        );
    }

    private void resetCooldowns(ClaimRulesStateDto state) {
        explosionCooldownSeconds = Math.max(0L, state.explosionCooldownSeconds());
        createCooldownSeconds = Math.max(0L, state.createCooldownSeconds());
        assemblyCooldownSeconds = Math.max(0L, state.assemblyCooldownSeconds());
        cooldownTickCounter = 0;
    }

    private boolean hasActiveCooldown() {
        return explosionCooldownSeconds > 0L || createCooldownSeconds > 0L || assemblyCooldownSeconds > 0L;
    }

    private static Component cooldownLine(String labelKey, long seconds) {
        Component label = Component.translatable(labelKey);
        if (seconds <= 0L) {
            return Component.translatable("screen.opac_better_protection.claimrules.cooldown.ready", label);
        }
        return Component.translatable(
                "screen.opac_better_protection.claimrules.cooldown.remaining",
                label,
                ClaimRulesService.formatDuration(seconds)
        );
    }

    private static String trim(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }
}
