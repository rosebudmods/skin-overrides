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
import net.orifu.skin_overrides.util.PlayerCapeRenderer;
import net.orifu.skin_overrides.util.ProfileHelper;

public class PlayerListEntry extends Entry<PlayerListEntry> {
    private final MinecraftClient client;
    public GameProfile profile;
    public final Type type;
    public final boolean isSkin;

    private final SkinOverridesScreen parent;

    public PlayerListEntry(MinecraftClient client, GameProfile profile, Type type, SkinOverridesScreen parent) {
        this.client = client;
        this.profile = profile;
        this.type = type;
        this.isSkin = parent.isSkin();
        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {
        // draw player face/cape
        if (this.isSkin) {
            PlayerFaceRenderer.draw(graphics, SkinOverrides.getSkin(this.profile), x, y, 32);
        } else {
            int capeX = x + (32 - PlayerCapeRenderer.WIDTH * 2) / 2;
            PlayerCapeRenderer.draw(graphics, SkinOverrides.getSkin(this.profile), capeX, y, 2);
        }

        // draw player name
        graphics.drawShadowedText(this.client.textRenderer, this.getPlayerName(), x + 32 + 2, y + 1, 0);
        // draw override status
        graphics.drawShadowedText(this.client.textRenderer, this.getOverrideStatus(), x + 32 + 2, y + 12, 0);
    }

    public GameProfile upgrade() {
        this.profile = ProfileHelper.tryUpgradeBasicProfile(this.profile);
        return this.profile;
    }

    protected Text getPlayerName() {
        Text name = Text.literal(this.profile.getName()).formatted(Formatting.WHITE);
        switch (this.type) {
            case USER:
                return Text.translatable("skin_overrides.player.you", name).formatted(Formatting.GRAY);
            case ONLINE:
                return Text.translatable("skin_overrides.player.online", name).formatted(Formatting.GRAY);
            case OFFLINE:
                return Text.translatable("skin_overrides.player.offline", name).formatted(Formatting.GRAY);
            default:
                throw new UnsupportedOperationException();
        }
    }

    protected Text getOverrideStatus() {
        if (this.isSkin) {
            if (Overrides.hasLocalSkinOverride(this.profile)) {
                return Text.translatable("skin_overrides.override.local_image").formatted(Formatting.GREEN);
            }

            var skinOverride = Overrides.getSkinCopyOverride(this.profile);
            if (skinOverride.isPresent()) {
                return skinOverride.get().description().formatted(Formatting.GREEN);
            }
        } else {
            if (Overrides.hasLocalCapeOverride(this.profile)) {
                return Text.translatable("skin_overrides.override.local_image").formatted(Formatting.GREEN);
            }

            var capeOverride = Overrides.getCapeCopyOverride(this.profile);
            if (capeOverride.isPresent()) {
                return capeOverride.get().description().formatted(Formatting.GREEN);
            }
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
        ONLINE,
        OFFLINE,
    }
}
