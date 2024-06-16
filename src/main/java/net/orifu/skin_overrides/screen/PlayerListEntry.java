package net.orifu.skin_overrides.screen;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.PlayerFaceRenderer;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget.Entry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.orifu.skin_overrides.Overrides;
import net.orifu.skin_overrides.SkinOverrides;

public class PlayerListEntry extends Entry<PlayerListEntry> {
    private final MinecraftClient client;
    public final GameProfile profile;
    public final Type type;

    private final SkinOverridesScreen parent;

    public PlayerListEntry(MinecraftClient client, GameProfile profile, Type type, SkinOverridesScreen parent) {
        this.client = client;
        this.profile = profile;
        this.type = type;
        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {
        // draw player face
        PlayerFaceRenderer.draw(graphics, SkinOverrides.getSkin(this.profile), x, y, 32);

        // draw player name
        graphics.drawShadowedText(this.client.textRenderer, this.getPlayerName(), x + 32 + 2, y + 1, 0);
        // draw override status
        graphics.drawShadowedText(this.client.textRenderer, this.getOverrideStatus(), x + 32 + 2, y + 12, 0);
    }

    protected Text getPlayerName() {
        Text name = Text.literal(this.profile.getName()).formatted(Formatting.WHITE);
        switch (this.type) {
            case USER:
                return Text.translatable("skin_overrides.player.you", name).formatted(Formatting.GRAY);
            case OFFLINE:
                return Text.translatable("skin_overrides.player.offline", name).formatted(Formatting.GRAY);
            case ONLINE:
                return Text.literal("wip");
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected Text getOverrideStatus() {
        if (Overrides.hasSkinImageOverride(this.profile)) {
            return Text.translatable("skin_overrides.override.local_image").formatted(Formatting.GREEN);
        }

        var copyOverride = Overrides.getSkinCopyOverride(this.profile);
        if (copyOverride.isPresent()) {
            return Text
                    .translatable("skin_overrides.override.copy", copyOverride.get().getName())
                    .formatted(Formatting.GREEN);
        }

        return Text.translatable("skin_overrides.override.none").formatted(Formatting.GRAY);
    }

    @Override
    public Text getNarration() {
        return Text.translatable("narrator.select", this.profile.getName());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.parent.selectPlayer(this);

        return true;
    }

    enum Type {
        USER,
        OFFLINE,
        ONLINE,
    }
}
