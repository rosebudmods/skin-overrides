package net.orifu.skin_overrides.override;

import static net.orifu.skin_overrides.Mod.SKIN_OVERRIDES;

import java.io.File;
import java.util.Optional;

import net.orifu.skin_overrides.Library;
import net.orifu.skin_overrides.Library.SkinEntry;
import net.orifu.skin_overrides.texture.CopiedSkinTexture;
import net.orifu.skin_overrides.util.Util;
import net.orifu.skin_overrides.util.OverrideFiles.Validated;

public class LibrarySkinOverride extends AbstractLibraryOverride<SkinEntry, CopiedSkinTexture> {
    public static final LibrarySkinOverride INSTANCE = new LibrarySkinOverride();

    @Override
    public String rootFolder() {
        return SKIN_OVERRIDES;
    }

    @Override
    protected Optional<Validated<SkinEntry>> validateFile(File file, String name, String ext) {
        if (ext.equals("txt")) {
            return Util.readFile(file).flatMap(Util::ensureLibraryIdentifier).flatMap(Library::getSkin)
                    .map(entry -> Validated.of(name, entry));
        }

        return Optional.empty();
    }

    @Override
    protected Optional<CopiedSkinTexture> tryGetTextureFromValidated(Validated<SkinEntry> v) {
        return Optional.of(CopiedSkinTexture.fromLibrary(v.data()));
    }
}
