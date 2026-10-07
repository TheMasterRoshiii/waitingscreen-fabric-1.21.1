package com.me.master.waitingscreen.client.screen;

import com.me.master.waitingscreen.client.network.ClientPacketHandlers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.util.Identifier;


@Environment(EnvType.CLIENT)
public class WaitingGameMenuScreen extends GameMenuScreen {

    public WaitingGameMenuScreen() {
        super(true);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        renderWaitingImage(context);
    }

    private void renderWaitingImage(DrawContext context) {
        Identifier tex = ClientPacketHandlers.getTexture(ClientPacketHandlers.getCurrentScreen());
        if (tex == null) {
            context.fill(0, 0, width, height, 0xFF000000);
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getTextureManager().getTexture(tex) == null) {
            context.fill(0, 0, width, height, 0xFF000000);
            return;
        }

        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        context.drawTexture(tex, 0, 0, 0, 0, width, height, width, height);
    }
}
