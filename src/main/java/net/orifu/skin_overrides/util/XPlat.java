package net.orifu.skin_overrides.util;

import java.lang.reflect.InvocationTargetException;

import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.text.Text;

public class XPlat {
    public static MultilineText createMultilineText(TextRenderer renderer, Text text, int width) {
        try {
            for (var method : MultilineText.class.getMethods()) {
                var params = method.getParameterTypes();
                if (params.length == 3 && params[2].equals(int.class)
                        && method.getReturnType().equals(MultilineText.class)) {
                    return (MultilineText) method.invoke(null, renderer, text, width);
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        }

        throw new RuntimeException("[skin overrides] failed to find method MultilineText.create");
    }

    public static void renderMultilineText(MultilineText text, GuiGraphics graphics, int centerX, int y) {
        try {
            for (var method : MultilineText.class.getMethods()) {
                var params = method.getParameterTypes();
                if (params.length == 3 && params[0].equals(GuiGraphics.class) && params[1].equals(int.class)
                        && params[2].equals(int.class)) {
                    method.invoke(text, graphics, centerX, y);
                    return;
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            System.out.println("invocation issue");
        }

        throw new RuntimeException("[skin overrides] failed to find method MultilineText.render");
    }
}
