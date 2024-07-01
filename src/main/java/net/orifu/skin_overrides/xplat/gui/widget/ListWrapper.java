package net.orifu.skin_overrides.xplat.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

public class ListWrapper<W extends AlwaysSelectedEntryListWidget<?>> extends ClickableWidget {
    protected final W listWidget;

    public ListWrapper(W listWidget) {
        super(0, 0, 0, 0, Text.empty());
        this.listWidget = listWidget;
    }

    //? if <1.20.2 {
    /*public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }
    *///?}

    @Override
    protected void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.listWidget.setDimensionsAndPosition(this.width, this.height, this.getX(), this.getY());
        this.listWidget.render(graphics, mouseX, mouseY, delta);
    }

    // TODO: mouse
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.listWidget.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void updateNarration(NarrationMessageBuilder builder) {
        // TODO
    }
}
