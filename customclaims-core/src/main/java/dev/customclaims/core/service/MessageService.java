package dev.customclaims.core.service;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class MessageService {
    public Component prefixed(String message) {
        return Component.literal("[CustomClaims] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(message).withStyle(ChatFormatting.YELLOW));
    }

    public void send(ServerPlayer player, String message) {
        player.sendSystemMessage(prefixed(message));
    }

    public void actionBar(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }
}
