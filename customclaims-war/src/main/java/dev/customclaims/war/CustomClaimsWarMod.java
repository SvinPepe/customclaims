package dev.customclaims.war;

import com.mojang.logging.LogUtils;
import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.war.command.WarCommand;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.event.WarActivityHandler;
import dev.customclaims.war.event.WarLivesEventHandler;
import dev.customclaims.war.event.WarTickHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(CustomClaimsWarMod.MOD_ID)
public final class CustomClaimsWarMod {
    public static final String MOD_ID = "customclaims_war";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static WarServices services;

    public CustomClaimsWarMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, WarConfig.SPEC);
        services = WarServices.create(CustomClaimsCoreMod.services());

        NeoForge.EVENT_BUS.addListener(WarCommand::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(WarTickHandler::onServerTick);
        NeoForge.EVENT_BUS.addListener(WarActivityHandler::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(WarActivityHandler::onLeftClickBlock);
        NeoForge.EVENT_BUS.addListener(WarActivityHandler::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(WarLivesEventHandler::onLivingDeath);

        LOGGER.info("Custom Claims War initialized");
    }

    public static WarServices services() {
        if (services == null) {
            throw new IllegalStateException("Custom Claims War services are not initialized yet");
        }
        return services;
    }
}
