package net.orifu.skin_overrides.texture;

//? if >=1.19.2 {
import com.mojang.blaze3d.texture.NativeImage;
//?} else
/*import net.minecraft.client.texture.NativeImage;*/
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.resource.ResourceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Objects;

public class LocalPlayerTexture extends PlayerSkinTexture {
    private final File textureFile;

    public LocalPlayerTexture(File textureFile, boolean isSkin) {
        super(textureFile, null, null, isSkin, null);

        this.textureFile = textureFile;
    }

    public LocalPlayerTexture(File textureFile) {
        this(textureFile, false);
    }

    @Override
    public void load(ResourceManager manager) {
        MinecraftClient.getInstance().execute(() -> {
            try {
                NativeImage texture = this.loadTexture(new FileInputStream(this.textureFile));
                this.onTextureLoaded(Objects.requireNonNull(texture));
            } catch (FileNotFoundException | NullPointerException e) {
                // shouldn't happen
            }
        });
    }
}
