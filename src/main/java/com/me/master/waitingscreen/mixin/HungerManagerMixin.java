package com.me.master.waitingscreen.mixin;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    
    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void freezeHunger(PlayerEntity player, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            Waitingscreen mod = Waitingscreen.getInstance();
            if (mod.isWaitingActive() && mod.isFreezeHunger()) {
                boolean isOP = serverPlayer.hasPermissionLevel(2);
                boolean isExempt = mod.isPlayerExempt(serverPlayer.getUuid());
                
                if (!isOP && !isExempt) {
                    ci.cancel();
                }
            }
        }
    }
}
