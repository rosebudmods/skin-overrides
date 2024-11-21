package net.orifu.skin_overrides.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.gui.OverrideListEntry.Type;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.xplat.gui.components.ObjectSelectionList;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class OverridesSelectionList extends ObjectSelectionList<OverrideListEntry> {
    private static final int PADDING = 8;
    private static final int ITEM_HEIGHT = 36;

    private final OverridesScreen parent;
    public final OverrideManager ov;

    private final ArrayList<OverrideListEntry> allEntries = new ArrayList<>();
    private String query = "";

    private final ArrayList<CompletableFuture<GameProfile>> loadingProfiles;

    public OverridesSelectionList(OverridesScreen parent, OverrideManager ov) {
        super(Minecraft.getInstance(), 0, 0, 0, ITEM_HEIGHT);

        this.parent = parent;
        this.ov = ov;

        // add local player
        GameProfile localPlayer = ProfileHelper.user();
        this.tryAddEntry(localPlayer, Type.USER);

        // add online players
        if (this.minecraft.player != null) {
            for (var player : this.minecraft.player.connection.getOnlinePlayers()) {
                this.tryAddEntry(player.getProfile(), Type.ONLINE);
            }
        }

        this.updateFilter();

        // add offline players
        this.loadingProfiles = new ArrayList<>(this.ov.profilesWithOverride());
    }

    public void tick() {
        // add loaded profiles to the list
        this.loadingProfiles.removeIf(futureProfile -> {
            if (futureProfile.isDone()) {
                this.tryAddEntry(futureProfile.getNow(null), Type.OFFLINE);
                this.updateFilter();
                return true;
            }

            return false;
        });
    }

    protected void tryAddEntry(GameProfile profile, Type type) {
        if (!this.hasOverrideFor(profile) || type.equals(Type.USER)) {
            this.allEntries.add(new OverrideListEntry(this.minecraft, profile, type, this.parent));
        }
    }

    public OverrideListEntry addEntry(GameProfile profile) {
        return this.getOverrideFor(profile).orElseGet(() -> {
            OverrideListEntry entry = new OverrideListEntry(this.minecraft, profile, Type.OFFLINE, this.parent);
            this.allEntries.add(entry);
            this.updateFilter();
            return entry;
        });
    }

    public boolean hasOverrideFor(GameProfile profile) {
        return this.getOverrideFor(profile).isPresent();
    }

    protected Optional<OverrideListEntry> getOverrideFor(GameProfile profile) {
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

    public void ensureVisible(OverrideListEntry entry) {
        super.ensureVisible(entry);
    }

    // pad left and right
    @Override
    public int getRowWidth() {
        return Math.min(this.width - PADDING * 2, 220);
    }

    // fix scrollbar position
    @Override
    public int getScrollbarPosition() {
        return this.getRight();
    }

    //? if >=1.20.4 {
    
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