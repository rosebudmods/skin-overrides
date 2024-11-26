package net.orifu.skin_overrides.override;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.texture.LocalPlayerTexture;
import net.orifu.skin_overrides.util.ComponentUtil;
import net.orifu.skin_overrides.util.Util;

import java.io.File;
import java.util.Locale;
import java.util.Objects;
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
                var texture = LocalPlayerTexture.fromFile(file, maybeModel);
                String hash = file.length() + "-" + file.lastModified();
                return Optional.of(new LocalSkinOverride(parts[0].toLowerCase(Locale.ROOT), texture, hash, maybeModel));
            }
        } else if (ext.equals("png")) {
            var texture = LocalPlayerTexture.fromFile(file, Skin.Model.WIDE);
            String hash = file.length() + "-" + file.lastModified();
            return Optional.of(new LocalSkinOverride(name.toLowerCase(Locale.ROOT), texture, hash, Skin.Model.WIDE));
        }

        return Optional.empty();
    }

    public record LocalSkinOverride(String playerIdent, AbstractTexture tex, String texHash, Skin.Model model) implements OverrideManager.Override {
        @Override
        public ResourceLocation texture() {
            return Util.texture("skin/local/" + this.playerIdent, this.tex);
        }

        @Override
        public Skin.Model model() {
            return this.model;
        }

        @Override
        public Component info() {
            return ComponentUtil.translatable("skin_overrides.override.local_image");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LocalSkinOverride that)) return false;
            return model == that.model && Objects.equals(playerIdent, that.playerIdent) && Objects.equals(texHash, that.texHash);
        }
    }
}
