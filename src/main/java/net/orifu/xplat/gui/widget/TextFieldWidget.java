package net.orifu.xplat.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class TextFieldWidget extends net.minecraft.client.gui.widget.TextFieldWidget {
    public TextFieldWidget(TextRenderer textRenderer, int width, int height, Text text) {
        //? if >=1.20.2 {
        super(textRenderer, width, height, text);
        //?} else
        /*super(textRenderer, 0, 0, width, height, text);*/
    }
}
