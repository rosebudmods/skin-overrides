package net.orifu.skin_overrides.util;

import com.mojang.authlib.GameProfile;
//? if >=1.20.2
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.DefaultSkinHelper;
//? if >=1.19.2 {
import net.minecraft.server.Services;
//?} else if >=1.19
/*import net.minecraft.util.ApiServices;*/
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.UserCache;
import net.orifu.skin_overrides.Skin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

//? if >=1.19.2 {
import static net.minecraft.util.UuidUtil.getOfflinePlayerUuid;
//?} else if >=1.19 {
/*import static net.minecraft.util.dynamic.DynamicSerializableUuid.getOfflinePlayerUuid;
*///?} else
/*import static net.minecraft.entity.player.PlayerEntity.getOfflinePlayerUuid;*/

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

        return profile.orElseGet(() -> new GameProfile(getOfflinePlayerUuid(id), id));
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
        //?} else if >=1.19.3 {
        /*ArrayList<Skin> skins = new ArrayList<>();
        for (var skin : DefaultSkinHelper. /^? if >=1.20.1 {^/ /^DEFAULT_SKINS ^//^?} else >>^/ field_41121 ) {
            skins.add(new Skin(
                    skin.texture(), null, null,
                    skin.model().equals(DefaultSkinHelper. /^? if >=1.20.1 {^/ /^ModelType ^//^?} else >>^/ C_pdcbqpco .WIDE)
                            ? Skin.Model.WIDE : Skin.Model.SLIM));
        }
        return skins.toArray(Skin[]::new);
        *///?} else {
        /*return new Skin[] {
                new Skin(DefaultSkinHelper.STEVE_SKIN, null, null, Skin.Model.WIDE),
                new Skin(DefaultSkinHelper.ALEX_SKIN, null, null, Skin.Model.SLIM)
        };
        *///?}
    }

    protected static UserCache getUserCache() {
        if (userCache != null) {
            return userCache;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        //? if >=1.19 {
        //? if >=1.19.2 {
        Services services = Services.create(client.authService, client.runDirectory);
        //?} else {
        /*ApiServices services = ApiServices.create(client.authenticationService, client.runDirectory);
        *///?}
        userCache = services.userCache();
        //?} else {
        /*var gameProfileRepository = new YggdrasilAuthenticationService(client.getNetworkProxy()).createProfileRepository();
        userCache = new UserCache(gameProfileRepository, new File(client.runDirectory, MinecraftServer.USER_CACHE_FILE.getName()));
        *///?}
        return userCache;
    }
}
