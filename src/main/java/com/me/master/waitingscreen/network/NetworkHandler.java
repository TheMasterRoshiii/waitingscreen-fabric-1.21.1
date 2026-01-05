package com.me.master.waitingscreen.network;

import com.me.master.waitingscreen.network.payload.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Objects;

public class NetworkHandler {

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(WaitingStatePayload.ID, WaitingStatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ImageDataPayload.ID, ImageDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ScreenChangePayload.ID, ScreenChangePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(VideoScreenPayload.ID, VideoScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MissingNamesPayload.ID, MissingNamesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UiConfigPayload.ID, UiConfigPayload.CODEC);
    }

    public static void sendWaitingState(ServerPlayerEntity player,
                                        boolean waiting, int current, int required,
                                        String screenName, boolean allowEsc, boolean exempt) {
        ServerPlayNetworking.send(player, new WaitingStatePayload(waiting, current, required, screenName, allowEsc, exempt));
    }


    public static void broadcastWaitingState(MinecraftServer server,
                                             boolean waiting, int current, int required,
                                             String screenName, boolean allowEsc,
                                             java.util.function.Function<ServerPlayerEntity, Boolean> exemptResolver) {

        Objects.requireNonNull(exemptResolver, "exemptResolver");

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            boolean exempt = Boolean.TRUE.equals(exemptResolver.apply(player));
            ServerPlayNetworking.send(player, new WaitingStatePayload(waiting, current, required, screenName, allowEsc, exempt));
        }
    }


    public static void sendImageData(ServerPlayerEntity player, String screenName, byte[] imageData) {
        ServerPlayNetworking.send(player, new ImageDataPayload(screenName, imageData));
    }

    public static void broadcastImageData(MinecraftServer server, String screenName, byte[] imageData) {
        ImageDataPayload payload = new ImageDataPayload(screenName, imageData);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void broadcastScreenChange(MinecraftServer server, String screenName) {
        ScreenChangePayload payload = new ScreenChangePayload(screenName);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void broadcastMissingNames(MinecraftServer server, List<String> names, int more) {
        MissingNamesPayload payload = new MissingNamesPayload(names, more);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendMissingNames(ServerPlayerEntity player, List<String> names, int more) {
        ServerPlayNetworking.send(player, new MissingNamesPayload(names, more));
    }

    public static void broadcastUiConfig(MinecraftServer server, String text, int color, float scale,
                                         int wtX, int wtY, int pcX, int pcY,
                                         int mtX, int mtY, int etX, int etY,
                                         int playerCurrentColor, int playerRequiredColor) {
        UiConfigPayload payload = new UiConfigPayload(
                text, color, scale,
                new UiConfigPayload.TextPosition(wtX, wtY),
                new UiConfigPayload.TextPosition(pcX, pcY),
                new UiConfigPayload.TextPosition(mtX, mtY),
                new UiConfigPayload.TextPosition(etX, etY),
                playerCurrentColor,
                playerRequiredColor
        );

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendUiConfig(ServerPlayerEntity player, String text, int color, float scale,
                                    int wtX, int wtY, int pcX, int pcY,
                                    int mtX, int mtY, int etX, int etY,
                                    int playerCurrentColor, int playerRequiredColor) {
        UiConfigPayload payload = new UiConfigPayload(
                text, color, scale,
                new UiConfigPayload.TextPosition(wtX, wtY),
                new UiConfigPayload.TextPosition(pcX, pcY),
                new UiConfigPayload.TextPosition(mtX, mtY),
                new UiConfigPayload.TextPosition(etX, etY),
                playerCurrentColor,
                playerRequiredColor
        );

        ServerPlayNetworking.send(player, payload);
    }
}
