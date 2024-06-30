package net.orifu.skin_overrides.texture;

import java.io.File;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.texture.PlayerSkin;

public class LocalSkinTexture extends LocalPlayerTexture {
    @Nullable
    public final PlayerSkin.Model model;

    public LocalSkinTexture(File textureFile, @Nullable PlayerSkin.Model model) {
        super(textureFile, true);

        this.model = model;
    }

    public LocalSkinTexture(File textureFile) {
        this(textureFile, null);
    }
}
