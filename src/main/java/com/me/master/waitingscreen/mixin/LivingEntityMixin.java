package com.me.master.waitingscreen.mixin;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private static volatile Waitingscreen waitingscreen$instance;

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void waitingscreen$preventDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof ServerPlayerEntity player)) {
            return;
        }

        Waitingscreen mod = waitingscreen$instance;
        if (mod == null) {
            try {
                mod = Waitingscreen.getInstance();
                waitingscreen$instance = mod;
            } catch (IllegalStateException e) {
                return;
            }
        }

        if (!mod.isWaitingActive() || !mod.isProtectPlayers()) {
            return;
        }

        if (player.hasPermissionLevel(2) || mod.isPlayerExempt(player.getUuid())) {
            return;
        }

        cir.setReturnValue(false);
    }
}
