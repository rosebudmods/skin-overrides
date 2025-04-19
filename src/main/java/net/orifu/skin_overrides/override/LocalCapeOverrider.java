package net.orifu.skin_overrides.override;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.TextureHelper;
import net.orifu.skin_overrides.util.Util;

import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class LocalCapeOverrider implements OverrideManager.Overrider {
    @Override
    public String fileName(GameProfile profile, Skin.Model data) {
        return Util.id(profile) + ".png";
    }

    @Override
    public Optional<OverrideManager.Override> get(File file, String name, String ext) {
        if (ext.equals("png")) {
            String hash = Util.hashFile(file);
            return Optional.of(new LocalCapeOverride(name.toLowerCase(Locale.ROOT), file, hash));
        }

        return Optional.empty();
    }

    public record LocalCapeOverride(String playerIdent, String texHash,
                Supplier<ResourceLocation> memoizedTexture) implements OverrideManager.Override {
        public LocalCapeOverride(String playerIdent, File file, String texHash) {
            this(playerIdent, texHash, Suppliers.memoize(() ->
                    TextureHelper.cape().location("cape/local/" + texHash).path(file).register().orElseThrow()));
        }

        @Override
        public ResourceLocation texture() {
            return this.memoizedTexture.get();
        }

        @Override
        public Component info() {
            return Component.translatable("skin_overrides.override.local_image");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LocalCapeOverride that)) return false;
            return Objects.equals(texHash, that.texHash) && Objects.equals(playerIdent, that.playerIdent);
        }
    }
}
