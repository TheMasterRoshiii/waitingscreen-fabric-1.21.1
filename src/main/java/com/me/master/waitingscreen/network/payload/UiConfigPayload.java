package com.me.master.waitingscreen.network.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UiConfigPayload(String waitingText, int waitingTextColor, float waitingTextScale) implements CustomPayload {
    public static final CustomPayload.Id<UiConfigPayload> ID =
            new CustomPayload.Id<>(Identifier.of("waitingscreen", "ui_config"));

    public static final PacketCodec<RegistryByteBuf, UiConfigPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, UiConfigPayload::waitingText,
                    PacketCodecs.INTEGER, UiConfigPayload::waitingTextColor,
                    PacketCodecs.FLOAT, UiConfigPayload::waitingTextScale,
                    UiConfigPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
