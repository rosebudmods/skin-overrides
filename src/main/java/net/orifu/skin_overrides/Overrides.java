package net.orifu.skin_overrides;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.texture.CopiedSkinTexture;
import net.orifu.skin_overrides.texture.LocalHttpTexture;
import net.orifu.skin_overrides.texture.LocalSkinTexture;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.util.OverrideFiles.Validated;

import static net.orifu.skin_overrides.SkinOverrides.CAPE_OVERRIDES;
import static net.orifu.skin_overrides.SkinOverrides.SKIN_OVERRIDES;
import static net.orifu.skin_overrides.util.OverrideFiles.deleteProfileFiles;
import static net.orifu.skin_overrides.util.OverrideFiles.findProfileFile;
import static net.orifu.skin_overrides.util.OverrideFiles.listProfiles;

public class Overrides {
    // #region local skin overrides

    protected static Optional<Validated<PlayerSkin.Model>> validateLocalSkinOverrideFile(String name, String ext) {
        if (name.contains(".")) {
            String[] parts = name.split("\\.", 2);
            if (ext.equals("png") && (parts[1].equals("wide") || parts[1].equals("slim"))) {
                return Optional.of(Validated.of(
                        parts[0], parts[1].equals("wide") ? PlayerSkin.Model.WIDE : PlayerSkin.Model.SLIM));
            }
        } else if (ext.equals("png")) {
            return Optional.of(Validated.of(name, PlayerSkin.Model.WIDE));
        }

        return Optional.empty();
    }

    protected static Optional<Validated<PlayerSkin.Model>> getLocalSkinOverrideFile(GameProfile profile) {
        return findProfileFile(SKIN_OVERRIDES, profile, Overrides::validateLocalSkinOverrideFile);
    }

    public static boolean hasLocalSkinOverride(GameProfile profile) {
        return getLocalSkinOverrideFile(profile).isPresent();
    }

    public static Optional<LocalSkinTexture> getLocalSkinOverride(GameProfile profile) {
        return getLocalSkinOverrideFile(profile).map(v -> new LocalSkinTexture(v.file(), v.data()));
    }

    public static void removeLocalSkinOverride(GameProfile profile) {
        deleteProfileFiles(SKIN_OVERRIDES, Overrides::validateLocalSkinOverrideFile, profile);
    }

    // #endregion
    // #region skin copy override

    protected static Optional<Validated<Boolean>> validateSkinCopyOverrideFile(String name, String ext) {
        if (ext.equals("txt")) {
            return Optional.of(Validated.of(name));
        }

        return Optional.empty();
    }

    protected static Optional<Validated<Boolean>> getSkinCopyOverrideFile(GameProfile profile) {
        return findProfileFile(SKIN_OVERRIDES, profile, Overrides::validateSkinCopyOverrideFile);
    }

    public static boolean hasSkinCopyOverride(GameProfile profile) {
        return getSkinCopyOverrideFile(profile).isPresent();
    }

    public static Optional<CopiedSkinTexture> getSkinCopyOverride(GameProfile profile) {
        return getSkinCopyOverrideFile(profile).flatMap(v -> {
            try {
                return Optional.of(Files.readString(v.file().toPath()).trim());
            } catch (IOException e) {
                return Optional.empty();
            }
        }).flatMap(content -> content.length() == 0 ? Optional.empty() : Optional.of(content))
                .flatMap(id -> {
                    Identifier textureId = Identifier.tryParse(id);
                    if (textureId.equals(null)) {
                        return Optional.empty();
                    } else if (textureId.getNamespace().equals("minecraft")) {
                        var remoteProfile = ProfileHelper.idToProfile(textureId.getPath());
                        if (remoteProfile.isPresent()) {
                            return Optional.of(new CopiedSkinTexture(remoteProfile.get()));
                        }
                    } else if (textureId.getNamespace().equals("skin_overrides")) {
                        var libraryEntry = Library.get(textureId.getPath());
                        if (libraryEntry != null) {
                            return Optional.of(new CopiedSkinTexture(libraryEntry));
                        }
                    }
                    return Optional.empty();
                });
    }

    public static void removeSkinCopyOverride(GameProfile profile) {
        deleteProfileFiles(SKIN_OVERRIDES, Overrides::validateSkinCopyOverrideFile, profile);
    }

    // #endregion
    // #region other skin override stuff

    public static void pickSkinFromLibrary(GameProfile profile, LibraryEntry libraryEntry) {
        removeLocalSkinOverride(profile);
        Path outputPath = Paths.get(SKIN_OVERRIDES,
                profile.getName() + ".txt");

        try {
            var writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
            writer.write(new Identifier("skin_overrides", libraryEntry.id).toString());
            writer.close();
        } catch (IOException e) {
            SkinOverrides.LOGGER.error("failed to save library entry with id {} to file", libraryEntry.id, e);
        }
    }

    public static void copyLocalSkinOverride(GameProfile profile, Path path, PlayerSkin.Model model) {
        removeLocalSkinOverride(profile);

        try {
            Path outputPath = Paths.get(SKIN_OVERRIDES,
                    profile.getName() + "." + model.toString().toLowerCase() + ".png");

            Files.copy(path, outputPath);
        } catch (IOException e) {
            SkinOverrides.LOGGER.error("failed to copy {}", path, e);
        }
    }

    public static List<GameProfile> profilesWithSkinOverride() {
        var li = new ArrayList<>(listProfiles(SKIN_OVERRIDES, Overrides::validateLocalSkinOverrideFile));
        li.addAll(listProfiles(SKIN_OVERRIDES, Overrides::validateSkinCopyOverrideFile));
        return li;
    }

    // #endregion
    // #region local cape override

    protected static Optional<Validated<Boolean>> validateLocalCapeOverrideFile(String name, String ext) {
        if (ext.equals("png")) {
            return Optional.of(Validated.of(name));
        }

        return Optional.empty();
    }

    protected static Optional<Validated<Boolean>> getLocalCapeOverrideFile(GameProfile profile) {
        return findProfileFile(CAPE_OVERRIDES, profile, Overrides::validateLocalCapeOverrideFile);
    }

    public static boolean hasLocalCapeOverride(GameProfile profile) {
        return getLocalCapeOverrideFile(profile).isPresent();
    }

    public static Optional<LocalHttpTexture> getLocalCapeOverride(GameProfile profile) {
        return getLocalCapeOverrideFile(profile).map(v -> new LocalHttpTexture(v.file()));
    }

    public static void copyLocalCapeOverride(GameProfile profile, Path path) {
        removeLocalCapeOverride(profile);

        try {
            Path outputPath = Paths.get(CAPE_OVERRIDES, profile.getName() + ".png");
            Files.copy(path, outputPath);
        } catch (IOException e) {
            SkinOverrides.LOGGER.error("failed to copy {}", path, e);
        }
    }

    public static void removeLocalCapeOverride(GameProfile profile) {
        deleteProfileFiles(CAPE_OVERRIDES, Overrides::validateLocalCapeOverrideFile, profile);
    }

    public static List<GameProfile> profilesWithCapeOverride() {
        return listProfiles(CAPE_OVERRIDES, Overrides::validateLocalCapeOverrideFile);
    }

    // #endregion
}
