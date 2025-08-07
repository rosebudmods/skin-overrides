package net.orifu.skin_overrides;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.networking.ModNetworking;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.util.TextureHelper;
import org.jetbrains.annotations.Nullable;

//? if >=1.20.2 {
import net.minecraft.client.resources.PlayerSkin;
//?} else {
/*import net.minecraft.client.resources.DefaultPlayerSkin;
*///?}

//? if <=1.20.2 {
/*import com.mojang.authlib.minecraft.MinecraftProfileTexture;
*///?}

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

    public static CompletableFuture<Optional<DownloadResult>> download(GameProfile profile, boolean skin) {
        return CompletableFuture.supplyAsync(() -> {
            //? if >=1.20.4 {
            var sessionService = Minecraft.getInstance().getMinecraftSessionService();
            var property = sessionService.getPackedTextures(profile);

            if (property == null) return Optional.empty();

            var textures = sessionService.unpackTextures(property);
            var mcTexture = skin ? textures.skin() : textures.cape();
            //?} else {
            /*var textures = Minecraft.getInstance().getMinecraftSessionService().getTextures(profile, false);
            var mcTexture = textures.get(MinecraftProfileTexture.Type.SKIN);
            *///?}

            if (mcTexture == null) return Optional.empty();

            var image = TextureHelper.skin().url(mcTexture.getUrl()).image();
            var model = Model.parse(mcTexture.getMetadata("model"));
            return image.map(img -> new DownloadResult(img, model));
        });
    }

    public record DownloadResult(NativeImage image, Model model) {}

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

    public CompletableFuture<Optional<String>> setUserSkin() {
        return SkinNetworking.setUserSkin(this);
    }

    public enum Model {
        WIDE("wide", "classic"),
        SLIM("slim");

        private final String key;
        public final String apiName;

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
            CompletableFuture<Optional<Signature>> signature();
        }
    }
}
