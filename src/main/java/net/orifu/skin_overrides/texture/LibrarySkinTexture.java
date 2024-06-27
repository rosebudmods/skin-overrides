package net.orifu.skin_overrides.texture;

import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.override.LibrarySkinOverride.SkinEntry;

public class LibrarySkinTexture extends AbstractLibraryTexture {
    public final PlayerSkin.Model model;

    protected LibrarySkinTexture(Identifier source, PlayerSkin.Model model, String name) {
        super(source, name);
        this.model = model;
    }

    public static LibrarySkinTexture fromLibrary(SkinEntry entry) {
        return new LibrarySkinTexture(entry.getTexture(), entry.getModel(), entry.getName());
    }

    public PlayerSkin.Model model() {
        return this.model;
    }
}
