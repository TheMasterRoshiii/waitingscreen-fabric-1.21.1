package com.me.master.waitingscreen.mixin;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.network.ClientConnection;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ExceptionHandlerMixin {
    
    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    private void handleException(io.netty.channel.ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
        Waitingscreen mod = Waitingscreen.getInstance();
        if (mod != null && mod.isWaitingActive()) {
            Waitingscreen.LOGGER.error("Network exception during waiting screen", throwable);
            

            if (throwable.getMessage() != null) {
                if (throwable.getMessage().contains("Connection reset")) {
                    Waitingscreen.LOGGER.warn("Client connection reset detected");
                } else if (throwable.getMessage().contains("timeout")) {
                    Waitingscreen.LOGGER.warn("Client connection timeout detected");
                }
            }
        }
    }
}
