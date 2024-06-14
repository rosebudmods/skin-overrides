package net.orifu.skin_overrides;

import java.io.File;

public class OverridenSkinTexture extends OverridenHttpTexture {
    public OverridenSkinTexture(File textureFile) {
        super(textureFile, true, () -> {
        });
    }
}
