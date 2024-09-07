package net.orifu.xplat.gui.widget;

import net.minecraft.text.Text;

import java.util.function.Function;

public class CyclingButtonWidget {
    public static <T>
            /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.button.CyclingButtonWidget
            /*?} else >>*/ /*net.minecraft.client.gui.widget.CyclingButtonWidget*/
            .Builder<T> builder(Function<T, Text> valueToText)
    {
        return /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.button.CyclingButtonWidget
                /*?} else >>*/ /*net.minecraft.client.gui.widget.CyclingButtonWidget*/
                .builder(valueToText);
    }
}
