package net.orifu.skin_overrides.xplat.gui.widget;

import net.minecraft.client.gui.widget.Widget;

import
        /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.layout.LayoutSettings
         /*?} else >>*/ /*net.minecraft.client.gui.widget.container.LayoutSettings*/ ;

public class LinearLayoutWidget extends
        /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.layout.LinearLayoutWidget
        /*?} else >>*/ /*net.minecraft.client.gui.widget.LinearLayoutWidget*/ {
    public LinearLayoutWidget(int width, int height, Orientation orientation) {
        super(width, height, orientation);
    }

    public static LinearLayoutWidget createHorizontal() {
        return new LinearLayoutWidget(0, 0, Orientation.HORIZONTAL);
    }

    public static LinearLayoutWidget createVertical() {
        return new LinearLayoutWidget(0, 0, Orientation.VERTICAL);
    }

    //? if <1.20.2 {
    /*public <T extends Widget> T add(T widget) {
        return this.addChild(widget);
    }

    public <T extends Widget> T add(T widget, LayoutSettings layout) {
        return this.addChild(widget, layout);
    }

    public LinearLayoutWidget setSpacing(int spacing) {
        // noop?
        return this;
    }
    *///?}
}
