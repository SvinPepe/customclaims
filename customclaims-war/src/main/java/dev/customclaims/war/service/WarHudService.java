package dev.customclaims.war.service;

import dev.customclaims.core.CoreServices;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.model.WarData;
import dev.customclaims.war.model.WarState;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class WarHudService {
    private final CoreServices coreServices;
    private final WarDisplayService displayService;
    private final Map<UUID, ServerBossEvent> bossEvents = new ConcurrentHashMap<>();

    public WarHudService(CoreServices coreServices, WarDisplayService displayService) {
        this.coreServices = coreServices;
        this.displayService = displayService;
    }

    public void update(MinecraftServer server, Collection<WarData> wars) {
        Set<UUID> visibleWarIds = new HashSet<>();
        for (WarData war : wars) {
            if (war.isTerminal()) {
                continue;
            }
            visibleWarIds.add(war.id());
            updateBossbar(server, war);
            sendActionbar(server, war);
        }

        bossEvents.entrySet().removeIf(entry -> {
            if (visibleWarIds.contains(entry.getKey())) {
                return false;
            }
            removeAllPlayers(server, entry.getValue());
            return true;
        });
    }

    public void remove(MinecraftServer server, WarData war) {
        ServerBossEvent bossEvent = bossEvents.remove(war.id());
        if (bossEvent != null) {
            removeAllPlayers(server, bossEvent);
        }
    }

    private void updateBossbar(MinecraftServer server, WarData war) {
        ServerBossEvent bossEvent = bossEvents.computeIfAbsent(war.id(), id -> new ServerBossEvent(
                Component.literal(displayService.label(server, war)),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        ));
        bossEvent.setName(Component.literal(displayService.label(server, war)));
        bossEvent.setProgress((float) Math.max(0.0D, Math.min(1.0D, war.progress() / 100.0D)));
        bossEvent.setColor(war.state() == WarState.PREPARING ? BossEvent.BossBarColor.YELLOW : BossEvent.BossBarColor.RED);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (shouldSeeBossbar(player, war)) {
                bossEvent.addPlayer(player);
            } else {
                bossEvent.removePlayer(player);
            }
        }
    }

    private void sendActionbar(MinecraftServer server, WarData war) {
        Optional<ServerLevel> level = resolveLevel(server, war.targetChunk());
        if (level.isEmpty()) {
            return;
        }

        ChunkPos target = war.targetChunk().toChunkPos();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level().dimension().equals(level.get().dimension()) && player.chunkPosition().equals(target)) {
                player.displayClientMessage(Component.literal(displayService.actionbarText(war)), true);
            }
        }
    }

    private boolean shouldSeeBossbar(ServerPlayer player, WarData war) {
        if (coreServices.partyService().isSameParty(player, war.attackerParty())
                || coreServices.partyService().isSameParty(player, war.defenderParty())) {
            return true;
        }
        if (!player.level().dimension().location().toString().equals(war.targetChunk().levelId())) {
            return false;
        }
        int radius = WarConfig.WAR_UI_BOSSBAR_VISIBLE_RADIUS_CHUNKS.get();
        return chunkDistance(player.chunkPosition(), war.targetChunk().toChunkPos()) <= radius;
    }

    private int chunkDistance(ChunkPos left, ChunkPos right) {
        return Math.max(Math.abs(left.x - right.x), Math.abs(left.z - right.z));
    }

    private void removeAllPlayers(MinecraftServer server, ServerBossEvent bossEvent) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            bossEvent.removePlayer(player);
        }
    }

    private Optional<ServerLevel> resolveLevel(MinecraftServer server, ChunkPosKey key) {
        ResourceLocation location = ResourceLocation.parse(key.levelId());
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, location);
        return Optional.ofNullable(server.getLevel(dimension));
    }
}
