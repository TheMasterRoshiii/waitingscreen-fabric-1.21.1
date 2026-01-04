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

import java.util.List;

@Environment(EnvType.CLIENT)
public class WaitingScreen extends Screen {

    public WaitingScreen() {
        super(Text.translatable("waitingscreen.title"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderImage(context);

        context.getMatrices().push();
        context.getMatrices().scale(3, 3, 1);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(WaitingscreenClient.getCurrentPlayers() + "/" + WaitingscreenClient.getRequiredPlayers()),
                (int) (width / 2f / 3f), (int) (height / 2f / 3f + 20), 0xFF00AAFF);
        context.getMatrices().pop();

        float s = WaitingscreenClient.getWaitingTextScale();
        if (s <= 0) s = 1.0f;

        context.getMatrices().push();
        context.getMatrices().scale(s, s, 1);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(WaitingscreenClient.getWaitingText()),
                (int) (width / 2f / s), (int) ((height / 2f + 100) / s), WaitingscreenClient.getWaitingTextColor());
        context.getMatrices().pop();

        String missingLine = buildMissingLine();
        if (!missingLine.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(missingLine),
                    width / 2, height / 2 + 120, 0xFFFFFFFF);
        }

        if (WaitingscreenClient.isAllowEscMenu()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("waitingscreen.press_esc"),
                    width / 2, height - 30, 0xFFAAAAAA);
        }
    }

    private String buildMissingLine() {
        List<String> names = WaitingscreenClient.getMissingNames();
        int more = WaitingscreenClient.getMissingMore();

        if ((names == null || names.isEmpty()) && more <= 0) return "";

        StringBuilder sb = new StringBuilder();
        if (names != null && !names.isEmpty()) {
            sb.append(String.join(", ", names));
        }

        if (more > 0) {
            if (!names.isEmpty()) {
                return Text.translatable("waitingscreen.missing_more", sb.toString(), more).getString();
            }
            return "";
        }

        return Text.translatable("waitingscreen.missing", sb.toString()).getString();
    }

    private void renderImage(DrawContext context) {
        Identifier tex = WaitingscreenClient.getTexture(WaitingscreenClient.getCurrentScreen());
        if (tex == null) {
            context.fill(0, 0, width, height, 0xFF000000);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("waitingscreen.loading", WaitingscreenClient.getCurrentScreen()),
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
