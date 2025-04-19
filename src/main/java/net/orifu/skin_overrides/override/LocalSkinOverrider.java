package net.orifu.skin_overrides.override;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.TextureHelper;
import net.orifu.skin_overrides.util.Util;

import java.io.File;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

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
                String hash = Util.hashFile(file);
                return Optional.of(new LocalSkinOverride(parts[0].toLowerCase(Locale.ROOT), file, hash, maybeModel));
            }
        } else if (ext.equals("png")) {
            String hash = Util.hashFile(file);
            return Optional.of(new LocalSkinOverride(name.toLowerCase(Locale.ROOT), file, hash, Skin.Model.WIDE));
        }

        return Optional.empty();
    }

    public record LocalSkinOverride(String playerIdent, String texHash, Skin.Model model,
                Supplier<ResourceLocation> memoizedTexture) implements OverrideManager.Override {
        public LocalSkinOverride(String playerIdent, File file, String texHash, Skin.Model model) {
            this(playerIdent, texHash, model, Suppliers.memoize(() ->
                    TextureHelper.skin().location("skin/local/" + texHash).path(file).register().orElseThrow()));
        }

        @Override
        public ResourceLocation texture() {
            return this.memoizedTexture.get();
        }

        @Override
        public Skin.Model model() {
            return this.model;
        }

        @Override
        public Component info() {
            return Component.translatable("skin_overrides.override.local_image");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LocalSkinOverride that)) return false;
            return model == that.model && Objects.equals(playerIdent, that.playerIdent) && Objects.equals(texHash, that.texHash);
        }
    }
}
