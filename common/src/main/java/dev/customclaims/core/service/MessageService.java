package dev.customclaims.core.service;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class MessageService {

    public MessageService() {
    }

    public void system(ServerPlayer player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    public void actionBar(ServerPlayer player, String message) {
        //? if <26.0 {
        player.displayClientMessage(Component.literal(message), true);
        //?} else {
        /*player.sendOverlayMessage(Component.literal(message));
        *///?}
    }
}
