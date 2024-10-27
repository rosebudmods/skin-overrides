package net.orifu.skin_overrides;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.yggdrasil.YggdrasilUserApiService;
import net.minecraft.client.MinecraftClient;
//? if >=1.20.2
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.mixin.YggdrasilServiceClientAccessor;
import net.orifu.skin_overrides.mixin.YggdrasilUserApiServiceAccessor;
import net.orifu.skin_overrides.util.Util;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record Skin(
        Identifier texture,
        @Nullable Identifier capeTexture,
        @Nullable Identifier elytraTexture,
        Model model
) {
    public static Skin fromProfile(GameProfile profile) {
        var provider = MinecraftClient.getInstance().getSkinProvider();

        //? if <1.20.2 {
        /*var textures = provider.getTextures(profile);
        var skin = textures.get(MinecraftProfileTexture.Type.SKIN);
        var cape = textures.get(MinecraftProfileTexture.Type.CAPE);
        var elytra = textures.get(MinecraftProfileTexture.Type.ELYTRA);

        var skinId = skin != null
                ? provider.loadSkin(skin, MinecraftProfileTexture.Type.SKIN)
                : DefaultSkinHelper.getTexture(profile.getId());

        return new Skin(
                skinId,
                cape != null ? provider.loadSkin(cape, MinecraftProfileTexture.Type.CAPE) : null,
                elytra != null ? provider.loadSkin(elytra, MinecraftProfileTexture.Type.ELYTRA) : null,
                skin != null ? Model.parse(skin.getMetadata("model")) : Model.parse(DefaultSkinHelper.getModel(profile.getId()))
        );
        *///?} else
        return fromPlayerSkin(provider.getSkin(profile));
    }

    public static CompletableFuture<Skin> fetchSkin(GameProfile profile) {
        var provider = MinecraftClient.getInstance().getSkinProvider();

        //? if <1.20.2 {
        /*CompletableFuture<Skin> future = new CompletableFuture<>();
        provider.loadSkin(profile, (ty, id, tx) -> {
            if (ty.equals(MinecraftProfileTexture.Type.SKIN)) {
                future.complete(new Skin(id, null, null, Model.parse(tx.getMetadata("model"))));
            }
        }, false);
        return future;
        *///?} else
        return provider.fetchSkin(profile).thenApply(Skin::fromPlayerSkin);
    }

    public static CompletableFuture<Skin> fetchCape(GameProfile profile) {
        var provider = MinecraftClient.getInstance().getSkinProvider();

        //? if <1.20.2 {
        /*CompletableFuture<Skin> future = new CompletableFuture<>();
        provider.loadSkin(profile, (ty, id, tx) -> {
            if (ty.equals(MinecraftProfileTexture.Type.CAPE)) {
                future.complete(new Skin(null, id, null, null));
            }
        }, false);
        return future;
        *///?} else
        return provider.fetchSkin(profile).thenApply(Skin::fromPlayerSkin);
    }

    public Skin withSkin(Identifier skin, Model model) {
        return new Skin(skin, this.capeTexture, this.elytraTexture, model);
    }

    public Skin withCape(@Nullable Identifier cape) {
        return new Skin(this.texture, cape, null, this.model);
    }

    //? if >=1.20.2 {
    public static Skin fromPlayerSkin(PlayerSkin skin) {
        return new Skin(skin.texture(), skin.capeTexture(), skin.elytraTexture(), Model.from(skin.model()));
    }

    public PlayerSkin toPlayerSkin() {
        return new PlayerSkin(
                this.texture, null,
                this.capeTexture, this.elytraTexture,
                this.model.toPlayerSkinModel(), false);
    }
    //?}

    public boolean setUserSkin() {
        try {
            var userApiService = (YggdrasilUserApiService) MinecraftClient.getInstance().userApiService;
            var userApiServiceAccessor = (YggdrasilUserApiServiceAccessor) userApiService;
            var serviceClient = userApiServiceAccessor.getMinecraftClient();
            var serviceClientAccessor = (YggdrasilServiceClientAccessor) serviceClient;
            var url = userApiServiceAccessor.getEnvironment().servicesHost() + "/minecraft/profile/skins";

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
                return false;
            }

            System.out.println(body);
        } catch (IOException e) {
            System.out.println("something went wrong");
            return false;
        }

        return true;
    }

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
            } else if (key.equals(Model.WIDE.key)) {
                return Model.WIDE;
            } else if (key.equals(Model.SLIM.key)) {
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
        public static Signature fromProperty(com.mojang.authlib.properties.Property property) {
            //? if >=1.20.2 {
            return new Signature(property.value(), property.signature());
            //?} else
            /*return new Signature(property.getValue(), property.getSignature());*/
        }

        public interface Provider {
            Optional<Signature> signature();
        }
    }
}
