package net.orifu.skin_overrides;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.MinecraftClient;
//? if >=1.20.2
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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

    public enum Model {
        WIDE("wide"),
        SLIM("slim");

        private final String key;

        Model(String key) {
            this.key = key;
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
