package dev.customclaims.war.service;

import dev.customclaims.core.CoreServices;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.model.WarData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class WarNotificationService {
    private final CoreServices coreServices;
    private final WarDisplayService displayService;

    public WarNotificationService(CoreServices coreServices, WarDisplayService displayService) {
        this.coreServices = coreServices;
        this.displayService = displayService;
    }

    public void notifyDeclared(MinecraftServer server, WarData war) {
        send(server, war, "War declared: " + displayService.label(server, war)
                + ". Preparation: " + displayService.formatDuration(WarConfig.PREPARATION_SECONDS.get()) + ".");
    }

    public void notifyPreparationWarning(MinecraftServer server, WarData war, long secondsRemaining) {
        send(server, war, "War starts in " + displayService.formatDuration(secondsRemaining)
                + ": " + displayService.label(server, war) + ".");
    }

    public void notifyAdminSkip(MinecraftServer server, WarData war) {
        send(server, war, "Preparation skipped by admin: " + displayService.label(server, war) + ".");
    }

    public void notifyActive(MinecraftServer server, WarData war) {
        send(server, war, "War is active: " + displayService.label(server, war)
                + ". " + displayService.progressText(war) + ".");
    }

    public void notifyProgressMilestone(MinecraftServer server, WarData war, int milestone) {
        send(server, war, "Capture reached " + milestone + "%: " + displayService.label(server, war) + ".");
    }

    public void notifyEmptyDecay(MinecraftServer server, WarData war) {
        send(server, war, "Contested chunk is empty, capture is decaying: "
                + displayService.label(server, war) + ".");
    }

    public void notifyLifeLost(MinecraftServer server, WarData war, ServerPlayer player, int remainingLives) {
        if (remainingLives > 0) {
            send(server, war, player.getGameProfile().getName() + " lost a war life: "
                    + remainingLives + " remaining.");
            return;
        }
        send(server, war, player.getGameProfile().getName()
                + " has no war lives left and no longer contributes to capture.");
    }

    public void notifyEnded(MinecraftServer server, WarData war) {
        send(server, war, displayService.stateName(war.state()) + ": "
                + displayService.label(server, war) + " (" + war.endReason() + ").");
    }

    private void send(MinecraftServer server, WarData war, String message) {
        Component component = Component.literal(message);
        Map<UUID, ServerPlayer> recipients = new LinkedHashMap<>();
        coreServices.partyService().onlineSideMembers(server, war.attackerSide())
                .forEach(player -> recipients.put(player.getUUID(), player));
        coreServices.partyService().onlineSideMembers(server, war.defenderSide())
                .forEach(player -> recipients.put(player.getUUID(), player));
        recipients.values().forEach(player -> player.sendSystemMessage(component));
    }
}
