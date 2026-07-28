package com.me.master.waitingscreen.mixin.client;

import com.me.master.waitingscreen.client.network.ClientPacketHandlers;
import com.me.master.waitingscreen.client.screen.WaitingScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Screen.class)
public abstract class ScreenBackgroundMixin {

    @Shadow public int width;
    @Shadow public int height;

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void waitingscreen$injectBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!ClientPacketHandlers.shouldBlockInput()) return;
        if ((Object) this instanceof WaitingScreen) return;

        Identifier tex = ClientPacketHandlers.getTexture(ClientPacketHandlers.getCurrentScreen());
        if (tex == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getTextureManager().getTexture(tex) == null) return;

        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        context.drawTexture(tex, 0, 0, 0, 0, width, height, width, height);
        ci.cancel();
    }
}
