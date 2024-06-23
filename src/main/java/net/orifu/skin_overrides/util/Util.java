package net.orifu.skin_overrides.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
}
