package net.orifu.xplat.gui.tab;

import net.minecraft.client.gui.screen.ScreenArea;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public interface Tab extends
        /*? if >=1.20.1 {*/ net.minecraft.client.gui.tab.Tab
        /*?} else >>*/ /*net.minecraft.class_8087*/ {
    //? if <1.20.1 {
    /*Text getTitle();

    void visitChildren(Consumer<ClickableWidget> consumer);

    void refreshLayout(ScreenArea screenArea);

    @Override
    default Text method_48610() {
        return this.getTitle();
    }

    default void method_48612(Consumer<ClickableWidget> consumer) {
        this.visitChildren(consumer);
    }

    default void method_48611(ScreenArea screenArea) {
        this.refreshLayout(screenArea);
    }
    *///?}
}
