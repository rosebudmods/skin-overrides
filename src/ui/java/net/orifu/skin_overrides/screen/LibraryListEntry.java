package net.orifu.skin_overrides.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.library.SkinLibrary.SkinEntry;
import net.orifu.skin_overrides.util.SkinModelRenderer;
import net.orifu.skin_overrides.util.PlayerCapeRenderer;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.xplat.gui.GuiGraphics;
import net.orifu.xplat.gui.widget.AlwaysSelectedEntryListWidget.Entry;

public class LibraryListEntry extends Entry<LibraryListEntry> {
    public static final int SKIN_WIDTH = PlayerSkinRenderer.WIDTH * 2;
    public static final int SKIN_HEIGHT = PlayerSkinRenderer.HEIGHT * 2;
    public static final int SKIN_OFFSET = 16;

    public static final int CAPE_WIDTH = PlayerCapeRenderer.WIDTH * 3;
    public static final int CAPE_HEIGHT = PlayerCapeRenderer.HEIGHT * 3;
    public static final int CAPE_OFFSET = SKIN_OFFSET + (SKIN_WIDTH - CAPE_WIDTH) / 2;

    public static final int PAD = 3;
    public static final int WIDTH = SKIN_WIDTH + SKIN_OFFSET * 2 + PAD * 2;
    public static final int SKIN_ENTRY_HEIGHT = SKIN_HEIGHT + 2 + 7 + PAD * 2;
    public static final int CAPE_ENTRY_HEIGHT = CAPE_HEIGHT + 2 + 7 + PAD * 2;

    private final LibraryScreen parent;
    private final MinecraftClient client;

    public LibraryEntry entry;
    public int index;

    private SkinModelRenderer preview;

    public LibraryListEntry(LibraryEntry entry, int index, LibraryScreen parent) {
        this.parent = parent;
        this.client = MinecraftClient.getInstance();

        this.entry = entry;
        this.index = index;

        if (this.entry instanceof SkinEntry skinEntry) {
            this.preview = new SkinModelRenderer(skinEntry.toSkin(), 2, this.client);
            this.preview.setPitchAndYaw(0, 0);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float delta) {
        int textOffset;
        if (this.entry instanceof SkinEntry) {
            this.preview.draw(graphics.portable(), x + SKIN_OFFSET + PAD, y + PAD);
            textOffset = SKIN_HEIGHT;

            if (this.isMouseOver(mouseX, mouseY)) {
                this.preview.spin(delta);
            } else {
                this.preview.setYaw(0);
            }
        } else {
            PlayerCapeRenderer.draw(graphics, entry.getTexture(), x + CAPE_OFFSET + PAD, y + PAD, 3);
            textOffset = CAPE_HEIGHT;
        }

        graphics.drawCenteredShadowedText(this.client.textRenderer, Text.literal(this.entry.getName()),
                x + WIDTH / 2, y + PAD + textOffset + 2, 0xffffff);
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
