package net.orifu.skin_overrides.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.orifu.xplat.gui.components.ObjectSelectionList;
import org.jetbrains.annotations.Nullable;

//? if >=1.20.1 {
import net.minecraft.client.gui.GuiGraphics;
        //?} else {
/*import net.minecraft.client.gui.GuiComponent;
import net.orifu.xplat.gui.GuiGraphics;
import com.mojang.blaze3d.vertex.PoseStack;
*///?}

public abstract class ObjectSelectionGrid<E extends ObjectSelectionList.Entry<E>> extends ObjectSelectionList<E> {
    protected final int itemWidth;
    protected int xTiles;

    public ObjectSelectionGrid(Minecraft client, int width, int height, int y, int itemWidth,
            int itemHeight, int xTiles) {
        super(client, width, height, y, itemHeight);

        this.itemWidth = itemWidth;
        this.xTiles = xTiles;
    }

    protected int xTiles() {
        return this.xTiles;
    }

    @Override
    public int contentHeight() {
        // divide entry count by x tiles
        return this.headerHeight + Mth.positiveCeilDiv(this.getItemCount(), this.xTiles()) * this.itemHeight + 4;
    }

    @Override
    public int getRowTop(int index) {
        // divide index by x tiles
        return this.getY() + 4 - (int) this.scrollAmount()
                + Mth.floorDiv(index, this.xTiles()) * this.itemHeight
                + this.headerHeight;
    }

    @Override
    /*? if >=1.20.6 {*/ protected void renderListItems(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    /*?} else if >=1.20.4 {*/ /*public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    *//*?} else if >=1.20.1 {*/ /*public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    *//*?} else*/ /*public void render(PoseStack graphics, int mouseX, int mouseY, float delta) {*/
        int baseX = this.getRowLeft();
        int w = this.itemWidth;
        int h = this.itemHeight - 4;

        for (int i = 0; i < this.getItemCount(); ++i) {
            // get X and Y accounting for the x tiles
            int x = baseX + i % this.xTiles() * this.itemWidth;
            int y = this.getRowTop(i);
            int y2 = this.getRowBottom(i);

            if (y2 >= this.getY() && y <= this.getBottom()) {
                this.renderItem(graphics, mouseX, mouseY, delta, i, x, y, w, h);
            }
        }
    }

    @Override
    protected void renderItem(
            /*? if >=1.20.1 {*/ GuiGraphics graphics, /*?} else*/ /*PoseStack graphics,*/
            int mouseX, int mouseY, float delta, int index, int x, int y, int width, int height) {
        // changed to use the new drawEntrySelectionHighlight method

        E entry = this.getEntry(index);
        boolean isHovered = this.getHovered() != null && this.getHovered().equals(entry);

        entry.renderBack(graphics, index, y, x, width, height, mouseX, mouseY, isHovered, delta);

        if (this.isSelectedItem(index)) {
            int borderColor = this.isFocused() ? 0xff_ffffff : 0xff_808080;
            this.renderSelection(graphics, x, y, borderColor, 0xff_000000);
        }

        entry.render(graphics, index, y, x, width, height, mouseX, mouseY, isHovered, delta);
    }

    //? if >=1.20.1 {
    protected void renderSelection(GuiGraphics graphics, int x, int y, int borderColor, int fillColor) {
        graphics.fill(x - 2, y - 2, x + this.itemWidth + 2, y + this.itemHeight + 2, borderColor);
        graphics.fill(x - 1, y - 1, x + this.itemWidth + 1, y + this.itemHeight + 1, fillColor);
    }
    //?} else {
    /*protected void renderSelection(PoseStack stack, int x, int y, int borderColor, int fillColor) {
        GuiComponent.fill(stack, x - 2, y - 2, x + this.itemWidth + 2, y + this.itemHeight + 2, borderColor);
        GuiComponent.fill(stack, x - 1, y - 1, x + itemWidth + 1, y + this.itemHeight + 1, fillColor);
    }
    *///?}

    @Override
    @Nullable
    protected E getEntryAtPosition(double x, double y) {
        double relativeX = x - this.getRowLeft();
        double unscrolledRelativeY = y - this.getY() - this.headerHeight - 4;
        double relativeY = unscrolledRelativeY + this.scrollAmount();

        // ensure position is within boundaries
        if (relativeX < 0 || relativeX > this.getRowWidth() || unscrolledRelativeY < 0
                || unscrolledRelativeY > this.getHeight()) {
            return null;
        }
        // ensure the position is not on the scroll bar
        if (x > this.scrollBarX()) {
            return null;
        }

        // convert the relative coordinates to item coordinates
        int entryX = (int) relativeX / this.itemWidth;
        int entryY = (int) relativeY / this.itemHeight;
        int i = entryY * this.xTiles() + entryX;

        return i < this.getItemCount() ? this.getEntry(i) : null;
    }

    @Override
    protected void ensureVisible(E entry) {
        int i = this.children().indexOf(entry);
        int top = this.getRowTop(i);
        int bottom = this.getRowBottom(i);

        if (top < this.getY()) {
            this.setScrollAmount(top - this.getRowTop(0));
        } else if (bottom > this.getBottom()) {
            this.setScrollAmount(bottom - this.height - this.getRowTop(0) + 4 + 4);
        }
    }

    @Deprecated
    protected void renderSelection(GuiGraphics graphics, int y, int entryWidth, int entryHeight,
            int borderColor, int fillColor) {
        // old bad method
        throw new UnsupportedOperationException();
    }
}
