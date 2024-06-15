package net.orifu.skin_overrides.screen;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.PlayerFaceRenderer;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget.Entry;
import net.minecraft.text.Text;
import net.minecraft.util.CommonColors;
import net.orifu.skin_overrides.SkinOverrides;

public class PlayerListEntry extends Entry<PlayerListEntry> {
    private final MinecraftClient client;
    private final GameProfile profile;

    public PlayerListEntry(MinecraftClient client, GameProfile profile) {
        this.client = client;
        this.profile = profile;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {
        graphics.fill(x, y, x + entryWidth, y + entryHeight, CommonColors.GRAY);

        PlayerFaceRenderer.draw(graphics, SkinOverrides.getSkin(this.profile), x, y, 32);

        graphics.drawShadowedText(this.client.textRenderer, this.profile.getName(), x + 32 + 2, y + 1, 0xffffff);
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", "guh");
    }
}
