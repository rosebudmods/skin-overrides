package net.orifu.skin_overrides.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget.Entry;
import net.minecraft.text.Text;
import net.minecraft.util.CommonColors;

public class PlayerListEntry extends Entry<PlayerListEntry> {
    private final MinecraftClient client;

    public PlayerListEntry(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {
        graphics.fill(x, y, x + entryWidth, y + entryHeight, CommonColors.GRAY);
        graphics.drawShadowedText(this.client.textRenderer, Text.literal("guh"), x, y, 0xffffff);
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", "guh");
    }
}
