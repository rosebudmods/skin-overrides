package net.orifu.skin_overrides.library;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.SkinNetworking;
import net.orifu.skin_overrides.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static net.orifu.skin_overrides.Mod.CAPE_OVERRIDES_PATH;

public class CapeLibrary extends AbstractLibrary {
    public static final CapeLibrary INSTANCE = new CapeLibrary();

    private CapeLibrary() {
        super(CAPE_OVERRIDES_PATH);
    }

    @Override
    protected Optional<LibraryEntry> tryLoadFromJson(JsonObject object, String name, String id, @Nullable File file, @Nullable ResourceLocation textureLoc) {
        return Optional.of(new CapeEntry(name, id, file, textureLoc));
    }

    @Override
    protected void addDefaultEntries() {
        this.entries.add(new CapeEntry("skin overrides", "skin_overrides", Mod.res("cape.png")));

        // add user's existing capes
        SkinNetworking.getPlayerProfile().ifPresent(profile -> {
            for (var cape : profile.capes().reversed()) {
                try {
                    var url = cape.url();
                    var file = File.createTempFile("skin-overrides_", "_user-cape");
                    file.delete();
                    var location =SkinTextureDownloader.downloadAndRegisterSkin(
                            Mod.res("temp/" + Util.randomId()), file.toPath(), url, false).get();

                    this.createInternal(cape.alias(), location, file.toPath(), cape.id());
                } catch (IOException | ExecutionException | InterruptedException e) {
                    Mod.LOGGER.error("error downloading cape", e);
                }
            }
        });
    }

    public Optional<CapeEntry> create(String name, Path path) {
        return this.createInternal(name, null, path, null);
    }

    public Optional<CapeEntry> create(String name, ResourceLocation texture) {
        return this.createInternal(name, texture, null, null);
    }

    private Optional<CapeEntry> createInternal(String name, ResourceLocation texture, Path path, @Nullable String id) {
        if (id == null)
            id = Util.randomId();

        try {
            File file = new File(this.libraryFolder, id + ".png");
            var entry = new CapeEntry(name, id, file, texture);

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
        private final Supplier<ResourceLocation> texture = Suppliers.memoize(() ->
                Util.texture("cape/library/" + this.fileHash, Util.textureFromFile(this.file).orElseThrow()));

        protected CapeEntry(String name, String id, @Nullable File file, @Nullable ResourceLocation textureLoc) {
            super(name, id, file, textureLoc);
        }

        protected CapeEntry(String name, String id, @NotNull ResourceLocation textureLoc) {
            super(name, id, textureLoc);
        }

        @Override
        protected ResourceLocation getTextureFromFile() {
            return this.texture.get();
        }
    }
}
