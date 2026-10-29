package dev.customclaims.war.service;

import dev.customclaims.core.CoreServices;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.discord.DiscordWebhookService;
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
        send(server, war, Component.translatable(
                "opac_warfare.notification.declared",
                displayService.label(server, war),
                displayService.formatDuration(WarConfig.PREPARATION_SECONDS.get())
        ));
        DiscordWebhookService.sendWarDeclared(
                war,
                displayService.sideName(server, war.attackerSide()),
                displayService.sideName(server, war.defenderSide())
        );
    }

    public void notifyPreparationWarning(MinecraftServer server, WarData war, long secondsRemaining) {
        send(server, war, Component.translatable(
                "opac_warfare.notification.preparation_warning",
                displayService.formatDuration(secondsRemaining),
                displayService.label(server, war)
        ));
    }

    public void notifyAdminSkip(MinecraftServer server, WarData war) {
        send(server, war, Component.translatable(
                "opac_warfare.notification.admin_skip",
                displayService.label(server, war)
        ));
    }

    public void notifyActive(MinecraftServer server, WarData war) {
        send(server, war, Component.translatable(
                "opac_warfare.notification.active",
                displayService.label(server, war),
                displayService.progressText(war)
        ));
        DiscordWebhookService.sendWarActive(
                war,
                displayService.sideName(server, war.attackerSide()),
                displayService.sideName(server, war.defenderSide())
        );
    }

    public void notifyProgressMilestone(MinecraftServer server, WarData war, int milestone) {
        send(server, war, Component.translatable(
                "opac_warfare.notification.milestone",
                milestone,
                displayService.label(server, war)
        ));
    }

    public void notifyEmptyDecay(MinecraftServer server, WarData war) {
        send(server, war, Component.translatable(
                "opac_warfare.notification.empty_decay",
                displayService.label(server, war)
        ));
    }

    public void notifyLifeLost(MinecraftServer server, WarData war, ServerPlayer player, int remainingLives) {
        if (remainingLives > 0) {
            send(server, war, Component.translatable(
                    "opac_warfare.notification.life_lost",
                    player.getScoreboardName(),
                    remainingLives
            ));
            return;
        }
        send(server, war, Component.translatable(
                "opac_warfare.notification.lives_exhausted",
                player.getScoreboardName()
        ));
    }

    public void notifyEnded(MinecraftServer server, WarData war) {
        send(server, war, Component.translatable(
                "opac_warfare.notification.ended",
                displayService.stateName(war.state()),
                displayService.label(server, war),
                war.endReason().isBlank() ? "ended" : war.endReason()
        ));
        DiscordWebhookService.sendWarEnded(
                war,
                displayService.sideName(server, war.attackerSide()),
                displayService.sideName(server, war.defenderSide())
        );
    }

    private void send(MinecraftServer server, WarData war, Component component) {
        if (WarConfig.BROADCAST_NOTIFICATIONS_TO_ALL_PLAYERS.get()) {
            server.getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(component));
            return;
        }

        Map<UUID, ServerPlayer> recipients = new LinkedHashMap<>();
        coreServices.partyService().onlineSideMembers(server, war.attackerSide())
                .forEach(player -> recipients.put(player.getUUID(), player));
        coreServices.partyService().onlineSideMembers(server, war.defenderSide())
                .forEach(player -> recipients.put(player.getUUID(), player));
        recipients.values().forEach(player -> player.sendSystemMessage(component));
    }
}
