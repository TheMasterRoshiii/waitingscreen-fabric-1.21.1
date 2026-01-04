package com.me.master.waitingscreen.mixin.client;

import net.minecraft.client.session.telemetry.TelemetrySender;
import net.minecraft.client.session.telemetry.WorldSession;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldSession.class)
public class DisableTelemetryMixin {

    @ModifyVariable(
            method = "<init>(Lnet/minecraft/client/session/telemetry/TelemetrySender;ZLjava/time/Duration;Ljava/lang/String;)V",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1
    )
    private static TelemetrySender waitingscreen$replaceSender(TelemetrySender original) {
        return TelemetrySender.NOOP;
    }
}
