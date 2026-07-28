package com.me.master.waitingscreen.mixin.client;

import com.me.master.waitingscreen.client.network.ClientPacketHandlers;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    private void blockCameraMovement(CallbackInfo ci) {
        if (ClientPacketHandlers.shouldBlockInput()) {
            ci.cancel();
        }
    }
}
