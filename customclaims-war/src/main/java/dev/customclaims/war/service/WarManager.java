package dev.customclaims.war.service;

import dev.customclaims.core.CoreServices;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.api.model.ClaimSnapshot;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.war.config.WarConfig;
import dev.customclaims.war.model.WarData;
import dev.customclaims.war.model.WarMarkerDto;
import dev.customclaims.war.model.WarState;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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
    private static final int DEFAULT_NEAR_RADIUS_CHUNKS = 8;
    private static final int MAX_NEAR_RADIUS_CHUNKS = 32;

    private final CoreServices coreServices;
    private final WarStorage warStorage;
    private final RaidWindowService raidWindowService;
    private final BorderChunkService borderChunkService;
    private final AfkTracker afkTracker;
    private final CaptureProgressService captureProgressService;
    private final PostWarProtectionService postWarProtectionService;
    private final WarDisplayService displayService;
    private final WarHudService hudService;
    private final WarNotificationService notificationService;
    private final WarLivesService livesService;
    private final WarScoreboardService scoreboardService;
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
            PostWarProtectionService postWarProtectionService,
            WarDisplayService displayService,
            WarHudService hudService,
            WarNotificationService notificationService,
            WarLivesService livesService,
            WarScoreboardService scoreboardService
    ) {
        this.coreServices = coreServices;
        this.warStorage = warStorage;
        this.raidWindowService = raidWindowService;
        this.borderChunkService = borderChunkService;
        this.afkTracker = afkTracker;
        this.captureProgressService = captureProgressService;
        this.postWarProtectionService = postWarProtectionService;
        this.displayService = displayService;
        this.hudService = hudService;
        this.notificationService = notificationService;
        this.livesService = livesService;
        this.scoreboardService = scoreboardService;
    }

    public WarOperationResult startWar(ServerPlayer player) {
        MinecraftServer server = player.server;
        ensureLoaded(server);

        ClaimSideId attackerSide = coreServices.partyService().getPlayerSide(player);

        ServerLevel level = player.serverLevel();
        ChunkPos target = player.chunkPosition();
        Optional<ClaimSideId> defenderSide = coreServices.territoryService().getClaimOwnerSide(level, target);
        if (defenderSide.isEmpty()) {
            return WarOperationResult.fail("This chunk is not claimed by another side.");
        }
        if (defenderSide.get().equals(attackerSide)) {
            return WarOperationResult.fail("You cannot attack your own claim.");
        }
        if (raidWindowService.isWarStartBlockedNow()) {
            return WarOperationResult.fail("Wars are blocked by the current raid window.");
        }
        if (!borderChunkService.isBorderChunk(
                level,
                target,
                attackerSide,
                defenderSide.get(),
                WarConfig.ALLOW_DIAGONAL_BORDER_CHUNKS.get()
        )) {
            return WarOperationResult.fail("Target chunk must border wilderness or attacker territory.");
        }

        ChunkPosKey key = ChunkPosKey.from(level, target);
        if (findByChunk(key).isPresent()) {
            return WarOperationResult.fail("This chunk is already involved in a war.");
        }
        if (activeWarsBySide(attackerSide) >= WarConfig.MAX_ACTIVE_WARS_PER_PARTY.get()) {
            return WarOperationResult.fail("Your side is already involved in a war.");
        }
        if (activeWarsBySide(defenderSide.get()) >= WarConfig.MAX_ACTIVE_WARS_PER_PARTY.get()) {
            return WarOperationResult.fail("The defending side is already involved in a war.");
        }
        if (!hasOnlineNonAfkDefender(server, defenderSide.get())) {
            return WarOperationResult.fail("The defending side has no online non-AFK members.");
        }

        WarData war = new WarData(UUID.randomUUID(), attackerSide, defenderSide.get(), key, Instant.now());
        wars.put(war.id(), war);
        save(server);
        coreServices.warLogService().log(server, "START " + describe(war));
        notificationService.notifyDeclared(server, war);
        return WarOperationResult.ok(displayService.formatStart(server, war));
    }

    public WarOperationResult status(ServerPlayer player) {
        ensureLoaded(player.server);
        ChunkPosKey key = ChunkPosKey.from(player.serverLevel(), player.chunkPosition());
        return findByChunk(key)
                .map(war -> WarOperationResult.ok(displayService.formatWarStatus(player.server, war, Instant.now())))
                .orElseGet(() -> WarOperationResult.ok("No active war for this chunk."));
    }

    public WarOperationResult status(MinecraftServer server) {
        ensureLoaded(server);
        List<WarData> active = activeWars().toList();
        if (active.isEmpty()) {
            return WarOperationResult.ok("No active or preparing wars.");
        }
        Instant now = Instant.now();
        return WarOperationResult.ok("Active wars: " + active.size() + "\n" + formatWarList(server, active, now));
    }

    public WarOperationResult list(ServerPlayer player) {
        ensureLoaded(player.server);
        List<WarData> visible = visibleWarsFor(player, DEFAULT_NEAR_RADIUS_CHUNKS).toList();
        if (visible.isEmpty()) {
            return WarOperationResult.ok("No visible active or preparing wars.");
        }
        return WarOperationResult.ok("Visible wars:\n" + formatWarList(player.server, visible, Instant.now()));
    }

    public WarOperationResult near(ServerPlayer player, int radiusChunks) {
        ensureLoaded(player.server);
        int radius = Math.max(0, Math.min(MAX_NEAR_RADIUS_CHUNKS, radiusChunks));
        List<WarData> nearby = activeWars()
                .filter(war -> isNear(player, war, radius))
                .toList();
        if (nearby.isEmpty()) {
            return WarOperationResult.ok("No active or preparing wars within " + radius + " chunks.");
        }
        return WarOperationResult.ok("Nearby wars within " + radius + " chunks:\n"
                + formatWarList(player.server, nearby, Instant.now()));
    }

    public WarOperationResult adminList(MinecraftServer server) {
        ensureLoaded(server);
        List<WarData> active = activeWars().toList();
        if (active.isEmpty()) {
            return WarOperationResult.ok("No active or preparing wars.");
        }
        return WarOperationResult.ok("Active wars:\n" + formatWarList(server, active, Instant.now()));
    }

    public WarOperationResult adminStop(MinecraftServer server, UUID warId) {
        ensureLoaded(server);
        WarData war = wars.get(warId);
        if (war == null || war.isTerminal()) {
            return WarOperationResult.fail("War not found or already ended.");
        }
        finish(server, war, WarState.CANCELLED, "admin_stop");
        return WarOperationResult.ok("Cancelled war: " + displayService.label(server, war));
    }

    public WarOperationResult adminStopChunk(MinecraftServer server, ChunkPosKey key) {
        ensureLoaded(server);
        Optional<WarData> war = findByChunk(key);
        if (war.isEmpty()) {
            return WarOperationResult.fail("No active or preparing war for " + displayService.chunkLabel(key) + ".");
        }
        finish(server, war.get(), WarState.CANCELLED, "admin_stop");
        return WarOperationResult.ok("Cancelled war: " + displayService.label(server, war.get()));
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
        return WarOperationResult.ok(displayService.formatAdminProgress(war));
    }

    public WarOperationResult adminSetProgressChunk(MinecraftServer server, ChunkPosKey key, double progress) {
        ensureLoaded(server);
        Optional<WarData> war = findByChunk(key);
        if (war.isEmpty()) {
            return WarOperationResult.fail("No active or preparing war for " + displayService.chunkLabel(key) + ".");
        }
        war.get().setProgress(progress);
        save(server);
        coreServices.warLogService().log(server, "ADMIN_SET_PROGRESS " + war.get().id() + " " + progress);
        return WarOperationResult.ok(displayService.formatAdminProgress(war.get()));
    }

    public WarOperationResult adminSkipPreparationChunk(MinecraftServer server, ChunkPosKey key) {
        ensureLoaded(server);
        Optional<WarData> war = findByChunk(key);
        if (war.isEmpty()) {
            return WarOperationResult.fail("No active or preparing war for " + displayService.chunkLabel(key) + ".");
        }
        if (war.get().state() != WarState.PREPARING) {
            return WarOperationResult.fail("War is not preparing: " + displayService.label(server, war.get()));
        }
        notificationService.notifyAdminSkip(server, war.get());
        if (!activate(server, war.get(), Instant.now())) {
            return WarOperationResult.fail("Failed to move war into active phase: " + displayService.label(server, war.get()));
        }
        save(server);
        return WarOperationResult.ok("Preparation skipped: " + displayService.label(server, war.get()));
    }

    public List<WarMarkerDto> visibleMarkersFor(ServerPlayer player, int radiusChunks) {
        ensureLoaded(player.server);
        int radius = Math.max(0, Math.min(MAX_NEAR_RADIUS_CHUNKS, radiusChunks));
        return activeWars()
                .filter(war -> isVisibleTo(player, war, radius))
                .map(war -> markerFor(player, war, radius))
                .toList();
    }

    public void onPlayerDeath(ServerPlayer player) {
        MinecraftServer server = player.server;
        ensureLoaded(server);
        Optional<WarData> war = activeWars()
                .filter(value -> value.state() == WarState.ACTIVE)
                .filter(value -> coreServices.partyService().isSameSide(player, value.attackerSide())
                        || coreServices.partyService().isSameSide(player, value.defenderSide()))
                .findFirst();
        if (war.isEmpty()) {
            return;
        }

        Optional<Integer> remainingLives = livesService.decrementForDeath(player, war.get());
        if (remainingLives.isEmpty()) {
            return;
        }

        coreServices.warLogService().log(server, "LIFE_LOST " + describe(war.get())
                + " player=" + player.getGameProfile().getName()
                + " remaining=" + remainingLives.get());
        notificationService.notifyLifeLost(server, war.get(), player, remainingLives.get());
        scoreboardService.update(server, activeWars().toList());
        save(server);
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

        hudService.update(server, activeWars().toList());
        scoreboardService.update(server, activeWars().toList());

        if (changed) {
            save(server);
        }
    }

    private boolean tickWar(MinecraftServer server, WarData war, Instant now) {
        if (war.state() == WarState.PREPARING) {
            long seconds = Duration.between(war.createdAt(), now).getSeconds();
            long remaining = Math.max(0L, WarConfig.PREPARATION_SECONDS.get() - seconds);
            maybeSendPreparationWarning(server, war, remaining);
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

        CaptureTickResult capture = captureProgressService.nextProgress(server, level.get(), war, afkTracker);
        war.setProgress(capture.progress());
        war.setCaptureSnapshot(capture.deltaPerSecond(), capture.attackersPresent(), capture.defendersPresent());
        maybeNotifyEmptyDecay(server, war, capture, now);
        notifyProgressMilestones(server, war);
        coreServices.warLogService().log(server, "PROGRESS " + war.id()
                + " " + Math.round(war.progress())
                + " delta=" + capture.deltaPerSecond()
                + " attackers=" + capture.attackersPresent()
                + " defenders=" + capture.defendersPresent());

        if (war.progress() >= 100.0D) {
            boolean transferred = coreServices.territoryService().transferClaimToSide(level.get(), war.targetChunk().toChunkPos(), war.attackerSide());
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

    private boolean activate(MinecraftServer server, WarData war, Instant now) {
        Optional<ServerLevel> level = resolveLevel(server, war.targetChunk());
        if (level.isEmpty()) {
            finish(server, war, WarState.FAILED, "level_missing");
            return false;
        }
        if (!setupContestedClaim(level.get(), war)) {
            finish(server, war, WarState.FAILED, "contested_claim_transfer_failed");
            return false;
        }

        war.setState(WarState.ACTIVE);
        war.setActiveAt(now);
        war.setProgress(Math.max(war.progress(), WarConfig.STARTING_PROGRESS.get()));
        livesService.initializeIfNeeded(server, war);
        coreServices.territoryStateService().markContested(war.targetChunk(), war.attackerSide(), war.defenderSide());
        coreServices.warLogService().log(server, "ACTIVE " + describe(war));
        notificationService.notifyActive(server, war);
        notifyProgressMilestones(server, war);
        return true;
    }

    private void finish(MinecraftServer server, WarData war, WarState state, String reason) {
        if (state != WarState.FINISHED) {
            resolveLevel(server, war.targetChunk()).ifPresent(level -> restoreOriginalClaim(level, war));
        }
        war.setState(state);
        war.setEndedAt(Instant.now());
        war.setEndReason(reason);
        coreServices.territoryStateService().clearStatus(war.targetChunk());
        postWarProtectionService.protect(war.targetChunk());
        hudService.remove(server, war);
        scoreboardService.update(server, activeWars().toList());
        coreServices.warLogService().log(server, state.name() + " " + describe(war) + " reason=" + reason);
        notificationService.notifyEnded(server, war);
        save(server);
    }

    private boolean hasOnlineNonAfkDefender(MinecraftServer server, ClaimSideId defenderSide) {
        return coreServices.partyService().onlineSideMembers(server, defenderSide).stream()
                .anyMatch(player -> !afkTracker.isAfk(player));
    }

    private long activeWarsBySide(ClaimSideId side) {
        return wars.values().stream()
                .filter(war -> !war.isTerminal())
                .filter(war -> war.attackerSide().equals(side) || war.defenderSide().equals(side))
                .count();
    }

    private Optional<WarData> findByChunk(ChunkPosKey key) {
        return wars.values().stream()
                .filter(war -> !war.isTerminal())
                .filter(war -> war.targetChunk().equals(key))
                .findFirst();
    }

    private java.util.stream.Stream<WarData> activeWars() {
        return wars.values().stream().filter(war -> !war.isTerminal());
    }

    private java.util.stream.Stream<WarData> visibleWarsFor(ServerPlayer player, int radiusChunks) {
        return activeWars()
                .filter(war -> isVisibleTo(player, war, radiusChunks))
                .sorted(Comparator.comparing(WarData::createdAt));
    }

    private boolean isVisibleTo(ServerPlayer player, WarData war, int radiusChunks) {
        return viewerRelation(player, war, radiusChunks).isPresent();
    }

    private Optional<String> viewerRelation(ServerPlayer player, WarData war, int radiusChunks) {
        if (coreServices.permissionService().hasPermission(player, dev.customclaims.core.permissions.CustomClaimsPermissions.WAR_ADMIN)) {
            return Optional.of("admin");
        }
        if (coreServices.partyService().isSameSide(player, war.attackerSide())) {
            return Optional.of("attacker");
        }
        if (coreServices.partyService().isSameSide(player, war.defenderSide())) {
            return Optional.of("defender");
        }
        if (isNear(player, war, radiusChunks)) {
            return Optional.of("nearby");
        }
        return Optional.empty();
    }

    private boolean isNear(ServerPlayer player, WarData war, int radiusChunks) {
        if (!player.level().dimension().location().toString().equals(war.targetChunk().levelId())) {
            return false;
        }
        ChunkPos playerChunk = player.chunkPosition();
        ChunkPos target = war.targetChunk().toChunkPos();
        int distance = Math.max(Math.abs(playerChunk.x - target.x), Math.abs(playerChunk.z - target.z));
        return distance <= radiusChunks;
    }

    private WarMarkerDto markerFor(ServerPlayer player, WarData war, int radiusChunks) {
        String attackerName = displayService.sideName(player.server, war.attackerSide());
        String defenderName = displayService.sideName(player.server, war.defenderSide());
        String relation = viewerRelation(player, war, radiusChunks).orElse("hidden");
        return new WarMarkerDto(
                markerLabel(player.server, war, relation),
                waypointName(player.server, war, relation),
                displayService.stateName(war.state()),
                war.targetChunk().levelId(),
                war.targetChunk().x(),
                war.targetChunk().z(),
                attackerName,
                defenderName,
                war.progress(),
                war.lastDeltaPerSecond(),
                war.lastAttackersPresent(),
                war.lastDefendersPresent(),
                relation
        );
    }

    private String markerLabel(MinecraftServer server, WarData war, String relation) {
        return switch (relation) {
            case "attacker" -> "Attack target: " + displayService.label(server, war);
            case "defender" -> "Defend target: " + displayService.label(server, war);
            case "admin" -> "War target: " + displayService.label(server, war);
            default -> displayService.label(server, war);
        };
    }

    private String waypointName(MinecraftServer server, WarData war, String relation) {
        String matchup = displayService.matchupName(server, war);
        return switch (relation) {
            case "attacker" -> "Attack: " + matchup;
            case "defender" -> "Defend: " + matchup;
            default -> "War: " + matchup;
        };
    }

    private String formatWarList(MinecraftServer server, Collection<WarData> wars, Instant now) {
        return wars.stream()
                .sorted(Comparator.comparing(WarData::createdAt))
                .map(war -> displayService.formatWarListEntry(server, war, now))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("No active or preparing wars.");
    }

    private void ensureLoaded(MinecraftServer server) {
        if (loaded) {
            return;
        }
        warStorage.load(server).forEach(war -> wars.put(war.id(), war));
        boolean changed = false;
        for (WarData war : wars.values()) {
            if (war.state() == WarState.ACTIVE) {
                coreServices.territoryStateService().markContested(war.targetChunk(), war.attackerSide(), war.defenderSide());
                resolveLevel(server, war.targetChunk()).ifPresent(level -> ensureContestedClaimOwner(level, war));
                changed |= livesService.initializeIfNeeded(server, war);
            }
        }
        loaded = true;
        if (changed) {
            save(server);
        }
    }

    private void save(MinecraftServer server) {
        warStorage.save(server, wars.values());
    }

    private Optional<ServerLevel> resolveLevel(MinecraftServer server, ChunkPosKey key) {
        ResourceLocation location = ResourceLocation.parse(key.levelId());
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, location);
        return Optional.ofNullable(server.getLevel(dimension));
    }

    private boolean setupContestedClaim(ServerLevel level, WarData war) {
        Optional<ClaimSnapshot> currentSnapshot = coreServices.territoryService()
                .getClaimSnapshot(level, war.targetChunk().toChunkPos());
        if (currentSnapshot.isEmpty()) {
            return false;
        }
        if (war.originalClaimSnapshot().isEmpty()) {
            war.setOriginalClaimSnapshot(currentSnapshot.get());
        }
        ClaimSnapshot original = war.originalClaimSnapshot().orElse(currentSnapshot.get());
        UUID contestedOwner = contestedOwnerUuid();
        boolean claimed = coreServices.territoryService().claimForPlayer(
                level,
                war.targetChunk().toChunkPos(),
                contestedOwner,
                original.subConfigIndex(),
                original.forceload()
        );
        return claimed && isClaimOwnedBy(level, war, contestedOwner);
    }

    private void ensureContestedClaimOwner(ServerLevel level, WarData war) {
        if (isClaimOwnedBy(level, war, contestedOwnerUuid())) {
            return;
        }
        war.originalClaimSnapshot().ifPresent(snapshot -> coreServices.territoryService().claimForPlayer(
                level,
                war.targetChunk().toChunkPos(),
                contestedOwnerUuid(),
                snapshot.subConfigIndex(),
                snapshot.forceload()
        ));
    }

    private void restoreOriginalClaim(ServerLevel level, WarData war) {
        war.originalClaimSnapshot().ifPresent(snapshot -> coreServices.territoryService().claimForPlayer(
                level,
                war.targetChunk().toChunkPos(),
                snapshot.ownerId(),
                snapshot.subConfigIndex(),
                snapshot.forceload()
        ));
    }

    private boolean isClaimOwnedBy(ServerLevel level, WarData war, UUID ownerId) {
        return coreServices.territoryService().getClaimSnapshot(level, war.targetChunk().toChunkPos())
                .map(snapshot -> snapshot.ownerId().equals(ownerId))
                .orElse(false);
    }

    private UUID contestedOwnerUuid() {
        try {
            return UUID.fromString(WarConfig.CONTESTED_OWNER_UUID.get());
        } catch (IllegalArgumentException exception) {
            return UUID.fromString("00000000-0000-0000-0000-00000000cc01");
        }
    }

    private void maybeSendPreparationWarning(MinecraftServer server, WarData war, long remainingSeconds) {
        int preparationSeconds = WarConfig.PREPARATION_SECONDS.get();
        if (preparationSeconds >= 60 && remainingSeconds <= 60 && !war.preparationWarning60Sent()) {
            war.setPreparationWarning60Sent();
            notificationService.notifyPreparationWarning(server, war, 60);
        }
        if (preparationSeconds >= 10 && remainingSeconds <= 10 && !war.preparationWarning10Sent()) {
            war.setPreparationWarning10Sent();
            notificationService.notifyPreparationWarning(server, war, 10);
        }
    }

    private void maybeNotifyEmptyDecay(MinecraftServer server, WarData war, CaptureTickResult capture, Instant now) {
        if (capture.attackersPresent() != 0 || capture.defendersPresent() != 0 || capture.deltaPerSecond() >= 0.0D) {
            return;
        }
        boolean canNotify = war.lastEmptyDecayNotificationAt()
                .map(last -> Duration.between(last, now).getSeconds() >= 30)
                .orElse(true);
        if (canNotify) {
            war.setLastEmptyDecayNotificationAt(now);
            notificationService.notifyEmptyDecay(server, war);
        }
    }

    private void notifyProgressMilestones(MinecraftServer server, WarData war) {
        int[] milestones = {25, 50, 75, 90, 100};
        for (int milestone : milestones) {
            if (milestone > war.highestNotifiedMilestone() && war.progress() >= milestone) {
                war.setHighestNotifiedMilestone(milestone);
                notificationService.notifyProgressMilestone(server, war, milestone);
            }
        }
    }

    private String describe(WarData war) {
        return war.id()
                + " attacker=" + war.attackerSide()
                + " defender=" + war.defenderSide()
                + " chunk=" + war.targetChunk().storageKey()
                + " progress=" + Math.round(war.progress());
    }
}
