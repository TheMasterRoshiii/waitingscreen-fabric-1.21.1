package com.me.master.waitingscreen.mixin;

import com.me.master.waitingscreen.Waitingscreen;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class PacketValidationMixin {
    
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", 
            at = @At("HEAD"), cancellable = true)
    private void validatePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        try {
            if (packet == null) {
                Waitingscreen.LOGGER.warn("Null packet received, cancelling");
                ci.cancel();
                return;
            }
            

            if (context == null || !context.channel().isActive()) {
                Waitingscreen.LOGGER.warn("Invalid channel context, cancelling packet");
                ci.cancel();
            }
        } catch (Exception e) {
            Waitingscreen.LOGGER.error("Critical error validating packet", e);
            ci.cancel();
        }
    }
}
