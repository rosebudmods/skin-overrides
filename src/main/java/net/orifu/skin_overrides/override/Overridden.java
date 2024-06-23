package net.orifu.skin_overrides.override;

import java.util.ArrayList;
import java.util.List;

import com.mojang.authlib.GameProfile;

import net.orifu.skin_overrides.Library;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.texture.AbstractLibraryTexture;

public interface Overridden {
    public <E, T> AbstractOverride<E, T> local();

    public <E extends LibraryEntry, T extends AbstractLibraryTexture> AbstractLibraryOverride<E, T> library();

    default public boolean hasOverride(GameProfile profile) {
        return this.local().hasOverride(profile) || this.library().hasOverride(profile);
    }

    default public void removeOverride(GameProfile profile) {
        this.local().removeOverride(profile);
        this.library().removeOverride(profile);
    }

    default public List<GameProfile> profilesWithOverride() {
        var li = new ArrayList<GameProfile>(this.local().profilesWithOverride());
        li.addAll(this.library().profilesWithOverride());
        return li;
    }

    // everything from this point on is stupid lol

    public boolean skin();

    default public List<LibraryEntry> libraryEntries() {
        return this.skin() ? new ArrayList<>(Library.skinEntries()) : new ArrayList<>(Library.capeEntries());
    }

    @SuppressWarnings("unchecked")
    public static class SkinOverrides implements Overridden {
        @Override
        public LocalSkinOverride local() {
            return LocalSkinOverride.INSTANCE;
        }

        @Override
        public LibrarySkinOverride library() {
            return LibrarySkinOverride.INSTANCE;
        }

        @Override
        public boolean skin() {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public static class CapeOverrides implements Overridden {
        @Override
        public LocalCapeOverride local() {
            return LocalCapeOverride.INSTANCE;
        }

        @Override
        public LibraryCapeOverride library() {
            return LibraryCapeOverride.INSTANCE;
        }

        @Override
        public boolean skin() {
            return false;
        }
    }
}
