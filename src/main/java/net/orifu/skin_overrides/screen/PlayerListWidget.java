package net.orifu.skin_overrides.screen;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget;
import net.orifu.skin_overrides.Overrides;
import net.orifu.skin_overrides.screen.PlayerListEntry.Type;

public class PlayerListWidget extends AlwaysSelectedEntryListWidget<PlayerListEntry> {
    private static final int PADDING = 8;
    private static final int ITEM_HEIGHT = 36;

    private final SkinOverridesScreen parent;

    public final boolean isSkin;

    public PlayerListWidget(SkinOverridesScreen parent, boolean isSkin) {
        super(MinecraftClient.getInstance(), 0, 0, 0, ITEM_HEIGHT);

        this.parent = parent;
        this.isSkin = isSkin;

        // add local player
        GameProfile localPlayer = this.client.method_53462();
        this.addEntry(new PlayerListEntry(this.client, localPlayer, Type.USER, this.parent));

        // add offline players
        var profiles = this.isSkin ? Overrides.profilesWithSkinOverride() : Overrides.profilesWithCapeOverride();
        for (GameProfile profile : profiles) {
            this.tryAddEntry(profile, Type.OFFLINE);
        }
    }

    public void tryAddEntry(GameProfile profile, Type type) {
        if (!this.hasOverrideFor(profile)) {
            this.addEntry(new PlayerListEntry(this.client, profile, type, this.parent));
        }
    }

    public boolean hasOverrideFor(GameProfile profile) {
        // special case when the user is unauthenticated and an actual player has their
        // username. because their uuids don't match, the authenticated account will
        // be listed separately to the current user otherwise.
        if (this.client.method_53462().getName().equalsIgnoreCase(profile.getName())) {
            return true;
        }

        for (var player : this.children()) {
            if (player.profile.equals(profile)) {
                return true;
            }
        }
        return false;
    }

    // pad left and right
    @Override
    public int getRowWidth() {
        return this.getWidth() - PADDING * 2;
    }

    // fix scrollbar position
    @Override
    public int getScrollbarPositionX() {
        return this.getXEnd();
    }

    // pad top
    @Override
    protected int getRowTop(int index) {
        return super.getRowTop(index) - 4 + PADDING;
    }

    // pad bottom
    @Override
    public int getMaxScroll() {
        int maxScroll = super.getMaxScroll();
        return maxScroll > 0 ? Math.max(0, super.getMaxScroll() - 4 + PADDING * 2) : 0;
    }
}
