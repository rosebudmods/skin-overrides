package net.orifu.skin_overrides.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Mod;

//? if >=1.21.4 {
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
//?} else
/*import net.minecraft.client.renderer.texture.HttpTexture;*/

//? if >=1.21.5 {
import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.textures.GpuTexture;
//?}

public class Util {
    public static Optional<String> readFile(File file) {
        try {
            return Optional.of(Files.readString(file.toPath()).trim())
                    .flatMap(content -> content.isEmpty() ? Optional.empty() : Optional.of(content));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static Optional<String> readString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return Optional.empty();
        }

        return Optional.ofNullable(obj.get(key).getAsString());
    }

    public static String hashFile(File file) {
        // it's cheap, and you'd have to go out of your way to have a collision.
        // it works, okay?!
        return file.length() + "-" + file.lastModified();
    }

    public static String randomId() {
        return UUID.randomUUID().toString();
    }

    public static String id(GameProfile profile) {
        var user = Minecraft.getInstance().getUser();
        return /*? if >=1.17.1 {*/ user.getType()
                /*?} else*/ /*user.type*/
                != User.Type.LEGACY
                ? profile.getId().toString()
                : profile.getName();
    }

    public static void texture(ResourceLocation res, AbstractTexture texture) {
        // don't register this texture if there is already one with this resource location
        if (Minecraft.getInstance().getTextureManager().byPath.containsKey(res)) {
            return;
        }

        Minecraft.getInstance().getTextureManager().register(res, texture);
    }

    public static ResourceLocation texture(String res, AbstractTexture texture) {
        ResourceLocation textureLoc = Mod.res(res);
        texture(textureLoc, texture);
        return textureLoc;
    }

    public static ResourceLocation texture(AbstractTexture texture) {
        return texture("temp/" + Util.randomId(), texture);
    }

    public static void saveTexture(ResourceLocation texture, int w, int h, Path path) {
        var future = new CompletableFuture<Path>();
        Consumer<NativeImage> imageConsumer = img -> {
            try {
                img.writeToFile(path);
                img.close();
                future.complete(path);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        };

        Runnable runnable = () -> {
            //? if >=1.21.5 {
            var imgFut = gpuTextureToNativeImage(Minecraft.getInstance().getTextureManager().getTexture(texture).getTexture());
            imgFut.thenAccept(imageConsumer);

            //?} else {
            /*//? if >=1.21.3 {
            RenderSystem.bindTexture(Minecraft.getInstance().getTextureManager().getTexture(texture).getId());
            //?} else if >=1.17.1 {
            /^Minecraft.getInstance().getTextureManager().bindForSetup(texture);
             ^///?} else
            /^Minecraft.getInstance().getTextureManager().bind(texture);^/

            NativeImage img = new NativeImage(w, h, false);
            img.downloadTexture(0, false);
            imageConsumer.accept(img);
            *///?}
        };

        if (RenderSystem.isOnRenderThread()) {
            runnable.run();
        } else {
            Minecraft.getInstance().progressTasks.add(runnable);
        }
    }

    //? if >=1.21.5 {
    public static CompletableFuture<NativeImage> gpuTextureToNativeImage(GpuTexture gpuTex) {
        // see TextureUtil.writeAsPNG
        RenderSystem.assertOnRenderThread();

        int mip = 0;
        int width = gpuTex.getWidth(mip);
        int height = gpuTex.getHeight(mip);

        int bufSize = gpuTex.getFormat().pixelSize() * width * height;
        var buffer = RenderSystem.getDevice().createBuffer(() -> "skin overrides - texture to PNG",
                BufferType.PIXEL_PACK, BufferUsage.STATIC_READ, bufSize);
        var commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        var fut = new CompletableFuture<NativeImage>();

        commandEncoder.copyTextureToBuffer(gpuTex, buffer, 0, () -> {
            var img = new NativeImage(width, height, false);
            var view = commandEncoder.readBuffer(buffer);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int px = view.data().getInt((x + y * width) * gpuTex.getFormat().pixelSize());
                    img.setPixelABGR(x, y, px);
                }
            }

            view.close();
            buffer.close();

            fut.complete(img);
        }, mip);

        return fut;
    }
    //?}

    public static AbstractTexture textureFromFile(File textureFile, Function<NativeImage, AbstractTexture> transform) {
        try {
            if (textureFile.isFile()) {
                var stream = Files.newInputStream(textureFile.toPath());
                var image = NativeImage.read(stream);

                return transform.apply(image);
            }
        } catch (IOException ignored) {}

        return new SimpleTexture(MissingTextureAtlasSprite.getLocation());
    }

    public static AbstractTexture textureFromFile(File textureFile) {
        return textureFromFile(textureFile, image -> new DynamicTexture(/*? if >=1.21.5 {*/null,/*?}*/ image));
    }

    public static AbstractTexture skinTextureFromFile(File textureFile) {
        return textureFromFile(textureFile, image ->
                /*? if >=1.21.4 {*/ new DynamicTexture(
                        /*? if >=1.21.5*/ null,
                        SkinTextureDownloader.processLegacySkin(image, textureFile.getName()))
                //?} else
                /*new HttpTexture(textureFile, "", ProfileHelper.getDefaultSkin(), true, null)*/
        );
    }
}
