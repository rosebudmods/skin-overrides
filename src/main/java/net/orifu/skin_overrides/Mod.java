package net.orifu.skin_overrides;

import java.util.Optional;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
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

	public static Skin override(GameProfile profile) {
		return override(profile, Skin.fromProfile(profile));
	}

	public static Skin override(GameProfile profile, Skin skin) {
		// override skin
		var ovSkin = overrideSkin(profile);
		if (ovSkin.isPresent())
			skin = skin.withSkin(ovSkin.get().getLeft(), ovSkin.get().getRight());

		// override cape
		var ovCape = overrideCape(profile);
		if (ovCape.isPresent())
			skin = skin.withCape(ovCape.get());

		return skin;
	}

	public static Optional<Pair<Identifier, Skin.Model>> overrideSkin(GameProfile profile) {
		MinecraftClient client = MinecraftClient.getInstance();
		Optional<Pair<Identifier, Skin.Model>> skin = Optional.empty();

		// skin image overrides
		var skinOverride = SKINS_LOCAL.getOverride(profile);
		if (skinOverride.isPresent()) {
			// register skin texture
			var texture = skinOverride.get();
			Identifier skinId = id("skin/" + profile.getId().toString());
			client.getTextureManager().registerTexture(skinId, texture);
			// update skin
			skin = Optional.of(new Pair<>(skinId, texture.model));
		}

		// skin library overrides
		var skinLibrary = SKINS_LIBRARY.getOverride(profile);
		if (skinLibrary.isPresent()) {
			var newSkin = skinLibrary.get();
			skin = Optional.of(new Pair<>(newSkin.texture(), newSkin.model()));
		}

		return skin;
	}

	public static Optional<Identifier> overrideCape(GameProfile profile) {
		MinecraftClient client = MinecraftClient.getInstance();
		Optional<Identifier> cape = Optional.empty();

		// cape image overrides
		var capeFile = CAPES_LOCAL.getOverride(profile);
		if (capeFile.isPresent()) {
			// register cape texture
			Identifier capeId = id("cape/" + profile.getId().toString());
			client.getTextureManager().registerTexture(capeId, capeFile.get());
			// update skin
			// note: the elytra texture is a separate part of the record,
			// but updating the cape still updates the elytra.
			cape = Optional.of(capeId);
		}

		// cape library overrides
		var capeLibrary = CAPES_LIBRARY.getOverride(profile);
		if (capeLibrary.isPresent()) {
			var newCape = capeLibrary.get();
			cape = Optional.of(newCape.texture());
		}

		return cape;
	}
}
