package net.orifu.skin_overrides.override;

import static net.orifu.skin_overrides.SkinOverrides.SKIN_OVERRIDES;

import java.io.File;
import java.util.Optional;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.texture.PlayerSkin;
import net.orifu.skin_overrides.texture.LocalSkinTexture;
import net.orifu.skin_overrides.util.OverrideFiles.Validated;

public class LocalSkinOverride extends AbstractOverride<PlayerSkin.Model, LocalSkinTexture> {
    public static final LocalSkinOverride INSTANCE = new LocalSkinOverride();

    @Override
    public String rootFolder() {
        return SKIN_OVERRIDES;
    }

    @Override
    protected String getFileName(GameProfile profile, PlayerSkin.Model model) {
        return profile.getName() + "." + model.toString().toLowerCase() + ".png";
    }

    @Override
    protected Optional<Validated<PlayerSkin.Model>> validateFile(File file, String name, String ext) {
        if (name.contains(".")) {
            String[] parts = name.split("\\.", 2);
            if (ext.equals("png") && (parts[1].equals("wide") || parts[1].equals("slim"))) {
                return Optional.of(Validated.of(
                        parts[0], parts[1].equals("wide") ? PlayerSkin.Model.WIDE : PlayerSkin.Model.SLIM));
            }
        } else if (ext.equals("png")) {
            return Optional.of(Validated.of(name, PlayerSkin.Model.WIDE));
        }

        return Optional.empty();
    }

    @Override
    protected Optional<LocalSkinTexture> tryGetTextureFromValidated(Validated<PlayerSkin.Model> v) {
        return Optional.of(new LocalSkinTexture(v.file(), v.data()));
    }
}
