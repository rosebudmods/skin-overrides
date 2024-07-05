package net.orifu.skin_overrides.screen;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.util.PlayerCapeRenderer;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.xplat.gui.GuiGraphics;
import net.orifu.xplat.gui.widget.AlwaysSelectedEntryListWidget.Entry;

public class PlayerListEntry extends Entry<PlayerListEntry> {
    private final MinecraftClient client;
    public GameProfile profile;
    public final Type type;
    public final OverrideManager ov;

    private final SkinOverridesScreen parent;

    public PlayerListEntry(MinecraftClient client, GameProfile profile, Type type, SkinOverridesScreen parent) {
        this.client = client;
        this.profile = profile;
        this.type = type;
        this.ov = parent.overrideManager();
        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {
        // draw player face/cape
        if (this.ov.skin) {
            PlayerSkinRenderer.drawFace(graphics, Mod.override(this.profile), x, y, 4);
        } else {
            int capeX = x + (32 - PlayerCapeRenderer.WIDTH * 2) / 2;
            PlayerCapeRenderer.draw(graphics, Mod.override(this.profile), capeX, y, 2);
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
        return this.ov.get(this.profile)
                .map(ov -> ov.info().copy().formatted(Formatting.GREEN))
                .orElse(Text.translatable("skin_overrides.override.none").formatted(Formatting.GRAY));
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
