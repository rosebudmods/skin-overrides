package net.orifu.skin_overrides.texture;

import java.io.File;

import net.minecraft.client.texture.PlayerSkin;

public class LocalSkinTexture extends LocalPlayerTexture {
    public final PlayerSkin.Model model;

    public LocalSkinTexture(File textureFile, PlayerSkin.Model model) {
        super(textureFile, true, null);

        this.model = model;
    }

    public LocalSkinTexture(File textureFile) {
        this(textureFile, null);
    }
}
