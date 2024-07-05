package net.orifu.skin_overrides.override;

import com.mojang.authlib.GameProfile;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Library;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.library.SkinLibrary;
import net.orifu.skin_overrides.util.Util;
import net.orifu.skin_overrides.util.TextUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class LibraryOverrider implements OverrideManager.Overrider {
    protected final Library library;
    protected final String rootFolder;

    public LibraryOverrider(Library library, String rootFolder) {
        this.library = library;
        this.rootFolder = rootFolder;
    }

    @Override
    public final String fileName(GameProfile profile, Skin.Model model) {
        return Util.id(profile) + ".txt";
    }

    @Override
    public Optional<OverrideManager.Override> get(File file, String name, String ext) {
        if (ext.equals("txt")) {
            return Util.readFile(file)
                    .flatMap(Util::ensureLibraryIdentifier)
                    .flatMap(this.library::get)
                    .map(entry -> new LibraryOverride(name, entry));
        }

        return Optional.empty();
    }

    public void addOverride(GameProfile profile, Library.LibraryEntry entry) {
        var outputPath = Paths.get(this.rootFolder, this.fileName(profile, null));

        try {
            var writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
            writer.write(Mod.id(entry.getId()).toString());
            writer.close();
        } catch (IOException e) {
            Mod.LOGGER.error("failed to save library entry with id {} to file", entry.getId(), e);
        }
    }

    public record LibraryOverride(String playerIdent, Library.LibraryEntry entry) implements OverrideManager.Override {
        @Override
        public Identifier texture() {
            return this.entry.getTexture();
        }

        @Override
        @Nullable
        public Skin.Model model() {
            if (this.entry instanceof SkinLibrary.SkinEntry entry) {
                return entry.getModel();
            }
            return null;
        }

        @Override
        public Text info() {
            return TextUtil.translatable("skin_overrides.override.library", this.entry.getName());
        }
    }
}
