package com.me.master.waitingscreen.mixin;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPlayerTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (player instanceof ServerPlayerEntity serverPlayer) {
            Waitingscreen mod = Waitingscreen.getInstance();
            if (mod.isWaitingActive()) {
                boolean isOP = serverPlayer.hasPermissionLevel(2);
                boolean isExempt = mod.isPlayerExempt(serverPlayer.getUuid());

                if (!isOP && !isExempt) {
                    serverPlayer.setVelocity(
                            0,
                            Math.max(serverPlayer.getVelocity().y, -0.1),
                            0
                    );
                }
            }
        }
    }
}
