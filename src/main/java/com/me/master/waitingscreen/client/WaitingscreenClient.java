package com.me.master.waitingscreen.client;

import com.me.master.waitingscreen.client.network.ClientPacketHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class WaitingscreenClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPacketHandlers.register();
        ClientEventHandlers.register();
    }
}
