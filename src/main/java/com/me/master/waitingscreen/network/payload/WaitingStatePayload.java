package com.me.master.waitingscreen.network.payload;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WaitingStatePayload(
        boolean waiting,
        int current,
        int required,
        String screenName,
        boolean allowEsc,
        boolean exempt
) implements CustomPayload {

    public static final Id<WaitingStatePayload> ID =
            new Id<>(Identifier.of(Waitingscreen.MOD_ID, "waiting_state"));

    public static final PacketCodec<RegistryByteBuf, WaitingStatePayload> CODEC =
            PacketCodec.of(WaitingStatePayload::write, WaitingStatePayload::new);

    public WaitingStatePayload(RegistryByteBuf buf) {
        this(
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readString(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }

    private void write(RegistryByteBuf buf) {
        buf.writeBoolean(waiting);
        buf.writeVarInt(current);
        buf.writeVarInt(required);
        buf.writeString(screenName);
        buf.writeBoolean(allowEsc);
        buf.writeBoolean(exempt);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
