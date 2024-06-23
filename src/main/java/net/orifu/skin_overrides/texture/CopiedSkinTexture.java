package net.orifu.skin_overrides.texture;

import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.override.LibrarySkinOverride.SkinEntry;

public class CopiedSkinTexture extends AbstractLibraryTexture {
    public final PlayerSkin.Model model;

    protected CopiedSkinTexture(Identifier source, PlayerSkin.Model model, String name, boolean isCopying) {
        super(source, name, isCopying);
        this.model = model;
    }

    public static CopiedSkinTexture fromLibrary(SkinEntry entry) {
        return new CopiedSkinTexture(entry.getTexture(), entry.getModel(), entry.getName(), false);
    }

    public PlayerSkin.Model model() {
        return this.model;
    }
}
