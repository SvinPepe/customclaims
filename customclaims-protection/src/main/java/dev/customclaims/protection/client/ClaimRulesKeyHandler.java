package dev.customclaims.protection.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.customclaims.protection.network.ServerboundOpenClaimRulesPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class ClaimRulesKeyHandler {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.parse("customclaims:category")
    );
    private static final KeyMapping OPEN_CLAIMRULES = new KeyMapping(
            "key.customclaims_protection.open_claimrules",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            CATEGORY
    );

    private ClaimRulesKeyHandler() {
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN_CLAIMRULES);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (OPEN_CLAIMRULES.consumeClick()) {
            if (minecraft.player == null || minecraft.level == null || minecraft.getConnection() == null) {
                continue;
            }
            minecraft.getConnection().send(new ServerboundCustomPayloadPacket(new ServerboundOpenClaimRulesPayload()));
        }
    }
}
