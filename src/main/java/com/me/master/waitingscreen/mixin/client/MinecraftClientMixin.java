package com.me.master.waitingscreen.mixin.client;

import com.me.master.waitingscreen.client.WaitingscreenClient;
import com.me.master.waitingscreen.client.screen.WaitingScreen;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public Screen currentScreen;
    @Shadow public abstract void setScreen(Screen screen);

    @Unique
    private static final Set<Class<? extends Screen>> ALLOWED_SCREENS = Set.of(
            GameMenuScreen.class,
            OptionsScreen.class,
            ControlsOptionsScreen.class,
            KeybindsScreen.class,
            VideoOptionsScreen.class,
            SoundOptionsScreen.class,
            LanguageOptionsScreen.class,
            AccessibilityOptionsScreen.class,
            MouseOptionsScreen.class,
            ChatOptionsScreen.class,
            SkinOptionsScreen.class,
            TelemetryInfoScreen.class,
            OnlineOptionsScreen.class,
            PackScreen.class,
            SocialInteractionsScreen.class,
            AdvancementsScreen.class,
            StatsScreen.class,
            DeathScreen.class
    );

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void forceWaitingScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof DeathScreen) return;
        if (currentScreen instanceof DeathScreen) return;

        if (!WaitingscreenClient.shouldBlockInput()) return;

        if (screen instanceof WaitingScreen) return;

        if (WaitingscreenClient.isAllowEscMenu()) {
            if (waitingscreen$isAllowedScreen(screen)) return;
            if (screen == null && waitingscreen$isAllowedScreen(currentScreen)) return;
        }

        ci.cancel();
        if (!(currentScreen instanceof WaitingScreen) && !waitingscreen$isAllowedScreen(currentScreen)) {
            setScreen(new WaitingScreen());
        }
    }

    @Unique
    private boolean waitingscreen$isAllowedScreen(Screen screen) {
        if (screen == null) return false;

        if (ALLOWED_SCREENS.contains(screen.getClass())) return true;

        String screenName = screen.getClass().getName();
        return screenName.contains("OptionsScreen")
                || screenName.contains("OptionScreen")
                || screenName.contains("KeybindsScreen")
                || screenName.contains("ControlsScreen");
    }
}
