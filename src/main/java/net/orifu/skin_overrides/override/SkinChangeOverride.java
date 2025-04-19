package net.orifu.skin_overrides.override;

import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

//? if >=1.21.4 {
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
//?} else {
/*import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.Minecraft;
*///?}

public class SkinChangeOverride {
    @Nullable
    protected static Tuple<ResourceLocation, Skin.Model> override;

    public static Optional<Tuple<ResourceLocation, Skin.Model>> texture() {
        return Optional.ofNullable(override);
    }

    public static Optional<Tuple<ResourceLocation, Skin.Model>> texture(GameProfile profile) {
        return profile.equals(ProfileHelper.user()) ? texture() : Optional.empty();
    }

    public static void set(String url, Skin.Model model) {
        try {
            var file = Util.nameTempFile().orElseThrow();
            var location = Mod.res("skin/user-change");

            // TODO: update this
            //? if >=1.21.4 {
            SkinTextureDownloader.downloadAndRegisterSkin(location, file.toPath(), url, true).thenRun(
                    () -> override = new Tuple<>(location, model));
            //?} else {
            /*var texture = new HttpTexture(file, url, ProfileHelper.userSkin(), true,
                    () -> override = new Tuple<>(location, model));

            Minecraft.getInstance().getTextureManager().register(location, texture);
            *///?}
        } catch (NullPointerException e) {
            Mod.LOGGER.error("failed to set player skin locally", e);
        }
    }
}
