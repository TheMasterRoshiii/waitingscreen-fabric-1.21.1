package com.me.master.waitingscreen.client.screen;

import com.me.master.waitingscreen.client.WaitingscreenClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class WaitingScreen extends Screen {

    public WaitingScreen() {
        super(Text.literal("Waiting"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderImage(context);

        context.getMatrices().push();
        context.getMatrices().scale(3, 3, 1);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(WaitingscreenClient.getCurrentPlayers() + "/" + WaitingscreenClient.getRequiredPlayers()),
                width / 2 / 3, height / 2 / 3 + 20, 0xFF00AAFF);
        context.getMatrices().pop();

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Esperando jugadores..."),
                width / 2, height / 2 + 100, 0xFFFFFFFF);

        if (WaitingscreenClient.isAllowEscMenu()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Presiona ESC para configuraciones"),
                    width / 2, height - 30, 0xFFAAAAAA);
        }
    }

    private void renderImage(DrawContext context) {
        Identifier tex = WaitingscreenClient.getTexture(WaitingscreenClient.getCurrentScreen());
        if (tex == null) {
            context.fill(0, 0, width, height, 0xFF000000);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Loading: " + WaitingscreenClient.getCurrentScreen()),
                    width / 2, height / 2, 0xFFFFFFFF);
            return;
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        context.drawTexture(tex, 0, 0, 0, 0, width, height, width, height);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return WaitingscreenClient.isAllowEscMenu();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (WaitingscreenClient.isAllowEscMenu()) {
            MinecraftClient.getInstance().setScreen(new GameMenuScreen(true));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && WaitingscreenClient.isAllowEscMenu()) {
            MinecraftClient.getInstance().setScreen(new GameMenuScreen(true));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
