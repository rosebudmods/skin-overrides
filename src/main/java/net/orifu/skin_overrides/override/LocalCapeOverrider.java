package net.orifu.skin_overrides.override;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.ComponentUtil;
import net.orifu.skin_overrides.util.Util;

import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class LocalCapeOverrider implements OverrideManager.Overrider {
    @Override
    public String fileName(GameProfile profile, Skin.Model data) {
        return Util.id(profile) + ".png";
    }

    @Override
    public Optional<OverrideManager.Override> get(File file, String name, String ext) {
        if (ext.equals("png")) {
            var texture = Util.textureFromFile(file);
            String hash = Util.hashFile(file);
            return Optional.of(new LocalCapeOverride(name.toLowerCase(Locale.ROOT), texture, hash));
        }

        return Optional.empty();
    }

    public record LocalCapeOverride(String playerIdent, AbstractTexture tex, String texHash) implements OverrideManager.Override {
        @Override
        public ResourceLocation texture() {
            return Util.texture("cape/local/" + this.playerIdent, this.tex);
        }

        @Override
        public Component info() {
            return ComponentUtil.translatable("skin_overrides.override.local_image");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LocalCapeOverride that)) return false;
            return Objects.equals(texHash, that.texHash) && Objects.equals(playerIdent, that.playerIdent);
        }
    }
}
