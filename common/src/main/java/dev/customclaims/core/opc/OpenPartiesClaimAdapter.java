package dev.customclaims.core.opc;

import dev.customclaims.core.api.ClaimAdapter;
import dev.customclaims.core.api.PartyAdapter;
import dev.customclaims.core.api.model.ClaimSnapshot;
import dev.customclaims.core.api.model.ClaimSideId;
import dev.customclaims.core.api.model.PartyDisplayInfo;
import dev.customclaims.core.api.model.PartyId;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelResource;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.claims.api.IServerClaimsManagerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;
import xaero.pac.common.server.player.config.api.v2.PlayerConfigOptions;

public final class OpenPartiesClaimAdapter implements ClaimAdapter, PartyAdapter {
    private static final UUID SERVER_CLAIM_OWNER_ID = new UUID(0L, 0L);

    @Override
    public String name() {
        return "open_parties_and_claims";
    }

    @Override
    public UUID serverClaimOwnerId() {
        return SERVER_CLAIM_OWNER_ID;
    }

    @Override
    public Optional<PartyId> getClaimOwner(ServerLevel level, ChunkPos chunkPos) {
        return getClaimSideOwner(level, chunkPos).flatMap(ClaimSideId::partyId);
    }

    @Override
    public Optional<ClaimSideId> getClaimSideOwner(ServerLevel level, ChunkPos chunkPos) {
        return getClaimSideOwner(api(level.getServer()), level, chunkPos);
    }

    @Override
    public boolean transferClaim(ServerLevel level, ChunkPos chunkPos, PartyId newOwner) {
        return transferClaimToSide(level, chunkPos, ClaimSideId.party(newOwner));
    }

    @Override
    public boolean transferClaimToSide(ServerLevel level, ChunkPos chunkPos, ClaimSideId newOwner) {
        if (newOwner.isPlayer()) {
            UUID ownerUuid = newOwner.playerUuid().orElse(null);
            if (ownerUuid == null) {
                return false;
            }

            //? if <26.0 {
            IPlayerChunkClaimAPI oldClaim = api(level.getServer()).getServerClaimsManager()
                    .get(level.dimension().location(), chunkPos);
            //?} else {
            /*IPlayerChunkClaimAPI oldClaim = api(level.getServer()).getServerClaimsManager()
                    .get(level.dimension().identifier(), chunkPos);
            *///?}
            int subConfigIndex = oldClaim == null ? 0 : oldClaim.getSubConfigIndex();
            boolean forceload = oldClaim != null && oldClaim.isForceloadable();
            return claimFromServer(level, chunkPos, ownerUuid, subConfigIndex, forceload)
                    && getClaimSnapshot(level, chunkPos)
                    .map(snapshot -> snapshot.ownerId().equals(ownerUuid))
                    .orElse(false);
        }

        OpenPACServerAPI api = api(level.getServer());

        Optional<UUID> partyUuid = newOwner.partyId().flatMap(this::parsePartyUuid);
        if (partyUuid.isEmpty()) {
            return false;
        }

        IServerPartyAPI party = api.getPartyManager().getPartyById(partyUuid.get());
        if (party == null) {
            return false;
        }

        UUID ownerUuid = party.getOwner().getUUID();
        IServerClaimsManagerAPI claimsManager = api.getServerClaimsManager();
        //? if <26.0 {
        IPlayerChunkClaimAPI oldClaim = claimsManager.get(level.dimension().location(), chunkPos);
        //?} else {
        /*IPlayerChunkClaimAPI oldClaim = claimsManager.get(level.dimension().identifier(), chunkPos);
        *///?}
        int subConfigIndex = oldClaim == null ? 0 : oldClaim.getSubConfigIndex();
        boolean forceload = oldClaim != null && oldClaim.isForceloadable();
        return claimFromServer(level, chunkPos, ownerUuid, subConfigIndex, forceload)
                && getClaimSideOwner(level, chunkPos).filter(newOwner::equals).isPresent();
    }

    @Override
    public Optional<ClaimSnapshot> getClaimSnapshot(ServerLevel level, ChunkPos chunkPos) {
        OpenPACServerAPI api = api(level.getServer());
        //? if <26.0 {
        IPlayerChunkClaimAPI claim = api.getServerClaimsManager().get(level.dimension().location(), chunkPos);
        //?} else {
        /*IPlayerChunkClaimAPI claim = api.getServerClaimsManager().get(level.dimension().identifier(), chunkPos);
        *///?}
        if (claim == null) {
            return Optional.empty();
        }
        var playerInfo = api.getServerClaimsManager().getPlayerInfo(claim.getPlayerId());
        return Optional.of(new ClaimSnapshot(
                claim.getPlayerId(),
                playerInfo != null && playerInfo.isPartyOwned(),
                claim.getSubConfigIndex(),
                claim.isForceloadable()
        ));
    }

    @Override
    public boolean claimFromServer(
            ServerLevel level,
            ChunkPos chunkPos,
            UUID ownerId,
            int subConfigIndex,
            boolean forceload
    ) {
        OpenPACServerAPI api = api(level.getServer());
        // claim(...) is OPaC's administrative mutation path. Do not replace this
        // with tryToClaim(...): that is the player request path and applies player
        // limits plus economy integrations intended for manually purchased claims.
        //? if <26.0 {
        api.getServerClaimsManager().claim(
                level.dimension().location(),
                ownerId,
                subConfigIndex,
                chunkPos.x,
                chunkPos.z,
                forceload
        );
        //?} else {
        /*api.getServerClaimsManager().claim(
                level.dimension().identifier(),
                ownerId,
                subConfigIndex,
                chunkPos.x(),
                chunkPos.z(),
                forceload
        );
        *///?}
        return getClaimSnapshot(level, chunkPos)
                .map(snapshot -> snapshot.ownerId().equals(ownerId))
                .orElse(false);
    }

    @Override
    public Optional<PartyId> getPlayerParty(ServerPlayer player) {
        IServerPartyAPI party = api(player.level().getServer()).getPartyManager().getPartyByMember(player.getUUID());
        return Optional.ofNullable(party).map(value -> PartyId.of(value.getId().toString()));
    }

    @Override
    public boolean isPlayerInParty(ServerPlayer player, PartyId partyId) {
        Optional<UUID> partyUuid = parsePartyUuid(partyId);
        if (partyUuid.isEmpty()) {
            return false;
        }

        IServerPartyAPI party = api(player.level().getServer()).getPartyManager().getPartyById(partyUuid.get());
        return party != null && party.getMemberInfo(player.getUUID()) != null;
    }

    @Override
    public Collection<ServerPlayer> getOnlinePartyMembers(MinecraftServer server, PartyId partyId) {
        Optional<UUID> partyUuid = parsePartyUuid(partyId);
        if (partyUuid.isEmpty()) {
            return java.util.List.of();
        }

        IServerPartyAPI party = api(server).getPartyManager().getPartyById(partyUuid.get());
        if (party == null) {
            return java.util.List.of();
        }
        return party.getOnlineMemberStream().toList();
    }

    @Override
    public Collection<UUID> getPartyMemberIds(MinecraftServer server, PartyId partyId) {
        Optional<UUID> partyUuid = parsePartyUuid(partyId);
        if (partyUuid.isEmpty()) {
            return java.util.List.of();
        }

        IServerPartyAPI party = api(server).getPartyManager().getPartyById(partyUuid.get());
        if (party == null) {
            return java.util.List.of();
        }

        Set<UUID> memberIds = new LinkedHashSet<>();
        memberIds.add(party.getOwner().getUUID());
        party.getMemberInfoStream().map(member -> member.getUUID()).forEach(memberIds::add);
        return java.util.List.copyOf(memberIds);
    }

    @Override
    public Optional<PartyDisplayInfo> describeParty(MinecraftServer server, PartyId partyId) {
        Optional<UUID> partyUuid = parsePartyUuid(partyId);
        if (partyUuid.isEmpty()) {
            return Optional.empty();
        }

        IServerPartyAPI party = api(server).getPartyManager().getPartyById(partyUuid.get());
        if (party == null) {
            return Optional.empty();
        }

        String name = party.getDefaultName();
        String ownerName = party.getOwner().getUsername();
        int onlineCount = (int) party.getOnlineMemberStream().count();
        return Optional.of(new PartyDisplayInfo(
                partyId,
                name,
                ownerName,
                onlineCount,
                party.getMemberCount()
        ));
    }

    private Optional<ClaimSideId> getClaimSideOwner(OpenPACServerAPI api, ServerLevel level, ChunkPos chunkPos) {
        //? if <26.0 {
        IPlayerChunkClaimAPI claim = api.getServerClaimsManager().get(level.dimension().location(), chunkPos);
        //?} else {
        /*IPlayerChunkClaimAPI claim = api.getServerClaimsManager().get(level.dimension().identifier(), chunkPos);
        *///?}
        if (claim == null) {
            return Optional.empty();
        }

        IServerPartyAPI party;
        var playerInfo = api.getServerClaimsManager().getPlayerInfo(claim.getPlayerId());
        if (playerInfo != null && playerInfo.isPartyOwned()) {
            party = api.getPartyManager().getPartyByOwner(claim.getPlayerId());
        } else {
            party = api.getPartyManager().getPartyByMember(claim.getPlayerId());
        }

        if (party != null) {
            return Optional.of(ClaimSideId.party(PartyId.of(party.getId().toString())));
        }
        return Optional.of(ClaimSideId.player(claim.getPlayerId()));
    }

    private static OpenPACServerAPI api(MinecraftServer server) {
        Objects.requireNonNull(server, "server");
        OpenPACServerAPI api = OpenPACServerAPI.get(server);
        if (api == null) {
            throw new IllegalStateException("OpenPartiesAndClaims API is not available");
        }
        return api;
    }

    private Optional<UUID> parsePartyUuid(PartyId partyId) {
        try {
            return Optional.of(UUID.fromString(partyId.value()));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    @Override
    public void initializeContestedClaimOwner(MinecraftServer server, UUID ownerId, String name, int color) {
        Path configsDir = server.getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve("openpartiesandclaims")
                .resolve("player-configs");
        Path configFile = configsDir.resolve(ownerId + ".toml");

        boolean fileExisted = Files.exists(configFile);
        if (!fileExisted) {
            try {
                Files.createDirectories(configsDir);
                String toml = generateContestedPlayerConfig(name, color);
                Files.writeString(configFile, toml, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException exception) {
                // If writing fails, continue
            }
        }

        try {
            OpenPACServerAPI api = OpenPACServerAPI.get(server);
            if (api != null && api.getPlayerConfigManager() != null) {
                var configManager = api.getPlayerConfigManager();
                var config = configManager.getLoadedConfig(ownerId);
                if (config != null && !fileExisted) {
                    config.tryToSet(PlayerConfigOptions.PROTECT_CLAIMED_CHUNKS, false);
                    config.tryToSet(PlayerConfigOptions.CLAIMS_NAME, name);
                    config.tryToSet(PlayerConfigOptions.CLAIMS_COLOR, color);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private static String generateContestedPlayerConfig(String name, int color) {
        String safeName = name == null ? "Contested Territory" : name.replace("\\", "\\\\").replace("\"", "\\\"");
        return "playerConfig = {customPlayerGroups = [], bonusPlayerGroups = 0, bonusPlayerGroupSpace = 0, lastPermissionValues = [], claims = {usedSub = \"main\", usedServerSub = \"main\", usedPartySub = \"main\", name = \""
                + safeName
                + "\", color = "
                + color
                + ", protectClaimedChunks = false, bonusChunkClaims = 0, bonusChunkForceloads = 0, bonusAnchors = 0, bonusAnchorRange = 0, wholePartyCanClaim = false, allowedToClaim = true, allowedToAddForceloads = true, protection = {fluidBarrier = true, pistonBarrier = true, neighborChunksItemUse = true, dispenserBarrier = true, playerDeathLoot = \"N\", overrideMobGriefingRule = true, exceptions = {fullAccess = \"P\", blocksByPlayers = \"N\", blocksByMobs = \"N\", blocksByOther = \"N\", blocksRedirect = true, blocksByExplosions = false, fireSpread = false, blocksByEnchantments = \"N\", cropTrample = false, buttonsByProjectiles = \"N\", targetsByProjectiles = \"N\", platesByPlayers = \"N\", platesByMobs = \"N\", platesByOther = \"N\", tripwireByPlayers = \"N\", tripwireByMobs = \"N\", tripwireByOther = \"N\", entitiesByPlayers = \"N\", entitiesByMobs = \"N\", entitiesByOther = \"N\", entitiesRedirect = true, entitiesByExplosions = false, entitiesByFire = false, raids = false, playersByPlayers = true, playersByMobs = true, playersByOther = true, playersRedirect = true, playerLightning = \"N\", chorusFruitTeleport = \"N\", netherPortalsPlayers = \"N\", netherPortalsMobs = \"N\", netherPortalsOther = \"N\", itemUse = \"N\", itemTossPlayers = \"E\", itemTossMobs = \"E\", itemTossOther = \"E\", itemTossRedirect = true, mobLoot = \"E\", itemPickupPlayers = \"E\", itemPickupMobs = \"E\", itemPickupRedirect = false, xpPickup = \"E\", naturalSpawnHostile = true, naturalSpawnFriendly = true, spawnersHostile = true, spawnersFriendly = true, projectileHitHostileSpawn = \"N\", projectileHitFriendlySpawn = \"N\", reclaimable = \"N\", canClaimNextTo = \"N\", groups = {entity = {barrier = {Players = \"E\", Ender_Pearls = \"E\"}, interact = {Traders = \"N\", Armor_Stands = \"N\", Players = \"N\"}, handInteract = {Item_Frames = \"N\"}, break = {Livestock = \"N\"}, blockAccess = {Villagers = \"N\"}, entityAccess = {Zombies = \"N\"}, playerAccess = {Zombies = \"N\"}, droppedItemAccess = {Villagers = \"N\", Piglins = \"N\", Foxes = \"N\"}}, block = {interact = {Controls = \"N\", Doors = \"N\", Chests = \"N\", Barrels = \"N\", Ender_Chests = \"N\", Shulker_Boxes = \"N\", Furnaces = \"N\", Hoppers = \"N\", Dispenser-like = \"N\", Anvils = \"N\", Stonecutters = \"N\", Grindstones = \"N\", Cartography_Tables = \"N\", Lecterns = \"N\", Smithing_Tables = \"N\", Looms = \"N\", Jukeboxes = \"N\", Beds = \"N\", Beacons = \"N\", Enchanting_Tables = \"N\"}, break = {Crops = \"N\"}}, item = {interact = {Books = \"N\"}}}}}, forceload = {enabled = true, offlineForceload = false}}, parties = {name = \"\", shareLocationWithParty = true, shareLocationWithMutualAllyParties = false, receiveLocationsFromParty = true, receiveLocationsFromMutualAllyParties = false}, version = 18}\n";
    }
}
