package dev.customclaims.forge;

import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.config.CoreConfig;
import dev.customclaims.core.platform.PlatformHelperHolder;
import dev.customclaims.forge.platform.ForgeNetworkHelper;
import dev.customclaims.forge.platform.ForgePermissionHelper;
import dev.customclaims.forge.platform.ForgePlatformHelper;
import dev.customclaims.war.CustomClaimsWarMod;
import dev.customclaims.war.command.WarAdminCommand;
import dev.customclaims.war.command.WarCommand;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.event.WarActivityHandler;
import dev.customclaims.war.event.WarLivesEventHandler;
import dev.customclaims.war.event.WarTickHandler;
import dev.customclaims.xaero.CustomClaimsXaeroMod;
import dev.customclaims.xaero.client.ClientOverlayService;
import dev.customclaims.xaero.config.XaeroCompatConfig;
import dev.customclaims.xaero.event.WarMarkerSyncHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("opac_warfare")
public final class OpacWarfareForgeMod {
    public OpacWarfareForgeMod() {
        PlatformHelperHolder.init(
                new ForgePlatformHelper(),
                new ForgePermissionHelper(),
                new ForgeNetworkHelper()
        );

        CustomClaimsCoreMod.init();
        CustomClaimsWarMod.init();
        CustomClaimsXaeroMod.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CoreConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WarConfig.SPEC, "opac_warfare-war.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, XaeroCompatConfig.SPEC, "opac_warfare-xaero.toml");

        MinecraftForge.EVENT_BUS.register(this);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(new ClientForgeEvents());
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ForgeNetworkHelper::registerPackets);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        CustomClaimsCoreMod.services().metricsService().onServerStarted(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        CustomClaimsCoreMod.services().metricsService().onServerStopping(event.getServer());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            CustomClaimsCoreMod.services().metricsService().onServerTick(event.getServer());
            WarTickHandler.onServerTick(event.getServer());
            WarMarkerSyncHandler.onServerTick(event.getServer());
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        WarCommand.register(event.getDispatcher());
        WarAdminCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WarActivityHandler.onPlayerActivity(player);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WarLivesEventHandler.onPlayerDeath(player);
        }
    }

    private static final class ClientForgeEvents {
        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                ClientOverlayService.onClientTick();
            }
        }

        @SubscribeEvent
        public void onRenderGui(RenderGuiOverlayEvent.Post event) {
            if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()) {
                ClientOverlayService.onRenderGui(event.getGuiGraphics());
            }
        }
    }
}
