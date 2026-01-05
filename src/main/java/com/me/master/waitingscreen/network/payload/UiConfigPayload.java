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
        TextPosition escTextPos,
        int playerCurrentColor,
        int playerRequiredColor
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
                new TextPosition(buf.readVarInt(), buf.readVarInt()),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    private void write(RegistryByteBuf buf) {
        buf.writeString(waitingText);
        buf.writeVarInt(waitingTextColor);
        buf.writeFloat(waitingTextScale);

        buf.writeVarInt(waitingTextPos.x());
        buf.writeVarInt(waitingTextPos.y());

        buf.writeVarInt(playerCountPos.x());
        buf.writeVarInt(playerCountPos.y());

        buf.writeVarInt(missingTextPos.x());
        buf.writeVarInt(missingTextPos.y());

        buf.writeVarInt(escTextPos.x());
        buf.writeVarInt(escTextPos.y());

        buf.writeVarInt(playerCurrentColor);
        buf.writeVarInt(playerRequiredColor);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record TextPosition(int x, int y) { }
}
