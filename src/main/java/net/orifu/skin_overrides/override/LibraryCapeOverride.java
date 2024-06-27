package net.orifu.skin_overrides.override;

import static net.orifu.skin_overrides.Mod.CAPE_OVERRIDES;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.override.LibraryCapeOverride.CapeEntry;
import net.orifu.skin_overrides.texture.LibraryCapeTexture;
import net.orifu.skin_overrides.texture.LocalPlayerTexture;
import net.orifu.skin_overrides.util.OverrideFiles.Validated;
import net.orifu.skin_overrides.util.Util;

public class LibraryCapeOverride extends AbstractLibraryOverride<CapeEntry, LibraryCapeTexture> {
    public static final LibraryCapeOverride INSTANCE = new LibraryCapeOverride();

    @Override
    public String rootFolder() {
        return CAPE_OVERRIDES;
    }

    @Override
    protected Optional<Validated<CapeEntry>> validateFile(File file, String name, String ext) {
        if (ext.equals("txt")) {
            return Util.readFile(file).flatMap(Util::ensureLibraryIdentifier).flatMap(this::get)
                    .map(entry -> Validated.of(name, (CapeEntry) entry));
        }

        return Optional.empty();
    }

    @Override
    protected Optional<LibraryCapeTexture> tryGetTextureFromValidated(Validated<CapeEntry> v) {
        return Optional.of(LibraryCapeTexture.fromLibrary(v.data()));
    }

    @Override
    protected void tryLoadFromJson(JsonElement element) {
        if (element.isJsonObject()) {
            var obj = element.getAsJsonObject();
            var name = Util.readString(GSON, obj, "name");
            var id = Util.readString(GSON, obj, "id");
            var file = Util.readString(GSON, obj, "file");
            var texture = Util.readString(GSON, obj, "texture");

            if (name.isPresent() && id.isPresent()) {
                if (file.isPresent()) {
                    this.entries.add(new CapeEntry(name.get(), id.get(), new File(this.libraryFolder(), file.get())));
                } else if (texture.isPresent()) {
                    Identifier textureId = Identifier.tryParse(texture.get());
                    if (textureId != null) {
                        this.entries.add(new CapeEntry(name.get(), id.get(), textureId));
                    }
                }
            }
        }
    }

    @Override
    protected void loadFailed() {
        this.entries
                .add(new CapeEntry("skin_overrides", "skin_overrides", new Identifier("skin_overrides", "cape.png")));
    }

    public static class CapeEntry extends LibraryEntry {
        protected final boolean isFile;
        @Nullable
        protected final File file;
        @Nullable
        protected final Identifier texture;

        public CapeEntry(String name, String id, File file) {
            super(name, id);

            this.isFile = true;
            this.file = file;
            this.texture = null;
        }

        public CapeEntry(String name, String id, Identifier texture) {
            super(name, id);

            this.isFile = false;
            this.file = null;
            this.texture = texture;
        }

        public static Optional<CapeEntry> create(String name, Path path) {
            return createInternal(name, null, path);
        }

        public static Optional<CapeEntry> create(String name, Identifier texture) {
            return createInternal(name, texture, null);
        }

        private static Optional<CapeEntry> createInternal(String name, Identifier texture, Path path) {
            try {
                String id = Util.randomId();
                File file = new File(LibraryCapeOverride.INSTANCE.libraryFolder(), id + ".png");
                var entry = new CapeEntry(name, id, file);

                if (path != null)
                    Files.copy(path, entry.file.toPath());
                else
                    Util.saveTexture(texture, 64, 32, file.toPath());

                LibraryCapeOverride.INSTANCE.add(entry);
                return Optional.of(entry);
            } catch (IOException e) {
                Mod.LOGGER.error("failed to copy {}", path, e);
                return Optional.empty();
            }
        }

        @Override
        public Identifier getTexture() {
            if (this.isFile) {
                Identifier id = new Identifier("skin_overrides", "skin/library/" + this.id);
                MinecraftClient.getInstance().getTextureManager().registerTexture(id,
                        new LocalPlayerTexture(this.file));
                return id;
            } else {
                return this.texture;
            }
        }

        @Override
        public JsonElement toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", this.name);
            obj.addProperty("id", this.id);
            if (this.isFile) {
                obj.addProperty("file", this.file.getName());
            } else {
                obj.addProperty("texture", this.texture.toString());
            }
            return obj;
        }

        @Override
        public void remove() {
            if (this.isFile) {
                this.file.delete();
            }
        }
    }
}
