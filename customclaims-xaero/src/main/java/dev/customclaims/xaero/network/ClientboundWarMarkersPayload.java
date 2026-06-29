package dev.customclaims.xaero.network;

import dev.customclaims.war.model.WarMarkerDto;
import dev.customclaims.xaero.CustomClaimsXaeroMod;
import dev.customclaims.xaero.client.WarClientPacketHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ClientboundWarMarkersPayload(List<WarMarkerDto> markers) implements CustomPacketPayload {
    public static final Type<ClientboundWarMarkersPayload> TYPE = new Type<>(
            ResourceLocation.parse(CustomClaimsXaeroMod.MOD_ID + ":war_markers")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWarMarkersPayload> STREAM_CODEC =
            StreamCodec.ofMember(ClientboundWarMarkersPayload::write, ClientboundWarMarkersPayload::read);

    public ClientboundWarMarkersPayload {
        markers = List.copyOf(markers);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(markers.size());
        for (WarMarkerDto marker : markers) {
            writeMarker(buffer, marker);
        }
    }

    public static ClientboundWarMarkersPayload read(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<WarMarkerDto> markers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            markers.add(readMarker(buffer));
        }
        return new ClientboundWarMarkersPayload(markers);
    }

    public static void handle(ClientboundWarMarkersPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> WarClientPacketHandler.handle(payload));
    }

    private static void writeMarker(RegistryFriendlyByteBuf buffer, WarMarkerDto marker) {
        buffer.writeUtf(marker.label());
        buffer.writeUtf(marker.waypointName());
        buffer.writeUtf(marker.state());
        buffer.writeUtf(marker.dimension());
        buffer.writeVarInt(marker.chunkX());
        buffer.writeVarInt(marker.chunkZ());
        buffer.writeUtf(marker.attackerName());
        buffer.writeUtf(marker.defenderName());
        buffer.writeDouble(marker.progress());
        buffer.writeDouble(marker.deltaPerSecond());
        buffer.writeVarInt(marker.attackerCount());
        buffer.writeVarInt(marker.defenderCount());
        buffer.writeUtf(marker.viewerRelation());
    }

    private static WarMarkerDto readMarker(RegistryFriendlyByteBuf buffer) {
        return new WarMarkerDto(
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readUtf()
        );
    }
}
