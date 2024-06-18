package net.orifu.skin_overrides;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;

public class Library {
    public static final File FILE = new File("skin_overrides/library.json");
    public static final Gson GSON = new Gson();

    protected ArrayList<LibraryEntry> entries;

    public Library() {
        this.reload();
    }

    public void reload() {
        this.entries = new ArrayList<>();

        try {
            var reader = Files.newReader(FILE, StandardCharsets.UTF_8);
            JsonArray arr = GSON.fromJson(reader, JsonArray.class);

            arr.forEach(j -> LibraryEntry.fromJson(j).ifPresent(this.entries::add));
            reader.close();
            return;
        } catch (FileNotFoundException e) {
        } catch (IOException | JsonParseException | NullPointerException e) {
            SkinOverrides.LOGGER.error("failed to load library file", e);
        }

        // add default player skins
        for (var playerSkin : DefaultSkinHelper.DEFAULT_SKINS) {
            String name = playerSkin.texture().getPath();
            name = name.substring(name.lastIndexOf('/') + 1);
            name = name.substring(0, 1).toUpperCase() + name.substring(1).replace(".png", "");
            name += playerSkin.model().equals(PlayerSkin.Model.WIDE) ? " (wide)" : " (slim)";
            this.entries.add(new LibraryEntry(name, playerSkin.texture()));
        }
    }

    public void save() {
        JsonArray arr = new JsonArray();
        this.entries.forEach(e -> arr.add(e.toJson()));

        try {
            var writer = Files.newWriter(FILE, StandardCharsets.UTF_8);
            writer.write(GSON.toJson(arr));
            writer.close();
        } catch (IOException e) {
            SkinOverrides.LOGGER.error("failed to save library file", e);
        }
    }

    public static class LibraryEntry {
        protected final boolean isFile;
        @Nullable
        protected final File skinFile;
        @Nullable
        protected final Identifier texture;

        protected String name;

        public LibraryEntry(String name, File file) {
            this.isFile = true;
            this.skinFile = file;
            this.texture = null;

            this.name = name;
        }

        public LibraryEntry(String name, Identifier texture) {
            this.isFile = false;
            this.skinFile = null;
            this.texture = texture;

            this.name = name;
        }

        protected static Optional<LibraryEntry> fromJson(JsonElement el) {
            if (!el.isJsonObject())
                return Optional.empty();

            JsonObject obj = el.getAsJsonObject();
            JsonElement name = obj.get("name");
            JsonElement file = obj.get("file");
            JsonElement texture = obj.get("texture");

            if (name == null)
                return Optional.empty();
            String nameStr = name.getAsString();
            if (nameStr == null)
                return Optional.empty();

            if (file != null) {
                String fileStr = file.getAsString();
                if (fileStr == null)
                    return Optional.empty();

                return Optional.of(new LibraryEntry(nameStr, new File(fileStr)));
            } else if (texture != null) {
                String textureStr = texture.getAsString();
                if (textureStr == null)
                    return Optional.empty();
                Identifier textureId = Identifier.tryParse(textureStr);
                if (textureId == null)
                    return Optional.empty();

                return Optional.of(new LibraryEntry(nameStr, textureId));
            } else {
                return Optional.empty();
            }
        }

        protected JsonElement toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", this.name);
            if (this.isFile) {
                obj.addProperty("file", this.skinFile.toString());
            } else {
                obj.addProperty("texture", this.texture.toString());
            }
            return obj;
        }
    }
}
