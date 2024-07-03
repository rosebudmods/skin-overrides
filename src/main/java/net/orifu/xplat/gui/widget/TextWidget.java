package net.orifu.xplat.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class TextWidget extends
        /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.text.TextWidget
        /*?} else >>*/ /*net.minecraft.client.gui.widget.TextWidget*/ {
    public TextWidget(Text text, TextRenderer renderer) {
        super(text, renderer);
    }

    public TextWidget(int width, int height, Text text, TextRenderer renderer) {
        super(width, height, text, renderer);
    }
}
