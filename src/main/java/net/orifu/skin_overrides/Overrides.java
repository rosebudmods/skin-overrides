package net.orifu.skin_overrides;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UndashedUuid;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.server.Services;
import net.minecraft.util.Pair;
import net.minecraft.util.UserCache;
import net.minecraft.util.UuidUtil;
import net.orifu.skin_overrides.texture.LocalHttpTexture;
import net.orifu.skin_overrides.texture.LocalSkinTexture;

import static net.orifu.skin_overrides.SkinOverrides.CAPE_OVERRIDES;
import static net.orifu.skin_overrides.SkinOverrides.SKIN_OVERRIDES;

public class Overrides {
    public static final String UUID_REGEX = "^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{12}$";

    private static UserCache userCache;

    protected static Optional<Pair<File, PlayerSkin.Model>> getLocalSkinOverrideFile(GameProfile profile) {
        return getTextureFor(SKIN_OVERRIDES, profile)
                .or(() -> getTextureFor(SKIN_OVERRIDES, profile, "wide"))
                .map(file -> new Pair<>(file, PlayerSkin.Model.WIDE))
                .or(() -> getTextureFor(SKIN_OVERRIDES, profile, "slim")
                        .map(file -> new Pair<>(file, PlayerSkin.Model.SLIM)));
    }

    public static boolean hasLocalSkinOverride(GameProfile profile) {
        return getLocalSkinOverrideFile(profile).isPresent();
    }

    public static Optional<LocalSkinTexture> getLocalSkinOverride(GameProfile profile) {
        return getLocalSkinOverrideFile(profile)
                .map(pair -> new LocalSkinTexture(pair.getLeft(), pair.getRight()));
    }

    public static void removeLocalSkinOverride(GameProfile profile) {
        Optional<Pair<File, PlayerSkin.Model>> file;
        while ((file = getLocalSkinOverrideFile(profile)).isPresent()) {
            file.get().getLeft().delete();
        }
    }

    protected static Optional<File> getSkinCopyOverrideFile(GameProfile profile) {
        return getTextureFor(SKIN_OVERRIDES, profile, null, "txt");
    }

    public static boolean hasSkinCopyOverride(GameProfile profile) {
        return getSkinCopyOverrideFile(profile).isPresent();
    }

    public static Optional<GameProfile> getSkinCopyOverride(GameProfile profile) {
        return getSkinCopyOverrideFile(profile).flatMap(file -> {
            try {
                return Optional.of(Files.readString(file.toPath()).trim());
            } catch (IOException e) {
                return Optional.empty();
            }
        }).flatMap(content -> content.length() == 0 ? Optional.empty() : Optional.of(content))
                .flatMap(id -> idToProfile(id));
    }

    public static void removeSkinCopyOverride(GameProfile profile) {
        Optional<File> file;
        while ((file = getSkinCopyOverrideFile(profile)).isPresent()) {
            file.get().delete();
        }
    }

    public static List<GameProfile> profilesWithSkinOverride() {
        File path = new File(SKIN_OVERRIDES);
        ArrayList<GameProfile> profiles = new ArrayList<>();
        for (File file : path.listFiles()) {
            String name = FilenameUtils.getBaseName(file.getName());
            String ext = FilenameUtils.getExtension(file.getName());
            Optional<GameProfile> profile = Optional.empty();

            if (name.contains(".")) {
                String[] parts = name.split("\\.", 2);
                if (ext.equals("png") && (parts[1].equals("wide") || parts[1].equals("slim"))) {
                    profile = Optional.of(idToBasicProfile(parts[0]));
                }
            } else if (ext.equals("png") || ext.equals("txt")) {
                profile = Optional.of(idToBasicProfile(name));
            }

            if (profile.isPresent()) {
                profiles.add(profile.get());
            }
        }

        return profiles;
    }

    public static boolean hasCapeImageOverride(GameProfile profile) {
        return getCapeImageOverride(profile).isPresent();
    }

    public static Optional<LocalHttpTexture> getCapeImageOverride(GameProfile profile) {
        return getTextureFor(CAPE_OVERRIDES, profile).map(file -> new LocalHttpTexture(file));
    }

    public static GameProfile idToBasicProfile(String id) {
        // get the uuid
        Optional<GameProfile> profile = Optional.empty();
        if (id.matches(UUID_REGEX)) {
            try {
                // parse uuid
                UUID uuid = id.contains("-") ? UUID.fromString(id) : UndashedUuid.fromString(id);
                // convert uuid to profile (cached)
                // if not in cache, fetch the profile (also cached)
                profile = getUserCache().getByUuid(uuid).or(() -> uuidToProfile(uuid));
            } catch (IllegalArgumentException e) {
            }

        } else {
            // convert player username to profile (cached)
            profile = getUserCache().findByName(id);
        }

        return profile.orElseGet(() -> UuidUtil.method_54140(id));
    }

    public static Optional<GameProfile> idToProfile(String id) {
        var basicProfile = idToBasicProfile(id);
        return uuidToProfile(basicProfile.getId());
    }

    public static GameProfile tryUpgradeBasicProfile(GameProfile basicProfile) {
        return uuidToProfile(basicProfile.getId()).orElse(basicProfile);
    }

    public static Optional<GameProfile> uuidToProfile(UUID uuid) {
        // get the full profile (cached)
        var profileResult = MinecraftClient.getInstance().getSessionService().fetchProfile(uuid, false);

        if (profileResult != null) {
            return Optional.of(profileResult.profile());
        }
        return Optional.empty();
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

    protected static UserCache getUserCache() {
        if (userCache != null) {
            return userCache;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Services services = Services.create(client.authService, client.runDirectory);
        userCache = services.userCache();
        return userCache;
    }
}
