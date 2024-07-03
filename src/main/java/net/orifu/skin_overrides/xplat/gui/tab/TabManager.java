package net.orifu.skin_overrides.xplat.gui.tab;

import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.function.Consumer;

public class TabManager extends
        /*? if >=1.20.1 {*/ net.minecraft.client.gui.tab.TabManager
        /*?} else >>*/ /*net.minecraft.class_8088*/ {
    public TabManager(Consumer<ClickableWidget> add, Consumer<ClickableWidget> remove) {
        super(add, remove);
    }
}
