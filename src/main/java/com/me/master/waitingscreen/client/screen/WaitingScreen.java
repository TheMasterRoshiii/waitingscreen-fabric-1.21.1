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
        super(Text.translatable("waitingscreen.waiting"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderImage(context);

        context.getMatrices().push();
        context.getMatrices().scale(3, 3, 1);

        int centerX = (width / 2 + WaitingscreenClient.getPlayerCountX()) / 3;
        int y = (height / 2 + WaitingscreenClient.getPlayerCountY()) / 3;

        String a = String.valueOf(WaitingscreenClient.getCurrentPlayers());
        String b = "/";
        String c = String.valueOf(WaitingscreenClient.getRequiredPlayers());

        int wa = textRenderer.getWidth(a);
        int wb = textRenderer.getWidth(b);
        int wc = textRenderer.getWidth(c);

        int left = centerX - (wa + wb + wc) / 2;

        context.drawTextWithShadow(textRenderer, a, left, y, WaitingscreenClient.getPlayerCurrentColor());
        context.drawTextWithShadow(textRenderer, b, left + wa, y, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, c, left + wa + wb, y, WaitingscreenClient.getPlayerRequiredColor());

        context.getMatrices().pop();

        float s = WaitingscreenClient.getWaitingTextScale();
        if (s <= 0) s = 1.0f;

        context.getMatrices().push();
        context.getMatrices().scale(s, s, 1);

        int wtX = (int) ((width / 2f + WaitingscreenClient.getWaitingTextX()) / s);
        int wtY = (int) ((height / 2f + WaitingscreenClient.getWaitingTextY()) / s);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(WaitingscreenClient.getWaitingText()),
                wtX, wtY, WaitingscreenClient.getWaitingTextColor());

        context.getMatrices().pop();

        Text missingText = buildMissingText();
        if (missingText != null) {
            int mtX = width / 2 + WaitingscreenClient.getMissingTextX();
            int mtY = height / 2 + WaitingscreenClient.getMissingTextY();
            context.drawCenteredTextWithShadow(textRenderer, missingText, mtX, mtY, 0xFFFFFFFF);
        }

        if (WaitingscreenClient.isAllowEscMenu()) {
            int etX = width / 2 + WaitingscreenClient.getEscTextX();
            int etY = WaitingscreenClient.getEscTextY() < 0 ? height + WaitingscreenClient.getEscTextY() : WaitingscreenClient.getEscTextY();
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("waitingscreen.press_esc"),
                    etX, etY, 0xFFAAAAAA);
        }
    }

    private Text buildMissingText() {
        List<String> names = WaitingscreenClient.getMissingNames();
        int more = WaitingscreenClient.getMissingMore();

        if ((names == null || names.isEmpty()) && more <= 0) return null;

        String nameList = "";
        if (names != null && !names.isEmpty()) {
            nameList = String.join(", ", names);
        }

        if (more > 0 && !nameList.isEmpty()) {
            return Text.translatable("waitingscreen.missing_more", nameList, more);
        } else if (!nameList.isEmpty()) {
            return Text.translatable("waitingscreen.missing", nameList);
        }

        return null;
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

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getTextureManager().getTexture(tex) == null) {
            context.fill(0, 0, width, height, 0xFF000000);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("waitingscreen.loading", WaitingscreenClient.getCurrentScreen()),
                    width / 2, height / 2, 0xFFFFFFFF);
            return;
        }

        RenderSystem.setShaderTexture(0, tex);
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
