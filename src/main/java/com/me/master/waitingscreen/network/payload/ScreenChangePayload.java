package com.me.master.waitingscreen.network.payload;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ScreenChangePayload(String screenName) implements CustomPayload {
    
    public static final CustomPayload.Id<ScreenChangePayload> ID = 
        new CustomPayload.Id<>(Identifier.of(Waitingscreen.MOD_ID, "screen_change"));
    
    public static final PacketCodec<RegistryByteBuf, ScreenChangePayload> CODEC = PacketCodec.tuple(
        PacketCodecs.STRING, ScreenChangePayload::screenName,
        ScreenChangePayload::new
    );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
