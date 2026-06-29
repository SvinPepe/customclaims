package dev.customclaims.war.service;

import dev.customclaims.core.CoreServices;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.model.WarData;
import dev.customclaims.war.model.WarState;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class WarLivesService {
    private final CoreServices coreServices;

    public WarLivesService(CoreServices coreServices) {
        this.coreServices = coreServices;
    }

    public boolean initializeIfNeeded(MinecraftServer server, WarData war) {
        if (war.livesInitialized()) {
            return false;
        }
        war.initializeLives(
                coreServices.partyService().sideMemberIds(server, war.attackerSide()),
                coreServices.partyService().sideMemberIds(server, war.defenderSide()),
                WarConfig.STARTING_LIVES.get()
        );
        return true;
    }

    public boolean canContribute(ServerPlayer player, WarData war) {
        if (war.state() != WarState.ACTIVE) {
            return true;
        }
        return war.hasLivesRemaining(player.getUUID());
    }

    public Optional<Integer> decrementForDeath(ServerPlayer player, WarData war) {
        if (war.state() != WarState.ACTIVE) {
            return Optional.empty();
        }
        return war.decrementLives(player.getUUID());
    }
}
