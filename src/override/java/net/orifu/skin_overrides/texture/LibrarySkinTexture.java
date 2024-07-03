package net.orifu.skin_overrides.texture;

import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.override.LibrarySkinOverride.SkinEntry;

public class LibrarySkinTexture extends AbstractLibraryTexture {
    public final Skin.Model model;

    protected LibrarySkinTexture(Identifier source, Skin.Model model, String name) {
        super(source, name);
        this.model = model;
    }

    public static LibrarySkinTexture fromLibrary(SkinEntry entry) {
        return new LibrarySkinTexture(entry.getTexture(), entry.getModel(), entry.getName());
    }

    public Skin.Model model() {
        return this.model;
    }
}
