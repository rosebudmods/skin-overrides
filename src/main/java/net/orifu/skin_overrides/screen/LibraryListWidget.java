package net.orifu.skin_overrides.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.orifu.skin_overrides.Library;

public class LibraryListWidget extends AlwaysSelectedEntryGridWidget<LibraryListEntry> {
    private final Library library;
    private final LibraryScreen parent;

    public LibraryListWidget(LibraryScreen parent) {
        super(MinecraftClient.getInstance(), 0, 0, 0,
                LibraryListEntry.WIDTH, LibraryListEntry.HEIGHT, 6);

        this.library = new Library();
        this.parent = parent;

        for (var entry : this.library.entries()) {
            this.addEntry(new LibraryListEntry(entry, this.parent));
        }
    }

    @Override
    public int getRowWidth() {
        return this.xTiles * this.itemWidth;
    }

    @Override
    public int getScrollbarPositionX() {
        return this.getXEnd() - SCROLLBAR_WIDTH;
    }

    @Override
    public void renderList(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.xTiles = this.width / this.itemWidth;
        super.renderList(graphics, mouseX, mouseY, delta);
    }
}
