package com.me.master.waitingscreen.mixin;

import com.me.master.waitingscreen.util.WaitingScreenUtil;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class PlayerInteractEntityMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onPlayerInteractEntity", at = @At("HEAD"), cancellable = true)
    private void blockEntityInteraction(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        if (WaitingScreenUtil.shouldBlockPlayerAction(player)) {
            ci.cancel();
        }
    }
}
