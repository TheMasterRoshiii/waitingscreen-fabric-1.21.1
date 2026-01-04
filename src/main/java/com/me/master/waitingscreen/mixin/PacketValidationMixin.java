package com.me.master.waitingscreen.mixin;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Slf4j
@Mixin(ClientConnection.class)
public class PacketValidationMixin {

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V",
            at = @At("HEAD"), cancellable = true)
    private void validatePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        try {
            if (packet == null) {
                log.warn("Null packet received, cancelling");
                ci.cancel();
                return;
            }

            if (context == null || !context.channel().isActive()) {
                log.warn("Invalid channel context, cancelling packet");
                ci.cancel();
            }
        } catch (Exception e) {
            log.error("Critical error validating packet", e);
            ci.cancel();
        }
    }
}
