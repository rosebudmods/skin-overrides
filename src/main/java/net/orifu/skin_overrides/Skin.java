package net.orifu.skin_overrides;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
//? if >=1.20.2
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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

        return new PlayerSkin(
                skinId,
                cape != null ? provider.loadSkin(cape, MinecraftProfileTexture.Type.CAPE) : null,
                elytra != null ? provider.loadSkin(elytra, MinecraftProfileTexture.Type.ELYTRA) : null,
                skin != null ? Model.parse(skin.getMetadata("model")) : Model.parse(DefaultSkinHelper.getModel(profile.getId()))
        );
        *///?} else {
        return fromPlayerSkin(provider.getSkin(profile));
        //?}
    }

    public static CompletableFuture<Skin> fetchProfile(GameProfile profile) {
        var provider = MinecraftClient.getInstance().getSkinProvider();

        return provider.fetchSkin(profile).thenApply(Skin::fromPlayerSkin);
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
        SLIM("slim"),
        WIDE("default");

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
}
