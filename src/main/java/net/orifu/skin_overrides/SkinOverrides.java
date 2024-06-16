package net.orifu.skin_overrides;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.util.Identifier;

public class SkinOverrides {
	public static final Logger LOGGER = LoggerFactory.getLogger("skin overrides");

	public static final String SKIN_OVERRIDES = "skin_overrides";
	public static final String CAPE_OVERRIDES = "cape_overrides";

	public static PlayerSkin getSkin(GameProfile profile) {
		return overrideSkin(profile, MinecraftClient.getInstance().getSkinProvider().getSkin(profile));
	}

	public static PlayerSkin overrideSkin(GameProfile profile, PlayerSkin skin) {
		MinecraftClient client = MinecraftClient.getInstance();

		var imageOverride = Overrides.getSkinImageOverride(profile);
		if (imageOverride.isPresent()) {
			// register skin texture
			var texture = imageOverride.get();
			Identifier skinId = new Identifier("skin_overrides", "skin/" + profile.getId().toString());
			client.getTextureManager().registerTexture(skinId, texture);
			// update skin
			skin = new PlayerSkin(skinId, null, skin.capeTexture(), skin.elytraTexture(), texture.model, false);
		}

		var profileOverride = Overrides.getSkinCopyOverride(profile);
		if (profileOverride.isPresent()) {
			// set the skin to the remote one (cached)
			PlayerSkin remoteSkin = client.getSkinProvider().getSkinSupplier(profileOverride.get()).get();
			skin = new PlayerSkin(remoteSkin.texture(), remoteSkin.textureUrl(), skin.capeTexture(),
					skin.elytraTexture(), remoteSkin.model(), false);
		}

		var capeFile = Overrides.getCapeImageOverride(profile);
		if (capeFile.isPresent()) {
			// register cape texture
			Identifier capeId = new Identifier("skin_overrides", "cape/" + profile.getId().toString());
			client.getTextureManager().registerTexture(capeId, capeFile.get());
			// update skin
			// note: the elytra texture is a separate part of the record,
			// but updating the cape still updates the elytra.
			skin = new PlayerSkin(skin.texture(), skin.textureUrl(), capeId, skin.elytraTexture(), skin.model(), false);
		}

		return skin;
	}
}
