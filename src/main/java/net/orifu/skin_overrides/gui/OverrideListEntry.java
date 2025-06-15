package net.orifu.skin_overrides.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.util.PlayerCapeRenderer;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.xplat.gui.components.ObjectSelectionList;
import org.jetbrains.annotations.NotNull;

public class OverrideListEntry extends ObjectSelectionList.Entry<OverrideListEntry> {
    private final Minecraft client;
    public GameProfile profile;
    public final Type type;
    public final OverrideManager ov;

    private final OverridesScreen parent;

    public OverrideListEntry(Minecraft client, GameProfile profile, Type type, OverridesScreen parent) {
        this.client = client;
        this.profile = profile;
        this.type = type;
        this.ov = parent.overrideManager();
        this.parent = parent;
    }

    @Override
    public void render(
            GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX,
            int mouseY, boolean hovered, float tickDelta) {
        // draw player face/cape
        if (this.ov.skin) {
            PlayerSkinRenderer.blitFace(graphics, Mod.override(this.profile), x, y, 4);
        } else {
            int capeX = x + (32 - PlayerCapeRenderer.WIDTH * 2) / 2;
            PlayerCapeRenderer.draw(graphics, Mod.override(this.profile), capeX, y, 2);
        }

        // draw player name
        graphics.drawString(this.client.font, this.getPlayerName(), x + 32 + 2, y + 1, 0xffffffff);
        // draw override status
        graphics.drawString(this.client.font, this.getOverrideStatus(), x + 32 + 2, y + 12, 0xffffffff);
    }

    public GameProfile upgrade() {
        this.profile = ProfileHelper.tryUpgradeBasicProfile(this.profile);
        return this.profile;
    }

    protected Component getPlayerName() {
        Component name = Component.literal(this.profile.getName()).withStyle(ChatFormatting.WHITE);
        return switch (this.type) {
            case USER ->
                    Component.translatable("skin_overrides.player.you", name).withStyle(ChatFormatting.GRAY);
            case ONLINE ->
                    Component.translatable("skin_overrides.player.online", name).withStyle(ChatFormatting.GRAY);
            case OFFLINE ->
                    Component.translatable("skin_overrides.player.offline", name).withStyle(ChatFormatting.GRAY);
        };
    }

    protected Component getOverrideStatus() {
        return this.ov.get(this.profile)
                .map(ov -> ov.info().copy().withStyle(ChatFormatting.GREEN))
                .orElse(Component.translatable("skin_overrides.override.none").withStyle(ChatFormatting.GRAY));
    }

    @Override
    @NotNull
    public Component getNarration() {
        return Component.translatable("narrator.select", this.profile.getName());
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
