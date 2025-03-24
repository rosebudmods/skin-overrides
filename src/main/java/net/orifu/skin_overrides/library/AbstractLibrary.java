package net.orifu.skin_overrides.library;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Library;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.override.LibraryOverrider;
import net.orifu.skin_overrides.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractLibrary implements Library {
    protected static final Gson GSON = new Gson();

    protected List<LibraryEntry> entries;

    protected final String rootFolder;
    protected final File libraryJsonFile;
    protected final File libraryFolder;

    public AbstractLibrary(String rootFolder) {
        this.rootFolder = rootFolder;
        this.libraryJsonFile = new File(rootFolder, "library.json");
        this.libraryFolder = new File(rootFolder, "library");

        this.libraryFolder.mkdirs();
    }

    @Override
    public List<LibraryEntry> entries() {
        if (this.entries == null) {
            this.reload();
        }

        return this.entries;
    }

    @Override
    public void save() {
        JsonArray arr = new JsonArray();
        this.entries().forEach(e -> arr.add(e.toJson()));

        try {
            var writer = Files.newBufferedWriter(this.libraryJsonFile.toPath(), StandardCharsets.UTF_8);
            writer.write(GSON.toJson(arr));
            writer.close();
        } catch (IOException e) {
            Mod.LOGGER.error("failed to save library file {}", this.libraryJsonFile, e);
        }
    }

    @Override
    public void reload() {
        ArrayList<LibraryEntry> newEntries = new ArrayList<>();

        try {
            var reader = Files.newBufferedReader(this.libraryJsonFile.toPath(), StandardCharsets.UTF_8);
            JsonArray arr = GSON.fromJson(reader, JsonArray.class);

            for (var el : arr) {
                var maybeLoaded = this.tryLoadFromJsonElement(el);

                if (maybeLoaded.isPresent()) {
                    var loaded = maybeLoaded.get();

                    // re-use existing entry (if any)
                    var existing = Optional.ofNullable(this.entries).flatMap(entries ->
                            entries.stream().filter(ent -> ent.equals(loaded)).findAny());
                    newEntries.add(existing.orElse(loaded));
                }
            }

            reader.close();
        } catch (FileNotFoundException | NoSuchFileException e) {
            this.addDefaultEntries();
            this.save();
        } catch (IOException | JsonParseException | NullPointerException e) {
            Mod.LOGGER.error("failed to load library file {}", this.libraryJsonFile, e);
        }

        this.entries = newEntries;
    }

    @Override
    public LibraryOverrider overrider() {
        return new LibraryOverrider(this, this.rootFolder);
    }

    protected Optional<LibraryEntry> tryLoadFromJsonElement(JsonElement el) {
        if (el.isJsonObject()) {
            var obj = el.getAsJsonObject();
            var name = Util.readString(obj, "name");
            var id = Util.readString(obj, "id");
            var file = Util.readString(obj, "file");
            var texture = Util.readString(obj, "texture");

            if (name.isPresent() && id.isPresent()) {
                if (file.isPresent() && texture.isEmpty()) {
                    var maybe = this.tryLoadFromJson(obj, name.get(), id.get(), new File(this.libraryFolder, file.get()), null);
                    if (maybe.isPresent()) return maybe;
                } else if (texture.isPresent() && file.isEmpty()) {
                    ResourceLocation textureLoc = ResourceLocation.tryParse(texture.get());
                    if (textureLoc != null) {
                        var maybe = this.tryLoadFromJson(obj, name.get(), id.get(), null, textureLoc);
                        if (maybe.isPresent()) return maybe;
                    }
                }
            }
        }

        Mod.LOGGER.warn("failed to load library entry {}", el);
        return Optional.empty();
    }

    protected abstract Optional<LibraryEntry> tryLoadFromJson(JsonObject object, String name, String id, @Nullable File file, @Nullable ResourceLocation textureLoc);

    protected void addDefaultEntries() {}

    public static abstract class AbstractLibraryEntry extends LibraryEntry {
        @Nullable
        protected final File file;
        @Nullable
        protected final String fileHash;
        @Nullable
        protected final ResourceLocation textureLoc;

        protected AbstractLibraryEntry(String name, String id, @Nullable File file, @Nullable ResourceLocation textureLoc) {
            super(name, id);
            this.file = file;
            this.fileHash = file == null ? null : Util.hashFile(file);
            this.textureLoc = textureLoc;
        }

        public AbstractLibraryEntry(String name, String id, @NotNull File file) {
            this(name, id, file, null);
        }

        public AbstractLibraryEntry(String name, String id, @NotNull ResourceLocation textureId) {
            this(name, id, null, textureId);
        }

        @Override
        public ResourceLocation getTexture() {
            return this.textureLoc != null ? this.textureLoc : this.getTextureFromFile();
        }

        protected abstract ResourceLocation getTextureFromFile();

        @Override
        public JsonElement toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", this.name);
            obj.addProperty("id", this.id);

            if (this.file != null) {
                obj.addProperty("file", this.file.getName());
            } else {
                obj.addProperty("texture", this.textureLoc.toString());
            }

            return obj;
        }

        @Override
        public void removeFiles() {
            if (this.file != null) {
                this.file.delete();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AbstractLibraryEntry that)) return false;

            return Objects.equals(file, that.file) && (file != null || Objects.equals(textureLoc, that.textureLoc));
        }
    }
}
