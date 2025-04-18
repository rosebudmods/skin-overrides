package net.orifu.skin_overrides.override;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Library;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.library.SkinLibrary;
import net.orifu.skin_overrides.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LibraryOverrider implements OverrideManager.Overrider {
    protected final Library library;
    protected final String rootFolder;

    public LibraryOverrider(Library library, String rootFolder) {
        this.library = library;
        this.rootFolder = rootFolder;
    }

    @Override
    public final String fileName(GameProfile profile, @Nullable Skin.Model model) {
        return Util.id(profile) + ".txt";
    }

    @Override
    public Optional<OverrideManager.Override> get(File file, String name, String ext) {
        if (ext.equals("txt")) {
            return Util.readFile(file)
                    .flatMap(loc -> Optional.ofNullable(ResourceLocation.tryParse(loc)))
                    .filter(loc -> loc.getNamespace().equals(Mod.MOD_ID))
                    .flatMap(loc -> this.library.get(loc.getPath()).map(entry -> new LibraryOverride(name, entry)));
        }

        return Optional.empty();
    }

    public void addOverride(GameProfile profile, Library.LibraryEntry entry) {
        var outputPath = Paths.get(this.rootFolder, this.fileName(profile, null));

        try {
            var writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
            writer.write(Mod.res(entry.getId()).toString());
            writer.close();
        } catch (IOException e) {
            Mod.LOGGER.error("failed to save library entry with id {} to file", entry.getId(), e);
        }
    }

    public record LibraryOverride(String playerIdent, Library.LibraryEntry entry) implements OverrideManager.Override {
        @Override
        public ResourceLocation texture() {
            return this.entry.getTexture();
        }

        @Override
        @Nullable
        public Skin.Model model() {
            if (this.entry instanceof SkinLibrary.SkinEntry skinEntry) {
                return skinEntry.getModel();
            }
            return null;
        }

        @Override
        public Component info() {
            return Component.translatable("skin_overrides.override.library", this.entry.getName());
        }

        @Override
        public CompletableFuture<Optional<Skin.Signature>> signature() {
            if (!(this.entry instanceof SkinLibrary.SkinEntry skinEntry)) {
                return CompletableFuture.completedFuture(Optional.empty());
            }

            return skinEntry.signed().thenApply(signed -> {
                // it would be best to reassign `this.entry` here, but it's final, and i'm not
                // bothered to make this class *not* be a record. we can rely on the fact that
                // overrides are reloaded every 500ms (see `ModClient`) to know this instance of
                // LibraryOverride with the outdated entry will be replaced with one that isn't
                // out of date soon.
                // if you're applying an unsigned library entry more often than every 500ms, kudos.
                signed.ifPresent(newEntry -> SkinLibrary.INSTANCE.replace(this.entry, newEntry));

                return signed.map(s -> s.signature);
            });
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LibraryOverride that)) return false;
            return Objects.equals(playerIdent, that.playerIdent) && Objects.equals(entry, that.entry);
        }
    }
}
