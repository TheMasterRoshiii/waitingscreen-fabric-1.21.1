package com.me.master.waitingscreen.client;

import com.me.master.waitingscreen.Waitingscreen;
import com.me.master.waitingscreen.client.screen.WaitingScreen;
import com.me.master.waitingscreen.network.payload.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
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
    @Getter
    private static String waitingText = "Esperando jugadores...";
    @Getter
    private static int waitingTextColor = 0xFFFFFFFF;
    @Getter
    private static float waitingTextScale = 1.0f;
    @Getter
    private static volatile List<String> missingNames = List.of();
    @Getter
    private static volatile int missingMore = 0;

    @Getter
    private static int waitingTextX = 0;
    @Getter
    private static int waitingTextY = 100;
    @Getter
    private static int playerCountX = 0;
    @Getter
    private static int playerCountY = 20;
    @Getter
    private static int missingTextX = 0;
    @Getter
    private static int missingTextY = 120;
    @Getter
    private static int escTextX = 0;
    @Getter
    private static int escTextY = -30;

    private static boolean wasInWaiting = false;
    private static final Map<String, Identifier> loadedTextures = new ConcurrentHashMap<>();

    private static final Set<Class<? extends Screen>> ALLOWED_SCREENS = Set.of(
            GameMenuScreen.class,
            OptionsScreen.class,
            ControlsOptionsScreen.class,
            KeybindsScreen.class,
            VideoOptionsScreen.class,
            SoundOptionsScreen.class,
            LanguageOptionsScreen.class,
            AccessibilityOptionsScreen.class,
            MouseOptionsScreen.class,
            ChatOptionsScreen.class,
            SkinOptionsScreen.class,
            TelemetryInfoScreen.class,
            OnlineOptionsScreen.class,
            PackScreen.class,
            SocialInteractionsScreen.class,
            AdvancementsScreen.class,
            StatsScreen.class
    );

    @Override
    public void onInitializeClient() {
        log.info("Initializing client");
        registerClientPacketHandlers();
        registerClientEvents();
    }

    private void registerClientPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(WaitingStatePayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    waitingActive = payload.isWaiting();
                    currentPlayers = payload.currentPlayers();
                    requiredPlayers = payload.requiredPlayers();
                    currentScreen = payload.screenName();
                    allowEscMenu = payload.allowEscMenu();
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(ImageDataPayload.ID, (payload, context) ->
                context.client().execute(() -> receiveImageData(payload.screenName(), payload.imageData()))
        );

        ClientPlayNetworking.registerGlobalReceiver(ScreenChangePayload.ID, (payload, context) ->
                context.client().execute(() -> currentScreen = payload.screenName())
        );

        ClientPlayNetworking.registerGlobalReceiver(VideoScreenPayload.ID, (payload, context) ->
                context.client().execute(() -> log.warn("Video not implemented: {}", payload.videoUrl()))
        );

        ClientPlayNetworking.registerGlobalReceiver(MissingNamesPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    missingNames = List.copyOf(payload.names());
                    missingMore = payload.more();
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(UiConfigPayload.ID, (payload, context) ->
                context.client().execute(() -> {
                    waitingText = payload.waitingText();
                    waitingTextColor = payload.waitingTextColor();
                    waitingTextScale = payload.waitingTextScale();

                    waitingTextX = payload.waitingTextPos().x();
                    waitingTextY = payload.waitingTextPos().y();

                    playerCountX = payload.playerCountPos().x();
                    playerCountY = payload.playerCountPos().y();

                    missingTextX = payload.missingTextPos().x();
                    missingTextY = payload.missingTextPos().y();

                    escTextX = payload.escTextPos().x();
                    escTextY = payload.escTextPos().y();
                })
        );
    }

        private void registerClientEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            cleanupTextures();
            wasInWaiting = false;
            waitingActive = false;
        });
    }

    private void onClientTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;

        boolean shouldShow = shouldShowWaitingScreen(mc.player);

        if (shouldShow) {
            if (!wasInWaiting) {
                wasInWaiting = true;
            }

            if (mc.currentScreen == null) {
                mc.setScreen(new WaitingScreen());
            } else if (!(mc.currentScreen instanceof WaitingScreen)) {
                if (!allowEscMenu || !isAllowedScreen(mc.currentScreen)) {
                    mc.setScreen(new WaitingScreen());
                }
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

    private static boolean shouldShowWaitingScreen(ClientPlayerEntity player) {
        if (!waitingActive || player == null) return false;

        Waitingscreen mod = Waitingscreen.getInstance();
        boolean isOP = player.hasPermissionLevel(2);
        boolean isExempt = mod != null && mod.isPlayerExempt(player.getUuid());

        return !isOP && !isExempt;
    }

    private boolean isAllowedScreen(Screen screen) {
        if (screen == null) return false;

        if (ALLOWED_SCREENS.contains(screen.getClass())) {
            return true;
        }

        String screenName = screen.getClass().getName();
        return screenName.contains("OptionsScreen") || screenName.contains("OptionsSubScreen");
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
                log.info("Loaded texture from server: {}", screenName);
            });
        } catch (IOException e) {
            log.error("Failed to load image: {}", screenName, e);
        }
    }

    public static void cleanupTextures() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;

        loadedTextures.forEach((name, id) -> {
            try {
                mc.getTextureManager().destroyTexture(id);
            } catch (Exception e) {
                System.err.println("Failed to destroy texture: " + name);
            }
        });
        loadedTextures.clear();
    }

    public static Identifier getTexture(String screenName) {
        return loadedTextures.get(screenName);
    }

    public static boolean shouldBlockInput() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;

        return shouldShowWaitingScreen(mc.player);
    }
}
