package net.minecraft.client.texture;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record PlayerSkin(
        Identifier texture,
        @Nullable String textureUrl,
        @Nullable Identifier capeTexture,
        @Nullable Identifier elytraTexture,
        Model model,
        boolean secure
) {
    public static PlayerSkin fromTextures(Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures) {
        var provider = MinecraftClient.getInstance().getSkinProvider();
        var skin = textures.get(MinecraftProfileTexture.Type.SKIN);
        var cape = textures.get(MinecraftProfileTexture.Type.CAPE);
        var elytra = textures.get(MinecraftProfileTexture.Type.ELYTRA);
        return new PlayerSkin(
                provider.loadSkin(skin, MinecraftProfileTexture.Type.SKIN),
                skin.getUrl(),
                provider.loadSkin(cape, MinecraftProfileTexture.Type.CAPE),
                provider.loadSkin(elytra, MinecraftProfileTexture.Type.ELYTRA),
                Model.parse(skin.getMetadata("model")),
                false
        );
    }

    public static PlayerSkin fromProfile(GameProfile profile) {
        return fromTextures(MinecraftClient.getInstance().getSkinProvider().getTextures(profile));
    }

    public enum Model {
        SLIM("slim"),
        WIDE("default");

        private final String key;

        Model(String key) {
            this.key = key;
        }

        public static Model parse(@Nullable String key) {
            if (key != null && key.equals("slim")) {
                return Model.SLIM;
            } else {
                return Model.WIDE;
            }
        }
    }
}
