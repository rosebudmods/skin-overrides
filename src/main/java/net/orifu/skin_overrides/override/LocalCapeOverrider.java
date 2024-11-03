package net.orifu.skin_overrides.override;

import com.mojang.authlib.GameProfile;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.texture.LocalPlayerTexture;
import net.orifu.skin_overrides.util.TextUtil;
import net.orifu.skin_overrides.util.Util;

import java.io.File;
import java.util.Locale;
import java.util.Optional;

public class LocalCapeOverrider implements OverrideManager.Overrider {
    @Override
    public String fileName(GameProfile profile, Skin.Model data) {
        return Util.id(profile) + ".png";
    }

    @Override
    public Optional<OverrideManager.Override> get(File file, String name, String ext) {
        if (ext.equals("png")) {
            return Optional.of(new LocalCapeOverride(name.toLowerCase(Locale.ROOT), new LocalPlayerTexture(file)));
        }

        return Optional.empty();
    }

    public record LocalCapeOverride(String playerIdent, LocalPlayerTexture tex) implements OverrideManager.Override {
        @Override
        public Identifier texture() {
            return Util.texture("cape/local/" + this.playerIdent, this.tex);
        }

        @Override
        public Text info() {
            return TextUtil.translatable("skin_overrides.override.local_image");
        }
    }
}
