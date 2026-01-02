package com.me.master.waitingscreen.network.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record MissingNamesPayload(List<String> names, int more) implements CustomPayload {
    public static final CustomPayload.Id<MissingNamesPayload> ID =
            new CustomPayload.Id<>(Identifier.of("waitingscreen", "missing_names"));

    public static final PacketCodec<RegistryByteBuf, MissingNamesPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING), MissingNamesPayload::names,
                    PacketCodecs.VAR_INT, MissingNamesPayload::more,
                    MissingNamesPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
