package com.me.master.waitingscreen.util;

import com.me.master.waitingscreen.Waitingscreen;
import net.minecraft.server.network.ServerPlayerEntity;

public class WaitingScreenUtil {

    public static boolean shouldBlockPlayerAction(ServerPlayerEntity player) {
        Waitingscreen mod = Waitingscreen.getInstance();
        if (mod == null || !mod.isWaitingActive() || !mod.isBlockInteractions()) {
            return false;
        }

        boolean isOP = player.hasPermissionLevel(2);
        boolean isExempt = mod.isPlayerExempt(player.getUuid());

        return !isOP && !isExempt;
    }

    public static boolean shouldRestrictPlayer(ServerPlayerEntity player) {
        Waitingscreen mod = Waitingscreen.getInstance();
        if (mod == null || !mod.isWaitingActive()) {
            return false;
        }

        boolean isOP = player.hasPermissionLevel(2);
        boolean isExempt = mod.isPlayerExempt(player.getUuid());

        return !isOP && !isExempt;
    }
}
