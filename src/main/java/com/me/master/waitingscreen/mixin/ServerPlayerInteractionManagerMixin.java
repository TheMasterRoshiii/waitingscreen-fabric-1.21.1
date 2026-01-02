package com.me.master.waitingscreen.mixin;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    
    @Final
    @Shadow
    protected ServerPlayerEntity player;
    
    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void blockInteraction(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        Waitingscreen mod = Waitingscreen.getInstance();
        if (mod.isWaitingActive() && mod.isBlockInteractions()) {
            boolean isOP = player.hasPermissionLevel(2);
            boolean isExempt = mod.isPlayerExempt(player.getUuid());
            
            if (!isOP && !isExempt) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        }
    }
    
    @Inject(method = "tryBreakBlock", at = @At("HEAD"), cancellable = true)
    private void blockBreaking(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Waitingscreen mod = Waitingscreen.getInstance();
        if (mod.isWaitingActive() && mod.isBlockInteractions()) {
            boolean isOP = player.hasPermissionLevel(2);
            boolean isExempt = mod.isPlayerExempt(player.getUuid());
            
            if (!isOP && !isExempt) {
                cir.setReturnValue(false);
            }
        }
    }
}
