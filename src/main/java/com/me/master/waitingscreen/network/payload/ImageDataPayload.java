package com.me.master.waitingscreen.network.payload;

import com.me.master.waitingscreen.Waitingscreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ImageDataPayload(String screenName, byte[] imageData) implements CustomPayload {
    
    public static final CustomPayload.Id<ImageDataPayload> ID = 
        new CustomPayload.Id<>(Identifier.of(Waitingscreen.MOD_ID, "image_data"));
    
    public static final PacketCodec<RegistryByteBuf, ImageDataPayload> CODEC = new PacketCodec<>() {
        @Override
        public ImageDataPayload decode(RegistryByteBuf buf) {
            String name = PacketCodecs.STRING.decode(buf);
            int length = buf.readInt();
            byte[] data = new byte[length];
            buf.readBytes(data);
            return new ImageDataPayload(name, data);
        }
        
        @Override
        public void encode(RegistryByteBuf buf, ImageDataPayload payload) {
            PacketCodecs.STRING.encode(buf, payload.screenName);
            buf.writeInt(payload.imageData.length);
            buf.writeBytes(payload.imageData);
        }
    };
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
