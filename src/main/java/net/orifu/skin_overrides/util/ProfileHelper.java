package net.orifu.skin_overrides.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
//? if >=1.20.2
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.server.Services;
import net.minecraft.util.UserCache;
import net.minecraft.util.UuidUtil;
import net.orifu.skin_overrides.Skin;

public class ProfileHelper {
    public static final String UUID_REGEX = "[0-9a-fA-F]{8}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{12}";

    private static UserCache userCache;

    public static GameProfile user() {
        //? if >=1.20.2 {
        return MinecraftClient.getInstance().method_53462();
        //?} else
        /*return MinecraftClient.getInstance().getSession().getProfile();*/
    }

    public static GameProfile idToBasicProfile(String id) {
        // get the uuid
        Optional<GameProfile> profile = Optional.empty();
        if (id.matches(UUID_REGEX)) {
            try {
                // parse uuid
                UUID uuid =
                        /*? if >=1.20.2 {*/ com.mojang.util.UndashedUuid.fromStringLenient
                        /*?} else >>*/ /*UUIDTypeAdapter.fromString*/ (id);
                // convert uuid to profile (cached)
                // if not in cache, fetch the profile (also cached)
                profile = getUserCache().getByUuid(uuid).or(() -> uuidToProfile(uuid));
            } catch (IllegalArgumentException e) {
            }

        } else {
            // convert player username to profile (cached)
            profile = getUserCache().findByName(id);
        }

        return profile.orElseGet(() ->
                /*? if >=1.20.6 {*/ UuidUtil.createOfflinePlayerProfile(id)
                 /*?} else if =1.20.4 {*/ /*UuidUtil.method_54140(id)
                *//*?} else*/ /*new GameProfile(UuidUtil.getOfflinePlayerUuid(id), id)*/
        );
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
        var profileResult = MinecraftClient.getInstance().getSessionService()
                /*? >=1.20.2 {*/ .fetchProfile(uuid, false);
                /*?} else*/ /*.fillProfileProperties(new GameProfile(uuid, null), false);*/

        //? if >=1.20.2 {
        return Optional.ofNullable(profileResult).map(ProfileResult::profile);
        //?} else
        /*return profileResult.getName() != null ? Optional.of(profileResult) : Optional.empty();*/
    }

    public static Skin[] getDefaultSkins() {
        //? if >=1.20.2 {
        return Arrays.stream(DefaultSkinHelper.DEFAULT_SKINS).map(Skin::fromPlayerSkin).toArray(Skin[]::new);
        //?} else {
        /*ArrayList<Skin> skins = new ArrayList<>();
        for (var skin : DefaultSkinHelper.DEFAULT_SKINS) {
            skins.add(new Skin(
                    skin.texture(), null, null,
                    skin.model().equals(DefaultSkinHelper.ModelType.WIDE)
                            ? Skin.Model.WIDE : Skin.Model.SLIM));
        }
        return skins.toArray(Skin[]::new);
        *///?}
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
