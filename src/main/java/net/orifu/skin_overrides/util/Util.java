package net.orifu.skin_overrides.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.texture.NativeImage;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;

public class Util {
    public static Optional<String> readFile(File file) {
        try {
            return Optional.of(Files.readString(file.toPath()).trim())
                    .flatMap(content -> content.length() == 0 ? Optional.empty() : Optional.of(content));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static Optional<String> ensureLibraryIdentifier(String identifier) {
        var maybeIdentifier = Identifier.tryParse(identifier);
        if (maybeIdentifier != null) {
            if (maybeIdentifier.getNamespace().equals("skin_overrides")) {
                return Optional.of(maybeIdentifier.getPath());
            }
        }
        return Optional.empty();
    }

    public static Optional<String> readString(Gson gson, JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) {
            return Optional.empty();
        }

        return Optional.ofNullable(obj.get(key).getAsString());
    }

    public static String randomId() {
        return UUID.randomUUID().toString();
    }

    public static Identifier texture(AbstractTexture texture) {
        Identifier textureId = new Identifier("skin_overrides", "temp/" + Util.randomId());
        MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);
        return textureId;
    }

    public static void saveTexture(Identifier texture, int w, int h, Path path) throws IOException {
        MinecraftClient.getInstance().getTextureManager().bindTexture(texture);
        NativeImage img = new NativeImage(w, h, false);
        img.loadFromTextureImage(0, false);
        img.writeFile(path);
        img.close();
    }
}
