package net.orifu.skin_overrides.xplat.gui.widget;

import net.orifu.skin_overrides.xplat.gui.tab.Tab;
import net.orifu.skin_overrides.xplat.gui.tab.TabManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HeaderBar extends
        /*? if >=1.20.1 {*/ net.minecraft.client.gui.widget.HeaderBar
        /*?} else >>*/ /*net.minecraft.class_8089*/ {
    private HeaderBar(int width, TabManager manager, List<Tab> tabs) {
        super(width, manager, new ArrayList<>(tabs));
    }

    public static Builder builder(TabManager tabManager, int width) {
        return new Builder(width, tabManager);
    }


    //? if <1.20.1 {
    /*public void setFocusedTab(int tab, boolean playClickSound) {
        super.method_48987(tab, playClickSound);
    }

    public void setWidth(int width) {
        super.method_48618(width);
    }

    public void arrangeElements() {
        super.method_49613();
    }
    *///?}

    public static class Builder {
        private final int width;
        private final TabManager manager;
        private final List<Tab> tabs = new ArrayList<>();

        private Builder(int width, TabManager manager) {
            this.width = width;
            this.manager = manager;
        }

        public Builder tabs(Tab... tabs) {
            Collections.addAll(this.tabs, tabs);
            return this;
        }

        public HeaderBar build() {
            return new HeaderBar(this.width, this.manager, this.tabs);
        }
    }
}
