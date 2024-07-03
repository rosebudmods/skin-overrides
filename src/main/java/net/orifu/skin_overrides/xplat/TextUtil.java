package net.orifu.skin_overrides.xplat;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class TextUtil {
    public static MutableText translatable(String translationKey) {
        return Text.translatable(translationKey);
    }

    public static MutableText translatable(String translationKey, Object... objects) {
        return Text.translatable(translationKey, objects);
    }
}
