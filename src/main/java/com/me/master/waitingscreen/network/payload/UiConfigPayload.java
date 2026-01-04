package com.me.master.waitingscreen.network.payload;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;



public record UiConfigPayload(
        String waitingText,
        int waitingTextColor,
        float waitingTextScale,
        TextPosition waitingTextPos,
        TextPosition playerCountPos,
        TextPosition missingTextPos,
        TextPosition escTextPos
) implements CustomPayload {

    public static final Id<UiConfigPayload> ID =
            new Id<>(Identifier.of(Waitingscreen.MOD_ID, "ui_config"));

    public static final PacketCodec<RegistryByteBuf, UiConfigPayload> CODEC =
            PacketCodec.of(UiConfigPayload::write, UiConfigPayload::new);

    public UiConfigPayload(RegistryByteBuf buf) {
        this(
                buf.readString(),
                buf.readVarInt(),
                buf.readFloat(),
                new TextPosition(buf.readVarInt(), buf.readVarInt()),
                new TextPosition(buf.readVarInt(), buf.readVarInt()),
                new TextPosition(buf.readVarInt(), buf.readVarInt()),
                new TextPosition(buf.readVarInt(), buf.readVarInt())
        );
    }

    private void write(RegistryByteBuf buf) {
        buf.writeString(this.waitingText);
        buf.writeVarInt(this.waitingTextColor);
        buf.writeFloat(this.waitingTextScale);

        buf.writeVarInt(this.waitingTextPos.x());
        buf.writeVarInt(this.waitingTextPos.y());

        buf.writeVarInt(this.playerCountPos.x());
        buf.writeVarInt(this.playerCountPos.y());

        buf.writeVarInt(this.missingTextPos.x());
        buf.writeVarInt(this.missingTextPos.y());

        buf.writeVarInt(this.escTextPos.x());
        buf.writeVarInt(this.escTextPos.y());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record TextPosition(int x, int y) { }
}
