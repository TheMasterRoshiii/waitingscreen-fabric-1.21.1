package com.me.master.waitingscreen;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
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

    @Getter
    private boolean whitelistMode = true;

    @Getter
    private int showNamesWhenMissingAtMost = 5;

    @Getter
    private int maxNamesToShow = 3;

    @Getter
    private String waitingText = "Esperando jugadores...";

    @Getter
    private int waitingTextColor = 0xFFFFFFFF;

    @Getter
    private float waitingTextScale = 1.0f;

    private final Set<UUID> exemptPlayers = ConcurrentHashMap.newKeySet();
    private final Map<String, byte[]> serverImageCache = new ConcurrentHashMap<>();
    private MinecraftServer currentServer = null;
    private int tickCounter = 0;
    private static final int UPDATE_INTERVAL = 20;

    private volatile Set<String> cachedWhitelistNames = Set.of();
    private volatile long whitelistLastModified = -1L;

    private volatile List<String> lastShownMissing = List.of();
    private volatile int lastMissingMore = 0;

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
        this.lastShownMissing = List.of();
        this.lastMissingMore = 0;
        LOGGER.info("Server stopped");
    }

    private void onPlayerJoin(ServerPlayerEntity player) {
        sendAllImagesToPlayer(player);
        if (waitingActive) {
            sendWaitingStateToPlayer(player);
            NetworkHandler.sendMissingNames(player, lastShownMissing, lastMissingMore);
            NetworkHandler.sendUiConfig(player, waitingText, waitingTextColor, waitingTextScale);
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
        if (whitelistMode) {
            refreshWhitelistCache();
            requiredPlayers = cachedWhitelistNames.size();
        } else {
            requiredPlayers = required;
        }
        waitingActive = true;
        currentPlayers = 0;
        updatePlayerCount();
        broadcastWaitingState();
        broadcastUiConfig();
    }

    public void stopWaiting() {
        waitingActive = false;
        currentPlayers = 0;
        lastShownMissing = List.of();
        lastMissingMore = 0;
        broadcastWaitingState();
        if (currentServer != null) NetworkHandler.broadcastMissingNames(currentServer, List.of(), 0);
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

    public void setWhitelistMode(boolean whitelistMode) {
        this.whitelistMode = whitelistMode;
        if (waitingActive) updatePlayerCount();
    }

    public void setShowNamesWhenMissingAtMost(int v) {
        this.showNamesWhenMissingAtMost = Math.max(0, v);
        if (waitingActive) updatePlayerCount();
    }

    public void setMaxNamesToShow(int v) {
        this.maxNamesToShow = Math.max(0, v);
        if (waitingActive) updatePlayerCount();
    }

    public void setWaitingText(String text) {
        this.waitingText = text;
        broadcastUiConfig();
    }

    public void setWaitingTextColor(int color) {
        this.waitingTextColor = color;
        broadcastUiConfig();
    }

    public void setWaitingTextScale(float scale) {
        this.waitingTextScale = scale;
        broadcastUiConfig();
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

    private void refreshWhitelistCache() {
        File f = new File("whitelist.json");
        if (!f.exists()) {
            cachedWhitelistNames = Set.of();
            whitelistLastModified = -1L;
            return;
        }

        long lm = f.lastModified();
        if (lm == whitelistLastModified) return;

        Set<String> out = new HashSet<>();
        try (FileReader r = new FileReader(f)) {
            JsonElement el = JsonParser.parseReader(r);
            if (el.isJsonArray()) {
                for (JsonElement e : el.getAsJsonArray()) {
                    if (!e.isJsonObject()) continue;
                    if (e.getAsJsonObject().has("name")) {
                        String n = e.getAsJsonObject().get("name").getAsString();
                        if (n != null && !n.isBlank()) out.add(n);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        cachedWhitelistNames = Set.copyOf(out);
        whitelistLastModified = lm;
    }

    private void updatePlayerCount() {
        if (!waitingActive || currentServer == null) return;

        if (whitelistMode) {
            refreshWhitelistCache();
            Set<String> whitelist = cachedWhitelistNames;
            int expected = whitelist.size();
            if (expected != requiredPlayers) requiredPlayers = expected;

            int count = 0;
            Set<String> onlineWhitelisted = new HashSet<>();

            for (ServerPlayerEntity p : currentServer.getPlayerManager().getPlayerList()) {
                if (p.hasPermissionLevel(2)) continue;
                if (isPlayerExempt(p.getUuid())) continue;

                String name = p.getGameProfile().getName();
                if (whitelist.contains(name)) {
                    onlineWhitelisted.add(name);
                    count++;
                }
            }

            List<String> missing = new ArrayList<>();
            for (String n : whitelist) if (!onlineWhitelisted.contains(n)) missing.add(n);
            missing.sort(String.CASE_INSENSITIVE_ORDER);

            List<String> shown;
            int more;

            if (!missing.isEmpty() && missing.size() <= showNamesWhenMissingAtMost && maxNamesToShow > 0) {
                int s = Math.min(maxNamesToShow, missing.size());
                shown = List.copyOf(missing.subList(0, s));
                more = missing.size() - s;
            } else {
                shown = List.of();
                more = 0;
            }

            if (!shown.equals(lastShownMissing) || more != lastMissingMore) {
                lastShownMissing = shown;
                lastMissingMore = more;
                NetworkHandler.broadcastMissingNames(currentServer, shown, more);
            }

            if (count != currentPlayers) {
                currentPlayers = count;
                broadcastWaitingState();
            }

            if (requiredPlayers > 0 && currentPlayers >= requiredPlayers) {
                stopWaiting();
            }

            return;
        }

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

    private void broadcastUiConfig() {
        if (currentServer == null) return;
        NetworkHandler.broadcastUiConfig(currentServer, waitingText, waitingTextColor, waitingTextScale);
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
