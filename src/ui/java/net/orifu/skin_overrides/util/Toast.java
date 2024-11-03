package net.orifu.skin_overrides.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public class Toast {
    public static void show(Text title, Text description) {
        //? if >=1.21.3 {
        SystemToast.show(MinecraftClient.getInstance().method_1566(), SystemToast.Id.PACK_LOAD_FAILURE, title, description);
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
