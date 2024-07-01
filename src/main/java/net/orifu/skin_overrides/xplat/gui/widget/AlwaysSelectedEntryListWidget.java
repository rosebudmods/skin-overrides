package net.orifu.skin_overrides.xplat.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ScreenArea;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;

import java.util.function.Consumer;

public abstract class AlwaysSelectedEntryListWidget
        <E extends AlwaysSelectedEntryListWidget.Entry<E>>
        extends /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget<E>
        /*?} else >>*/ /*net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget<E>*/ {
    public AlwaysSelectedEntryListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
        //? if >=1.20.4 {
        super(client, width, height, y, itemHeight);
        //?} else
        /*super(client, width, height, y, y + height, itemHeight);*/
    }

    @Override
    public ScreenArea getArea() {
        return super.getArea();
    }

    //? if >=1.20.6 {
    protected void renderList(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.method_25311(graphics, mouseX, mouseY, delta);
    }

    @Override
    protected void method_25311(GuiGraphics graphics, int i, int j, float f) {
        this.renderList(graphics, i, j, f);
    }
    //?}

    //? if <1.20.4 {
    /*public int getX() {
        return this.left;
    }

    public int getY() {
        return this.top;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setX(int x) {
        this.left = x;
        this.right = x + this.width;
    }

    public void setY(int y) {
        this.top = y;
        this.bottom = y + this.height;
    }

    public void setDimensionsAndPosition(int width, int height, int x, int y) {
        this.width = width;
        this.height = height;
        this.setX(x);
        this.setY(y);
    }

    *///?}

    public static abstract class Entry<E extends AlwaysSelectedEntryListWidget.Entry<E>>
            extends /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget.Entry<E>
            /*?} else >>*/ /*net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget.Entry<E>*/ {
    }
}
