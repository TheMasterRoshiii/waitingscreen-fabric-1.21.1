package com.me.master.waitingscreen.client.screen;

import com.me.master.waitingscreen.client.network.ClientPacketHandlers;
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

        int centerX = (width / 2 + ClientPacketHandlers.getPlayerCountX()) / 3;
        int y = (height / 2 + ClientPacketHandlers.getPlayerCountY()) / 3;

        String a = String.valueOf(ClientPacketHandlers.getCurrentPlayers());
        String b = "/";
        String c = String.valueOf(ClientPacketHandlers.getRequiredPlayers());

        int wa = textRenderer.getWidth(a);
        int wb = textRenderer.getWidth(b);
        int wc = textRenderer.getWidth(c);

        int left = centerX - (wa + wb + wc) / 2;

        context.drawTextWithShadow(textRenderer, a, left, y, ClientPacketHandlers.getPlayerCurrentColor());
        context.drawTextWithShadow(textRenderer, b, left + wa, y, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, c, left + wa + wb, y, ClientPacketHandlers.getPlayerRequiredColor());

        context.getMatrices().pop();

        float s = ClientPacketHandlers.getWaitingTextScale();
        if (s <= 0) s = 1.0f;

        context.getMatrices().push();
        context.getMatrices().scale(s, s, 1);

        int wtX = (int) ((width / 2f + ClientPacketHandlers.getWaitingTextX()) / s);
        int wtY = (int) ((height / 2f + ClientPacketHandlers.getWaitingTextY()) / s);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(ClientPacketHandlers.getWaitingText()),
                wtX, wtY, ClientPacketHandlers.getWaitingTextColor());

        context.getMatrices().pop();

        Text missingText = buildMissingText();
        if (missingText != null) {
            int mtX = width / 2 + ClientPacketHandlers.getMissingTextX();
            int mtY = height / 2 + ClientPacketHandlers.getMissingTextY();
            context.drawCenteredTextWithShadow(textRenderer, missingText, mtX, mtY, 0xFFFFFFFF);
        }

        if (ClientPacketHandlers.isAllowEscMenu()) {
            int etX = width / 2 + ClientPacketHandlers.getEscTextX();
            int etY = ClientPacketHandlers.getEscTextY() < 0 ? height + ClientPacketHandlers.getEscTextY() : ClientPacketHandlers.getEscTextY();
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("waitingscreen.press_esc"),
                    etX, etY, 0xFFAAAAAA);
        }
    }

    private Text buildMissingText() {
        List<String> names = ClientPacketHandlers.getMissingNames();
        int more = ClientPacketHandlers.getMissingMore();

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
        Identifier tex = ClientPacketHandlers.getTexture(ClientPacketHandlers.getCurrentScreen());
        if (tex == null) {
            context.fill(0, 0, width, height, 0xFF000000);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("waitingscreen.loading", ClientPacketHandlers.getCurrentScreen()),
                    width / 2, height / 2, 0xFFFFFFFF);
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getTextureManager().getTexture(tex) == null) {
            context.fill(0, 0, width, height, 0xFF000000);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.translatable("waitingscreen.loading", ClientPacketHandlers.getCurrentScreen()),
                    width / 2, height / 2, 0xFFFFFFFF);
            return;
        }

        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        context.drawTexture(tex, 0, 0, 0, 0, width, height, width, height);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return ClientPacketHandlers.isAllowEscMenu();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (ClientPacketHandlers.isAllowEscMenu()) {
            MinecraftClient.getInstance().setScreen(new WaitingGameMenuScreen());
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256 && ClientPacketHandlers.isAllowEscMenu()) {
            MinecraftClient.getInstance().setScreen(new WaitingGameMenuScreen());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
