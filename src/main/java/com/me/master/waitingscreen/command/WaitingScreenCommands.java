package com.me.master.waitingscreen.command;

import com.me.master.waitingscreen.Waitingscreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import lombok.experimental.UtilityClass;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@UtilityClass
public class WaitingScreenCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("waitingscreen")
                .requires(s -> s.hasPermissionLevel(2))

                .then(CommandManager.literal("start")
                        .executes(c -> {
                            Waitingscreen.getInstance().startWaiting(4);
                            c.getSource().sendFeedback(() -> Text.literal("§aWaiting started (4 players required)"), true);
                            return 1;
                        })
                        .then(CommandManager.argument("players", IntegerArgumentType.integer(0, 999))
                                .executes(c -> {
                                    int players = IntegerArgumentType.getInteger(c, "players");
                                    Waitingscreen.getInstance().startWaiting(players);
                                    if (players == 0) {
                                        c.getSource().sendFeedback(() -> Text.literal("§aWaiting started (manual mode - use /waitingscreen stop)"), true);
                                    } else {
                                        c.getSource().sendFeedback(() -> Text.literal("§aWaiting started (" + players + " players required)"), true);
                                    }
                                    return 1;
                                })))

                .then(CommandManager.literal("stop")
                        .executes(c -> {
                            Waitingscreen.getInstance().stopWaiting();
                            c.getSource().sendFeedback(() -> Text.literal("§aWaiting stopped"), true);
                            return 1;
                        }))

                .then(CommandManager.literal("playerscount")
                        .then(CommandManager.argument("count", IntegerArgumentType.integer(0, 999))
                                .executes(c -> {
                                    int count = IntegerArgumentType.getInteger(c, "count");
                                    Waitingscreen.getInstance().setRequiredPlayers(count);
                                    if (count == 0) {
                                        c.getSource().sendFeedback(() -> Text.literal("§aPlayer count set to: " + count + " (manual mode)"), true);
                                    } else {
                                        c.getSource().sendFeedback(() -> Text.literal("§aPlayer count set to: " + count), true);
                                    }
                                    return 1;
                                })))

                .then(CommandManager.literal("whitelistmode")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(c -> {
                                    boolean enabled = BoolArgumentType.getBool(c, "enabled");
                                    Waitingscreen.getInstance().setWhitelistMode(enabled);
                                    c.getSource().sendFeedback(() -> Text.literal("§aWhitelist mode " + (enabled ? "enabled" : "disabled")), true);
                                    return 1;
                                })))

                .then(CommandManager.literal("missingnames")
                        .then(CommandManager.literal("showatmost")
                                .then(CommandManager.argument("value", IntegerArgumentType.integer(0, 999))
                                        .executes(c -> {
                                            int v = IntegerArgumentType.getInteger(c, "value");
                                            Waitingscreen.getInstance().setShowNamesWhenMissingAtMost(v);
                                            c.getSource().sendFeedback(() -> Text.literal("§aShow missing names when missing <= " + v), true);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("max")
                                .then(CommandManager.argument("value", IntegerArgumentType.integer(0, 999))
                                        .executes(c -> {
                                            int v = IntegerArgumentType.getInteger(c, "value");
                                            Waitingscreen.getInstance().setMaxNamesToShow(v);
                                            c.getSource().sendFeedback(() -> Text.literal("§aMax missing names to show = " + v), true);
                                            return 1;
                                        }))))

                .then(CommandManager.literal("ui")
                        .then(CommandManager.literal("text")
                                .then(CommandManager.argument("value", StringArgumentType.greedyString())
                                        .executes(c -> {
                                            String v = StringArgumentType.getString(c, "value");
                                            Waitingscreen.getInstance().setWaitingText(v);
                                            c.getSource().sendFeedback(() -> Text.literal("§aWaiting text updated"), true);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("color")
                                .then(CommandManager.argument("value", StringArgumentType.string())
                                        .executes(c -> {
                                            String raw = StringArgumentType.getString(c, "value");
                                            int color = parseColor(raw);
                                            Waitingscreen.getInstance().setWaitingTextColor(color);
                                            c.getSource().sendFeedback(() -> Text.literal("§aWaiting text color set to: " + raw), true);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("scale")
                                .then(CommandManager.argument("value", FloatArgumentType.floatArg(0.05f, 20.0f))
                                        .executes(c -> {
                                            float v = FloatArgumentType.getFloat(c, "value");
                                            Waitingscreen.getInstance().setWaitingTextScale(v);
                                            c.getSource().sendFeedback(() -> Text.literal("§aWaiting text scale set to: " + v), true);
                                            return 1;
                                        }))))

                .then(CommandManager.literal("allowesc")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(c -> {
                                    boolean enabled = BoolArgumentType.getBool(c, "enabled");
                                    Waitingscreen.getInstance().setAllowEscMenu(enabled);
                                    c.getSource().sendFeedback(() -> Text.literal("§aESC menu " + (enabled ? "enabled" : "disabled")), true);
                                    return 1;
                                })))

                .then(CommandManager.literal("blockchat")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(c -> {
                                    boolean enabled = BoolArgumentType.getBool(c, "enabled");
                                    Waitingscreen.getInstance().setBlockChat(enabled);
                                    c.getSource().sendFeedback(() -> Text.literal("§aChat blocking " + (enabled ? "enabled" : "disabled")), true);
                                    return 1;
                                })))

                .then(CommandManager.literal("protect")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(c -> {
                                    boolean enabled = BoolArgumentType.getBool(c, "enabled");
                                    Waitingscreen.getInstance().setProtectPlayers(enabled);
                                    c.getSource().sendFeedback(() -> Text.literal("§aPlayer protection " + (enabled ? "enabled" : "disabled")), true);
                                    return 1;
                                })))

                .then(CommandManager.literal("blockinteractions")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(c -> {
                                    boolean enabled = BoolArgumentType.getBool(c, "enabled");
                                    Waitingscreen.getInstance().setBlockInteractions(enabled);
                                    c.getSource().sendFeedback(() -> Text.literal("§aInteraction blocking " + (enabled ? "enabled" : "disabled")), true);
                                    return 1;
                                })))

                .then(CommandManager.literal("setscreen")
                        .then(CommandManager.argument("screen", StringArgumentType.string())
                                .suggests((c, b) -> net.minecraft.command.CommandSource.suggestMatching(
                                        Waitingscreen.getInstance().getAvailableScreens(), b))
                                .executes(c -> {
                                    String name = StringArgumentType.getString(c, "screen");
                                    if (Waitingscreen.getInstance().changeScreen(name)) {
                                        c.getSource().sendFeedback(() -> Text.literal("§aChanged to: " + name), true);
                                    } else {
                                        c.getSource().sendError(Text.literal("§cScreen not found: " + name));
                                    }
                                    return 1;
                                })))

                .then(CommandManager.literal("listscreens")
                        .executes(c -> {
                            c.getSource().sendFeedback(() -> Text.literal("§eAvailable screens:"), false);
                            for (String s : Waitingscreen.getInstance().getAvailableScreens()) {
                                c.getSource().sendFeedback(() -> Text.literal("§7- " + s), false);
                            }
                            return 1;
                        }))

                .then(CommandManager.literal("reload")
                        .executes(c -> {
                            Waitingscreen.getInstance().reloadScreens();
                            c.getSource().sendFeedback(() -> Text.literal("§aReloaded screens"), true);
                            return 1;
                        }))

                .then(CommandManager.literal("exempt")
                        .then(CommandManager.literal("add")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(c -> {
                                            ServerPlayerEntity p = EntityArgumentType.getPlayer(c, "player");
                                            Waitingscreen.getInstance().addExemptPlayer(p.getUuid());
                                            c.getSource().sendFeedback(() -> Text.literal("§aAdded " + p.getName().getString()), true);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("remove")
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .executes(c -> {
                                            ServerPlayerEntity p = EntityArgumentType.getPlayer(c, "player");
                                            Waitingscreen.getInstance().removeExemptPlayer(p.getUuid());
                                            c.getSource().sendFeedback(() -> Text.literal("§aRemoved " + p.getName().getString()), true);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("list")
                                .executes(c -> {
                                    c.getSource().sendFeedback(() -> Text.literal("§eExempt players:"), false);
                                    Waitingscreen.getInstance().getExemptPlayers().forEach(id ->
                                            c.getSource().sendFeedback(() -> Text.literal("§7" + id), false));
                                    return 1;
                                }))
                        .then(CommandManager.literal("clear")
                                .executes(c -> {
                                    Waitingscreen.getInstance().clearExemptPlayers();
                                    c.getSource().sendFeedback(() -> Text.literal("§aCleared exempt players"), true);
                                    return 1;
                                })))

                .then(CommandManager.literal("status")
                        .executes(c -> {
                            Waitingscreen mod = Waitingscreen.getInstance();
                            c.getSource().sendFeedback(() -> Text.literal("§e=== Waiting Screen Status ==="), false);
                            c.getSource().sendFeedback(() -> Text.literal("§eActive: " + mod.isWaitingActive()), false);
                            c.getSource().sendFeedback(() -> Text.literal("§ePlayers: " + mod.getCurrentPlayers() + "/" + mod.getRequiredPlayers()), false);
                            c.getSource().sendFeedback(() -> Text.literal("§eWhitelist Mode: " + mod.isWhitelistMode()), false);
                            c.getSource().sendFeedback(() -> Text.literal("§eMissing Names: showAtMost=" + mod.getShowNamesWhenMissingAtMost() + ", max=" + mod.getMaxNamesToShow()), false);
                            c.getSource().sendFeedback(() -> Text.literal("§eUI Text: " + mod.getWaitingText()), false);
                            c.getSource().sendFeedback(() -> Text.literal("§eUI Color: 0x" + Integer.toHexString(mod.getWaitingTextColor()).toUpperCase()), false);
                            c.getSource().sendFeedback(() -> Text.literal("§eUI Scale: " + mod.getWaitingTextScale()), false);
                            c.getSource().sendFeedback(() -> Text.literal("§eCurrent Screen: " + mod.getCurrentScreen()), false);
                            c.getSource().sendFeedback(() -> Text.literal("§eESC Menu Allowed: " + mod.isAllowEscMenu()), false);
                            c.getSource().sendFeedback(() -> Text.literal("§eChat Blocked: " + mod.isBlockChat()), false);
                            c.getSource().sendFeedback(() -> Text.literal("§ePlayer Protection: " + mod.isProtectPlayers()), false);
                            c.getSource().sendFeedback(() -> Text.literal("§eInteractions Blocked: " + mod.isBlockInteractions()), false);
                            return 1;
                        })));
    }

    private static int parseColor(String raw) {
        String s = raw.trim();
        if (s.startsWith("#")) s = s.substring(1);
        if (s.startsWith("0x") || s.startsWith("0X")) s = s.substring(2);

        if (s.length() == 6) {
            int rgb = (int) Long.parseLong(s, 16);
            return 0xFF000000 | rgb;
        }

        if (s.length() == 8) {
            return (int) Long.parseLong(s, 16);
        }

        return (int) Long.parseLong(s, 10);
    }
}
