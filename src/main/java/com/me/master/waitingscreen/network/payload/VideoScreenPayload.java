package com.me.master.waitingscreen.network.payload;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record VideoScreenPayload(String videoUrl) implements CustomPayload {
    
    public static final CustomPayload.Id<VideoScreenPayload> ID = 
        new CustomPayload.Id<>(Identifier.of(Waitingscreen.MOD_ID, "video_screen"));
    
    public static final PacketCodec<RegistryByteBuf, VideoScreenPayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, VideoScreenPayload::videoUrl,
        VideoScreenPayload::new
    );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
