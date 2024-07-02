package net.orifu.skin_overrides.screen;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.override.Overridden;
import net.orifu.skin_overrides.util.PlayerCapeRenderer;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.xplat.gui.widget.AlwaysSelectedEntryListWidget.Entry;

public class PlayerListEntry extends Entry<PlayerListEntry> {
    private final MinecraftClient client;
    public GameProfile profile;
    public final Type type;
    public final Overridden ov;

    private final SkinOverridesScreen parent;

    public PlayerListEntry(MinecraftClient client, GameProfile profile, Type type, SkinOverridesScreen parent) {
        this.client = client;
        this.profile = profile;
        this.type = type;
        this.ov = parent.overridden();
        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {
        // draw player face/cape
        if (this.ov.skin()) {
            PlayerSkinRenderer.drawFace(graphics, Mod.getSkin(this.profile), x, y, 4);
        } else {
            int capeX = x + (32 - PlayerCapeRenderer.WIDTH * 2) / 2;
            PlayerCapeRenderer.draw(graphics, Mod.getSkin(this.profile), capeX, y, 2);
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
        return switch (this.type) {
            case USER ->
                    Text.translatable("skin_overrides.player.you", name).formatted(Formatting.GRAY);
            case ONLINE ->
                    Text.translatable("skin_overrides.player.online", name).formatted(Formatting.GRAY);
            case OFFLINE ->
                    Text.translatable("skin_overrides.player.offline", name).formatted(Formatting.GRAY);
        };
    }

    protected Text getOverrideStatus() {
        if (this.ov.local().hasOverride(this.profile)) {
            return Text.translatable("skin_overrides.override.local_image").formatted(Formatting.GREEN);
        }

        var skinOverride = this.ov.library().getOverride(this.profile);
        if (skinOverride.isPresent()) {
            return Text.translatable("skin_overrides.override.library", skinOverride.get().name)
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

    public enum Type {
        USER,
        ONLINE,
        OFFLINE,
    }
}
