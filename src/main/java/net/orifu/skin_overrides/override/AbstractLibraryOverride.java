package net.orifu.skin_overrides.override;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import net.orifu.skin_overrides.texture.AbstractLibraryTexture;
import net.orifu.skin_overrides.Library;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.util.Util;

public abstract class AbstractLibraryOverride<E extends LibraryEntry, T extends AbstractLibraryTexture>
        extends AbstractOverride<E, T> implements Library {
    protected static final Gson GSON = new Gson();

    protected ArrayList<LibraryEntry> entries;

    @Override
    protected String getFileName(GameProfile profile, E entry) {
        return Util.id(profile) + ".txt";
    }

    public void addOverride(GameProfile profile, E entry) {
        this.removeOverride(profile);
        var outputPath = Paths.get(this.rootFolder(), this.getFileName(profile, entry));

        try {
            var writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
            writer.write(Mod.id(entry.getId()).toString());
            writer.close();
        } catch (IOException e) {
            Mod.LOGGER.error("failed to save library entry with id {} to file", entry.getId(), e);
        }
    }

    @Override
    public List<LibraryEntry> entries() {
        if (this.entries == null) {
            this.reload();
        }

        return this.entries;
    }

    public File libraryJsonFile() {
        return new File(this.rootFolder(), "library.json");
    }

    public File libraryFolder() {
        File folder = new File(this.rootFolder(), "library");
        folder.mkdirs();
        return folder;
    }

    @Override
    public void save() {
        JsonArray arr = new JsonArray();
        this.entries().forEach(e -> arr.add(e.toJson()));

        try {
            var writer = Files.newBufferedWriter(this.libraryJsonFile().toPath(), StandardCharsets.UTF_8);
            writer.write(GSON.toJson(arr));
            writer.close();
        } catch (IOException e) {
            Mod.LOGGER.error("failed to save library file {}", this.libraryJsonFile(), e);
        }
    }

    protected abstract void tryLoadFromJson(JsonElement element);

    protected void loadFailed() {
    }

    @Override
    public void reload() {
        this.entries = new ArrayList<>();

        try {
            var reader = Files.newBufferedReader(this.libraryJsonFile().toPath(), StandardCharsets.UTF_8);
            JsonArray arr = GSON.fromJson(reader, JsonArray.class);

            arr.forEach(this::tryLoadFromJson);
            reader.close();
        } catch (FileNotFoundException | NoSuchFileException e) {
            this.loadFailed();
        } catch (IOException | JsonParseException | NullPointerException e) {
            Mod.LOGGER.error("failed to load library file {}", this.libraryJsonFile(), e);
        }
    }
}
