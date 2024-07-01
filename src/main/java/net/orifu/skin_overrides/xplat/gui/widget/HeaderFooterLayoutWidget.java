package net.orifu.skin_overrides.xplat.gui.widget;

import net.minecraft.client.gui.screen.Screen;

public class HeaderFooterLayoutWidget extends
        /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget
        /*?} else >>*/ /*net.minecraft.client.gui.widget.HeaderAndFooterWidget*/ {
    public HeaderFooterLayoutWidget(Screen screen) {
        super(screen);
    }
}
