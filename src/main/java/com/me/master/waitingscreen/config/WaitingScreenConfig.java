package com.me.master.waitingscreen.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
public final class WaitingScreenConfig {

    private static final Path CONFIG_FILE = Path.of("config", "waitingscreen.json");
    private static final int CONFIG_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private WaitingScreenConfig() {
    }

    public static Settings load() {
        Settings defaults = Settings.defaults();

        if (!Files.exists(CONFIG_FILE)) {
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) {
                throw new IllegalStateException("The root value must be a JSON object");
            }

            JsonObject root = parsed.getAsJsonObject();
            return new Settings(
                    intValue(root, "required_players", defaults.requiredPlayers()),
                    stringValue(root, "current_screen", defaults.currentScreen()),
                    booleanValue(root, "keep_screen_on_full", defaults.keepScreenOnFull()),
                    booleanValue(root, "allow_esc_menu", defaults.allowEscMenu()),
                    booleanValue(root, "block_chat", defaults.blockChat()),
                    booleanValue(root, "protect_players", defaults.protectPlayers()),
                    booleanValue(root, "block_interactions", defaults.blockInteractions()),
                    booleanValue(root, "freeze_hunger", defaults.freezeHunger()),
                    booleanValue(root, "whitelist_mode", defaults.whitelistMode()),
                    intValue(root, "show_names_when_missing_at_most", defaults.showNamesWhenMissingAtMost()),
                    intValue(root, "max_names_to_show", defaults.maxNamesToShow()),
                    stringValue(root, "waiting_text", defaults.waitingText()),
                    intValue(root, "waiting_text_color", defaults.waitingTextColor()),
                    floatValue(root, "waiting_text_scale", defaults.waitingTextScale()),
                    intValue(root, "waiting_text_x", defaults.waitingTextX()),
                    intValue(root, "waiting_text_y", defaults.waitingTextY()),
                    intValue(root, "player_count_x", defaults.playerCountX()),
                    intValue(root, "player_count_y", defaults.playerCountY()),
                    intValue(root, "missing_text_x", defaults.missingTextX()),
                    intValue(root, "missing_text_y", defaults.missingTextY()),
                    intValue(root, "esc_text_x", defaults.escTextX()),
                    intValue(root, "esc_text_y", defaults.escTextY()),
                    intValue(root, "player_current_color", defaults.playerCurrentColor()),
                    intValue(root, "player_required_color", defaults.playerRequiredColor()),
                    uuidSet(root, "exempt_players")
            );
        } catch (IOException | RuntimeException exception) {
            log.error("Could not load {}. Using default settings.", CONFIG_FILE, exception);
            return defaults;
        }
    }

    public static void save(Settings settings) {
        Path temporaryFile = CONFIG_FILE.resolveSibling(CONFIG_FILE.getFileName() + ".tmp");

        try {
            Path parent = CONFIG_FILE.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                    temporaryFile,
                    toJson(settings),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );

            try {
                Files.move(temporaryFile, CONFIG_FILE,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, CONFIG_FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            log.error("Could not save {}.", CONFIG_FILE, exception);
            try {
                Files.deleteIfExists(temporaryFile);
            } catch (IOException cleanupException) {
                log.warn("Could not clean up temporary config file {}.", temporaryFile, cleanupException);
            }
        }
    }

    private static String toJson(Settings settings) {
        JsonObject root = new JsonObject();
        root.addProperty("version", CONFIG_VERSION);
        root.addProperty("required_players", settings.requiredPlayers());
        root.addProperty("current_screen", settings.currentScreen());
        root.addProperty("keep_screen_on_full", settings.keepScreenOnFull());
        root.addProperty("allow_esc_menu", settings.allowEscMenu());
        root.addProperty("block_chat", settings.blockChat());
        root.addProperty("protect_players", settings.protectPlayers());
        root.addProperty("block_interactions", settings.blockInteractions());
        root.addProperty("freeze_hunger", settings.freezeHunger());
        root.addProperty("whitelist_mode", settings.whitelistMode());
        root.addProperty("show_names_when_missing_at_most", settings.showNamesWhenMissingAtMost());
        root.addProperty("max_names_to_show", settings.maxNamesToShow());
        root.addProperty("waiting_text", settings.waitingText());
        root.addProperty("waiting_text_color", settings.waitingTextColor());
        root.addProperty("waiting_text_scale", settings.waitingTextScale());
        root.addProperty("waiting_text_x", settings.waitingTextX());
        root.addProperty("waiting_text_y", settings.waitingTextY());
        root.addProperty("player_count_x", settings.playerCountX());
        root.addProperty("player_count_y", settings.playerCountY());
        root.addProperty("missing_text_x", settings.missingTextX());
        root.addProperty("missing_text_y", settings.missingTextY());
        root.addProperty("esc_text_x", settings.escTextX());
        root.addProperty("esc_text_y", settings.escTextY());
        root.addProperty("player_current_color", settings.playerCurrentColor());
        root.addProperty("player_required_color", settings.playerRequiredColor());

        JsonArray exemptPlayers = new JsonArray();
        settings.exemptPlayers().stream()
                .map(UUID::toString)
                .sorted()
                .forEach(exemptPlayers::add);
        root.add("exempt_players", exemptPlayers);

        return GSON.toJson(root) + System.lineSeparator();
    }

    private static boolean booleanValue(JsonObject root, String key, boolean fallback) {
        JsonElement value = root.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            return fallback;
        }
        return value.getAsBoolean();
    }

    private static int intValue(JsonObject root, String key, int fallback) {
        JsonElement value = root.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            return fallback;
        }

        try {
            return value.getAsInt();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static float floatValue(JsonObject root, String key, float fallback) {
        JsonElement value = root.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            return fallback;
        }

        try {
            float result = value.getAsFloat();
            return Float.isFinite(result) && result > 0.0f ? result : fallback;
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private static String stringValue(JsonObject root, String key, String fallback) {
        JsonElement value = root.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            return fallback;
        }

        String result = value.getAsString();
        return result.isBlank() ? fallback : result;
    }

    private static Set<UUID> uuidSet(JsonObject root, String key) {
        JsonElement value = root.get(key);
        if (value == null || !value.isJsonArray()) {
            return Set.of();
        }

        Set<UUID> result = new HashSet<>();
        for (JsonElement element : value.getAsJsonArray()) {
            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                continue;
            }

            try {
                result.add(UUID.fromString(element.getAsString()));
            } catch (IllegalArgumentException exception) {
                log.warn("Ignoring invalid exempt player UUID in {}: {}", CONFIG_FILE, element.getAsString());
            }
        }
        return result;
    }

    public record Settings(
            int requiredPlayers,
            String currentScreen,
            boolean keepScreenOnFull,
            boolean allowEscMenu,
            boolean blockChat,
            boolean protectPlayers,
            boolean blockInteractions,
            boolean freezeHunger,
            boolean whitelistMode,
            int showNamesWhenMissingAtMost,
            int maxNamesToShow,
            String waitingText,
            int waitingTextColor,
            float waitingTextScale,
            int waitingTextX,
            int waitingTextY,
            int playerCountX,
            int playerCountY,
            int missingTextX,
            int missingTextY,
            int escTextX,
            int escTextY,
            int playerCurrentColor,
            int playerRequiredColor,
            Set<UUID> exemptPlayers
    ) {
        public Settings {
            requiredPlayers = Math.max(0, requiredPlayers);
            currentScreen = currentScreen == null || currentScreen.isBlank() ? "default" : currentScreen;
            showNamesWhenMissingAtMost = Math.max(0, showNamesWhenMissingAtMost);
            maxNamesToShow = Math.max(0, maxNamesToShow);
            waitingText = waitingText == null || waitingText.isBlank() ? "Esperando jugadores..." : waitingText;
            waitingTextScale = Float.isFinite(waitingTextScale) && waitingTextScale > 0.0f
                    ? waitingTextScale
                    : 1.0f;
            exemptPlayers = exemptPlayers == null ? Set.of() : Set.copyOf(exemptPlayers);
        }

        public static Settings defaults() {
            return new Settings(
                    4,
                    "default",
                    false,
                    true,
                    false,
                    true,
                    true,
                    true,
                    true,
                    5,
                    3,
                    "Esperando jugadores...",
                    0xFFFFFFFF,
                    1.0f,
                    0,
                    100,
                    0,
                    20,
                    0,
                    120,
                    0,
                    -30,
                    0xFFFFFFFF,
                    0xFFFFFFFF,
                    Set.of()
            );
        }
    }
}
