package dev.customclaims.protection.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.customclaims.protection.network.ServerboundOpenClaimRulesPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class ClaimRulesKeyHandler {
    private static final KeyMapping OPEN_CLAIMRULES = new KeyMapping(
            "key.customclaims_protection.open_claimrules",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.customclaims"
    );

    private ClaimRulesKeyHandler() {
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CLAIMRULES);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN_CLAIMRULES.consumeClick()) {
            if (minecraft.player == null || minecraft.level == null) {
                continue;
            }
            PacketDistributor.sendToServer(new ServerboundOpenClaimRulesPayload());
        }
    }
}
