package net.orifu.skin_overrides.screen;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.screen.PlayerListEntry.Type;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.xplat.gui.widget.AlwaysSelectedEntryListWidget;

import java.util.ArrayList;
import java.util.Optional;

public class PlayerListWidget extends AlwaysSelectedEntryListWidget<PlayerListEntry> {
    private static final int PADDING = 8;
    private static final int ITEM_HEIGHT = 36;

    private final SkinOverridesScreen parent;
    public final OverrideManager ov;

    private final ArrayList<PlayerListEntry> allEntries = new ArrayList<>();
    private String query = "";

    public PlayerListWidget(SkinOverridesScreen parent, OverrideManager ov) {
        super(MinecraftClient.getInstance(), 0, 0, 0, ITEM_HEIGHT);

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

        this.updateFilter();

        // add offline players
        for (var futureProfile : this.ov.profilesWithOverride()) {
            futureProfile.thenAccept(profile -> {
                this.tryAddEntry(profile, Type.OFFLINE);
                this.updateFilter();
            });
        }
    }

    protected synchronized void tryAddEntry(GameProfile profile, Type type) {
        if (!this.hasOverrideFor(profile) || type.equals(Type.USER)) {
            this.allEntries.add(new PlayerListEntry(this.client, profile, type, this.parent));
        }
    }

    public PlayerListEntry addEntry(GameProfile profile) {
        return this.getOverrideFor(profile).orElseGet(() -> {
            PlayerListEntry entry = new PlayerListEntry(this.client, profile, Type.OFFLINE, this.parent);
            this.allEntries.add(entry);
            this.updateFilter();
            return entry;
        });
    }

    public boolean hasOverrideFor(GameProfile profile) {
        return this.getOverrideFor(profile).isPresent();
    }

    protected Optional<PlayerListEntry> getOverrideFor(GameProfile profile) {
        // special case when the user is unauthenticated and an actual player has their
        // username. because their uuids don't match, the authenticated account will
        // be listed separately to the current user otherwise.
        if (!this.allEntries.isEmpty()
                && ProfileHelper.user().getName().equalsIgnoreCase(profile.getName())) {
            return Optional.of(this.allEntries.get(0));
        }

        for (var player : this.allEntries) {
            if (player.profile.equals(profile)) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    public void filter(String query) {
        this.query = query.toLowerCase();
        this.updateFilter();
    }

    public void updateFilter() {
        this.children().clear(); // clearEntries removes selection
        for (var entry : this.allEntries) {
            if (entry.profile.getName().toLowerCase().contains(this.query)) {
                this.addEntry(entry);
            }
        }
    }

    public void ensureVisible(PlayerListEntry entry) {
        super.ensureVisible(entry);
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
    public int getRowTop(int index) {
        return super.getRowTop(index) - 4 + PADDING;
    }

    // pad bottom
    @Override
    public int getMaxScroll() {
        int maxScroll = super.getMaxScroll();
        return maxScroll > 0 ? Math.max(0, super.getMaxScroll() - 4 + PADDING * 2) : 0;
    }

    //?}
}