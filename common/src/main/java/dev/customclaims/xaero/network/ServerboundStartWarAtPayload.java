package dev.customclaims.xaero.network;

import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.war.CustomClaimsWarMod;
import dev.customclaims.war.service.WarOperationResult;
import dev.customclaims.xaero.CustomClaimsXaeroMod;
import dev.customclaims.xaero.config.XaeroCompatConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
//? if >=1.20.2 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} else {
/*import dev.customclaims.core.network.CustomPacketPayload;
*///?}
//? if <26.0 {
import net.minecraft.resources.ResourceLocation;
//?} else {
/*import net.minecraft.resources.Identifier;
*///?}
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

public record ServerboundStartWarAtPayload(String dimension, int chunkX, int chunkZ) implements CustomPacketPayload {
//? if >=1.20.2 {
    //? if <26.0 {
    public static final Type<ServerboundStartWarAtPayload> TYPE = new Type<>(
            ResourceLocation.parse(CustomClaimsXaeroMod.MOD_ID + ":start_war_at")
    );
    //?} else {
    /*public static final Type<ServerboundStartWarAtPayload> TYPE = new Type<>(
            Identifier.parse(CustomClaimsXaeroMod.MOD_ID + ":start_war_at")
    );
    *///?}

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStartWarAtPayload> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundStartWarAtPayload::write, ServerboundStartWarAtPayload::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        writeCommon(buffer);
    }

    public static ServerboundStartWarAtPayload read(RegistryFriendlyByteBuf buffer) {
        return readCommon(buffer);
    }
//?} else {
/*    public static final ResourceLocation ID = new ResourceLocation(CustomClaimsXaeroMod.MOD_ID, "start_war_at");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        writeCommon(buffer);
    }

    public static ServerboundStartWarAtPayload read(FriendlyByteBuf buffer) {
        return readCommon(buffer);
    }
*///?}

    public ServerboundStartWarAtPayload {
        dimension = dimension == null ? "" : dimension;
    }

    private void writeCommon(FriendlyByteBuf buffer) {
        buffer.writeUtf(dimension);
        buffer.writeVarInt(chunkX);
        buffer.writeVarInt(chunkZ);
    }

    private static ServerboundStartWarAtPayload readCommon(FriendlyByteBuf buffer) {
        return new ServerboundStartWarAtPayload(
                buffer.readUtf(128),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    public static void handle(ServerboundStartWarAtPayload payload, ServerPlayer player) {
        if (player == null) {
            return;
        }
        WarOperationResult result = validateAndStart(player, payload);
        player.sendSystemMessage(Component.literal(result.message()));
    }

    private static WarOperationResult validateAndStart(ServerPlayer player, ServerboundStartWarAtPayload payload) {
        if (!XaeroCompatConfig.MAP_WAR_START_ENABLED.get()) {
            return WarOperationResult.fail("Starting wars from the Xaero map is disabled on this server.");
        }
        if (!CustomClaimsCoreMod.services().permissionService()
                .hasPermission(player, CustomClaimsPermissions.WAR_START)) {
            return WarOperationResult.fail("You do not have permission to start wars.");
        }
        if (!isValidDimension(payload.dimension())) {
            return WarOperationResult.fail("Invalid target dimension.");
        }

        //? if <26.0 {
        String playerDimension = player.level().dimension().location().toString();
        //?} else {
        /*String playerDimension = player.level().dimension().identifier().toString();
        *///?}
        if (!playerDimension.equals(payload.dimension())) {
            return WarOperationResult.fail("You can only start wars from the Xaero map in your current dimension.");
        }
        if (!isWithinMapStartDistance(player, payload.chunkX(), payload.chunkZ())) {
            return WarOperationResult.fail("Target chunk is too far away to start a war from the Xaero map.");
        }

        CustomClaimsWarMod.services().afkTracker().markActive(player);
        return CustomClaimsWarMod.services()
                .warManager()
                .startWarAt(player, new ChunkPosKey(payload.dimension(), payload.chunkX(), payload.chunkZ()));
    }

    private static boolean isValidDimension(String dimension) {
        try {
            //? if <26.0 {
            return ResourceLocation.tryParse(dimension) != null;
            //?} else {
            /*return Identifier.tryParse(dimension) != null;
            *///?}
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static boolean isWithinMapStartDistance(ServerPlayer player, int targetChunkX, int targetChunkZ) {
        int maxDistance = XaeroCompatConfig.MAP_WAR_START_MAX_DISTANCE_CHUNKS.get();
        if (maxDistance <= 0) {
            return true;
        }

        ChunkPos playerChunk = player.chunkPosition();
        //? if <26.0 {
        int deltaX = Math.abs(playerChunk.x - targetChunkX);
        int deltaZ = Math.abs(playerChunk.z - targetChunkZ);
        //?} else {
        /*int deltaX = Math.abs(playerChunk.x() - targetChunkX);
        int deltaZ = Math.abs(playerChunk.z() - targetChunkZ);
        *///?}
        return Math.max(deltaX, deltaZ) <= maxDistance;
    }
}
