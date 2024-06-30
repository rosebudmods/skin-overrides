package net.orifu.skin_overrides.override;

import java.util.ArrayList;
import java.util.List;

import com.mojang.authlib.GameProfile;

import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.texture.AbstractLibraryTexture;

public interface Overridden {
    <E, T> AbstractOverride<E, T> local();

    <E extends LibraryEntry, T extends AbstractLibraryTexture> AbstractLibraryOverride<E, T> library();

    default boolean hasOverride(GameProfile profile) {
        return this.local().hasOverride(profile) || this.library().hasOverride(profile);
    }

    default void removeOverride(GameProfile profile) {
        this.local().removeOverride(profile);
        this.library().removeOverride(profile);
    }

    default List<GameProfile> profilesWithOverride() {
        var li = new ArrayList<>(this.local().profilesWithOverride());
        li.addAll(this.library().profilesWithOverride());
        return li;
    }

    // everything from this point on is stupid lol

    boolean skin();

    @SuppressWarnings("unchecked")
    class SkinOverrides implements Overridden {
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
    class CapeOverrides implements Overridden {
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
