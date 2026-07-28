package com.me.master.waitingscreen.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.GameMenuScreen;

@Environment(EnvType.CLIENT)
public class WaitingGameMenuScreen extends GameMenuScreen {

    public WaitingGameMenuScreen() {
        super(true);
    }
}
