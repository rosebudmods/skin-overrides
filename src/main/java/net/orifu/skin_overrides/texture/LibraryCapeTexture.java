package net.orifu.skin_overrides.texture;

import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.override.LibraryCapeOverride.CapeEntry;

public class LibraryCapeTexture extends AbstractLibraryTexture {
    protected LibraryCapeTexture(Identifier source, String name) {
        super(source, name);
    }

    public static LibraryCapeTexture fromLibrary(CapeEntry entry) {
        return new LibraryCapeTexture(entry.getTexture(), entry.getName());
    }
}
