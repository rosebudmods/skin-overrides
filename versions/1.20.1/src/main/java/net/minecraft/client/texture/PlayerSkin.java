package net.minecraft.client.texture;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.DefaultSkinHelper;
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
    public static PlayerSkin fromProfile(GameProfile profile) {
        var provider = MinecraftClient.getInstance().getSkinProvider();
        var textures = provider.getTextures(profile);
        var skin = textures.get(MinecraftProfileTexture.Type.SKIN);
        var cape = textures.get(MinecraftProfileTexture.Type.CAPE);
        var elytra = textures.get(MinecraftProfileTexture.Type.ELYTRA);

        var maybeSkinId = skin != null
                ? provider.loadSkin(skin, MinecraftProfileTexture.Type.SKIN)
                : DefaultSkinHelper.getTexture(profile.getId());

        return new PlayerSkin(
                maybeSkinId,
                null,
                cape != null ? provider.loadSkin(cape, MinecraftProfileTexture.Type.CAPE) : null,
                elytra != null ? provider.loadSkin(elytra, MinecraftProfileTexture.Type.ELYTRA) : null,
                skin != null ? Model.parse(skin.getMetadata("model")) : Model.parse(DefaultSkinHelper.getModel(profile.getId())),
                false
        );
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
