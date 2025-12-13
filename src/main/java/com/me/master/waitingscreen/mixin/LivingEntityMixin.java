package com.me.master.waitingscreen.mixin;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void preventDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            Waitingscreen mod = Waitingscreen.getInstance();
            if (mod.isWaitingActive() && mod.isProtectPlayers()) {
                boolean isOP = player.hasPermissionLevel(2);
                boolean isExempt = mod.isPlayerExempt(player.getUuid());
                
                if (!isOP && !isExempt) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
