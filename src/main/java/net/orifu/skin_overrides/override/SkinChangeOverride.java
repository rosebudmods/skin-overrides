package net.orifu.skin_overrides.override;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.ProfileHelper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class SkinChangeOverride {
    @Nullable
    protected static Pair<Identifier, Skin.Model> override;

    public static Optional<Pair<Identifier, Skin.Model>> texture() {
        return Optional.ofNullable(override);
    }

    public static void set(String url, Skin.Model model) {
        try {
            File file = File.createTempFile("skin-overrides_", "_user-change");
            file.delete();

            var identifier = Mod.id("skin/user-change");
            var texture = new PlayerSkinTexture(file, url, ProfileHelper.userSkin(), true,
                    () -> override = new Pair<>(identifier, model));

            MinecraftClient.getInstance().getTextureManager().registerTexture(identifier, texture);
        } catch (IOException e) {
            Mod.LOGGER.error("failed to set player skin locally", e);
        }
    }
}
