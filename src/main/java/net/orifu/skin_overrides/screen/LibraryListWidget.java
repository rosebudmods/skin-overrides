package net.orifu.skin_overrides.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget;
import net.orifu.skin_overrides.Library;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;

public class LibraryListWidget extends AlwaysSelectedEntryListWidget<LibraryListEntry> {
    private final Library library;

    public LibraryListWidget() {
        super(MinecraftClient.getInstance(), 0, 0, 0, PlayerSkinRenderer.HEIGHT * 2);

        this.library = new Library();

        for (var entry : this.library.entries()) {
            this.addEntry(new LibraryListEntry(entry));
        }
    }
}
