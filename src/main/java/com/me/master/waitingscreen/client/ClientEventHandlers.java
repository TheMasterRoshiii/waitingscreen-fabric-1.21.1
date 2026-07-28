package com.me.master.waitingscreen.client;

import com.me.master.waitingscreen.client.network.ClientPacketHandlers;
import com.me.master.waitingscreen.client.screen.WaitingScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.ClientPlayerEntity;

@Environment(EnvType.CLIENT)
public class ClientEventHandlers {

    private static boolean wasInWaiting = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(ClientEventHandlers::onClientTick);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientPacketHandlers.cleanupTextures();
            ClientPacketHandlers.reset();
            wasInWaiting = false;
        });
    }

    private static void onClientTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen instanceof DeathScreen) return;

        boolean shouldShow = isWaitingActive(mc.player);

        if (shouldShow) {
            if (!wasInWaiting) wasInWaiting = true;

            if (mc.currentScreen == null) {
                mc.setScreen(new WaitingScreen());
            } else if (!(mc.currentScreen instanceof WaitingScreen) && !ClientPacketHandlers.isAllowedScreen(mc.currentScreen)) {
                mc.setScreen(new WaitingScreen());
            }
        } else {
            if (wasInWaiting) {
                if (mc.currentScreen instanceof WaitingScreen) mc.setScreen(null);
                wasInWaiting = false;
            }
        }
    }

    private static boolean isWaitingActive(ClientPlayerEntity player) {
        return ClientPacketHandlers.isWaitingActive() && player != null && !ClientPacketHandlers.isExempt();
    }
}
