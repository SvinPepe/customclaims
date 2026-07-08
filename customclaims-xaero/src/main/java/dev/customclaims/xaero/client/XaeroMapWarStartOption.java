package dev.customclaims.xaero.client;

import dev.customclaims.xaero.CustomClaimsXaeroMod;
import dev.customclaims.xaero.network.ServerboundStartWarAtPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import xaero.map.gui.GuiMap;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

public final class XaeroMapWarStartOption extends RightClickOption {
    private static final String TRANSLATION_KEY = "gui.customclaims_xaero.start_war_here";

    private final String dimension;
    private final int chunkX;
    private final int chunkZ;

    public XaeroMapWarStartOption(
            int index,
            IRightClickableElement target,
            ResourceKey<Level> dimension,
            int blockX,
            int blockZ
    ) {
        super(TRANSLATION_KEY, index, target);
        this.dimension = dimension.location().toString();
        this.chunkX = SectionPos.blockToSectionCoord(blockX);
        this.chunkZ = SectionPos.blockToSectionCoord(blockZ);
    }

    @Override
    public void onAction(Screen screen) {
        try {
            PacketDistributor.sendToServer(new ServerboundStartWarAtPayload(dimension, chunkX, chunkZ));
        } catch (RuntimeException exception) {
            CustomClaimsXaeroMod.LOGGER.warn("Failed to send Xaero map war-start request", exception);
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(
                        Component.literal("Xaero map war start is not available on this server."),
                        false
                );
            }
        }
    }
}
