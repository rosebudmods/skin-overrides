package net.orifu.skin_overrides.texture;

//? if >=1.21.4 {
/*import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.orifu.skin_overrides.Skin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LocalPlayerTexture {
    private LocalPlayerTexture() {}

    public static AbstractTexture fromFile(File textureFile, @Nullable Skin.Model model) {
        try {
            if (textureFile.isFile()) {
                var stream = new FileInputStream(textureFile);
                var image = NativeImage.read(stream);

                // TODO: process skin

                return new DynamicTexture(image);
            }
        } catch (IOException ignored) {}

        return new SimpleTexture(MissingTextureAtlasSprite.getLocation());
    }

    public static AbstractTexture fromFile(File textureFile) {
        return fromFile(textureFile, null);
    }
}
*///?} else {
import com.mojang.blaze3d.platform.NativeImage;
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
//?}
