package dev.customclaims.war.service;

import dev.customclaims.core.CoreServices;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.PartyId;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.model.WarData;
import dev.customclaims.war.model.WarState;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class WarManager {
    private final CoreServices coreServices;
    private final WarStorage warStorage;
    private final RaidWindowService raidWindowService;
    private final BorderChunkService borderChunkService;
    private final AfkTracker afkTracker;
    private final CaptureProgressService captureProgressService;
    private final PostWarProtectionService postWarProtectionService;
    private final Map<UUID, WarData> wars = new LinkedHashMap<>();
    private boolean loaded;
    private long ticks;

    public WarManager(
            CoreServices coreServices,
            WarStorage warStorage,
            RaidWindowService raidWindowService,
            BorderChunkService borderChunkService,
            AfkTracker afkTracker,
            CaptureProgressService captureProgressService,
            PostWarProtectionService postWarProtectionService
    ) {
        this.coreServices = coreServices;
        this.warStorage = warStorage;
        this.raidWindowService = raidWindowService;
        this.borderChunkService = borderChunkService;
        this.afkTracker = afkTracker;
        this.captureProgressService = captureProgressService;
        this.postWarProtectionService = postWarProtectionService;
    }

    public WarOperationResult startWar(ServerPlayer player) {
        MinecraftServer server = player.server;
        ensureLoaded(server);

        Optional<PartyId> attackerParty = coreServices.partyService().getPlayerParty(player);
        if (attackerParty.isEmpty()) {
            return WarOperationResult.fail("You must be in a party to start a war.");
        }

        ServerLevel level = player.serverLevel();
        ChunkPos target = player.chunkPosition();
        Optional<PartyId> defenderParty = coreServices.territoryService().getClaimOwner(level, target);
        if (defenderParty.isEmpty()) {
            return WarOperationResult.fail("This chunk is not claimed by another party.");
        }
        if (defenderParty.get().equals(attackerParty.get())) {
            return WarOperationResult.fail("You cannot attack your own party claim.");
        }
        if (raidWindowService.isWarStartBlockedNow()) {
            return WarOperationResult.fail("Wars are blocked by the current raid window.");
        }
        if (!borderChunkService.isBorderChunk(
                level,
                target,
                attackerParty.get(),
                defenderParty.get(),
                WarConfig.ALLOW_DIAGONAL_BORDER_CHUNKS.get()
        )) {
            return WarOperationResult.fail("Target chunk must border attacker territory.");
        }

        ChunkPosKey key = ChunkPosKey.from(level, target);
        if (findByChunk(key).isPresent()) {
            return WarOperationResult.fail("This chunk is already involved in a war.");
        }
        if (activeWarsByAttacker(attackerParty.get()) >= WarConfig.MAX_ACTIVE_WARS_PER_PARTY.get()) {
            return WarOperationResult.fail("Your party has reached the active war limit.");
        }
        if (!hasOnlineNonAfkDefender(server, defenderParty.get())) {
            return WarOperationResult.fail("The defending party has no online non-AFK members.");
        }

        WarData war = new WarData(UUID.randomUUID(), attackerParty.get(), defenderParty.get(), key, Instant.now());
        wars.put(war.id(), war);
        save(server);
        coreServices.warLogService().log(server, "START " + describe(war));
        return WarOperationResult.ok("War started. Preparation phase active for war " + war.id() + ".");
    }

    public WarOperationResult status(ServerPlayer player) {
        ensureLoaded(player.server);
        ChunkPosKey key = ChunkPosKey.from(player.serverLevel(), player.chunkPosition());
        return findByChunk(key)
                .map(war -> WarOperationResult.ok(formatWarStatus(war)))
                .orElseGet(() -> WarOperationResult.ok("No active war for this chunk."));
    }

    public WarOperationResult status(MinecraftServer server) {
        ensureLoaded(server);
        long active = wars.values().stream().filter(war -> !war.isTerminal()).count();
        return WarOperationResult.ok("Tracked wars: " + active + " active/preparing, " + wars.size() + " total loaded.");
    }

    public WarOperationResult adminStop(MinecraftServer server, UUID warId) {
        ensureLoaded(server);
        WarData war = wars.get(warId);
        if (war == null || war.isTerminal()) {
            return WarOperationResult.fail("War not found or already ended.");
        }
        finish(server, war, WarState.CANCELLED, "admin_stop");
        return WarOperationResult.ok("War " + warId + " cancelled.");
    }

    public WarOperationResult adminSetProgress(MinecraftServer server, UUID warId, double progress) {
        ensureLoaded(server);
        WarData war = wars.get(warId);
        if (war == null || war.isTerminal()) {
            return WarOperationResult.fail("War not found or already ended.");
        }
        war.setProgress(progress);
        save(server);
        coreServices.warLogService().log(server, "ADMIN_SET_PROGRESS " + war.id() + " " + progress);
        return WarOperationResult.ok("War " + warId + " progress set to " + Math.round(war.progress()) + "%.");
    }

    public void tick(MinecraftServer server) {
        ensureLoaded(server);
        ticks++;
        if (ticks % 20 != 0) {
            return;
        }

        boolean changed = false;
        Instant now = Instant.now();
        for (WarData war : wars.values().stream()
                .filter(war -> !war.isTerminal())
                .sorted(Comparator.comparing(WarData::createdAt))
                .toList()) {
            changed |= tickWar(server, war, now);
        }

        if (changed) {
            save(server);
        }
    }

    private boolean tickWar(MinecraftServer server, WarData war, Instant now) {
        if (war.state() == WarState.PREPARING) {
            long seconds = Duration.between(war.createdAt(), now).getSeconds();
            if (seconds >= WarConfig.PREPARATION_SECONDS.get()) {
                activate(server, war, now);
                return true;
            }
            return false;
        }

        if (war.state() != WarState.ACTIVE) {
            return false;
        }

        if (!WarConfig.ALLOW_ONGOING_WARS_TO_CONTINUE_AFTER_WINDOW_START.get()
                && raidWindowService.isWarStartBlockedNow()) {
            finish(server, war, WarState.FAILED, "raid_window_started");
            return true;
        }

        Optional<ServerLevel> level = resolveLevel(server, war.targetChunk());
        if (level.isEmpty()) {
            finish(server, war, WarState.FAILED, "level_missing");
            return true;
        }

        double nextProgress = captureProgressService.nextProgress(server, level.get(), war, afkTracker);
        war.setProgress(nextProgress);
        coreServices.warLogService().log(server, "PROGRESS " + war.id() + " " + Math.round(war.progress()));

        if (war.progress() >= 100.0D) {
            boolean transferred = coreServices.territoryService().transferClaim(level.get(), war.targetChunk().toChunkPos(), war.attackerParty());
            if (!transferred) {
                finish(server, war, WarState.FAILED, "claim_transfer_failed");
                return true;
            }
            finish(server, war, WarState.FINISHED, "attacker_captured");
            return true;
        }

        if (war.progress() <= 0.0D && war.activeAt().isPresent()
                && Duration.between(war.activeAt().get(), Instant.now()).getSeconds() > 5) {
            finish(server, war, WarState.FAILED, "progress_depleted");
            return true;
        }

        if (war.activeAt().isPresent()
                && Duration.between(war.activeAt().get(), Instant.now()).getSeconds() >= WarConfig.MAX_DURATION_SECONDS.get()) {
            finish(server, war, WarState.FAILED, "max_duration_reached");
            return true;
        }

        return true;
    }

    private void activate(MinecraftServer server, WarData war, Instant now) {
        war.setState(WarState.ACTIVE);
        war.setActiveAt(now);
        war.setProgress(Math.max(war.progress(), WarConfig.STARTING_PROGRESS.get()));
        coreServices.territoryStateService().markContested(war.targetChunk());
        coreServices.warLogService().log(server, "ACTIVE " + describe(war));
    }

    private void finish(MinecraftServer server, WarData war, WarState state, String reason) {
        war.setState(state);
        war.setEndedAt(Instant.now());
        war.setEndReason(reason);
        postWarProtectionService.protect(war.targetChunk());
        coreServices.warLogService().log(server, state.name() + " " + describe(war) + " reason=" + reason);
        save(server);
    }

    private boolean hasOnlineNonAfkDefender(MinecraftServer server, PartyId defenderParty) {
        return coreServices.partyService().onlineMembers(server, defenderParty).stream()
                .anyMatch(player -> !afkTracker.isAfk(player));
    }

    private long activeWarsByAttacker(PartyId attackerParty) {
        return wars.values().stream()
                .filter(war -> !war.isTerminal())
                .filter(war -> war.attackerParty().equals(attackerParty))
                .count();
    }

    private Optional<WarData> findByChunk(ChunkPosKey key) {
        return wars.values().stream()
                .filter(war -> !war.isTerminal())
                .filter(war -> war.targetChunk().equals(key))
                .findFirst();
    }

    private void ensureLoaded(MinecraftServer server) {
        if (loaded) {
            return;
        }
        warStorage.load(server).forEach(war -> wars.put(war.id(), war));
        for (WarData war : wars.values()) {
            if (war.state() == WarState.ACTIVE) {
                coreServices.territoryStateService().markContested(war.targetChunk());
            }
        }
        loaded = true;
    }

    private void save(MinecraftServer server) {
        warStorage.save(server, wars.values());
    }

    private Optional<ServerLevel> resolveLevel(MinecraftServer server, ChunkPosKey key) {
        ResourceLocation location = ResourceLocation.parse(key.levelId());
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, location);
        return Optional.ofNullable(server.getLevel(dimension));
    }

    private String formatWarStatus(WarData war) {
        return "War " + war.id()
                + " state=" + war.state()
                + " attacker=" + war.attackerParty()
                + " defender=" + war.defenderParty()
                + " progress=" + Math.round(war.progress()) + "%";
    }

    private String describe(WarData war) {
        return war.id()
                + " attacker=" + war.attackerParty()
                + " defender=" + war.defenderParty()
                + " chunk=" + war.targetChunk().storageKey()
                + " progress=" + Math.round(war.progress());
    }
}
