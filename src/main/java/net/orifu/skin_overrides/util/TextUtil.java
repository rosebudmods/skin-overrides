package net.orifu.skin_overrides.util;

//? if >=1.16.5 {
import net.minecraft.text.MutableText;
//?} else
/*import net.minecraft.text.BaseText;*/
import net.minecraft.text.Text;

public class TextUtil {
    public static /*? if >=1.16.5 {*/ MutableText /*?} else*/ /*BaseText*/
            translatable(String translationKey) {
        //? if >=1.19 {
        return Text.translatable(translationKey);
        //?} else
        /*return new net.minecraft.text.TranslatableText(translationKey);*/
    }

    public static /*? if >=1.16.5 {*/ MutableText /*?} else*/ /*BaseText*/
            translatable(String translationKey, Object... objects) {
        //? if >=1.19 {
        return Text.translatable(translationKey, objects);
        //?} else
        /*return new net.minecraft.text.TranslatableText(translationKey, objects);*/
    }
}
