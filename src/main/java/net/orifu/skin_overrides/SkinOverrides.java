package net.orifu.skin_overrides;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UndashedUuid;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.server.Services;
import net.minecraft.util.Identifier;
import net.minecraft.util.UserCache;

public class SkinOverrides {
	public static final Logger LOGGER = LoggerFactory.getLogger("skin overrides");

	public static final String SKIN_OVERRIDES = "skin_overrides";
	public static final String CAPE_OVERRIDES = "cape_overrides";

	public static final String UUID_REGEX = "^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{12}$";

	private static UserCache userCache;

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

		var userOverride = Overrides.getSkinCopyOverride(profile);
		if (userOverride.isPresent()) {
			String name = userOverride.get();

			// get the uuid
			Optional<UUID> uuid = Optional.empty();
			if (name.matches(UUID_REGEX)) {
				// parse uuid
				try {
					uuid = Optional.of(name.contains("-") ? UUID.fromString(name) : UndashedUuid.fromString(name));
				} catch (IllegalArgumentException e) {
				}
			} else {
				// convert player username to uuid (cached)
				var remoteProfile = getUserCache().findByName(name);
				if (remoteProfile.isPresent()) {
					uuid = Optional.of(remoteProfile.get().getId());
				}
			}

			// if we have a uuid, get the full profile (cached)
			if (uuid.isPresent()) {
				var profileResult = client.getSessionService().fetchProfile(uuid.get(), false);

				// set the skin to the remote one (cached)
				if (profileResult != null) {
					PlayerSkin remoteSkin = client.getSkinProvider().getSkinSupplier(profileResult.profile()).get();
					skin = new PlayerSkin(remoteSkin.texture(), remoteSkin.textureUrl(), skin.capeTexture(),
							skin.elytraTexture(), remoteSkin.model(), false);
				}
			}
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

	private static UserCache getUserCache() {
		if (userCache != null) {
			return userCache;
		}

		MinecraftClient client = MinecraftClient.getInstance();
		Services services = Services.create(client.authService, client.runDirectory);
		userCache = services.userCache();
		return userCache;
	}
}
