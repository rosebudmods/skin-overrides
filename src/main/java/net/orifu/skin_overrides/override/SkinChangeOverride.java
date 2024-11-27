package net.orifu.skin_overrides.override;

//import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.ProfileHelper;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class SkinChangeOverride {
    @Nullable
    protected static Tuple<ResourceLocation, Skin.Model> override;

    public static Optional<Tuple<ResourceLocation, Skin.Model>> texture() {
        return Optional.ofNullable(override);
    }

    public static void set(String url, Skin.Model model) {
        try {
            File file = File.createTempFile("skin-overrides_", "_user-change");
            file.delete();

            var location = Mod.res("skin/user-change");

            SkinTextureDownloader.downloadAndRegisterSkin(location, file.toPath(), url, true).thenRun(
                    () -> override = new Tuple<>(location, model));

            // TODO
//            var texture = new HttpTexture(file, url, ProfileHelper.userSkin(), true,
//                    () -> override = new Tuple<>(location, model));
//
//            Minecraft.getInstance().getTextureManager().register(location, texture);
        } catch (IOException e) {
            Mod.LOGGER.error("failed to set player skin locally", e);
        }
    }
}
