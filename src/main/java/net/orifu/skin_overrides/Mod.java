package net.orifu.skin_overrides;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.override.LibraryCapeOverride;
import net.orifu.skin_overrides.override.LibrarySkinOverride;
import net.orifu.skin_overrides.override.LocalCapeOverride;
import net.orifu.skin_overrides.override.LocalSkinOverride;
import net.orifu.skin_overrides.override.Overridden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mod {
	public static final Logger LOGGER = LoggerFactory.getLogger("skin overrides");
	public static final String MOD_ID = "skin_overrides";

	public static final String SKIN_OVERRIDES = "skin_overrides";
	public static final String CAPE_OVERRIDES = "cape_overrides";

	public static final LocalSkinOverride SKINS_LOCAL = LocalSkinOverride.INSTANCE;
	public static final LibrarySkinOverride SKINS_LIBRARY = LibrarySkinOverride.INSTANCE;
	public static final Overridden SKINS = new Overridden.SkinOverrides();
	public static final LocalCapeOverride CAPES_LOCAL = LocalCapeOverride.INSTANCE;
	public static final LibraryCapeOverride CAPES_LIBRARY = LibraryCapeOverride.INSTANCE;
	public static final Overridden CAPES = new Overridden.CapeOverrides();


	public static Identifier id(String path) {
		//? if >=1.21 {
		 return Identifier.of(MOD_ID, path); 
		//?} else
		/*return new Identifier(MOD_ID, path);*/
	}

	public static Skin getSkin(GameProfile profile) {
		return overrideSkin(profile, Skin.fromProfile(profile));
	}

	public static Skin overrideSkin(GameProfile profile, Skin skin) {
		MinecraftClient client = MinecraftClient.getInstance();

		// skin image overrides
		var skinOverride = SKINS_LOCAL.getOverride(profile);
		if (skinOverride.isPresent()) {
			// register skin texture
			var texture = skinOverride.get();
			Identifier skinId = id("skin/" + profile.getId().toString());
			client.getTextureManager().registerTexture(skinId, texture);
			// update skin
			skin = new Skin(skinId, skin.capeTexture(), skin.elytraTexture(), texture.model);
		}

		// skin library overrides
		var skinLibrary = SKINS_LIBRARY.getOverride(profile);
		if (skinLibrary.isPresent()) {
			var newSkin = skinLibrary.get();
			skin = new Skin(newSkin.texture(), skin.capeTexture(),
					skin.elytraTexture(), newSkin.model());
		}

		// cape image overrides
		var capeFile = CAPES_LOCAL.getOverride(profile);
		if (capeFile.isPresent()) {
			// register cape texture
			Identifier capeId = id("cape/" + profile.getId().toString());
			client.getTextureManager().registerTexture(capeId, capeFile.get());
			// update skin
			// note: the elytra texture is a separate part of the record,
			// but updating the cape still updates the elytra.
			skin = new Skin(skin.texture(), capeId, skin.elytraTexture(), skin.model());
		}

		// cape library overrides
		var capeLibrary = CAPES_LIBRARY.getOverride(profile);
		if (capeLibrary.isPresent()) {
			var newCape = capeLibrary.get();
			skin = new Skin(skin.texture(), newCape.texture(),
					skin.elytraTexture(), skin.model());
		}

		return skin;
	}
}
