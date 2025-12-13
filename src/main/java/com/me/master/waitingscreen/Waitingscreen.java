package com.me.master.waitingscreen;

import com.me.master.waitingscreen.command.WaitingScreenCommands;
import com.me.master.waitingscreen.network.NetworkHandler;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Waitingscreen implements ModInitializer {
    public static final String MOD_ID = "waitingscreen";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Getter
    private static Waitingscreen instance;

    @Getter
    private boolean waitingActive = false;

    @Getter
    private int requiredPlayers = 4;

    @Getter
    private int currentPlayers = 0;

    @Getter
    private String currentScreen = "default";

    @Getter
    private boolean allowEscMenu = true;

    @Getter
    private boolean blockChat = false;

    @Getter
    private boolean protectPlayers = true;

    @Getter
    private boolean blockInteractions = true;

    @Getter
    private boolean freezeHunger = true;

    private final Set<UUID> exemptPlayers = ConcurrentHashMap.newKeySet();
    private final Map<String, byte[]> serverImageCache = new ConcurrentHashMap<>();
    private MinecraftServer currentServer = null;
    private int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 20;

    @Override
    public void onInitialize() {
        instance = this;
        LOGGER.info("Initializing Waiting Screen Mod");

        NetworkHandler.registerPackets();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                WaitingScreenCommands.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                onPlayerJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                onPlayerLeave(handler.player));
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

        LOGGER.info("Waiting Screen Mod initialized");
    }

    private void onServerStarting(MinecraftServer server) {
        this.currentServer = server;
        LOGGER.info("Server starting - loading images");
        loadServerImages();
    }

    private void onServerStopped(MinecraftServer server) {
        this.currentServer = null;
        this.waitingActive = false;
        this.currentPlayers = 0;
        this.serverImageCache.clear();
        LOGGER.info("Server stopped");
    }

    private void onPlayerJoin(ServerPlayerEntity player) {
        sendAllImagesToPlayer(player);
        if (waitingActive) {
            sendWaitingStateToPlayer(player);
            updatePlayerCount();
        }
    }

    private void onPlayerLeave(ServerPlayerEntity player) {
        if (waitingActive) {
            updatePlayerCount();
        }
    }

    private void onServerTick(MinecraftServer server) {
        if (waitingActive) {
            tickCounter++;
            if (tickCounter >= UPDATE_INTERVAL) {
                updatePlayerCount();
                tickCounter = 0;
            }
        }
    }

    private void loadServerImages() {
        File dir = new File("config/waitingscreens/");
        if (!dir.exists()) {
            dir.mkdirs();
            LOGGER.warn("Created config/waitingscreens/ directory");
        }

        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".png") || lower.endsWith(".jpg") ||
                    lower.endsWith(".jpeg") || lower.endsWith(".webp") ||
                    lower.endsWith(".bmp") || lower.endsWith(".gif");
        });

        serverImageCache.clear();
        if (files != null && files.length > 0) {
            for (File file : files) {
                try {
                    String name = file.getName().substring(0, file.getName().lastIndexOf('.'));

                    LOGGER.info("Processing image: {}", file.getName());
                    BufferedImage image = ImageIO.read(file);

                    if (image == null) {
                        LOGGER.error("Failed to read image: {} - Unsupported format or corrupted", file.getName());
                        continue;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(image, "PNG", baos);
                    byte[] imageData = baos.toByteArray();

                    if (imageData.length == 0) {
                        LOGGER.error("Image {} converted to 0 bytes - Skipping", file.getName());
                        continue;
                    }

                    serverImageCache.put(name, imageData);
                    LOGGER.info("✓ Loaded and converted image: {} ({}x{}, {} bytes)",
                            name, image.getWidth(), image.getHeight(), imageData.length);

                } catch (IOException e) {
                    LOGGER.error("Failed to load/convert image: {}", file.getName(), e);
                } catch (Exception e) {
                    LOGGER.error("Unexpected error processing image: {}", file.getName(), e);
                }
            }
        } else {
            LOGGER.error("=================================================");
            LOGGER.error("NO IMAGES FOUND IN config/waitingscreens/");
            LOGGER.error("Please add at least one image (PNG, JPG, WEBP, BMP, GIF)");
            LOGGER.error("Example: config/waitingscreens/default.png");
            LOGGER.error("=================================================");
        }
    }

    private void sendAllImagesToPlayer(ServerPlayerEntity player) {
        serverImageCache.forEach((name, data) ->
                NetworkHandler.sendImageData(player, name, data));
    }

    private void broadcastAllImages() {
        if (currentServer == null) return;
        serverImageCache.forEach((name, data) ->
                NetworkHandler.broadcastImageData(currentServer, name, data));
    }

    public void startWaiting(int required) {
        requiredPlayers = required;
        waitingActive = true;
        currentPlayers = 0;
        updatePlayerCount();
        broadcastWaitingState();
    }

    public void stopWaiting() {
        waitingActive = false;
        currentPlayers = 0;
        broadcastWaitingState();
    }

    public boolean changeScreen(String name) {
        if (serverImageCache.containsKey(name)) {
            currentScreen = name;
            broadcastScreenChange();
            return true;
        }
        return false;
    }

    public void reloadScreens() {
        loadServerImages();
        broadcastAllImages();
    }

    public String[] getAvailableScreens() {
        return serverImageCache.keySet().toArray(new String[0]);
    }

    public void setRequiredPlayers(int count) {
        this.requiredPlayers = count;
        if (waitingActive) {
            broadcastWaitingState();
            if (count > 0) {
                updatePlayerCount();
            }
        }
    }

    public void setAllowEscMenu(boolean allow) {
        this.allowEscMenu = allow;
        broadcastWaitingState();
    }

    public void setBlockChat(boolean block) {
        this.blockChat = block;
    }

    public void setProtectPlayers(boolean protect) {
        this.protectPlayers = protect;
    }

    public void setBlockInteractions(boolean block) {
        this.blockInteractions = block;
    }

    public void setFreezeHunger(boolean freeze) {
        this.freezeHunger = freeze;
    }

    public void addExemptPlayer(UUID id) {
        exemptPlayers.add(id);
        if (waitingActive) updatePlayerCount();
    }

    public void removeExemptPlayer(UUID id) {
        exemptPlayers.remove(id);
        if (waitingActive) updatePlayerCount();
    }

    public boolean isPlayerExempt(UUID id) {
        return exemptPlayers.contains(id);
    }

    public void clearExemptPlayers() {
        exemptPlayers.clear();
        if (waitingActive) updatePlayerCount();
    }

    public Set<UUID> getExemptPlayers() {
        return Set.copyOf(exemptPlayers);
    }

    private void updatePlayerCount() {
        if (!waitingActive || currentServer == null) return;
        int count = 0;
        for (ServerPlayerEntity p : currentServer.getPlayerManager().getPlayerList()) {
            if (!p.hasPermissionLevel(2) && !isPlayerExempt(p.getUuid())) count++;
        }
        if (count != currentPlayers) {
            currentPlayers = count;
            broadcastWaitingState();
        }
        if (requiredPlayers > 0 && currentPlayers >= requiredPlayers) {
            stopWaiting();
        }
    }

    private void broadcastWaitingState() {
        if (currentServer == null) return;
        NetworkHandler.broadcastWaitingState(currentServer, waitingActive, currentPlayers, requiredPlayers, currentScreen, allowEscMenu);
    }

    private void broadcastScreenChange() {
        if (currentServer == null) return;
        NetworkHandler.broadcastScreenChange(currentServer, currentScreen);
    }

    public void sendWaitingStateToPlayer(ServerPlayerEntity player) {
        NetworkHandler.sendWaitingState(player, waitingActive, currentPlayers, requiredPlayers, currentScreen, allowEscMenu);
    }
}
