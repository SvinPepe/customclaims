package dev.customclaims.fabric;

import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.platform.PlatformHelperHolder;
import dev.customclaims.fabric.platform.FabricNetworkHelper;
import dev.customclaims.fabric.platform.FabricPermissionHelper;
import dev.customclaims.fabric.platform.FabricPlatformHelper;
import dev.customclaims.war.CustomClaimsWarMod;
import dev.customclaims.war.command.WarAdminCommand;
import dev.customclaims.war.command.WarCommand;
import dev.customclaims.war.event.WarActivityHandler;
import dev.customclaims.war.event.WarLivesEventHandler;
import dev.customclaims.war.event.WarTickHandler;
import dev.customclaims.xaero.CustomClaimsXaeroMod;
import dev.customclaims.xaero.event.WarMarkerSyncHandler;
import dev.customclaims.xaero.network.ClientboundWarMarkersPayload;
import dev.customclaims.xaero.network.ServerboundStartWarAtPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
//? if >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//?}
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public final class CustomClaimsFabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        PlatformHelperHolder.init(
                new FabricPlatformHelper(),
                new FabricPermissionHelper(),
                new FabricNetworkHelper()
        );

        CustomClaimsCoreMod.init();
        CustomClaimsWarMod.init();
        CustomClaimsXaeroMod.init();

        //? if >=1.20.5 {
        PayloadTypeRegistry.playS2C().register(ClientboundWarMarkersPayload.TYPE, ClientboundWarMarkersPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(ServerboundStartWarAtPayload.TYPE, ServerboundStartWarAtPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerboundStartWarAtPayload.TYPE, (payload, context) -> {
            ServerboundStartWarAtPayload.handle(payload, context.player());
        });
        //?} else {
        /*ServerPlayNetworking.registerGlobalReceiver(ServerboundStartWarAtPayload.ID, (server, player, handler, buf, responseSender) -> {
            ServerboundStartWarAtPayload payload = ServerboundStartWarAtPayload.read(buf);
            server.execute(() -> ServerboundStartWarAtPayload.handle(payload, player));
        });
        *///?}

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CustomClaimsCoreMod.services().metricsService().onServerStarted(server);
            CustomClaimsWarMod.services().onServerStarted(server);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
                CustomClaimsCoreMod.services().metricsService().onServerStopping(server));
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            CustomClaimsCoreMod.services().metricsService().onServerTick(server);
            WarTickHandler.onServerTick(server);
            WarMarkerSyncHandler.onServerTick(server);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            WarCommand.register(dispatcher);
            WarAdminCommand.register(dispatcher);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                WarActivityHandler.onPlayerActivity(serverPlayer);
            }
            return InteractionResult.PASS;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                WarLivesEventHandler.onPlayerDeath(player);
            }
        });
    }
}
