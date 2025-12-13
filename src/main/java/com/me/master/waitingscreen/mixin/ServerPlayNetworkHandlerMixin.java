package com.me.master.waitingscreen.mixin;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onCommandExecution", at = @At("HEAD"), cancellable = true)
    private void blockCommands(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        Waitingscreen mod = Waitingscreen.getInstance();
        if (mod != null && mod.isWaitingActive()) {
            boolean isOP = player.hasPermissionLevel(2);
            boolean isExempt = mod.isPlayerExempt(player.getUuid());

            if (!isOP && !isExempt) {
                player.sendMessage(Text.literal("§cCommands are disabled during waiting screen"), false);
                ci.cancel();
            }
        }
    }
}
