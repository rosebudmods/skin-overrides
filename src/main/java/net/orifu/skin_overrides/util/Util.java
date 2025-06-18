package net.orifu.skin_overrides.util;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Mod;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Util {
    public static Optional<String> readFile(File file) {
        try {
            return Optional.of(Files.readString(file.toPath()).trim())
                    .flatMap(content -> content.isEmpty() ? Optional.empty() : Optional.of(content));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static Optional<String> readString(@Nullable JsonObject obj, String key) {
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
        return user.getType() != User.Type.LEGACY
                ? profile.getId().toString()
                : profile.getName();
    }

    public static Optional<File> nameTempFile() {
        try {
            File file = File.createTempFile("skin-overrides_", "_temp");
            file.delete();
            return Optional.of(file);
        } catch (IOException ignored) {}

        return Optional.empty();
    }

    public static void runOnRenderThread(Runnable runnable) {
        //? if >=1.21.5 {
        if (RenderSystem.tryGetDevice() == null) {
            return;
        }
        //?}

        if (RenderSystem.isOnRenderThread()) {
            runnable.run();
        } else {
            Minecraft.getInstance().progressTasks.add(runnable);
        }
    }

    public static void saveTexture(ResourceLocation texture, int w, int h, Path path) {
        var future = new CompletableFuture<Path>();
        saveTexture(texture, w, h).thenAccept(img -> {
            try {
                img.writeToFile(path);
                img.close();
                future.complete(path);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });
    }

    public static CompletableFuture<NativeImage> saveTexture(ResourceLocation texture, int w, int h) {
        var future = new CompletableFuture<NativeImage>();

        runOnRenderThread(() -> {
            //? if >=1.21.5 {
            var abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
            if (abstractTexture instanceof DynamicTexture dtx) {
                future.complete(dtx.getPixels());
            } else {
                var ex = new UnsupportedOperationException("tried to save a non-dynamic texture. this is a bug! please report it <3");
                Mod.LOGGER.error("failed to save texture", ex);
                future.completeExceptionally(ex);
            }

            //?} else {
            /*//? if >=1.21.3 {
            RenderSystem.bindTexture(Minecraft.getInstance().getTextureManager().getTexture(texture).getId());
            //?} else
            /^Minecraft.getInstance().getTextureManager().bindForSetup(texture);^/

            NativeImage img = new NativeImage(w, h, false);
            img.downloadTexture(0, false);
            future.complete(img);
            *///?}
        });

        return future;
    }
}
