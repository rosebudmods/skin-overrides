package net.orifu.skin_overrides;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.texture.PlayerSkin;
import net.orifu.skin_overrides.texture.LocalHttpTexture;
import net.orifu.skin_overrides.texture.LocalSkinTexture;

import static net.orifu.skin_overrides.SkinOverrides.CAPE_OVERRIDES;
import static net.orifu.skin_overrides.SkinOverrides.SKIN_OVERRIDES;

public class Overrides {
    public static boolean hasSkinImageOverride(GameProfile profile) {
        return getSkinImageOverride(profile).isPresent();
    }

    public static Optional<LocalSkinTexture> getSkinImageOverride(GameProfile profile) {
        return getTextureFor(SKIN_OVERRIDES, profile)
                .or(() -> getTextureFor(SKIN_OVERRIDES, profile, "wide"))
                .map(file -> new LocalSkinTexture(file, PlayerSkin.Model.WIDE))
                .or(() -> getTextureFor(SKIN_OVERRIDES, profile, "slim")
                        .map(file -> new LocalSkinTexture(file, PlayerSkin.Model.SLIM)));
    }

    public static boolean hasSkinCopyOverride(GameProfile profile) {
        return getSkinCopyOverride(profile).isPresent();
    }

    public static Optional<String> getSkinCopyOverride(GameProfile profile) {
        return getTextureFor(SKIN_OVERRIDES, profile, null, "txt").flatMap(file -> {
            try {
                return Optional.of(Files.readString(file.toPath()).trim());
            } catch (IOException e) {
                return Optional.empty();
            }
        }).flatMap(content -> content.length() == 0 ? Optional.empty() : Optional.of(content));
    }

    public static boolean hasCapeImageOverride(GameProfile profile) {
        return getCapeImageOverride(profile).isPresent();
    }

    public static Optional<LocalHttpTexture> getCapeImageOverride(GameProfile profile) {
        return getTextureFor(CAPE_OVERRIDES, profile).map(file -> new LocalHttpTexture(file));
    }

    protected static Optional<File> getTextureFor(String path, GameProfile profile, @Nullable String suffix,
            @Nullable String ext) {
        String suff = (suffix == null ? "" : "." + suffix) + "." + (ext == null ? "png" : ext);

        // username
        File file = Paths.get(path, profile.getName() + suff).toFile();
        if (file.exists())
            return Optional.of(file);

        // uuid with hyphens
        String uuid = profile.getId().toString();
        file = Paths.get(path, uuid + suff).toFile();
        if (file.exists())
            return Optional.of(file);

        // uuid without hyphens
        file = Paths.get(path, uuid.replace("-", "") + suff).toFile();
        if (file.exists())
            return Optional.of(file);

        return Optional.empty();
    }

    protected static Optional<File> getTextureFor(String path, GameProfile profile, @Nullable String suffix) {
        return getTextureFor(path, profile, suffix, null);
    }

    protected static Optional<File> getTextureFor(String path, GameProfile profile) {
        return getTextureFor(path, profile, null);
    }
}
