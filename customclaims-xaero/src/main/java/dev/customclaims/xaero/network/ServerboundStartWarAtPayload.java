package dev.customclaims.xaero.network;

import dev.customclaims.core.CustomClaimsCoreMod;
import dev.customclaims.core.api.model.ChunkPosKey;
import dev.customclaims.core.permissions.CustomClaimsPermissions;
import dev.customclaims.war.CustomClaimsWarMod;
import dev.customclaims.war.service.WarOperationResult;
import dev.customclaims.xaero.CustomClaimsXaeroMod;
import dev.customclaims.xaero.config.XaeroCompatConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundStartWarAtPayload(String dimension, int chunkX, int chunkZ) implements CustomPacketPayload {
    public static final Type<ServerboundStartWarAtPayload> TYPE = new Type<>(
            ResourceLocation.parse(CustomClaimsXaeroMod.MOD_ID + ":start_war_at")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStartWarAtPayload> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundStartWarAtPayload::write, ServerboundStartWarAtPayload::read);

    public ServerboundStartWarAtPayload {
        dimension = dimension == null ? "" : dimension;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(dimension);
        buffer.writeVarInt(chunkX);
        buffer.writeVarInt(chunkZ);
    }

    public static ServerboundStartWarAtPayload read(RegistryFriendlyByteBuf buffer) {
        return new ServerboundStartWarAtPayload(
                buffer.readUtf(128),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    public static void handle(ServerboundStartWarAtPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            WarOperationResult result = validateAndStart(player, payload);
            player.sendSystemMessage(Component.literal(result.message()));
        });
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

        String playerDimension = player.level().dimension().location().toString();
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
            ResourceLocation.parse(dimension);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static boolean isWithinMapStartDistance(ServerPlayer player, int targetChunkX, int targetChunkZ) {
        int maxDistance = Math.max(0, Math.min(32, XaeroCompatConfig.MAP_WAR_START_MAX_DISTANCE_CHUNKS.get()));
        ChunkPos playerChunk = player.chunkPosition();
        long dx = Math.abs((long) playerChunk.x - targetChunkX);
        long dz = Math.abs((long) playerChunk.z - targetChunkZ);
        return Math.max(dx, dz) <= maxDistance;
    }
}
