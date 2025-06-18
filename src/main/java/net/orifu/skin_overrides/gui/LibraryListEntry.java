package net.orifu.skin_overrides.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.gui.util.PlayerModelRenderer;
import net.orifu.skin_overrides.library.SkinLibrary.SkinEntry;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.xplat.gui.components.ObjectSelectionList;

public class LibraryListEntry extends ObjectSelectionList.Entry<LibraryListEntry> {
    public static final int SKIN_WIDTH = PlayerSkinRenderer.WIDTH * 2;
    public static final int SKIN_HEIGHT = PlayerSkinRenderer.HEIGHT * 2;
    public static final int SKIN_OFFSET = 16;

    public static final int PAD = 3;
    public static final int WIDTH = SKIN_WIDTH + SKIN_OFFSET * 2 + PAD * 2;
    public static final int SKIN_ENTRY_HEIGHT = SKIN_HEIGHT + 2 + 7 + PAD * 2;

    private final LibraryScreen parent;
    private final Minecraft client;

    public LibraryEntry entry;
    public int index;

    private PlayerModelRenderer preview;

    public LibraryListEntry(LibraryEntry entry, int index, LibraryScreen parent) {
        this.parent = parent;
        this.client = Minecraft.getInstance();

        this.entry = entry;
        this.index = index;

        if (this.entry instanceof SkinEntry skinEntry) {
            this.preview = new PlayerModelRenderer(skinEntry.getId(), skinEntry.toSkin(), 2, this.client);
            this.preview.setRotation(0, 0);
        } else {
            this.preview = new PlayerModelRenderer(entry.getId(), parent.userSkin, 2, this.client);
            this.preview.setCape(this.entry.getTexture());
            this.preview.setRotation(0, 180);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float delta) {
        this.preview.draw(graphics, x + SKIN_OFFSET + PAD, y + PAD);

        if (this.isMouseOver(mouseX, mouseY)) {
            this.preview.spin(delta);
        } else {
            this.preview.setYRot(this.parent.ov.skin ? 0 : 180);
        }

        graphics.drawCenteredString(this.client.font, Component.literal(this.entry.getName()),
                x + WIDTH / 2, y + PAD + SKIN_HEIGHT + 2, 0xffffffff);
    }

    @Override
    public Component getNarration() {
        return Component.translatable("narrator.select", this.entry.getName());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.parent.selectEntry(this);

        return true;
    }
}
