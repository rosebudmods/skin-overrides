package net.orifu.xplat.gui;

public class LayoutSettings {
    public static
        /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.layout.LayoutSettings
        /*?} else >>*/ /*net.minecraft.client.gui.widget.container.LayoutSettings*/
    create() {
        return new
                /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.layout.LayoutSettings
                /*?} else >>*/ /*net.minecraft.client.gui.widget.container.LayoutSettings*/
                .Impl();
    }
}
