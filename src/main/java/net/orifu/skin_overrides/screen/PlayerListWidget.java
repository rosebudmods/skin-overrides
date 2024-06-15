package net.orifu.skin_overrides.screen;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget;

public class PlayerListWidget extends AlwaysSelectedEntryListWidget<PlayerListEntry> {
    private static final int ITEM_HEIGHT = 36;

    public PlayerListWidget() {
        super(MinecraftClient.getInstance(), 0, 0, 0, ITEM_HEIGHT);

        // add local player
        GameProfile localPlayer = this.client.method_53462();
        this.addEntry(new PlayerListEntry(this.client, localPlayer));
    }
}
