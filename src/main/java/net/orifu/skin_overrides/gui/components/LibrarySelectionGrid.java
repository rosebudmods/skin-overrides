package net.orifu.skin_overrides.gui.components;

import net.minecraft.client.Minecraft;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.gui.screen.LibraryScreen;

import java.util.ArrayList;

public class LibrarySelectionGrid extends ObjectSelectionGrid<LibraryListEntry> {
    private final LibraryScreen parent;
    private final OverrideManager ov;

    private final ArrayList<LibraryListEntry> allEntries = new ArrayList<>();
    private String query = "";

    public LibrarySelectionGrid(LibraryScreen parent, OverrideManager ov) {
        super(Minecraft.getInstance(), 0, 0, 0,
                LibraryListEntry.WIDTH, LibraryListEntry.SKIN_ENTRY_HEIGHT, 6);

        this.parent = parent;
        this.ov = ov;

        this.reload();
    }

    public int indexOf(LibraryListEntry entry) {
        return this.children().indexOf(entry);
    }

    public void reload() {
        this.ov.library().reload();

        int i = 0;
        for (var entry : this.ov.library().entries()) {
            boolean add = true;
            for (var child : this.allEntries) {
                if (child.entry.getId().equals(entry.getId())) {
                    add = false;
                    child.entry = entry;
                    child.index = i++;
                    break;
                }
            }

            if (add) {
                this.allEntries.add(i, new LibraryListEntry(entry, i++, this.parent));
            }
        }

        this.allEntries.sort((a, b) -> Integer.compare(a.index, b.index));
        this.filter(this.query);
    }

    public void removeFromLibrary() {
        int index = this.getSelected().index;
        int childIndex = this.indexOf(this.getSelected());
        int newIndex = childIndex == this.children().size() - 1 ? childIndex - 1 : childIndex;

        // remove from library
        this.allEntries.remove(index);
        this.ov.library().remove(index);
        this.reload();

        // update this and parent
        var newEntry = newIndex >= 0 ? this.getEntry(newIndex) : null;
        this.setSelected(newEntry);
        this.parent.selectEntry(newEntry);
    }

    public void move(int i, int j) {
        this.ov.library().move(this.children().get(i).index, this.children().get(j).index);
        this.reload();
    }

    public void moveSelection(int amount) {
        var newSelected = this.getEntry(this.indexOf(this.getSelected()) + amount);
        this.setSelected(newSelected);
        this.parent.selectEntry(newSelected);
    }

    public void filter(String query) {
        this.query = query.toLowerCase();

        this.children().clear(); // clearEntries removes selection
        for (var entry : this.allEntries) {
            if (entry.entry.getName().toLowerCase().contains(this.query)) {
                this.addEntry(entry);
            }
        }
    }

    @Override
    protected int xTiles() {
        return Math.max(this.width / this.itemWidth, 1);
    }

    @Override
    public int getRowWidth() {
        return this.xTiles() * this.itemWidth;
    }

    @Override
    protected int scrollBarX() {
        return this.getRight() - SCROLLBAR_WIDTH;
    }
}
