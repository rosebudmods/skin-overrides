package net.orifu.skin_overrides.xplat.gui.widget;

import net.minecraft.text.Text;

public class ButtonWidget extends
            /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.button.ButtonWidget
            /*?} else >>*/ /*net.minecraft.client.gui.widget.ButtonWidget*/ {
    protected ButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress, NarrationFactory narrationFactory) {
        super(x, y, width, height, message, onPress, narrationFactory);
    }
}
