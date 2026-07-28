package com.me.master.waitingscreen.mixin.client;

import com.me.master.waitingscreen.client.network.ClientPacketHandlers;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void blockKeys(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (!ClientPacketHandlers.shouldBlockInput()) return;

        if (action == 1 || action == 2) {
            if (key == 256) {
                if (!ClientPacketHandlers.isAllowEscMenu()) {
                    ci.cancel();
                }
                return;
            }

            if (key == 290 || key == 292) {
                ci.cancel();
            }
        }
    }
}
