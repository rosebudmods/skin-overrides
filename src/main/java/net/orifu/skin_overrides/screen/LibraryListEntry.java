package net.orifu.skin_overrides.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget.Entry;
import net.minecraft.text.Text;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.Library.SkinEntry;
import net.orifu.skin_overrides.util.PlayerCapeRenderer;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;

public class LibraryListEntry extends Entry<LibraryListEntry> {
    public static final int SKIN_WIDTH = PlayerSkinRenderer.WIDTH * 2;
    public static final int SKIN_HEIGHT = PlayerSkinRenderer.HEIGHT * 2;
    public static final int SKIN_OFFSET = 16;
    public static final int PAD = 3;
    public static final int WIDTH = SKIN_WIDTH + SKIN_OFFSET * 2 + PAD * 2;
    public static final int HEIGHT = SKIN_HEIGHT + 2 + 7 + PAD * 2;
    public static final int CAPE_OFFSET = SKIN_OFFSET + (SKIN_WIDTH - PlayerCapeRenderer.WIDTH * 3) / 2;

    public final LibraryEntry entry;
    private final LibraryScreen parent;
    public final int index;
    private final MinecraftClient client;

    public LibraryListEntry(LibraryEntry entry, int index, LibraryScreen parent) {
        this.entry = entry;
        this.parent = parent;
        this.index = index;
        this.client = MinecraftClient.getInstance();
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {
        if (this.entry instanceof SkinEntry entry) {
            var texture = entry.getTexture();
            var model = entry.getModel();
            PlayerSkinRenderer.draw(graphics, texture, model, x + SKIN_OFFSET + PAD, y + PAD, 2);
        } else {
            PlayerCapeRenderer.draw(graphics, entry.getTexture(), x + CAPE_OFFSET + PAD, y + PAD, 3);
        }

        graphics.drawCenteredShadowedText(this.client.textRenderer, Text.literal(this.entry.getName()),
                x + WIDTH / 2, y + PAD + SKIN_HEIGHT + 2, 0xffffff);
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", this.entry.getName());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.parent.selectEntry(this);

        return true;
    }
}
