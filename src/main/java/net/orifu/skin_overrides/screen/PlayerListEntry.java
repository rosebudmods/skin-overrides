package net.orifu.skin_overrides.screen;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.PlayerFaceRenderer;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget.Entry;
import net.minecraft.text.Text;
import net.orifu.skin_overrides.SkinOverrides;
import net.orifu.skin_overrides.screen.SkinOverridesScreen.OverridesTab;

public class PlayerListEntry extends Entry<PlayerListEntry> {
    private final MinecraftClient client;
    private final GameProfile profile;

    private final OverridesTab parent;

    public PlayerListEntry(MinecraftClient client, GameProfile profile, OverridesTab parent) {
        this.client = client;
        this.profile = profile;
        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {
        // draw player face
        PlayerFaceRenderer.draw(graphics, SkinOverrides.getSkin(this.profile), x, y, 32);

        // draw player name
        graphics.drawShadowedText(this.client.textRenderer, this.profile.getName(), x + 32 + 2, y + 1, 0xffffff);
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", this.profile.getName());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.parent.select(this);

        return true;
    }
}
