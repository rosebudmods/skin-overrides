package net.orifu.skin_overrides.xplat.gui.widget;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;

public class HeaderFooterLayoutWidget extends
        /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget
        /*?} else if =1.20.1 {*/ /*net.minecraft.client.gui.widget.HeaderAndFooterWidget
        *//*?} else >>*/ /*net.minecraft.class_8132*/ {
    public HeaderFooterLayoutWidget(Screen screen) {
        super(screen);
    }

    //? if <1.20.1 {
    /*public <T extends Widget> T addToHeader(T widget) {
        return super.method_48992(widget);
    }

    public <T extends Widget> T addToContents(T widget) {
        return super.method_48999(widget);
    }

    public <T extends Widget> T addToFooter(T widget) {
        return super.method_48996(widget);
    }

    public void setHeaderHeight(int height) {
        this.method_48995(height);
    }

    public int getFooterHeight() {
        return this.method_48994();
    }
    *///?}
}
