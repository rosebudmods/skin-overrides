package net.orifu.skin_overrides.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId;
import net.minecraft.network.chat.Component;

public class Toast {
    public static void show(Component title, Component description) {
        //? if >=1.21.3 {
        SystemToast.addOrUpdate(Minecraft.getInstance().getToastManager(), SystemToastId.PACK_LOAD_FAILURE, title, description);
        //?} else {
        /*MinecraftClient.getInstance().getToastManager().add(new SystemToast(
                SystemToast.
                        /^? if >=1.20.6 {^/ /^Id.PACK_LOAD_FAILURE
                        ^//^?} else if =1.20.4 {^/ /^C_ozahoshp.field_47585
                        ^//^?} else >>^/ Type.PACK_LOAD_FAILURE ,
                title, description
        ));
        *///?}
    }
}
