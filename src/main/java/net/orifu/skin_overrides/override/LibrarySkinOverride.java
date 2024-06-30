package net.orifu.skin_overrides.override;

import static net.orifu.skin_overrides.Mod.SKIN_OVERRIDES;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.override.LibrarySkinOverride.SkinEntry;
import net.orifu.skin_overrides.texture.LibrarySkinTexture;
import net.orifu.skin_overrides.texture.LocalSkinTexture;
import net.orifu.skin_overrides.util.Util;
import net.orifu.skin_overrides.util.OverrideFiles.Validated;

public class LibrarySkinOverride extends AbstractLibraryOverride<SkinEntry, LibrarySkinTexture> {
    public static final LibrarySkinOverride INSTANCE = new LibrarySkinOverride();

    @Override
    public String rootFolder() {
        return SKIN_OVERRIDES;
    }

    @Override
    protected Optional<Validated<SkinEntry>> validateFile(File file, String name, String ext) {
        if (ext.equals("txt")) {
            return Util.readFile(file).flatMap(Util::ensureLibraryIdentifier).flatMap(this::get)
                    .map(entry -> Validated.of(name, (SkinEntry) entry));
        }

        return Optional.empty();
    }

    @Override
    protected Optional<LibrarySkinTexture> tryGetTextureFromValidated(Validated<SkinEntry> v) {
        return Optional.of(LibrarySkinTexture.fromLibrary(v.data()));
    }

    @Override
    protected void tryLoadFromJson(JsonElement element) {
        if (element.isJsonObject()) {
            var obj = element.getAsJsonObject();
            var name = Util.readString(GSON, obj, "name");
            var id = Util.readString(GSON, obj, "id");
            var model = Util.readString(GSON, obj, "model");
            var file = Util.readString(GSON, obj, "file");
            var texture = Util.readString(GSON, obj, "texture");

            if (name.isPresent() && id.isPresent() && model.isPresent()) {
                PlayerSkin.Model skinModel = model.get().equals("wide") ? PlayerSkin.Model.WIDE
                        : model.get().equals("slim") ? PlayerSkin.Model.SLIM : null;
                if (skinModel == null) {
                    return;
                }

                if (file.isPresent()) {
                    this.entries.add(
                            new SkinEntry(name.get(), id.get(), new File(this.libraryFolder(), file.get()), skinModel));
                } else if (texture.isPresent()) {
                    Identifier textureId = Identifier.tryParse(texture.get());
                    if (textureId != null) {
                        this.entries.add(new SkinEntry(name.get(), id.get(), textureId, skinModel));
                    }
                }
            }
        }
    }

    @Override
    protected void loadFailed() {
        // add default player skins
        for (var playerSkin : DefaultSkinHelper.DEFAULT_SKINS) {
            String name = playerSkin.texture().getPath();
            name = name.substring(name.lastIndexOf('/') + 1);
            name = name.substring(0, 1).toUpperCase() + name.substring(1).replace(".png", "");
            name += playerSkin.model().equals(PlayerSkin.Model.WIDE) ? " (wide)" : " (slim)";
            this.entries.add(new SkinEntry(name, Util.randomId(), playerSkin.texture(), playerSkin.model()));
        }
    }

    public static class SkinEntry extends LibraryEntry {
        protected final PlayerSkin.Model model;

        protected final boolean isFile;
        @Nullable
        protected final File file;
        @Nullable
        protected final Identifier texture;

        public SkinEntry(String name, String id, @NotNull File file, PlayerSkin.Model model) {
            super(name, id);

            this.model = model;
            this.isFile = true;
            this.file = file;
            this.texture = null;
        }

        public SkinEntry(String name, String id, @NotNull Identifier texture, PlayerSkin.Model model) {
            super(name, id);

            this.model = model;
            this.isFile = false;
            this.file = null;
            this.texture = texture;
        }

        public static Optional<SkinEntry> create(String name, Path path, PlayerSkin.Model model) {
            return createInternal(name, null, path, model);
        }

        public static Optional<SkinEntry> create(String name, Identifier texture, PlayerSkin.Model model) {
            return createInternal(name, texture, null, model);
        }

        private static Optional<SkinEntry> createInternal(String name, Identifier texture, Path path,
                PlayerSkin.Model model) {
            try {
                String id = Util.randomId();
                File file = new File(LibrarySkinOverride.INSTANCE.libraryFolder(), id + ".png");
                var entry = new SkinEntry(name, id, file, model);

                if (path != null) {
                    Files.copy(path, entry.file.toPath());
                } else {
                    Util.saveTexture(texture, 64, 64, file.toPath());
                }

                LibrarySkinOverride.INSTANCE.add(entry);
                return Optional.of(entry);
            } catch (IOException e) {
                Mod.LOGGER.error("failed to copy {}", path, e);
                return Optional.empty();
            }
        }

        public PlayerSkin.Model getModel() {
            return this.model;
        }

        @Override
        public Identifier getTexture() {
            if (this.isFile) {
                Identifier id = new Identifier("skin_overrides", "cape/library/" + this.id);
                MinecraftClient.getInstance().getTextureManager().registerTexture(id,
                        new LocalSkinTexture(this.file, null));
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
            obj.addProperty("model", this.model.equals(PlayerSkin.Model.WIDE) ? "wide" : "slim");
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
