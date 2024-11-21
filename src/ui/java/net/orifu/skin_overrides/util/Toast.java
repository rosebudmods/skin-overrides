package net.orifu.skin_overrides.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

public class Toast {
    public static void show(Component title, Component description) {
        //? if >=1.21.3 {
        SystemToast.addOrUpdate(Minecraft.getInstance().getToastManager(), SystemToast.SystemToastId.PACK_LOAD_FAILURE, title, description);
        //?} else {
        /*Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.
                /^? if >=1.20.4 {^/ /^SystemToastId ^//^?} else >>^/ SystemToastIds
                .PACK_LOAD_FAILURE, title, description));
        *///?}
    }
}
