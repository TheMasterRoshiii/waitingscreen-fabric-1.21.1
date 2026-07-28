package com.me.master.waitingscreen.mixin.client;

import com.me.master.waitingscreen.client.network.ClientPacketHandlers;
import com.me.master.waitingscreen.client.screen.WaitingScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow public Screen currentScreen;
    @Shadow public abstract void setScreen(Screen screen);

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void forceWaitingScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof DeathScreen) return;
        if (currentScreen instanceof DeathScreen) return;
        if (!ClientPacketHandlers.shouldBlockInput()) return;
        if (screen instanceof WaitingScreen) return;

        if (ClientPacketHandlers.isAllowEscMenu()) {
            if (ClientPacketHandlers.isAllowedScreen(screen)) return;
            if (ClientPacketHandlers.isAllowedScreen(currentScreen)) return;
            if (screen == null) return;
        }

        ci.cancel();
        if (!(currentScreen instanceof WaitingScreen) && !ClientPacketHandlers.isAllowedScreen(currentScreen)) {
            setScreen(new WaitingScreen());
        }
    }
}
