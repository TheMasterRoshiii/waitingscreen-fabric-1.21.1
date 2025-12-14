package com.me.master.waitingscreen.mixin.client;

import net.minecraft.client.gui.screen.option.MouseOptionsScreen;
import net.minecraft.client.gui.screen.option.ChatOptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.screen.option.TelemetryInfoScreen;
import net.minecraft.client.gui.screen.option.OnlineOptionsScreen;
import com.me.master.waitingscreen.client.WaitingscreenClient;
import com.me.master.waitingscreen.client.screen.WaitingScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.gui.screen.pack.PackScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public Screen currentScreen;

    @Shadow
    public abstract void setScreen(Screen screen);

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void forceWaitingScreen(Screen screen, CallbackInfo ci) {
        if (!WaitingscreenClient.shouldBlockInput()) return;

        if (screen instanceof WaitingScreen) {
            return;
        }

        if (WaitingscreenClient.isAllowEscMenu()) {
            if (isAllowedScreen(screen)) {
                return;
            }

            if (screen == null && currentScreen != null && isAllowedScreen(currentScreen)) {
                return;
            }
        }

        ci.cancel();
        if (!(currentScreen instanceof WaitingScreen) && !isAllowedScreen(currentScreen)) {
            setScreen(new WaitingScreen());
        }
    }

    private boolean isAllowedScreen(Screen screen) {
        if (screen == null) return false;

        String screenName = screen.getClass().getName();

        return screen instanceof GameMenuScreen ||
                screen instanceof ControlsOptionsScreen ||
                screen instanceof VideoOptionsScreen ||
                screen instanceof SoundOptionsScreen ||
                screen instanceof LanguageOptionsScreen ||
                screen instanceof AccessibilityOptionsScreen ||
                screen instanceof MouseOptionsScreen ||
                screen instanceof ChatOptionsScreen ||
                screen instanceof SkinOptionsScreen ||
                screen instanceof TelemetryInfoScreen ||
                screen instanceof OnlineOptionsScreen ||
                screen instanceof PackScreen ||
                screen instanceof SocialInteractionsScreen ||
                screen instanceof AdvancementsScreen ||
                screen instanceof StatsScreen ||
                screenName.contains("OptionsScreen") ||
                screenName.contains("OptionsSubScreen");
    }
}
