package net.orifu.skin_overrides.override;

import java.io.File;
import java.util.Optional;

import com.mojang.authlib.GameProfile;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.texture.LocalSkinTexture;
import net.orifu.skin_overrides.util.OverrideFiles.Validated;
import net.orifu.skin_overrides.util.Util;

import static net.orifu.skin_overrides.Mod.SKIN_OVERRIDES;

public class LocalSkinOverride extends AbstractOverride<Skin.Model, LocalSkinTexture> {
    public static final LocalSkinOverride INSTANCE = new LocalSkinOverride();

    @Override
    public String rootFolder() {
        return SKIN_OVERRIDES;
    }

    @Override
    protected String getFileName(GameProfile profile, Skin.Model model) {
        return Util.id(profile) + "." + model.toString().toLowerCase() + ".png";
    }

    @Override
    protected Optional<Validated<Skin.Model>> validateFile(File file, String name, String ext) {
        if (name.contains(".")) {
            String[] parts = name.split("\\.", 2);
            Skin.Model maybeModel = Skin.Model.tryParse(parts[1]);
            if (ext.equals("png") && maybeModel != null) {
                return Optional.of(Validated.of(parts[0], maybeModel));
            }
        } else if (ext.equals("png")) {
            return Optional.of(Validated.of(name, Skin.Model.WIDE));
        }

        return Optional.empty();
    }

    @Override
    protected Optional<LocalSkinTexture> tryGetTextureFromValidated(Validated<Skin.Model> v) {
        return Optional.of(new LocalSkinTexture(v.file(), v.data()));
    }
}
