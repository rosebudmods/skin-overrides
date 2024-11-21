package net.orifu.xplat.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.orifu.xplat.gui.GuiGraphics;

import java.util.function.Consumer;

public class ObjectSelectionList<E extends ObjectSelectionList.Entry<E>> extends net.minecraft.client.gui.components.ObjectSelectionList<E> {
    public ObjectSelectionList(Minecraft minecraft, int i, int j, int k, int l) {
        super(minecraft, i, j, k, l);
    }

    protected void renderListItems(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderListItems(graphics.portable(), mouseX, mouseY, delta);
    }

    @Override
    protected void renderListItems(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderListItems(new GuiGraphics(graphics), mouseX, mouseY, delta);
    }

    protected void renderItem(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, int index,
            int x, int y, int width, int height) {
        super.renderItem(guiGraphics, mouseX, mouseY, delta, index, x, y, width, height);
    }

    @Override
    protected void renderItem(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY,
            float delta, int index, int x, int y, int width, int height) {
        this.renderItem(new GuiGraphics(graphics), mouseX, mouseY, delta, index, x, y, width, height);
    }

    public static abstract class Entry<E extends Entry<E>> extends net.minecraft.client.gui.components.ObjectSelectionList.Entry<E> {
        public abstract void render(GuiGraphics graphics, int index, int y, int x,
                int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta);

        @Override
        public void render(net.minecraft.client.gui.GuiGraphics graphics, int index, int y, int x,
                int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.render(new GuiGraphics(graphics), index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        }
    }

    public <T extends GuiEventListener & Renderable & NarratableEntry> void addEntry(Consumer<LayoutElement> add, Consumer<T> screen) {
        add.accept(this);
    }
}
