package net.orifu.skin_overrides.library;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.texture.LocalPlayerTexture;
import net.orifu.skin_overrides.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static net.orifu.skin_overrides.Mod.CAPE_OVERRIDES_PATH;

public class CapeLibrary extends AbstractLibrary {
    public static final CapeLibrary INSTANCE = new CapeLibrary();

    private CapeLibrary() {
        super(CAPE_OVERRIDES_PATH);
    }

    @Override
    protected boolean tryLoadFromJson(JsonObject object, String name, String id, @Nullable File file, @Nullable ResourceLocation textureLoc) {
        this.entries.add(new CapeEntry(name, id, file, textureLoc));
        return true;
    }

    @Override
    protected void addDefaultEntries() {
        this.entries.add(new CapeEntry("skin overrides", "skin_overrides", Mod.res("cape.png")));
    }

    public Optional<CapeEntry> create(String name, Path path) {
        return this.createInternal(name, null, path);
    }

    public Optional<CapeEntry> create(String name, ResourceLocation texture) {
        return this.createInternal(name, texture, null);
    }

    private Optional<CapeEntry> createInternal(String name, ResourceLocation texture, Path path) {
        try {
            String id = Util.randomId();
            File file = new File(this.libraryFolder, id + ".png");
            var entry = new CapeEntry(name, id, file);

            if (path != null) {
                Files.copy(path, file.toPath());
            } else {
                Util.saveTexture(texture, 64, 32, file.toPath());
            }

            this.add(entry);
            return Optional.of(entry);
        } catch (IOException e) {
            Mod.LOGGER.error("failed to copy {}", path, e);
            return Optional.empty();
        }
    }

    public static class CapeEntry extends AbstractLibraryEntry {
        protected CapeEntry(String name, String id, @Nullable File file, @Nullable ResourceLocation textureLoc) {
            super(name, id, file, textureLoc);
        }

        protected CapeEntry(String name, String id, @NotNull File file) {
            super(name, id, file);
        }

        protected CapeEntry(String name, String id, @NotNull ResourceLocation textureLoc) {
            super(name, id, textureLoc);
        }

        @Override
        protected ResourceLocation getTextureFromFile() {
            return Util.texture("cape/library/" + this.id, LocalPlayerTexture.fromFile(this.file));
        }
    }
}
