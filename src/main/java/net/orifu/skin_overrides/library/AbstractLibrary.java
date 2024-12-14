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
        this.entries = new ArrayList<>();

        try {
            var reader = Files.newBufferedReader(this.libraryJsonFile.toPath(), StandardCharsets.UTF_8);
            JsonArray arr = GSON.fromJson(reader, JsonArray.class);

            synchronized (this.entries) {
                arr.forEach(this::tryLoadFromJsonElement);
            }

            reader.close();
        } catch (FileNotFoundException | NoSuchFileException e) {
            this.addDefaultEntries();
            this.save();
        } catch (IOException | JsonParseException | NullPointerException e) {
            Mod.LOGGER.error("failed to load library file {}", this.libraryJsonFile, e);
        }
    }

    @Override
    public LibraryOverrider overrider() {
        return new LibraryOverrider(this, this.rootFolder);
    }

    protected void tryLoadFromJsonElement(JsonElement el) {
        if (el.isJsonObject()) {
            var obj = el.getAsJsonObject();
            var name = Util.readString(obj, "name");
            var id = Util.readString(obj, "id");
            var file = Util.readString(obj, "file");
            var texture = Util.readString(obj, "texture");

            if (name.isPresent() && id.isPresent()) {
                if (file.isPresent() && texture.isEmpty()
                        && this.tryLoadFromJson(obj, name.get(), id.get(), new File(this.libraryFolder, file.get()), null)) {
                    return;
                } else if (texture.isPresent() && file.isEmpty()) {
                    ResourceLocation textureLoc = ResourceLocation.tryParse(texture.get());
                    if (textureLoc != null && this.tryLoadFromJson(obj, name.get(), id.get(), null, textureLoc)) {
                        return;
                    }
                }
            }
        }

        Mod.LOGGER.warn("failed to load library entry {}", el);
    }

    protected abstract boolean tryLoadFromJson(JsonObject object, String name, String id, @Nullable File file, @Nullable ResourceLocation textureLoc);

    protected void addDefaultEntries() {}

    public static abstract class AbstractLibraryEntry extends LibraryEntry {
        protected final boolean isFile;
        @Nullable
        protected final File file;
        @Nullable
        protected final String fileHash;
        @Nullable
        protected final ResourceLocation textureLoc;

        protected AbstractLibraryEntry(String name, String id, @Nullable File file, @Nullable ResourceLocation textureLoc) {
            super(name, id);
            this.isFile = file != null;
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
            return this.isFile ? this.getTextureFromFile() : this.textureLoc;
        }

        protected abstract ResourceLocation getTextureFromFile();

        @Override
        public JsonElement toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", this.name);
            obj.addProperty("id", this.id);

            if (this.isFile) {
                obj.addProperty("file", this.file.getName());
            } else {
                obj.addProperty("texture", this.textureLoc.toString());
            }

            return obj;
        }

        @Override
        public void removeFiles() {
            if (this.isFile) {
                this.file.delete();
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AbstractLibraryEntry that)) return false;
            return isFile == that.isFile && Objects.equals(file, that.file) && Objects.equals(textureLoc, that.textureLoc);
        }
    }
}
