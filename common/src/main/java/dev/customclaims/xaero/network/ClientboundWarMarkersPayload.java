package dev.customclaims.xaero.network;

import dev.customclaims.war.model.WarMarkerDto;
import dev.customclaims.xaero.CustomClaimsXaeroMod;
import dev.customclaims.xaero.client.WarClientPacketHandler;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
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

public record ClientboundWarMarkersPayload(List<WarMarkerDto> markers) implements CustomPacketPayload {
//? if >=1.20.2 {
    //? if <26.0 {
    public static final Type<ClientboundWarMarkersPayload> TYPE = new Type<>(
            ResourceLocation.parse(CustomClaimsXaeroMod.MOD_ID + ":war_markers")
    );
    //?} else {
    /*public static final Type<ClientboundWarMarkersPayload> TYPE = new Type<>(
            Identifier.parse(CustomClaimsXaeroMod.MOD_ID + ":war_markers")
    );
    *///?}

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWarMarkersPayload> STREAM_CODEC =
            StreamCodec.ofMember(ClientboundWarMarkersPayload::write, ClientboundWarMarkersPayload::read);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        writeCommon(buffer);
    }

    public static ClientboundWarMarkersPayload read(RegistryFriendlyByteBuf buffer) {
        return readCommon(buffer);
    }
//?} else {
/*    public static final ResourceLocation ID = new ResourceLocation(CustomClaimsXaeroMod.MOD_ID, "war_markers");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        writeCommon(buffer);
    }

    public static ClientboundWarMarkersPayload read(FriendlyByteBuf buffer) {
        return readCommon(buffer);
    }
*///?}

    public ClientboundWarMarkersPayload {
        markers = List.copyOf(markers);
    }

    private void writeCommon(FriendlyByteBuf buffer) {
        buffer.writeVarInt(markers.size());
        for (WarMarkerDto marker : markers) {
            writeMarker(buffer, marker);
        }
    }

    private static ClientboundWarMarkersPayload readCommon(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<WarMarkerDto> markers = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            markers.add(readMarker(buffer));
        }
        return new ClientboundWarMarkersPayload(markers);
    }

    public static void handle(ClientboundWarMarkersPayload payload) {
        WarClientPacketHandler.handle(payload);
    }

    private static void writeMarker(FriendlyByteBuf buffer, WarMarkerDto marker) {
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

    private static WarMarkerDto readMarker(FriendlyByteBuf buffer) {
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
