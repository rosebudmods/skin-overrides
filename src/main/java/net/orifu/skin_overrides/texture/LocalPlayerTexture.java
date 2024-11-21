package net.orifu.skin_overrides.texture;

//? if >=1.19.2 {
import com.mojang.blaze3d.platform.NativeImage;
//?} else
/*import net.minecraft.client.texture.NativeImage;*/
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class LocalPlayerTexture extends HttpTexture {
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
        Minecraft.getInstance().execute(() -> {
            try {
                NativeImage texture = this.load(new FileInputStream(this.textureFile));
                this.loadCallback(Objects.requireNonNull(texture));
            } catch (NullPointerException | IOException e) {
                // shouldn't happen
            }
        });
    }
}
