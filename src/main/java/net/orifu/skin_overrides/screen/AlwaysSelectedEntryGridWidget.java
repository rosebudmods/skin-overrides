package net.orifu.skin_overrides.screen;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget.Entry;
import net.minecraft.util.math.MathHelper;

public abstract class AlwaysSelectedEntryGridWidget<E extends Entry<E>> extends AlwaysSelectedEntryListWidget<E> {
    protected final int itemWidth;
    protected int xTiles;

    public AlwaysSelectedEntryGridWidget(MinecraftClient client, int width, int height, int y, int itemWidth,
            int itemHeight, int xTiles) {
        super(client, width, height, y, itemHeight);

        this.itemWidth = itemWidth;
        this.xTiles = xTiles;
    }

    @Override
    protected int getMaxPosition() {
        // divide entry count by x tiles
        return this.headerHeight + MathHelper.ceilDiv(this.getEntryCount(), this.xTiles) * this.itemHeight + 4;
    }

    @Override
    protected int getRowTop(int index) {
        // divide index by x tiles
        return this.getY() + 4 - (int) this.getScrollAmount()
                + MathHelper.floorDiv(index, this.xTiles) * this.itemHeight
                + this.headerHeight;
    }

    @Override
    protected void renderList(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int baseX = this.getRowLeft();
        int w = this.itemWidth;
        int h = this.itemHeight - 4;

        for (int i = 0; i < this.getEntryCount(); ++i) {
            // get X and Y accounting for the x tiles
            int x = baseX + i % this.xTiles * this.itemWidth;
            int y = this.getRowTop(i);
            int y2 = this.getRowBottom(i);

            if (y2 >= this.getY() && y <= this.getYEnd()) {
                this.renderEntry(graphics, mouseX, mouseY, delta, i, x, y, w, h);
            }
        }
    }

    @Override
    protected void renderEntry(GuiGraphics graphics, int mouseX, int mouseY, float delta, int index, int x, int y,
            int width, int height) {
        // changed to use the new drawEntrySelectionHighlight method

        E entry = this.getEntry(index);
        boolean isHovered = this.getHoveredEntry() != null ? this.getHoveredEntry().equals(entry) : false;

        entry.drawBorder(graphics, index, y, x, width, height, mouseX, mouseY, isHovered, delta);

        if (this.isSelectedEntry(index)) {
            int borderColor = this.isFocused() ? 0xff_ffffff : 0xff_808080;
            this.drawEntrySelectionHighlight(graphics, x, y, borderColor, 0xff_000000);
        }

        entry.render(graphics, index, y, x, width, height, mouseX, mouseY, isHovered, delta);
    }

    protected void drawEntrySelectionHighlight(GuiGraphics graphics, int x, int y, int borderColor, int fillColor) {
        graphics.fill(x - 2, y - 2, x + this.itemWidth + 2, y + this.itemHeight + 2, borderColor);
        graphics.fill(x - 1, y - 1, x + this.itemWidth + 1, y + this.itemHeight + 1, fillColor);
    }

    @Override
    @Nullable
    protected final E getEntryAtPosition(double x, double y) {
        double relativeX = x - this.getRowLeft();
        double unscrolledRelativeY = y - this.getY() - this.headerHeight - 4;
        double relativeY = unscrolledRelativeY + this.getScrollAmount();

        // ensure position is within boundaries
        if (relativeX < 0 || relativeX > this.getRowWidth() || unscrolledRelativeY < 0
                || unscrolledRelativeY > this.getHeight()) {
            return null;
        }
        // ensure the position is not on the scroll bar
        if (x > this.getScrollbarPositionX()) {
            return null;
        }

        // convert the relative coordinates to item coordinates
        int entryX = (int) relativeX / this.itemWidth;
        int entryY = (int) relativeY / this.itemHeight;
        int i = entryY * this.xTiles + entryX;

        return i < this.getEntryCount() ? this.getEntry(i) : null;
    }

    @Override
    protected void ensureVisible(E entry) {
        int i = this.children().indexOf(entry);
        int top = this.getRowTop(i);
        int bottom = this.getRowBottom(i);

        if (top < this.getY()) {
            this.setScrollAmount(top - this.getRowTop(0));
        } else if (bottom > this.getYEnd()) {
            this.setScrollAmount(bottom - this.height - this.getRowTop(0) + 4 + 4);
        }
    }

    @Deprecated
    protected void drawEntrySelectionHighlight(GuiGraphics graphics, int y, int entryWidth, int entryHeight,
            int borderColor, int fillColor) {
        // old bad method
        throw new UnsupportedOperationException();
    }
}
