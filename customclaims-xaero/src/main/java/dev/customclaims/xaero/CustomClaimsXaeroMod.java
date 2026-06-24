package dev.customclaims.xaero;

import com.mojang.logging.LogUtils;
import dev.customclaims.xaero.client.ClientEventRegistrar;
import dev.customclaims.xaero.config.XaeroCompatConfig;
import dev.customclaims.xaero.event.WarMarkerSyncHandler;
import dev.customclaims.xaero.network.XaeroNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(CustomClaimsXaeroMod.MOD_ID)
public final class CustomClaimsXaeroMod {
    public static final String MOD_ID = "customclaims_xaero";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CustomClaimsXaeroMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, XaeroCompatConfig.SPEC);
        modEventBus.addListener(XaeroNetwork::registerPayloads);
        NeoForge.EVENT_BUS.addListener(WarMarkerSyncHandler::onServerTick);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEventRegistrar.register();
        }

        LOGGER.info("Custom Claims Xaero fair-play overlay initialized");
    }
}
