package dev.customclaims.warfare;

import dev.customclaims.core.CoreServices;
import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.config.CoreConfig;
import dev.customclaims.core.platform.NeoForgeNetworkHelper;
import dev.customclaims.core.platform.NeoForgePermissionHelper;
import dev.customclaims.core.platform.NeoForgePlatformHelper;
import dev.customclaims.core.platform.PlatformHelperHolder;
import dev.customclaims.war.CustomClaimsWarMod;
import dev.customclaims.war.command.WarCommand;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.event.WarActivityHandler;
import dev.customclaims.war.event.WarLivesEventHandler;
import dev.customclaims.war.event.WarTickHandler;
import dev.customclaims.xaero.CustomClaimsXaeroMod;
import dev.customclaims.xaero.client.ClientEventRegistrar;
import dev.customclaims.xaero.config.XaeroCompatConfig;
import dev.customclaims.xaero.event.WarMarkerSyncHandler;
import dev.customclaims.xaero.network.XaeroNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(OpacWarfareMod.MOD_ID)
public final class OpacWarfareMod {
    public static final String MOD_ID = "opac_warfare";

    public OpacWarfareMod(IEventBus modEventBus, ModContainer modContainer) {
        PlatformHelperHolder.init(
                new NeoForgePlatformHelper(),
                new NeoForgePermissionHelper(),
                new NeoForgeNetworkHelper()
        );

        modContainer.registerConfig(ModConfig.Type.COMMON, CoreConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, WarConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, XaeroCompatConfig.SPEC);

        CustomClaimsCoreMod.init();
        CustomClaimsWarMod.init();
        CustomClaimsXaeroMod.init();

        modEventBus.addListener(NeoForgePermissionHelper::onGatherNodes);
        modEventBus.addListener(XaeroNetwork::registerPayloads);

        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) ->
                CustomClaimsCoreMod.services().metricsService().onServerStarted(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) ->
                CustomClaimsCoreMod.services().metricsService().onServerStopping(event.getServer()));
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            CustomClaimsCoreMod.services().metricsService().onServerTick(event.getServer());
            WarTickHandler.onServerTick(event.getServer());
            WarMarkerSyncHandler.onServerTick(event.getServer());
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                WarCommand.register(event.getDispatcher()));

        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                WarActivityHandler.onPlayerActivity(player);
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.LeftClickBlock event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                WarActivityHandler.onPlayerActivity(player);
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteract event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                WarActivityHandler.onPlayerActivity(player);
            }
        });

        NeoForge.EVENT_BUS.addListener((LivingDeathEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                WarLivesEventHandler.onPlayerDeath(player);
            }
        });

        //? if <26.0 {
        if (FMLEnvironment.dist == Dist.CLIENT) {
        //?} else {
        /*if (FMLEnvironment.getDist() == Dist.CLIENT) {
        *///?}
            ClientEventRegistrar.register();
        }
    }
}
