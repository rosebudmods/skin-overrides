package net.orifu.skin_overrides.override;

import java.util.ArrayList;
import java.util.List;

import com.mojang.authlib.GameProfile;

import net.orifu.skin_overrides.Library;
import net.orifu.skin_overrides.Library.LibraryEntry;

@SuppressWarnings("rawtypes")
public class Overridden {
    protected final AbstractOverride local;
    protected final AbstractLibraryOverride library;

    public Overridden(AbstractOverride local, AbstractLibraryOverride library) {
        this.local = local;
        this.library = library;
    }

    public AbstractOverride local() {
        return this.local;
    }

    public AbstractLibraryOverride library() {
        return this.library;
    }

    public boolean hasOverride(GameProfile profile) {
        return this.local.hasOverride(profile) || this.library.hasOverride(profile);
    }

    public void removeOverride(GameProfile profile) {
        this.local.removeOverride(profile);
        this.library.removeOverride(profile);
    }

    @SuppressWarnings("unchecked")
    public List<GameProfile> profilesWithOverride() {
        var li = new ArrayList<GameProfile>(this.local.profilesWithOverride());
        li.addAll(this.library.profilesWithOverride());
        return li;
    }

    // everything from this point on is stupid lol

    public boolean skin() {
        return this.local instanceof LocalSkinOverride;
    }

    public List<LibraryEntry> libraryEntries() {
        return this.skin() ? new ArrayList<>(Library.skinEntries()) : new ArrayList<>(Library.capeEntries());
    }
}
