package net.orifu.skin_overrides.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.server.players.GameProfileCache;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

//? if <1.20.4
/*import com.mojang.authlib.minecraft.MinecraftProfileTexture;*/
//? if >=1.20.2 {
import com.mojang.authlib.yggdrasil.ProfileResult;
//?} else {
/*import java.util.ArrayList;
import com.mojang.util.UUIDTypeAdapter;
*///?}

public class ProfileHelper {
    public static final String UUID_REGEX = "[0-9a-fA-F]{8}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{12}";

    //? if <1.20.2
    /*private static GameProfile cachedUserProfile;*/
    private static GameProfileCache profileCache;

    public static GameProfile user() {
        //? if >=1.20.2 {
        return Minecraft.getInstance().getGameProfile();
        //?} else {
        /*if (cachedUserProfile != null) return cachedUserProfile;
        cachedUserProfile = Minecraft.getInstance().getUser().getGameProfile();
        cachedUserProfile.getProperties().putAll(Minecraft.getInstance().getProfileProperties());
        return cachedUserProfile;
        *///?}
    }

    public static ResourceLocation userSkin() {
        //? if >=1.20.2 {
        return DefaultPlayerSkin.getDefaultTexture();
        //?} else
        /*return DefaultPlayerSkin.getDefaultSkin(user().getId());*/
    }

    public static CompletableFuture<GameProfile> idToBasicProfile(String id) {
        return CompletableFuture.supplyAsync(() -> idToBasicProfileSync(id));
    }

    public static GameProfile idToBasicProfileSync(String id) {
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
                profile = getProfileCache().get(uuid).or(() -> uuidToProfile(uuid));
            } catch (IllegalArgumentException ignored) {}

        } else {
            // convert player username to profile (cached)
            profile = getProfileCache().get(id);
        }

        return profile.orElseGet(() -> new GameProfile(getOfflinePlayerUuid(id), id));
    }

    public static CompletableFuture<Optional<GameProfile>> idToProfile(String id) {
        return CompletableFuture.supplyAsync(() -> idToProfileSync(id));
    }

    public static CompletableFuture<Optional<GameProfile>> idToSecureProfile(String id) {
        return idToBasicProfile(id).thenApplyAsync(profile -> uuidToSecureProfile(profile.getId()));
    }

    public static Optional<GameProfile> idToProfileSync(String id) {
        var basicProfile = idToBasicProfileSync(id);
        return uuidToProfile(basicProfile.getId());
    }

    public static GameProfile tryUpgradeBasicProfile(GameProfile basicProfile) {
        return uuidToProfile(basicProfile.getId()).orElse(basicProfile);
    }

    public static Optional<GameProfile> uuidToProfile(UUID uuid) {
        return uuidToProfile(uuid, false);
    }

    public static Optional<GameProfile> uuidToSecureProfile(UUID uuid) {
        return uuidToProfile(uuid, true);
    }

    public static CompletableFuture<Optional<GameProfile>> uuidToProfileExpectingSkinUrl(UUID uuid, String skinUrl) {
        // when updating your skin, fetching the profile from the services may not be up to date.
        // seems the best way to work around this is to just keep trying?
        // here we try 4 times 5 seconds apart (giving up after 20 seconds).
        // mineskin's api's source code seems to use 3 attempts 5 seconds apart (see getSkinDataWithRetry)
        // https://github.com/MineSkin/api.mineskin.org/blob/master/src/generator/Generator.ts
        // in the future it may be worth deferring this to mineskin if it fails often enough (sorry mineskin)

        return CompletableFuture.supplyAsync(() -> {
            for (int tries = 0; tries < 4; tries++) {
                var profile = uuidToSecureProfile(uuid);
                if (profile.isEmpty()) continue;

                String receivedUrl = getProfileSkinUrl(profile.get());
                if (receivedUrl.equals(skinUrl)) return profile;

                Mod.LOGGER.debug("expected and received skin urls:\n" + skinUrl + "\n" + receivedUrl);

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}
            }

            return Optional.empty();
        });
    }

    public static String getProfileSkinUrl(GameProfile profile) {
        //? if >=1.20.4 {
        var packed = Minecraft.getInstance().getMinecraftSessionService().getPackedTextures(profile);
        return Minecraft.getInstance().getMinecraftSessionService().unpackTextures(packed).skin().getUrl();
        //?} else {
        /*return Minecraft.getInstance().getMinecraftSessionService().getTextures(profile, false)
                .get(MinecraftProfileTexture.Type.SKIN).getUrl();
        *///?}
    }

    public static Skin.Signature getProfileSkinSignature(GameProfile profile) {
        //? if >=1.20.4 {
        var packed = Minecraft.getInstance().getMinecraftSessionService().getPackedTextures(profile);
        return new Skin.Signature(packed.value(), packed.signature());
        //?} else {
        /*var property = profile.getProperties().get("textures").stream().findFirst().orElseThrow();
        /^? if >=1.20.2 {^/ return new Skin.Signature(property.value(), property.signature());
        /^?} else^/ /^return new Skin.Signature(property.getValue(), property.getSignature());^/
        *///?}
    }

    protected static Optional<GameProfile> uuidToProfile(UUID uuid, boolean secure) {
        // get the full profile (cached if not secure)
        var profileResult = Minecraft.getInstance().getMinecraftSessionService()
                /*? >=1.20.2 {*/ .fetchProfile(uuid, secure);
                /*?} else*/ /*.fillProfileProperties(new GameProfile(uuid, null), true);*/

        //? if >=1.20.2 {
        return Optional.ofNullable(profileResult).map(ProfileResult::profile);
        //?} else
        /*return profileResult.getName() != null ? Optional.of(profileResult) : Optional.empty();*/
    }

    public static ResourceLocation getDefaultSkin() {
        //? if >=1.20.2 {
        return DefaultPlayerSkin.getDefaultTexture();
        //?} else
        /*return DefaultPlayerSkin.getDefaultSkin();*/
    }

    public static Skin[] getDefaultSkins() {
        //? if >=1.20.2 {
        return Arrays.stream(DefaultPlayerSkin.DEFAULT_SKINS).map(Skin::fromPlayerSkin).toArray(Skin[]::new);
        //?} else {
        /*ArrayList<Skin> skins = new ArrayList<>();
        for (var skin : DefaultPlayerSkin.DEFAULT_SKINS) {
            skins.add(new Skin(
                    skin.texture(), null, null,
                    skin.model().equals(DefaultPlayerSkin.ModelType.WIDE)
                            ? Skin.Model.WIDE : Skin.Model.SLIM));
        }
        return skins.toArray(Skin[]::new);
        *///?}
    }

    public static UUID getOfflinePlayerUuid(String name) {
        return UUIDUtil.createOfflinePlayerUUID(name);
    }

    protected static GameProfileCache getProfileCache() {
        if (profileCache != null) {
            return profileCache;
        }

        Minecraft client = Minecraft.getInstance();
        Services services = Services.create(client.authenticationService, client.gameDirectory);
        profileCache = services.profileCache();
        return profileCache;
    }
}
