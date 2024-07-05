package net.orifu.skin_overrides.texture;

import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Skin;

public class LibrarySkinTexture extends AbstractLibraryTexture {
    public final Skin.Model model;

    public LibrarySkinTexture(Identifier source, Skin.Model model, String name) {
        super(source, name);
        this.model = model;
    }

    public Skin.Model model() {
        return this.model;
    }
}
