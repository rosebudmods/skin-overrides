package net.orifu.skin_overrides.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget.Entry;
import net.minecraft.text.Text;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;

public class LibraryListEntry extends Entry<LibraryListEntry> {
    private final LibraryEntry entry;

    public LibraryListEntry(LibraryEntry entry) {
        this.entry = entry;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {
        var texture = this.entry.getTexture();
        var model = this.entry.getModel();
        PlayerSkinRenderer.draw(graphics, texture, model, x, y, 2);
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", this.entry.getName());
    }
}
