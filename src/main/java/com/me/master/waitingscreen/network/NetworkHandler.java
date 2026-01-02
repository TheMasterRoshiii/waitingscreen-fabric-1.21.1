package com.me.master.waitingscreen.network;

import com.me.master.waitingscreen.network.payload.*;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

@UtilityClass
public class NetworkHandler {

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(WaitingStatePayload.ID, WaitingStatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ImageDataPayload.ID, ImageDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ScreenChangePayload.ID, ScreenChangePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(VideoScreenPayload.ID, VideoScreenPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(MissingNamesPayload.ID, MissingNamesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UiConfigPayload.ID, UiConfigPayload.CODEC);
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

    public static void sendMissingNames(ServerPlayerEntity player, List<String> names, int more) {
        ServerPlayNetworking.send(player, new MissingNamesPayload(names, more));
    }

    public static void broadcastMissingNames(MinecraftServer server, List<String> names, int more) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendMissingNames(player, names, more);
        }
    }

    public static void sendUiConfig(ServerPlayerEntity player, String waitingText, int waitingTextColor, float waitingTextScale) {
        ServerPlayNetworking.send(player, new UiConfigPayload(waitingText, waitingTextColor, waitingTextScale));
    }

    public static void broadcastUiConfig(MinecraftServer server, String waitingText, int waitingTextColor, float waitingTextScale) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendUiConfig(player, waitingText, waitingTextColor, waitingTextScale);
        }
    }
}
