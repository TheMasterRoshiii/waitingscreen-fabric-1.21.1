package com.me.master.waitingscreen.client;

import net.minecraft.client.gui.screen.option.MouseOptionsScreen;
import net.minecraft.client.gui.screen.option.ChatOptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.screen.option.TelemetryInfoScreen;
import net.minecraft.client.gui.screen.option.OnlineOptionsScreen;
import com.me.master.waitingscreen.Waitingscreen;
import com.me.master.waitingscreen.client.screen.WaitingScreen;
import com.me.master.waitingscreen.network.payload.*;
import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class WaitingscreenClient implements ClientModInitializer {

    @Getter
    private static boolean waitingActive = false;

    @Getter
    private static int currentPlayers = 0;

    @Getter
    private static int requiredPlayers = 4;

    @Getter
    private static String currentScreen = "default";

    @Getter
    private static boolean allowEscMenu = true;

    private static boolean wasInWaiting = false;
    private static final Map<String, Identifier> loadedTextures = new ConcurrentHashMap<>();

    @Override
    public void onInitializeClient() {
        Waitingscreen.LOGGER.info("Initializing client");
        registerClientPacketHandlers();
        registerClientEvents();
    }

    private void registerClientPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(WaitingStatePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                waitingActive = payload.isWaiting();
                currentPlayers = payload.currentPlayers();
                requiredPlayers = payload.requiredPlayers();
                currentScreen = payload.screenName();
                allowEscMenu = payload.allowEscMenu();
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ImageDataPayload.ID, (payload, context) -> {
            context.client().execute(() -> receiveImageData(payload.screenName(), payload.imageData()));
        });

        ClientPlayNetworking.registerGlobalReceiver(ScreenChangePayload.ID, (payload, context) -> {
            context.client().execute(() -> currentScreen = payload.screenName());
        });

        ClientPlayNetworking.registerGlobalReceiver(VideoScreenPayload.ID, (payload, context) -> {
            context.client().execute(() -> Waitingscreen.LOGGER.warn("Video not implemented: {}", payload.videoUrl()));
        });
    }

    private void registerClientEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;

        boolean isOP = mc.player.hasPermissionLevel(2);
        boolean isExempt = Waitingscreen.getInstance() != null &&
                Waitingscreen.getInstance().isPlayerExempt(mc.player.getUuid());
        boolean shouldShow = waitingActive && !isOP && !isExempt;

        if (shouldShow) {
            if (!wasInWaiting) {
                wasInWaiting = true;
            }

            if (mc.currentScreen == null) {
                mc.setScreen(new WaitingScreen());
            } else if (mc.currentScreen instanceof WaitingScreen) {
                return;
            } else if (allowEscMenu && isAllowedScreen(mc.currentScreen)) {
                return;
            } else {
                mc.setScreen(new WaitingScreen());
            }
        } else {
            if (wasInWaiting) {
                if (mc.currentScreen instanceof WaitingScreen) {
                    mc.setScreen(null);
                }
                wasInWaiting = false;
            }
        }
    }


    private boolean isAllowedScreen(Screen screen) {
        if (screen == null) return false;

        String screenName = screen.getClass().getName();

        return screen instanceof GameMenuScreen ||
                screen instanceof ControlsOptionsScreen ||
                screen instanceof VideoOptionsScreen ||
                screen instanceof SoundOptionsScreen ||
                screen instanceof LanguageOptionsScreen ||
                screen instanceof AccessibilityOptionsScreen ||
                screen instanceof MouseOptionsScreen ||
                screen instanceof ChatOptionsScreen ||
                screen instanceof SkinOptionsScreen ||
                screen instanceof TelemetryInfoScreen ||
                screen instanceof OnlineOptionsScreen ||
                screen instanceof PackScreen ||
                screen instanceof SocialInteractionsScreen ||
                screen instanceof AdvancementsScreen ||
                screen instanceof StatsScreen ||
                screenName.contains("OptionsScreen") ||
                screenName.contains("OptionsSubScreen");
    }



    @Environment(EnvType.CLIENT)
    private void receiveImageData(String screenName, byte[] imageData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
            net.minecraft.client.texture.NativeImage image = net.minecraft.client.texture.NativeImage.read(bais);
            Identifier location = Identifier.of(Waitingscreen.MOD_ID, "screen_" + screenName.toLowerCase());

            MinecraftClient.getInstance().execute(() -> {
                Identifier old = loadedTextures.put(screenName, location);
                if (old != null) {
                    MinecraftClient.getInstance().getTextureManager().destroyTexture(old);
                }
                MinecraftClient.getInstance().getTextureManager().registerTexture(location,
                        new NativeImageBackedTexture(image));
                Waitingscreen.LOGGER.info("Loaded texture from server: {}", screenName);
            });
        } catch (IOException e) {
            Waitingscreen.LOGGER.error("Failed to load image: {}", screenName, e);
        }
    }

    public static Identifier getTexture(String screenName) {
        return loadedTextures.get(screenName);
    }

    public static boolean shouldBlockInput() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !waitingActive) return false;

        boolean isOP = mc.player.hasPermissionLevel(2);
        boolean isExempt = Waitingscreen.getInstance() != null &&
                Waitingscreen.getInstance().isPlayerExempt(mc.player.getUuid());

        return !isOP && !isExempt;
    }
}
