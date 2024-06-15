package net.orifu.skin_overrides.screen;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget;
import net.orifu.skin_overrides.screen.SkinOverridesScreen.OverridesTab;

public class PlayerListWidget extends AlwaysSelectedEntryListWidget<PlayerListEntry> {
    private static final int PADDING = 8;
    private static final int ITEM_HEIGHT = 36;

    private final OverridesTab parent;

    public PlayerListWidget(OverridesTab parent) {
        super(MinecraftClient.getInstance(), 0, 0, 0, ITEM_HEIGHT);

        this.parent = parent;

        // add local player
        GameProfile localPlayer = this.client.method_53462();
        this.addEntry(new PlayerListEntry(this.client, localPlayer, this.parent));
    }

    @Override
    public int getRowWidth() {
        return this.getWidth() - PADDING * 2;
    }

    @Override
    protected int getRowTop(int index) {
        return super.getRowTop(index) - 4 + PADDING;
    }
}
