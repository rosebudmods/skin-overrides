package net.orifu.xplat.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LayoutElement;

//? if <1.20.4
/*import net.minecraft.client.gui.layouts.FrameLayout;*/

import java.util.function.Consumer;

public abstract class ObjectSelectionList<E extends ObjectSelectionList.Entry<E>> extends net.minecraft.client.gui.components.ObjectSelectionList<E> {
    //? if <1.20.2 || >=1.21.4
    public static final int SCROLLBAR_WIDTH = 6;

    //? if <1.20.4
    /*private final ListShim shim = new ListShim();*/

    public ObjectSelectionList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        //? if >=1.20.4 {
        super(minecraft, width, height, y, itemHeight);
        //?} else
        /*super(minecraft, width, height, y, y + height, itemHeight);*/

        //? if <1.20.2
        /*this.setRenderTopAndBottom(false);*/
    }

    public static abstract class Entry<E extends Entry<E>> extends net.minecraft.client.gui.components.ObjectSelectionList.Entry<E> {
    }

    public void addEntry(Consumer<LayoutElement> add, Consumer<ObjectSelectionList<E>> screen) {
        //? if >=1.20.4 {
        add.accept(this);
        //?} else {
        /*add.accept(this.shim);
        screen.accept(this);
        *///?}
    }

    //? if <1.21.4 {
    /*public double scrollAmount() {
        return this.getScrollAmount();
    }

    @Override
    public int getScrollbarPosition() {
        return this.scrollBarX();
    }

    protected int scrollBarX() {
        return super.getScrollbarPosition();
    }

    @Override
    public int getMaxPosition() {
        return this.contentHeight();
    }

    public int contentHeight() {
        return super.getMaxPosition();
    }
    *///?}

    //? if <1.20.4 {
    /*public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        this.shim.setMinDimensions(width, height);
    }

    public int getX() {
        return this.x0;
    }

    public int getRight() {
        return this.x1;
    }

    public int getY() {
        return this.y0;
    }

    public int getBottom() {
        return this.y1;
    }

    public void setX(int x) {
        this.x0 = x;
        this.x1 = x + this.width;
    }

    public void setY(int y) {
        this.y0 = y;
        this.y1 = y + this.height;
    }

    public class ListShim extends FrameLayout {
        @Override
        public void arrangeElements() {
            super.arrangeElements();

            // set dimensions
            ObjectSelectionList.this.setSize(this.width, this.height);

            // set position
            ObjectSelectionList.this.setX(this.getX());
            ObjectSelectionList.this.setY(this.getY());
        }

        @Override
        public void setX(int x) {
            super.setX(x);
            ObjectSelectionList.this.setX(x);
        }

        @Override
        public void setY(int y) {
            super.setY(y);
            ObjectSelectionList.this.setY(y);
        }
    }
    *///?}
}
