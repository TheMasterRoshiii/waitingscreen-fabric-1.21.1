package com.me.master.waitingscreen.mixin;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class PlayerActionMixin {
    
    @Shadow
    public ServerPlayerEntity player;
    
    @Inject(method = "onPlayerAction", at = @At("HEAD"), cancellable = true)
    private void blockItemDrop(PlayerActionC2SPacket packet, CallbackInfo ci) {
        Waitingscreen mod = Waitingscreen.getInstance();
        if (mod != null && mod.isWaitingActive() && mod.isBlockInteractions()) {
            boolean isOP = player.hasPermissionLevel(2);
            boolean isExempt = mod.isPlayerExempt(player.getUuid());
            
            if (!isOP && !isExempt) {
                PlayerActionC2SPacket.Action action = packet.getAction();
                if (action == PlayerActionC2SPacket.Action.DROP_ITEM || 
                    action == PlayerActionC2SPacket.Action.DROP_ALL_ITEMS) {
                    ci.cancel();
                }
            }
        }
    }
}
