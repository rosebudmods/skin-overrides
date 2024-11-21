package net.orifu.skin_overrides.util;

//? if >=1.16.5 {
import net.minecraft.network.chat.MutableComponent;
//?} else
/*import net.minecraft.text.BaseText;*/
import net.minecraft.network.chat.Component;

public class ComponentUtil {
    public static /*? if >=1.16.5 {*/ MutableComponent /*?} else*/ /*BaseText*/
            translatable(String translationKey) {
        //? if >=1.19 {
        return Component.translatable(translationKey);
        //?} else
        /*return new net.minecraft.text.TranslatableText(translationKey);*/
    }

    public static /*? if >=1.16.5 {*/ MutableComponent /*?} else*/ /*BaseText*/
            translatable(String translationKey, Object... objects) {
        //? if >=1.19 {
        return Component.translatable(translationKey, objects);
        //?} else
        /*return new net.minecraft.text.TranslatableText(translationKey, objects);*/
    }
}
