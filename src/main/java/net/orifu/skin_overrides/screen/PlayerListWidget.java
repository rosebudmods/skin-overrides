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
        for (GameProfile profile : Overrides.profilesWithSkinOverride()) {
            if (!this.hasOverrideFor(profile)) {
                this.addEntry(new PlayerListEntry(this.client, profile, Type.OFFLINE, this.parent));
            }
        }
    }

    @Override
    public int getRowWidth() {
        return this.getWidth() - PADDING * 2;
    }

    @Override
    protected int getRowTop(int index) {
        return super.getRowTop(index) - 4 + PADDING;
    }

    public boolean hasOverrideFor(GameProfile profile) {
        for (var player : this.children()) {
            if (player.profile.equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
