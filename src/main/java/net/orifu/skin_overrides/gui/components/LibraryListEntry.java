package net.orifu.skin_overrides.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.gui.screen.LibraryScreen;
import net.orifu.skin_overrides.gui.util.PlayerModelRenderer;
import net.orifu.skin_overrides.library.SkinLibrary.SkinEntry;
import net.orifu.skin_overrides.gui.util.SimpleSkinRenderer;
import net.orifu.xplat.gui.components.ObjectSelectionList;

//? if >=1.20.1 {
import net.minecraft.client.gui.GuiGraphics;
 //?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
*///?}

public class LibraryListEntry extends ObjectSelectionList.Entry<LibraryListEntry> {
    public static final int SKIN_WIDTH = SimpleSkinRenderer.WIDTH * 2;
    public static final int SKIN_HEIGHT = SimpleSkinRenderer.HEIGHT * 2;
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
    /*? if >=1.20.1 {*/ public void render(GuiGraphics graphics,
    /*?} else*/ /*public void render(PoseStack graphics,*/
            int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float delta) {
        this.preview.draw(graphics, x + SKIN_OFFSET + PAD, y + PAD);

        if (this.isMouseOver(mouseX, mouseY)) {
            this.preview.spin(delta);
        } else {
            this.preview.setYRot(this.parent.ov.skin ? 0 : 180);
        }

        //? if >=1.20.1 {
        graphics.drawCenteredString(this.client.font, Component.literal(this.entry.getName()),
                x + WIDTH / 2, y + PAD + SKIN_HEIGHT + 2, 0xffffffff);
        //?} else {
        /*GuiComponent.drawCenteredString(graphics, this.client.font, Component.literal(this.entry.getName()).getVisualOrderText(),
                x + WIDTH / 2, y + PAD + SKIN_HEIGHT + 2, 0xffffff);
        *///?}
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
