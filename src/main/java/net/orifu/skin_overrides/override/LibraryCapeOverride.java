package net.orifu.skin_overrides.override;

import static net.orifu.skin_overrides.SkinOverrides.CAPE_OVERRIDES;

import java.io.File;
import java.util.Optional;

import net.orifu.skin_overrides.Library;
import net.orifu.skin_overrides.Library.CapeEntry;
import net.orifu.skin_overrides.texture.CopiedCapeTexture;
import net.orifu.skin_overrides.util.OverrideFiles.Validated;
import net.orifu.skin_overrides.util.Util;

public class LibraryCapeOverride extends AbstractLibraryOverride<CapeEntry, CopiedCapeTexture> {
    public static final LibraryCapeOverride INSTANCE = new LibraryCapeOverride();

    @Override
    public String rootFolder() {
        return CAPE_OVERRIDES;
    }

    @Override
    protected Optional<Validated<CapeEntry>> validateFile(File file, String name, String ext) {
        if (ext.equals("txt")) {
            return Util.readFile(file).flatMap(Util::ensureLibraryIdentifier).flatMap(Library::getCape)
                    .map(entry -> Validated.of(name, entry));
        }

        return Optional.empty();
    }

    @Override
    protected Optional<CopiedCapeTexture> tryGetTextureFromValidated(Validated<CapeEntry> v) {
        return Optional.of(CopiedCapeTexture.fromLibrary(v.data()));
    }
}
