package com.me.master.waitingscreen.network.payload;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ImageDataPayload(String screenName, int totalLength, int chunkIndex, int chunkCount, byte[] data) implements CustomPayload {

    public static final int CHUNK_SIZE = 512 * 1024;

    public static final CustomPayload.Id<ImageDataPayload> ID =
        new CustomPayload.Id<>(Identifier.of(Waitingscreen.MOD_ID, "image_data"));

    public static final PacketCodec<RegistryByteBuf, ImageDataPayload> CODEC = new PacketCodec<>() {
        @Override
        public ImageDataPayload decode(RegistryByteBuf buf) {
            String name = PacketCodecs.STRING.decode(buf);
            int total = buf.readVarInt();
            int index = buf.readVarInt();
            int count = buf.readVarInt();
            int length = buf.readVarInt();
            byte[] chunk = new byte[length];
            buf.readBytes(chunk);
            return new ImageDataPayload(name, total, index, count, chunk);
        }

        @Override
        public void encode(RegistryByteBuf buf, ImageDataPayload payload) {
            PacketCodecs.STRING.encode(buf, payload.screenName);
            buf.writeVarInt(payload.totalLength);
            buf.writeVarInt(payload.chunkIndex);
            buf.writeVarInt(payload.chunkCount);
            buf.writeVarInt(payload.data.length);
            buf.writeBytes(payload.data);
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
