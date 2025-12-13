package com.me.master.waitingscreen.mixin;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class NetworkSafetyMixin {
    
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static void safePacketHandling(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        try {
            if (listener instanceof ServerPlayNetworkHandler handler) {
                ServerPlayerEntity player = handler.player;
                Waitingscreen mod = Waitingscreen.getInstance();
                
                if (mod != null && mod.isWaitingActive() && player != null) {
                    if (player.isRemoved() || player.getHealth() <= 0) {
                        Waitingscreen.LOGGER.warn("Packet received for invalid player state: {}", player.getName().getString());
                        ci.cancel();
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Waitingscreen.LOGGER.error("Error handling packet safely", e);
            ci.cancel();
        }
    }
}
