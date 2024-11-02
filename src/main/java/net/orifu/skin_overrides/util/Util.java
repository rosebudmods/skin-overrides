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
//? if >=1.19.2 {
import com.mojang.blaze3d.texture.NativeImage;
//?} else
/*import net.minecraft.client.texture.NativeImage;*/
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.Session;
import net.minecraft.util.Identifier;
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
        var session = MinecraftClient.getInstance().getSession();
        return /*? if >=1.17.1 {*/ session.getAccountType()
                /*?} else*/ /*session.accountType*/
                != Session.AccountType.LEGACY
                ? profile.getId().toString()
                : profile.getName();
    }

    public static Identifier texture(String id, AbstractTexture texture) {
        Identifier textureId = Mod.id(id);
        MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);
        return textureId;
    }

    public static Identifier texture(AbstractTexture texture) {
        return texture("temp/" + Util.randomId(), texture);
    }

    public static void saveTexture(Identifier texture, int w, int h, Path path) {
        var future = new CompletableFuture<Path>();
        Runnable runnable = () -> {
            try {
                //? if >=1.21.3 {
                RenderSystem.bindTexture(MinecraftClient.getInstance().getTextureManager().getTexture(texture).getGlId());
                //?} else
                /*MinecraftClient.getInstance().getTextureManager().bindTexture(texture);*/
                NativeImage img = new NativeImage(w, h, false);
                img.loadFromTextureImage(0, false);
                /*? if >=1.19.2 || <1.17.1 {*/ img.writeFile(path);
                /*?} else*/ /*img.writeTo(path);*/
                img.close();
                future.complete(path);
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        };

        if (RenderSystem.isOnRenderThread()) {
            runnable.run();
        } else {
            MinecraftClient.getInstance().renderTaskQueue.add(runnable);
            future.join();
        }
    }
}
