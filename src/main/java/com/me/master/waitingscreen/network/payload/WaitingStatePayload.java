package com.me.master.waitingscreen.network.payload;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WaitingStatePayload(boolean isWaiting, int currentPlayers, int requiredPlayers, String screenName, boolean allowEscMenu) 
    implements CustomPayload {
    
    public static final CustomPayload.Id<WaitingStatePayload> ID = 
        new CustomPayload.Id<>(Identifier.of(Waitingscreen.MOD_ID, "waiting_state"));
    
    public static final PacketCodec<RegistryByteBuf, WaitingStatePayload> CODEC = PacketCodec.tuple(
        PacketCodecs.BOOL, WaitingStatePayload::isWaiting,
        PacketCodecs.VAR_INT, WaitingStatePayload::currentPlayers,
        PacketCodecs.VAR_INT, WaitingStatePayload::requiredPlayers,
        PacketCodecs.STRING, WaitingStatePayload::screenName,
        PacketCodecs.BOOL, WaitingStatePayload::allowEscMenu,
        WaitingStatePayload::new
    );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
