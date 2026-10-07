package com.me.master.waitingscreen.server;

import com.me.master.waitingscreen.Waitingscreen;
import com.me.master.waitingscreen.command.WaitingScreenCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class ServerEventHandlers {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                WaitingScreenCommands.register(dispatcher));

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Waitingscreen mod = Waitingscreen.getInstance();
            mod.setCurrentServer(server);
            mod.loadConfig();
            mod.loadServerImages();
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            Waitingscreen mod = Waitingscreen.getInstance();
            mod.setCurrentServer(null);
            mod.onServerStopped();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                Waitingscreen.getInstance().onPlayerJoin(handler.player));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                Waitingscreen.getInstance().onPlayerLeave());

        ServerTickEvents.END_SERVER_TICK.register(server ->
                Waitingscreen.getInstance().onServerTick(server));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                Waitingscreen.getInstance().onPlayerRespawn(newPlayer));
    }
}
