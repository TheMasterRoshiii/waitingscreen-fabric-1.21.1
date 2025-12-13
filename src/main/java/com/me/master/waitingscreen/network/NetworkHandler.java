package com.me.master.waitingscreen.network;

import com.me.master.waitingscreen.network.payload.*;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

@UtilityClass
public class NetworkHandler {

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(WaitingStatePayload.ID, WaitingStatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ImageDataPayload.ID, ImageDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ScreenChangePayload.ID, ScreenChangePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(VideoScreenPayload.ID, VideoScreenPayload.CODEC);
    }

    public static void sendWaitingState(ServerPlayerEntity player, boolean isWaiting, int current, int required, String screen, boolean allowEsc) {
        ServerPlayNetworking.send(player, new WaitingStatePayload(isWaiting, current, required, screen, allowEsc));
    }

    public static void broadcastWaitingState(MinecraftServer server, boolean isWaiting, int current, int required, String screen, boolean allowEsc) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendWaitingState(player, isWaiting, current, required, screen, allowEsc);
        }
    }

    public static void sendImageData(ServerPlayerEntity player, String screenName, byte[] imageData) {
        ServerPlayNetworking.send(player, new ImageDataPayload(screenName, imageData));
    }

    public static void broadcastImageData(MinecraftServer server, String screenName, byte[] imageData) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendImageData(player, screenName, imageData);
        }
    }

    public static void sendScreenChange(ServerPlayerEntity player, String screenName) {
        ServerPlayNetworking.send(player, new ScreenChangePayload(screenName));
    }

    public static void broadcastScreenChange(MinecraftServer server, String screenName) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendScreenChange(player, screenName);
        }
    }
}
