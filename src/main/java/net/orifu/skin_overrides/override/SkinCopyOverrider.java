package net.orifu.skin_overrides.override;

import com.mojang.authlib.GameProfile;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SkinCopyOverrider implements OverrideManager.Overrider {
    @Override
    public String fileName(GameProfile profile, Skin.Model model) {
        return Util.id(profile) + ".txt";
    }

    @Override
    public Optional<OverrideManager.Override> get(File file, String name, String ext) {
        if (ext.equals("txt")) {
            return Util.readFile(file)
                    .flatMap(id -> Optional.ofNullable(Identifier.tryParse(id)))
                    .filter(id -> id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE))
                    .flatMap(id -> ProfileHelper.idToProfile(id.getPath()))
                    .map(profile -> new SkinCopyOverride(name, profile, Skin.fetchSkin(profile)));
        }

        return Optional.empty();
    }

    public record SkinCopyOverride(String playerIdent, GameProfile profile, CompletableFuture<Skin> copyFrom) implements OverrideManager.Override {
        @Override
        public Identifier texture() {
            Skin skin = this.copyFrom.getNow(null);
            return skin != null ? skin.texture() : null;
        }

        @Override
        public Text info() {
            return Text.translatable("skin_overrides.override.copy", this.profile.getName());
        }

        @Override
        @Nullable
        public Skin.Model model() {
            Skin skin = this.copyFrom.getNow(null);
            return skin != null ? skin.model() : null;
        }
    }
}
