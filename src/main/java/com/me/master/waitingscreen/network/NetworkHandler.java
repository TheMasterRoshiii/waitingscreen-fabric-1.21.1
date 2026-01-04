package com.me.master.waitingscreen.network;

import com.me.master.waitingscreen.network.payload.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class NetworkHandler {

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(WaitingStatePayload.ID, WaitingStatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ImageDataPayload.ID, ImageDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ScreenChangePayload.ID, ScreenChangePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(VideoScreenPayload.ID, VideoScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MissingNamesPayload.ID, MissingNamesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UiConfigPayload.ID, UiConfigPayload.CODEC);
    }

    public static void sendWaitingState(ServerPlayerEntity player, boolean waiting, int current, int required, String screenName, boolean allowEsc) {
        WaitingStatePayload payload = new WaitingStatePayload(waiting, current, required, screenName, allowEsc);
        ServerPlayNetworking.send(player, payload);
    }

    public static void broadcastWaitingState(MinecraftServer server, boolean waiting, int current, int required, String screenName, boolean allowEsc) {
        WaitingStatePayload payload = new WaitingStatePayload(waiting, current, required, screenName, allowEsc);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendImageData(ServerPlayerEntity player, String screenName, byte[] imageData) {
        ImageDataPayload payload = new ImageDataPayload(screenName, imageData);
        ServerPlayNetworking.send(player, payload);
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
        MissingNamesPayload payload = new MissingNamesPayload(names, more);
        ServerPlayNetworking.send(player, payload);
    }

    public static void broadcastUiConfig(MinecraftServer server, String text, int color, float scale,
                                         int wtX, int wtY, int pcX, int pcY,
                                         int mtX, int mtY, int etX, int etY) {
        UiConfigPayload payload = new UiConfigPayload(
                text, color, scale,
                new UiConfigPayload.TextPosition(wtX, wtY),
                new UiConfigPayload.TextPosition(pcX, pcY),
                new UiConfigPayload.TextPosition(mtX, mtY),
                new UiConfigPayload.TextPosition(etX, etY)
        );

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void sendUiConfig(ServerPlayerEntity player, String text, int color, float scale,
                                    int wtX, int wtY, int pcX, int pcY,
                                    int mtX, int mtY, int etX, int etY) {
        UiConfigPayload payload = new UiConfigPayload(
                text, color, scale,
                new UiConfigPayload.TextPosition(wtX, wtY),
                new UiConfigPayload.TextPosition(pcX, pcY),
                new UiConfigPayload.TextPosition(mtX, mtY),
                new UiConfigPayload.TextPosition(etX, etY)
        );

        ServerPlayNetworking.send(player, payload);
    }
}
