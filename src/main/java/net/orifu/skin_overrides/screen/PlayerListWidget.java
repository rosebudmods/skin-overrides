package net.orifu.skin_overrides.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget;

public class PlayerListWidget extends AlwaysSelectedEntryListWidget<PlayerListEntry> {
    private static final int ITEM_HEIGHT = 36;

    public PlayerListWidget() {
        super(MinecraftClient.getInstance(), 0, 0, 0, ITEM_HEIGHT);

        this.addEntry(new PlayerListEntry(this.client));
        this.addEntry(new PlayerListEntry(this.client));
        this.addEntry(new PlayerListEntry(this.client));
    }
}
