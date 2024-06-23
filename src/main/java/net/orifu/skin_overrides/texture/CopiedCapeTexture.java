package net.orifu.skin_overrides.texture;

import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.override.LibraryCapeOverride.CapeEntry;

public class CopiedCapeTexture extends AbstractLibraryTexture {
    protected CopiedCapeTexture(Identifier source, String name, boolean isCopying) {
        super(source, name, isCopying);
    }

    public static CopiedCapeTexture fromLibrary(CapeEntry entry) {
        return new CopiedCapeTexture(entry.getTexture(), entry.getName(), false);
    }
}
