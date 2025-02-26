package net.orifu.skin_overrides;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.util.ProfileHelper;
import org.jetbrains.annotations.Nullable;

//? if >=1.20.2 {
import net.minecraft.client.resources.PlayerSkin;
//?} else {
/*import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
*///?}

//? if hasUi {
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.yggdrasil.YggdrasilUserApiService;
import net.minecraft.util.Tuple;
import net.orifu.skin_overrides.mixin.YggdrasilServiceClientAccessor;
import net.orifu.skin_overrides.mixin.YggdrasilUserApiServiceAccessor;
import net.orifu.skin_overrides.util.Util;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
//?}

//? if hasNetworking
import net.orifu.skin_overrides.networking.ModNetworking;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record Skin(
        ResourceLocation texture,
        @Nullable ResourceLocation capeTexture,
        @Nullable ResourceLocation elytraTexture,
        Model model
) {
    public static final Skin DEFAULT_SKIN = new Skin();

    public Skin() {
        this(ProfileHelper.getDefaultSkin(), null, null, Model.WIDE);
    }

    public static Skin fromProfile(GameProfile profile) {
        var manager = Minecraft.getInstance().getSkinManager();

        //? if >=1.21.4 {
        return manager.getOrLoad(profile).thenApply(sk -> sk.map(Skin::fromPlayerSkin).orElse(DEFAULT_SKIN)).getNow(DEFAULT_SKIN);
        //?} else if >=1.20.2 {
        /*return manager.getOrLoad(profile).thenApply(Skin::fromPlayerSkin).getNow(DEFAULT_SKIN);
        *///?} else {
        /*var textures = manager.getInsecureSkinInformation(profile);
        var skin = textures.get(MinecraftProfileTexture.Type.SKIN);
        var cape = textures.get(MinecraftProfileTexture.Type.CAPE);
        var elytra = textures.get(MinecraftProfileTexture.Type.ELYTRA);

        var skinId = skin != null
                ? manager.registerTexture(skin, MinecraftProfileTexture.Type.SKIN)
                : DefaultPlayerSkin.getDefaultSkin(profile.getId());

        return new Skin(
                skinId,
                cape != null ? manager.registerTexture(cape, MinecraftProfileTexture.Type.CAPE) : null,
                elytra != null ? manager.registerTexture(elytra, MinecraftProfileTexture.Type.ELYTRA) : null,
                skin != null ? Model.parse(skin.getMetadata("model")) : Model.parse(DefaultPlayerSkin.getSkinModelName(profile.getId()))
        );
        *///?}
    }

    public static CompletableFuture<Skin> fetchSkin(GameProfile profile) {
        var manager = Minecraft.getInstance().getSkinManager();

        //? if >=1.20.2 {
        return manager.getOrLoad(profile).thenApply(s -> Skin.fromPlayerSkin(s, profile));
        //?} else {
        /*CompletableFuture<Skin> future = new CompletableFuture<>();
        manager.registerSkins(profile, (ty, id, tx) -> {
            if (ty.equals(MinecraftProfileTexture.Type.SKIN)) {
                future.complete(new Skin(id, null, null, Model.parse(tx.getMetadata("model"))));
            }
        }, false);
        return future;
        *///?}
    }

    public static CompletableFuture<Skin> fetchCape(GameProfile profile) {
        //? if >=1.20.2 {
        return Skin.fetchSkin(profile);
        //?} else {
        /*var manager = Minecraft.getInstance().getSkinManager();
        CompletableFuture<Skin> future = new CompletableFuture<>();
        manager.registerSkins(profile, (ty, id, tx) -> {
            if (ty.equals(MinecraftProfileTexture.Type.CAPE)) {
                future.complete(new Skin(null, id, null, null));
            }
        }, false);
        return future;
        *///?}
    }

    public Skin withSkin(ResourceLocation skin, Model model) {
        return new Skin(skin, this.capeTexture, this.elytraTexture, model);
    }

    public Skin withCape(@Nullable ResourceLocation cape) {
        return new Skin(this.texture, cape, null, this.model);
    }

    public Skin withDefaultCape(GameProfile profile) {
        //? if hasNetworking {
        if (profile.getProperties().containsKey(ModNetworking.DEFAULT_TEXTURES_KEY)) {
            // get the default textures
            var manager = Minecraft.getInstance().getSkinManager();
            var property = profile.getProperties().get(ModNetworking.DEFAULT_TEXTURES_KEY).stream().findFirst().orElseThrow();

            //? if >=1.20.2 {
            // get the skin from the default textures
            //? if >=1.20.4 {
            var skinFuture = manager.skinCache.getUnchecked(new SkinManager.CacheKey(profile.getId(), property));
            //?} else
            /*var skinFuture = manager.skinCache.getUnchecked(new SkinManager.CacheKey(profile));*/

            // if we have the default skin, use its cape. otherwise, use what we currently have.
            return Optional.ofNullable(skinFuture.getNow(null)).map(s -> Skin.fromPlayerSkin(s, profile))
                    .map(sk -> this.withCape(sk.capeTexture())).orElse(this);
            //?} else {
            /*// get the skin from the default textures
            var textures = manager.insecureSkinCache.getUnchecked(property.getValue());

            // register its cape and use it
            var capeTexture = textures.get(MinecraftProfileTexture.Type.CAPE);
            var location = capeTexture != null
                    ? manager.registerTexture(capeTexture, MinecraftProfileTexture.Type.CAPE)
                    : null;
            return this.withCape(location);
            *///?}
        }
        //?}

        return this;
    }

    //? if >=1.20.2 {
    public static Skin fromPlayerSkin(PlayerSkin skin) {
        return new Skin(skin.texture(), skin.capeTexture(), skin.elytraTexture(), Model.from(skin.model()));
    }

    public static Skin fromPlayerSkin(PlayerSkin skin, GameProfile profile) {
        return Skin.fromPlayerSkin(skin);
    }

    public static Skin fromPlayerSkin(Optional<PlayerSkin> skin, GameProfile profile) {
        return Skin.fromPlayerSkin(skin.orElseGet(() -> DefaultPlayerSkin.get(profile)), profile);
    }

    public PlayerSkin toPlayerSkin() {
        return new PlayerSkin(
                this.texture, null,
                this.capeTexture, this.elytraTexture,
                this.model.toPlayerSkinModel(), false);
    }
    //?}

    //? if hasUi {
    public Optional<Tuple<String, Model>> setUserSkin() {
        try {
            var userApiService = (YggdrasilUserApiService) Minecraft.getInstance().userApiService;
            var userApiServiceAccessor = (YggdrasilUserApiServiceAccessor) userApiService;
            var serviceClient = userApiServiceAccessor.getMinecraftClient();
            var serviceClientAccessor = (YggdrasilServiceClientAccessor) serviceClient;
            var servicesHost = /*? if >=1.20.2 {*/ userApiServiceAccessor.getEnvironment().servicesHost();
                /*?} else*/ /*userApiServiceAccessor.getEnvironment().getServicesHost();*/
            var url = servicesHost + "/minecraft/profile/skins";

            // TODO: do not use saveTexture
            File skin = File.createTempFile("skin-overrides_", "_temp-skin");
            Util.saveTexture(this.texture, 64, 64, skin.toPath());

            var post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + serviceClientAccessor.getAccessToken());
            post.setEntity(MultipartEntityBuilder.create()
                    .addTextBody("variant", this.model.apiName)
                    .addBinaryBody("file", skin, ContentType.IMAGE_PNG, "skin.png")
                    .build());

            var client = HttpClients.createDefault();
            var response = client.execute(post);
            String body = EntityUtils.toString(response.getEntity(), "utf-8");
            client.close();

            if (response.getStatusLine().getStatusCode() / 100 != 2) {
                Mod.LOGGER.error("failed to set skin, got API response:\n" + body);
                return Optional.empty();
            }

            var jsonBody = new Gson().fromJson(body, JsonObject.class);
            var skinInfo = jsonBody.getAsJsonArray("skins").get(0).getAsJsonObject();
            String textureUrl = skinInfo.get("url").getAsString();
            Model model = Model.parse(skinInfo.get("variant").getAsString());

            return Optional.of(new Tuple<>(textureUrl, model));
        } catch (IOException e) {
            Mod.LOGGER.error("failed to set skin, got IO error:", e);
            return Optional.empty();
        }
    }
    //?}

    public enum Model {
        WIDE("wide", "classic"),
        SLIM("slim");

        private final String key;
        private final String apiName;

        Model(String key) {
            this(key, key);
        }

        Model(String key, String apiName) {
            this.key = key;
            this.apiName = apiName;
        }

        public static Model parse(@Nullable String key) {
            var model = tryParse(key);
            return model == null ? Model.WIDE : model;
        }

        @Nullable
        public static Model tryParse(@Nullable String key) {
            if (key == null) {
                return null;
            }

            key = key.toLowerCase(Locale.ROOT);
            if (key.equals(Model.WIDE.key) || key.equals(Model.WIDE.apiName)) {
                return Model.WIDE;
            } else if (key.equals(Model.SLIM.key) || key.equals(Model.SLIM.apiName)) {
                return Model.SLIM;
            }
            return null;
        }

        public String id() {
            return this.key;
        }

        //? if >=1.20.2 {
        public static Model from(PlayerSkin.Model model) {
            return switch (model) {
                case WIDE -> WIDE;
                case SLIM -> SLIM;
            };
        }

        public PlayerSkin.Model toPlayerSkinModel() {
            return switch (this) {
                case WIDE -> PlayerSkin.Model.WIDE;
                case SLIM -> PlayerSkin.Model.SLIM;
            };
        }
        //?}
    }

    public record Signature(String value, String signature) {
        public static Optional<Signature> fromProperty(com.mojang.authlib.properties.Property property) {
            //? if >=1.20.2 {
            return property.signature() != null ? Optional.of(new Signature(property.value(), property.signature())) : Optional.empty();
            //?} else
            /*return property.getSignature() != null ? Optional.of(new Signature(property.getValue(), property.getSignature())) : Optional.empty();*/
        }

        public interface Provider {
            Optional<Signature> signature();
        }
    }
}
