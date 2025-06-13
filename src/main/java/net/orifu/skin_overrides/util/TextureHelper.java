package net.orifu.skin_overrides.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Mod;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

//? if >=1.21.4 {
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import java.util.concurrent.ExecutionException;
//?} else {
/*import net.minecraft.client.renderer.texture.HttpTexture;
import org.apache.commons.io.FileUtils;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
*///?}

public class TextureHelper {
    public static SourceBuilder skin() {
        return new SourceBuilder(TextureType.SKIN);
    }

    public static SourceBuilder cape() {
        return new SourceBuilder(TextureType.CAPE);
    }

    public static ResourceLocation registerGenericTexture(ResourceLocation location, AbstractTexture texture) {
        // don't register this texture if there is already one with this resource location
        if (Minecraft.getInstance().getTextureManager().byPath.containsKey(location)) {
            return location;
        }

        Minecraft.getInstance().getTextureManager().register(location, texture);
        return location;
    }

    public static Optional<ResourceLocation> registerPlayerTexture(NativeImage image, ResourceLocation location, boolean isSkin) {
        var maybeImage = isSkin ? processLegacySkin(image, location) : Optional.of(image);

        //? if >=1.21.4 {
        return maybeImage.flatMap(updatedImage -> {
            // process legacy skins and register the skin using the method in the SkinTextureDownloader.
            // this *can* be done normally (see #texture above) but this is done in case another
            // mod mixes into that method.
            try {
                SkinTextureDownloader.registerTextureInManager(location, updatedImage).get();
                return Optional.of(location);
            } catch (InterruptedException | ExecutionException ignored) {}

            return Optional.empty();
        });

        //?} else {
        /*return maybeImage.map(updatedImage -> {
            var tex = new DynamicTexture(updatedImage);
            registerGenericTexture(location, tex);
            return location;
        });
        *///?}
    }

    public static Optional<NativeImage> processLegacySkin(NativeImage image, ResourceLocation location) {
        //? if >=1.21.4 {
        try {
            return Optional.of(SkinTextureDownloader.processLegacySkin(image, location.toString()));
        } catch (IllegalStateException ignored) {
            return Optional.empty();
        }
        //?} else {
        /*var fake = new HttpTexture(null, "", location, true, null);
        var newImage = fake.processLegacySkin(image);
        fake.close();
        return Optional.ofNullable(newImage);
        *///?}
    }

    public static Optional<NativeImage> downloadTexture(String url, Path cachePath) {
        //? if >=1.21.4 {
        try {
            var image = SkinTextureDownloader.downloadSkin(cachePath, url);
            return Optional.of(image);
        } catch (IOException ignored) {
            return Optional.empty();
        }
        //?} else {
        /*// taken from HttpTexture.load (both of them)
        try {
            var conn = (HttpURLConnection) (new URL(url)).openConnection(Minecraft.getInstance().getProxy());
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.connect();
            if (conn.getResponseCode() / 100 != 2) {
                return Optional.empty();
            }

            FileUtils.copyInputStreamToFile(conn.getInputStream(), cachePath.toFile());
            var stream = new FileInputStream(cachePath.toFile());
            return Optional.of(NativeImage.read(stream));
        } catch (IOException ignored) {
            return Optional.empty();
        }
        *///?}
    }

    public enum TextureType {
        SKIN,
        CAPE,
        GENERIC
    }

    public static class SourceBuilder {
        private final TextureType type;

        @Nullable
        private ResourceLocation location;

        @Nullable
        private String url;
        @Nullable
        private Path path;

        public SourceBuilder(TextureType type) {
            this.type = type;
        }

        public SourceBuilder location(ResourceLocation location) {
            this.location = location;
            return this;
        }

        public SourceBuilder location(String location) {
            this.location = Mod.res(location);
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return new Builder(this);
        }

        public Builder path(Path path) {
            this.path = path;
            return new Builder(this);
        }

        public Builder path(File file) {
            this.path = file.toPath();
            return new Builder(this);
        }
    }

    public static class Builder {
        private final SourceBuilder builder;

        private final ResourceLocation location;
        private final Path path;

        public Builder(SourceBuilder builder) {
            this.builder = builder;

            this.location = builder.location == null ? Mod.res("temp/" + Util.randomId()) : builder.location;
            this.path = builder.path != null ? builder.path : Util.nameTempFile().orElseThrow().toPath();
        }

        public Optional<Path> path() {
            if (this.path.toFile().isFile()) {
                return Optional.of(this.path);
            }

            return Optional.empty();
        }

        public Optional<NativeImage> image() {
            RenderSystem.assertOnRenderThread();

            // if there is a URL, download the image at this URL.
            // this uses SkinTextureDownloader.downloadSkin, which will also use the file at the
            // path if it exists.
            if (builder.url != null) {
                return downloadTexture(this.builder.url, this.path);
            }

            // this loads the image from a path.
            if (builder.path != null) {
                try {
                    if (builder.path.toFile().isFile()) {
                        var stream = Files.newInputStream(builder.path);
                        var image = NativeImage.read(stream);

                        return Optional.of(image);
                    }
                } catch (IOException ignored) {}
            }

            return Optional.empty();
        }

        public Optional<AbstractTexture> texture() {
            // if this is a generic texture, we can just register it.
            if (this.builder.type.equals(TextureType.GENERIC)) {
                return this.image().map(image ->
                        new DynamicTexture(/*? if >=1.21.5 {*/ this.location::toString, /*?}*/ image));
            }

            return Optional.empty();
        }

        public Optional<ResourceLocation> register() {
            return switch (this.builder.type) {
                case SKIN -> this.image().flatMap(image -> registerPlayerTexture(image, this.location, true));
                case CAPE -> this.image().flatMap(image -> registerPlayerTexture(image, this.location, false));
                case GENERIC -> this.texture().map(tex -> registerGenericTexture(this.location, tex));
            };
        }
    }
}
