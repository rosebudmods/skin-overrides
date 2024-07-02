package net.orifu.skin_overrides.texture;

import java.io.File;

import net.orifu.skin_overrides.Skin;
import org.jetbrains.annotations.Nullable;

public class LocalSkinTexture extends LocalPlayerTexture {
    @Nullable
    public final Skin.Model model;

    public LocalSkinTexture(File textureFile, @Nullable Skin.Model model) {
        super(textureFile, true);

        this.model = model;
    }

    public LocalSkinTexture(File textureFile) {
        this(textureFile, null);
    }
}
