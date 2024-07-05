package net.orifu.skin_overrides.override;

import com.mojang.authlib.GameProfile;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.texture.LocalSkinTexture;
import net.orifu.skin_overrides.util.Util;
import net.orifu.skin_overrides.util.TextUtil;

import java.io.File;
import java.util.Optional;

public class LocalSkinOverrider implements OverrideManager.Overrider {
    @Override
    public String fileName(GameProfile profile, Skin.Model model) {
        return Util.id(profile) + "." + model.toString().toLowerCase() + ".png";
    }

    @Override
    public Optional<OverrideManager.Override> get(File file, String name, String ext) {
        if (name.contains(".")) {
            String[] parts = name.split("\\.", 2);
            Skin.Model maybeModel = Skin.Model.tryParse(parts[1]);
            if (ext.equals("png") && maybeModel != null) {
                LocalSkinTexture texture = new LocalSkinTexture(file, maybeModel);
                return Optional.of(new LocalSkinOverride(parts[0], texture, maybeModel));
            }
        } else if (ext.equals("png")) {
            LocalSkinTexture texture = new LocalSkinTexture(file, Skin.Model.WIDE);
            return Optional.of(new LocalSkinOverride(name, texture, Skin.Model.WIDE));
        }

        return Optional.empty();
    }

    public record LocalSkinOverride(String playerIdent, LocalSkinTexture tex, Skin.Model model) implements OverrideManager.Override {
        @Override
        public Identifier texture() {
            return Util.texture("skin/local/" + this.playerIdent, this.tex);
        }

        @Override
        public Skin.Model model() {
            return this.model;
        }

        @Override
        public Text info() {
            return TextUtil.translatable("skin_overrides.override.local_image");
        }
    }
}
