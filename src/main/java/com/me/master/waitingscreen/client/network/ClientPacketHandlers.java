package com.me.master.waitingscreen.client.network;

import com.me.master.waitingscreen.Waitingscreen;
import com.me.master.waitingscreen.network.payload.*;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.texture.NativeImage;
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
public class ClientPacketHandlers {

    private static final Set<Class<? extends Screen>> ALLOWED_SCREENS = Set.of(
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
            StatsScreen.class,
            DeathScreen.class
    );

    private static volatile boolean waitingActive = false;
    private static volatile int currentPlayers = 0;
    private static volatile int requiredPlayers = 4;
    private static volatile String currentScreen = "default";
    private static volatile boolean allowEscMenu = true;
    private static volatile boolean exempt = false;

    private static volatile String waitingText = "Esperando jugadores...";
    private static volatile int waitingTextColor = 0xFFFFFFFF;
    private static volatile float waitingTextScale = 1.0f;
    private static volatile int playerCurrentColor = 0xFFFFFFFF;
    private static volatile int playerRequiredColor = 0xFFFFFFFF;

    private static volatile List<String> missingNames = List.of();
    private static volatile int missingMore = 0;

    private static volatile int waitingTextX = 0;
    private static volatile int waitingTextY = 100;
    private static volatile int playerCountX = 0;
    private static volatile int playerCountY = 20;
    private static volatile int missingTextX = 0;
    private static volatile int missingTextY = 120;
    private static volatile int escTextX = 0;
    private static volatile int escTextY = -30;

    private static final Map<String, Identifier> loadedTextures = new ConcurrentHashMap<>();
    private static final Map<String, ImageAssembly> pendingImages = new ConcurrentHashMap<>();

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(WaitingStatePayload.ID,
                (payload, context) -> context.client().execute(() -> applyWaitingState(payload)));

        ClientPlayNetworking.registerGlobalReceiver(ImageDataPayload.ID,
                (payload, context) -> context.client().execute(() -> receiveImageChunk(payload)));

        ClientPlayNetworking.registerGlobalReceiver(ScreenChangePayload.ID,
                (payload, context) -> context.client().execute(() -> currentScreen = payload.screenName()));

        ClientPlayNetworking.registerGlobalReceiver(VideoScreenPayload.ID,
                (payload, context) -> log.warn("Video not implemented: {}", payload.videoUrl()));

        ClientPlayNetworking.registerGlobalReceiver(MissingNamesPayload.ID,
                (payload, context) -> context.client().execute(() -> {
                    missingNames = List.copyOf(payload.names());
                    missingMore = payload.more();
                }));

        ClientPlayNetworking.registerGlobalReceiver(UiConfigPayload.ID,
                (payload, context) -> context.client().execute(() -> applyUiConfig(payload)));
    }

    public static void reset() {
        waitingActive = false;
        exempt = false;
        currentPlayers = 0;
        requiredPlayers = 4;
        currentScreen = "default";
        missingNames = List.of();
        missingMore = 0;
        pendingImages.clear();
    }

    public static boolean isAllowedScreen(Screen screen) {
        if (screen == null) return false;
        if (screen instanceof GameMenuScreen) return true;
        if (screen instanceof GameOptionsScreen) return true;
        if (ALLOWED_SCREENS.contains(screen.getClass())) return true;
        String name = screen.getClass().getName();
        return name.contains("Option")
                || name.contains("Setting")
                || name.contains("Config")
                || name.contains("Keybind")
                || name.contains("Control")
                || name.contains("Video")
                || name.contains("Sound")
                || name.contains("Pack")
                || name.contains("Gui")
                || name.contains("GUI")
                || name.contains("Mod")
                || name.contains("Menu");
    }

    public static boolean shouldBlockInput() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        if (mc.currentScreen instanceof DeathScreen) return false;
        return waitingActive && !exempt;
    }

    public static void cleanupTextures() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        loadedTextures.forEach((name, id) -> {
            try { mc.getTextureManager().destroyTexture(id); } catch (Exception ignored) {}
        });
        loadedTextures.clear();
    }

    private static void applyWaitingState(WaitingStatePayload payload) {
        waitingActive = payload.waiting();
        currentPlayers = payload.current();
        requiredPlayers = payload.required();
        currentScreen = payload.screenName();
        allowEscMenu = payload.allowEsc();
        exempt = payload.exempt();
    }

    private static void applyUiConfig(UiConfigPayload payload) {
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
        playerCurrentColor = payload.playerCurrentColor();
        playerRequiredColor = payload.playerRequiredColor();
    }

    private static void receiveImageChunk(ImageDataPayload payload) {
        String screenName = payload.screenName();
        ImageAssembly assembly = pendingImages.computeIfAbsent(screenName,
                k -> new ImageAssembly(payload.totalLength(), payload.chunkCount()));

        byte[] chunk = payload.data();
        int offset = payload.chunkIndex() * ImageDataPayload.CHUNK_SIZE;
        if (offset < 0 || offset + chunk.length > assembly.buffer.length) {
            pendingImages.remove(screenName);
            log.error("Invalid image chunk for {}: out of bounds", screenName);
            return;
        }

        System.arraycopy(chunk, 0, assembly.buffer, offset, chunk.length);
        assembly.received++;

        if (assembly.received >= assembly.chunkCount) {
            pendingImages.remove(screenName);
            uploadTexture(screenName, assembly.buffer);
        }
    }

    private static void uploadTexture(String screenName, byte[] imageData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData)) {
            NativeImage image = NativeImage.read(bais);
            Identifier location = Identifier.of(Waitingscreen.MOD_ID, "screen_" + screenName.toLowerCase());

            MinecraftClient.getInstance().execute(() -> {
                Identifier old = loadedTextures.put(screenName, location);
                if (old != null) MinecraftClient.getInstance().getTextureManager().destroyTexture(old);
                MinecraftClient.getInstance().getTextureManager()
                        .registerTexture(location, new NativeImageBackedTexture(image));
            });
        } catch (IOException e) {
            log.error("Failed to load image: {}", screenName, e);
        }
    }

    public static Identifier getTexture(String screenName)  { return loadedTextures.get(screenName); }
    public static boolean isWaitingActive()                 { return waitingActive; }
    public static int getCurrentPlayers()                   { return currentPlayers; }
    public static int getRequiredPlayers()                  { return requiredPlayers; }
    public static String getCurrentScreen()                 { return currentScreen; }
    public static boolean isAllowEscMenu()                  { return allowEscMenu; }
    public static boolean isExempt()                        { return exempt; }
    public static String getWaitingText()                   { return waitingText; }
    public static int getWaitingTextColor()                 { return waitingTextColor; }
    public static float getWaitingTextScale()               { return waitingTextScale; }
    public static int getPlayerCurrentColor()               { return playerCurrentColor; }
    public static int getPlayerRequiredColor()              { return playerRequiredColor; }
    public static List<String> getMissingNames()            { return missingNames; }
    public static int getMissingMore()                      { return missingMore; }
    public static int getWaitingTextX()                     { return waitingTextX; }
    public static int getWaitingTextY()                     { return waitingTextY; }
    public static int getPlayerCountX()                     { return playerCountX; }
    public static int getPlayerCountY()                     { return playerCountY; }
    public static int getMissingTextX()                     { return missingTextX; }
    public static int getMissingTextY()                     { return missingTextY; }
    public static int getEscTextX()                         { return escTextX; }
    public static int getEscTextY()                         { return escTextY; }

    private static final class ImageAssembly {
        final byte[] buffer;
        final int chunkCount;
        int received;

        ImageAssembly(int totalLength, int chunkCount) {
            this.buffer = new byte[totalLength];
            this.chunkCount = chunkCount;
        }
    }
}
