package net.orifu.skin_overrides.screen;

import net.minecraft.client.MinecraftClient;
import net.orifu.skin_overrides.Library;

public class LibraryListWidget extends AlwaysSelectedEntryGridWidget<LibraryListEntry> {
    private final LibraryScreen parent;
    private final boolean isSkin;

    public LibraryListWidget(LibraryScreen parent, boolean isSkin) {
        super(MinecraftClient.getInstance(), 0, 0, 0,
                LibraryListEntry.WIDTH,
                isSkin ? LibraryListEntry.SKIN_ENTRY_HEIGHT : LibraryListEntry.CAPE_ENTRY_HEIGHT, 6);

        this.parent = parent;
        this.isSkin = isSkin;

        this.reload();
    }

    public void reload() {
        Library.reload();

        int i = 0;
        for (var entry : this.isSkin ? Library.skinEntries() : Library.capeEntries()) {
            boolean add = true;
            for (var child : this.children()) {
                if (child.entry.getId().equals(entry.getId())) {
                    add = false;
                    child.entry = entry;
                    child.index = i++;
                    break;
                }
            }

            if (add) {
                this.children().add(i, new LibraryListEntry(entry, i++, this.parent));
            }
        }
    }

    public void removeFromLibrary() {
        int index = this.getSelectedOrNull().index;
        int newIndex = index == this.children().size() - 1 ? index - 1 : index;
        // remove element from this list
        this.remove(index);

        // remove from library
        if (this.isSkin) {
            Library.removeSkin(index);
        } else {
            Library.removeCape(index);
        }
        this.reload();

        // update this and parent
        var newEntry = this.getEntry(newIndex);
        this.setSelected(newEntry);
        this.parent.selectEntry(newEntry);
    }

    public void moveSelection(int amount) {
        var newSelected = this.getEntry(this.getSelectedOrNull().index + amount);
        this.setSelected(newSelected);
        this.ensureVisible(newSelected);
        this.parent.selectEntry(newSelected);
    }

    @Override
    protected int xTiles() {
        return this.width / this.itemWidth;
    }

    @Override
    public int getRowWidth() {
        return this.xTiles() * this.itemWidth;
    }

    @Override
    public int getScrollbarPositionX() {
        return this.getXEnd() - SCROLLBAR_WIDTH;
    }
}
