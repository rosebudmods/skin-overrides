package net.orifu.xplat.gui.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ScreenArea;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.util.math.MatrixStack;
import net.orifu.xplat.gui.GuiGraphics;

import java.util.function.Consumer;

public abstract class AlwaysSelectedEntryListWidget
        <E extends AlwaysSelectedEntryListWidget.Entry<E>>
        extends /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget<E>
        /*?} else >>*/ /*net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget<E>*/ {
    //? if <1.20.2
    /*public static final int SCROLLBAR_WIDTH = 6;*/

    private final ListWidgetShim shim = new ListWidgetShim();

    public AlwaysSelectedEntryListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
        //? if >=1.20.4 {
        super(client, width, height, y, itemHeight);
        //?} else
        /*super(client, width, height, y, y + height, itemHeight);*/

        //? if <1.20.2
        /*this.setRenderHorizontalShadows(false);*/
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
    protected void method_25311(net.minecraft.client.gui.GuiGraphics graphics, int i, int j, float f) {
        this.renderList(new GuiGraphics(graphics), i, j, f);
    }
    //?} else {
    /*protected void renderList(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderList(graphics.portable(), mouseX, mouseY, delta);
    }
    *///?}

    protected void renderEntry(GuiGraphics graphics, int mouseX, int mouseY, float delta,
                               int index, int x, int y, int width, int height) {
        super.renderEntry(graphics.portable(), mouseX, mouseY, delta, index, x, y, width, height);
    }

    //? if <1.20.1 {
    /*@Override
    protected void renderList(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderList(new GuiGraphics(matrices), mouseX, mouseY, delta);
    }

    @Override
    protected void renderEntry(MatrixStack matrices, int mouseX, int mouseY, float delta, int index, int x, int y, int width, int height) {
        this.renderEntry(new GuiGraphics(matrices), mouseX, mouseY, delta, index, x, y, width, height);
    }
    *///?} else {
    @Override
    protected void renderEntry(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float delta, int index, int x, int y, int width, int height) {
        this.renderEntry(new GuiGraphics(graphics), mouseX, mouseY, delta, index, x, y, width, height);
    }
    //?}

    //? if <1.20.4 {
    /*public int getX() {
        return this.left;
    }

    public int getXEnd() {
        return this.right;
    }

    public int getY() {
        return this.top;
    }

    public int getYEnd() {
        return this.bottom;
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

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
        this.shim.setMinDimensions(width, height);
    }

    *///?}

    //? if <=1.20.4 {
    /*public void setDimensionsAndPosition(int width, int height, int x, int y) {
        this.setDimensions(width, height);
        this.setX(x);
        this.setY(y);
    }
    *///?}

    public <T extends Element & Drawable & Selectable> void add(Consumer<Widget> add, Consumer<T> screen) {
        //? if <=1.20.2 {
        /*add.accept(this.shim);
        screen.accept((T) this);
        *///?} else
        add.accept(this);
    }

    public class ListWidgetShim extends FrameWidget {
        @Override
        public void arrangeElements() {
            super.arrangeElements();
            AlwaysSelectedEntryListWidget.this.setDimensionsAndPosition(
                    this.width, this.height, this.getX(), this.getY());
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            AlwaysSelectedEntryListWidget.this.setX(x);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            AlwaysSelectedEntryListWidget.this.setY(y);
        }
    }

    public static abstract class Entry<E extends AlwaysSelectedEntryListWidget.Entry<E>>
            extends /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget.Entry<E>
            /*?} else >>*/ /*net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget.Entry<E>*/ {
        public abstract void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
                int mouseY, boolean hovered, float tickDelta);

        //? if <1.20.1 {
        /*@Override
        public final void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
                int mouseY, boolean hovered, float tickDelta) {
            this.render(new GuiGraphics(matrices), index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        }
        *///?} else {
        @Override
        public final void render(net.minecraft.client.gui.GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
                int mouseY, boolean hovered, float tickDelta) {
            this.render(new GuiGraphics(graphics), index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        }
        //?}
    }
}
