package net.orifu.skin_overrides.xplat.gui.widget;

import net.minecraft.client.gui.widget.Widget;

import
        /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.layout.LayoutWidget
        /*?} else >>*/ /*net.minecraft.client.gui.widget.LayoutWidget*/ ;
import
        /*? if >=1.20.2 {*/ net.minecraft.client.gui.widget.layout.LayoutSettings
         /*?} else >>*/ /*net.minecraft.client.gui.widget.container.LayoutSettings*/ ;

import java.util.function.Consumer;

public class LinearLayoutWidget implements LayoutWidget {
    private final GridWidget grid = new GridWidget();
    private final Orientation orientation;
    private int children = 0;

    private LinearLayoutWidget(Orientation orientation) {
        this.orientation = orientation;
    }

    public static LinearLayoutWidget createHorizontal() {
        return new LinearLayoutWidget(Orientation.Horizontal);
    }

    public static LinearLayoutWidget createVertical() {
        return new LinearLayoutWidget(Orientation.Vertical);
    }

    public <T extends Widget> T add(T widget) {
        return this.add(widget, LayoutSettings.create());
    }

    public <T extends Widget> T add(T widget, LayoutSettings layout) {
        int index = this.children++;
        return switch (this.orientation) {
            case Horizontal -> grid.add(widget, 0, index, layout);
            case Vertical -> grid.add(widget, index, 0, layout);
        };
    }

    public LinearLayoutWidget setSpacing(int spacing) {
        this.grid.setSpacing(spacing);
        return this;
    }

    @Override
    public void visitChildren(Consumer<Widget> consumer) {
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
        Horizontal,
        Vertical
    }
}
