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
import java.io.IOException;
import java.util.Objects;

public class LocalPlayerTexture extends PlayerSkinTexture {
    private final File textureFile;

    public LocalPlayerTexture(File textureFile, boolean isSkin) {
        super(
                textureFile,
                null,
                null,
                /*? if >=1.15.2 {*/ isSkin, null
                /*?} else*/ /*isSkin ? new net.minecraft.client.texture.SkinRemappingImageFilter() : null*/
        );

        this.textureFile = textureFile;
    }

    public LocalPlayerTexture(File textureFile) {
        this(textureFile, false);
    }

    @Override
    public void load(ResourceManager manager) {
        MinecraftClient.getInstance().execute(() -> {
            try {
                //? if >=1.15.2 {
                NativeImage texture = this.loadTexture(new FileInputStream(this.textureFile));
                this.onTextureLoaded(Objects.requireNonNull(texture));
                //?} else {
                /*NativeImage texture = NativeImage.read(new FileInputStream(this.textureFile));
                if (this.filter != null) {
                    texture = this.filter.filterImage(texture);
                }
                this.method_4534(texture);
                *///?}
            } catch (NullPointerException | IOException e) {
                // shouldn't happen
            }
        });
    }
}
