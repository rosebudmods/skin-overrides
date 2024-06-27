package net.orifu.skin_overrides.override;

import static net.orifu.skin_overrides.Mod.CAPE_OVERRIDES;

import java.io.File;
import java.util.Optional;

import com.mojang.authlib.GameProfile;

import net.orifu.skin_overrides.texture.LocalPlayerTexture;
import net.orifu.skin_overrides.util.OverrideFiles.Validated;

public class LocalCapeOverride extends AbstractOverride<Void, LocalPlayerTexture> {
    public static final LocalCapeOverride INSTANCE = new LocalCapeOverride();

    @Override
    public String rootFolder() {
        return CAPE_OVERRIDES;
    }

    @Override
    protected String getFileName(GameProfile profile, Void data) {
        return profile.getName() + ".png";
    }

    @Override
    protected Optional<Validated<Void>> validateFile(File file, String name, String ext) {
        if (ext.equals("png")) {
            return Optional.of(Validated.of(name));
        }

        return Optional.empty();
    }

    @Override
    protected Optional<LocalPlayerTexture> tryGetTextureFromValidated(Validated<Void> v) {
        return Optional.of(new LocalPlayerTexture(v.file()));
    }
}
