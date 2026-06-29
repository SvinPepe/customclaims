package dev.customclaims.protection;

import com.mojang.logging.LogUtils;
import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.protection.command.ClaimRulesCommand;
import dev.customclaims.protection.config.ProtectionConfig;
import dev.customclaims.protection.event.BlockInteractionHandler;
import dev.customclaims.protection.event.ExplosionEventHandler;
import dev.customclaims.protection.event.ForeignInteractionLimitTickHandler;
import dev.customclaims.protection.event.StorageProtectionHandler;
import dev.customclaims.protection.event.VillagerDamageHandler;
import dev.customclaims.protection.event.WitherEventHandler;
import dev.customclaims.protection.network.ProtectionNetwork;
import java.time.Instant;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(CustomClaimsProtectionMod.MOD_ID)
public final class CustomClaimsProtectionMod {
    public static final String MOD_ID = "customclaims_protection";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static ProtectionServices services;

    public CustomClaimsProtectionMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, ProtectionConfig.SPEC);
        services = ProtectionServices.create(CustomClaimsCoreMod.services());
        modEventBus.addListener(ProtectionNetwork::registerPayloads);
        modEventBus.addListener(CustomClaimsProtectionMod::onConfigLoading);
        modEventBus.addListener(CustomClaimsProtectionMod::onConfigReloading);

        NeoForge.EVENT_BUS.addListener(ClaimRulesCommand::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, BlockInteractionHandler::onLeftClickBlock);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, BlockInteractionHandler::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, BlockInteractionHandler::onBreakBlock);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, BlockInteractionHandler::onPlaceBlock);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ExplosionEventHandler::onExplosionDetonate);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, StorageProtectionHandler::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(WitherEventHandler::onEntityJoinLevel);
        NeoForge.EVENT_BUS.addListener(VillagerDamageHandler::onLivingDamage);
        NeoForge.EVENT_BUS.addListener(ForeignInteractionLimitTickHandler::onServerTick);

        LOGGER.info("Custom Claims Protection initialized");
    }

    public static ProtectionServices services() {
        if (services == null) {
            throw new IllegalStateException("Custom Claims Protection services are not initialized yet");
        }
        return services;
    }

    private static void onConfigLoading(ModConfigEvent.Loading event) {
        rescheduleForeignInteractionLimitReset(event);
    }

    private static void onConfigReloading(ModConfigEvent.Reloading event) {
        rescheduleForeignInteractionLimitReset(event);
    }

    private static void rescheduleForeignInteractionLimitReset(ModConfigEvent event) {
        if (event.getConfig().getSpec() != ProtectionConfig.SPEC || services == null) {
            return;
        }
        services.foreignInteractionLimitService().rescheduleReset(Instant.now());
    }
}
