package net.orifu.skin_overrides.util;

import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UndashedUuid;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.Services;
import net.minecraft.util.UserCache;
import net.minecraft.util.UuidUtil;

public class ProfileHelper {
    public static final String UUID_REGEX = "^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{12}$";

    private static UserCache userCache;

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
