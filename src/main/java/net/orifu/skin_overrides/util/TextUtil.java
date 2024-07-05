package net.orifu.skin_overrides.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TextUtil {
    public static MutableText translatable(String translationKey) {
        //? if >=1.19 {
        return Text.translatable(translationKey);
        //?} else
        /*return new net.minecraft.text.TranslatableText(translationKey);*/
    }

    public static MutableText translatable(String translationKey, Object... objects) {
        //? if >=1.19 {
        return Text.translatable(translationKey, objects);
        //?} else
        /*return new net.minecraft.text.TranslatableText(translationKey, objects);*/
    }
}
