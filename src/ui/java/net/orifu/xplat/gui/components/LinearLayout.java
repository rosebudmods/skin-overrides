package net.orifu.xplat.gui.components;

//? if >=1.20.2 {
public class LinearLayout extends net.minecraft.client.gui.layouts.LinearLayout {
    public LinearLayout(int i, int j, Orientation orientation) {
        super(i, j, orientation);
    }
}
//?} else {
/*import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;

import java.util.function.Consumer;

public class LinearLayout implements Layout {
    private final GridLayout grid = new GridLayout();
    private final Orientation orientation;
    private int children = 0;

    private LinearLayout(Orientation orientation) {
        this.orientation = orientation;
    }

    public static LinearLayout horizontal() {
        return new LinearLayout(Orientation.HORIZONTAL);
    }

    public static LinearLayout vertical() {
        return new LinearLayout(Orientation.VERTICAL);
    }

    public <T extends LayoutElement> T addChild(T element) {
        return this.addChild(element, LayoutSettings.defaults());
    }

    public <T extends LayoutElement> T addChild(T element, LayoutSettings layout) {
        int index = this.children++;
        return switch (this.orientation) {
            case HORIZONTAL -> grid.addChild(element, 0, index, layout);
            case VERTICAL -> grid.addChild(element, index, 0, layout);
        };
    }

    public LinearLayout spacing(int spacing) {
        this.grid.spacing(spacing);
        return this;
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        this.grid.visitChildren(consumer);
    }

    @Override
    public void arrangeElements() {
        this.grid.arrangeElements();
    }

    @Override
    public void setX(int x) {
        this.grid.setX(x);
    }

    @Override
    public void setY(int y) {
        this.grid.setY(y);
    }

    @Override
    public int getX() {
        return this.grid.getX();
    }

    @Override
    public int getY() {
        return this.grid.getY();
    }

    @Override
    public int getWidth() {
        return this.grid.getWidth();
    }

    @Override
    public int getHeight() {
        return this.grid.getHeight();
    }

    public enum Orientation {
        HORIZONTAL,
        VERTICAL
    }
}
*///?}
