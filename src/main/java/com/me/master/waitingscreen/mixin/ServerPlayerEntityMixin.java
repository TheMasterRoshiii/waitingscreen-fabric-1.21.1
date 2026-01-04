package com.me.master.waitingscreen.mixin;

import com.me.master.waitingscreen.util.WaitingScreenUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onServerPlayerTick(CallbackInfo ci) {
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) (Object) this;

        if (WaitingScreenUtil.shouldRestrictPlayer(serverPlayer)) {
            if (Math.abs(serverPlayer.getVelocity().x) > 0.001 ||
                    Math.abs(serverPlayer.getVelocity().z) > 0.001) {
                serverPlayer.setVelocity(
                        0,
                        serverPlayer.getVelocity().y,
                        0
                );
            }
        }
    }
}
