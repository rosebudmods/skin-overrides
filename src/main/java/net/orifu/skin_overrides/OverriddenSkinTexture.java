package net.orifu.skin_overrides;

import java.io.File;

import net.minecraft.client.texture.PlayerSkin;

public class OverriddenSkinTexture extends OverriddenHttpTexture {
    public final PlayerSkin.Model model;

    public OverriddenSkinTexture(File textureFile, PlayerSkin.Model model) {
        super(textureFile, true, () -> {
        });

        this.model = model;
    }
}
