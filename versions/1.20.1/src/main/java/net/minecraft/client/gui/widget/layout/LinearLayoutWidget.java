package net.minecraft.client.gui.widget.layout;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.container.LayoutSettings;

public class LinearLayoutWidget extends net.minecraft.client.gui.widget.LinearLayoutWidget {
    public LinearLayoutWidget(int width, int height, Orientation orientation) {
        super(width, height, orientation);
    }

    public static LinearLayoutWidget createHorizontal() {
        return new LinearLayoutWidget(0, 0, Orientation.HORIZONTAL);
    }

    public static LinearLayoutWidget createVertical() {
        return new LinearLayoutWidget(0, 0, Orientation.VERTICAL);
    }

    public <T extends Widget> T add(T widget) {
        return this.addChild(widget);
    }

    public <T extends Widget> T add(T widget, LayoutSettings layout) {
        return this.addChild(widget, layout);
    }

    public LinearLayoutWidget setSpacing(int spacing) {
        // noop?
        return this;
    }
}
