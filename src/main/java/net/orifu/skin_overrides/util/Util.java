package net.orifu.skin_overrides.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Mod;

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

    public static ResourceLocation texture(String res, AbstractTexture texture) {
        ResourceLocation textureLoc = Mod.res(res);
        Minecraft.getInstance().getTextureManager().register(textureLoc, texture);
        return textureLoc;
    }

    public static ResourceLocation texture(AbstractTexture texture) {
        return texture("temp/" + Util.randomId(), texture);
    }

    public static void saveTexture(ResourceLocation texture, int w, int h, Path path) {
        var future = new CompletableFuture<Path>();
        Runnable runnable = () -> {
            try {
                //? if >=1.21.3 {
                RenderSystem.bindTexture(Minecraft.getInstance().getTextureManager().getTexture(texture).getId());
                //?} else if >=1.17.1 {
                /*Minecraft.getInstance().getTextureManager().bindForSetup(texture);
                *///?} else
                /*Minecraft.getInstance().getTextureManager().bind(texture);*/
                NativeImage img = new NativeImage(w, h, false);
                img.downloadTexture(0, false);
                img.writeToFile(path);
                img.close();
                future.complete(path);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        };

        if (RenderSystem.isOnRenderThread()) {
            runnable.run();
        } else {
            Minecraft.getInstance().progressTasks.add(runnable);
            future.join();
        }
    }
}
