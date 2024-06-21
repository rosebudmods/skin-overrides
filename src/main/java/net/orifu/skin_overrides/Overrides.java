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
import net.orifu.skin_overrides.Library.CapeEntry;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.Library.SkinEntry;
import net.orifu.skin_overrides.texture.CopiedCapeTexture;
import net.orifu.skin_overrides.texture.CopiedSkinTexture;
import net.orifu.skin_overrides.texture.LocalPlayerTexture;
import net.orifu.skin_overrides.texture.LocalSkinTexture;
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
        return getSkinCopyOverrideFile(profile).flatMap(Overrides::getIdentifierFromFile)
                .flatMap(CopiedSkinTexture::fromIdentifier);
    }

    public static void removeSkinCopyOverride(GameProfile profile) {
        deleteProfileFiles(SKIN_OVERRIDES, Overrides::validateSkinCopyOverrideFile, profile);
    }

    // #endregion
    // #region other skin override stuff

    public static boolean hasSkinOverride(GameProfile profile) {
        return hasLocalSkinOverride(profile) || hasSkinCopyOverride(profile);
    }

    public static List<GameProfile> profilesWithSkinOverride() {
        var li = new ArrayList<>(listProfiles(SKIN_OVERRIDES, Overrides::validateLocalSkinOverrideFile));
        li.addAll(listProfiles(SKIN_OVERRIDES, Overrides::validateSkinCopyOverrideFile));
        return li;
    }

    public static void removeSkinOverride(GameProfile profile) {
        removeLocalSkinOverride(profile);
        removeSkinCopyOverride(profile);
    }

    public static void pickSkinFromLibrary(GameProfile profile, SkinEntry libraryEntry) {
        removeSkinOverride(profile);
        Path outputPath = Paths.get(SKIN_OVERRIDES, profile.getName() + ".txt");

        try {
            var writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
            writer.write(new Identifier("skin_overrides", libraryEntry.id).toString());
            writer.close();
        } catch (IOException e) {
            SkinOverrides.LOGGER.error("failed to save library entry with id {} to file", libraryEntry.id, e);
        }
    }

    public static void addSkinToLibrary(String name, Path path, PlayerSkin.Model model) {
        try {
            var entry = new SkinEntry(name, model);
            Files.copy(path, entry.file.toPath());
            Library.addSkin(entry);
        } catch (IOException e) {
            SkinOverrides.LOGGER.error("failed to copy {}", path, e);
        }
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

    public static Optional<LocalPlayerTexture> getLocalCapeOverride(GameProfile profile) {
        return getLocalCapeOverrideFile(profile).map(v -> new LocalPlayerTexture(v.file()));
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

    // #endregion
    // #region cape copy override

    protected static Optional<Validated<Boolean>> validateCapeCopyOverrideFile(String name, String ext) {
        if (ext.equals("txt")) {
            return Optional.of(Validated.of(name));
        }

        return Optional.empty();
    }

    protected static Optional<Validated<Boolean>> getCapeCopyOverrideFile(GameProfile profile) {
        return findProfileFile(CAPE_OVERRIDES, profile, Overrides::validateCapeCopyOverrideFile);
    }

    public static boolean hasCapeCopyOverride(GameProfile profile) {
        return getCapeCopyOverrideFile(profile).isPresent();
    }

    public static Optional<CopiedCapeTexture> getCapeCopyOverride(GameProfile profile) {
        return getCapeCopyOverrideFile(profile).flatMap(Overrides::getIdentifierFromFile)
                .flatMap(CopiedCapeTexture::fromIdentifier);
    }

    public static void removeCapeCopyOverride(GameProfile profile) {
        deleteProfileFiles(CAPE_OVERRIDES, Overrides::validateCapeCopyOverrideFile, profile);
    }

    // #endregion
    // #region other cape override stuff

    public static boolean hasCapeOverride(GameProfile profile) {
        return hasLocalCapeOverride(profile) || hasCapeCopyOverride(profile);
    }

    public static List<GameProfile> profilesWithCapeOverride() {
        var li = new ArrayList<>(listProfiles(CAPE_OVERRIDES, Overrides::validateLocalCapeOverrideFile));
        li.addAll(listProfiles(CAPE_OVERRIDES, Overrides::validateCapeCopyOverrideFile));
        return li;
    }

    public static void removeCapeOverride(GameProfile profile) {
        removeLocalCapeOverride(profile);
        removeCapeCopyOverride(profile);
    }

    public static void pickCapeFromLibrary(GameProfile profile, LibraryEntry libraryEntry) {
        removeCapeOverride(profile);
        Path outputPath = Paths.get(CAPE_OVERRIDES, profile.getName() + ".txt");

        try {
            var writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
            writer.write(new Identifier("skin_overrides", libraryEntry.id).toString());
            writer.close();
        } catch (IOException e) {
            SkinOverrides.LOGGER.error("failed to save library entry with id {} to file", libraryEntry.id, e);
        }
    }

    public static void addCapeToLibrary(String name, Path path) {
        try {
            var entry = new CapeEntry(name);
            Files.copy(path, entry.file.toPath());
            Library.addCape(entry);
        } catch (IOException e) {
            SkinOverrides.LOGGER.error("failed to copy {}", path, e);
        }
    }

    // #endregion
    // #region utilities

    private static <T> Optional<Identifier> getIdentifierFromFile(Validated<T> v) {
        // read file
        Optional<String> empty = Optional.empty();
        return empty.or(() -> {
            try {
                return Optional.of(Files.readString(v.file().toPath()).trim());
            } catch (IOException e) {
                return Optional.empty();
            }
        })
                // ignore empty files
                .flatMap(content -> content.length() == 0 ? Optional.empty() : Optional.of(content))
                // convert to id
                .flatMap(rawId -> {
                    Identifier id = Identifier.tryParse(rawId);
                    return id != null ? Optional.of(id) : Optional.empty();
                });
    }

    // #endregion
}
