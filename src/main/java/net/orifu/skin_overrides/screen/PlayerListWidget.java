package net.orifu.skin_overrides.screen;

import com.mojang.authlib.GameProfile;

import java.util.ArrayList;

import net.minecraft.client.MinecraftClient;
//? if >=1.20.2 {
import net.minecraft.client.gui.widget.list.AlwaysSelectedEntryListWidget;
//?} else
/*import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;*/
import net.orifu.skin_overrides.override.Overridden;
import net.orifu.skin_overrides.screen.PlayerListEntry.Type;
import net.orifu.skin_overrides.util.ProfileHelper;

public class PlayerListWidget extends AlwaysSelectedEntryListWidget<PlayerListEntry> {
    private static final int PADDING = 8;
    private static final int ITEM_HEIGHT = 36;

    private final SkinOverridesScreen parent;
    public final Overridden ov;

    private final ArrayList<PlayerListEntry> allEntries = new ArrayList<>();
    private String query = "";

    public PlayerListWidget(SkinOverridesScreen parent, Overridden ov) {
        //? if >=1.20.4 {
        super(MinecraftClient.getInstance(), 0, 0, 0, ITEM_HEIGHT);
        //?} else
        /*super(MinecraftClient.getInstance(), 0, 0, 0, 0, ITEM_HEIGHT);*/

        this.parent = parent;
        this.ov = ov;

        // add local player
        GameProfile localPlayer = ProfileHelper.user();
        this.tryAddEntry(localPlayer, Type.USER);

        // add online players
        if (this.client.player != null) {
            for (var player : this.client.player.networkHandler.getPlayerList()) {
                this.tryAddEntry(player.getProfile(), Type.ONLINE);
            }
        }

        // add offline players
        for (GameProfile profile : this.ov.profilesWithOverride()) {
            this.tryAddEntry(profile, Type.OFFLINE);
        }

        this.filter(this.query);
    }

    protected void tryAddEntry(GameProfile profile, Type type) {
        if (!this.hasOverrideFor(profile) || type.equals(Type.USER)) {
            this.allEntries.add(new PlayerListEntry(this.client, profile, type, this.parent));
        }
    }

    public boolean hasOverrideFor(GameProfile profile) {
        // special case when the user is unauthenticated and an actual player has their
        // username. because their uuids don't match, the authenticated account will
        // be listed separately to the current user otherwise.
        if (ProfileHelper.user().getName().equalsIgnoreCase(profile.getName())) {
            return true;
        }

        for (var player : this.allEntries) {
            if (player.profile.equals(profile)) {
                return true;
            }
        }
        return false;
    }

    public void filter(String query) {
        this.query = query.toLowerCase();

        this.children().clear(); // clearEntries removes selection
        for (var entry : this.allEntries) {
            if (entry.profile.getName().toLowerCase().contains(this.query)) {
                this.addEntry(entry);
            }
        }
    }

    // pad left and right
    @Override
    public int getRowWidth() {
        return Math.min(this.width - PADDING * 2, 220);
    }

    //? if >=1.20.4 {

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

    //?} else {

    /*public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setPosition(int x, int y) {
        this.left = x;
        this.top = y;
        this.right = x + this.width;
        this.bottom = y + this.height;
    }

    *///?}
}