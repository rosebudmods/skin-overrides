package net.orifu.skin_overrides;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.orifu.skin_overrides.library.CapeLibrary;
import net.orifu.skin_overrides.library.SkinLibrary;
//? if hasNetworking
import net.orifu.skin_overrides.networking.ModNetworking;
import net.orifu.skin_overrides.override.LibraryOverrider;
import net.orifu.skin_overrides.override.LocalCapeOverrider;
import net.orifu.skin_overrides.override.LocalSkinOverrider;
import org.jetbrains.annotations.Nullable;
//? if >=1.17.1 {
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//?} else {
/*import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
*///?}

import java.util.Locale;
import java.util.Optional;

public class Mod {
	public static final Logger LOGGER = /*? if >=1.17.1 {*/ LoggerFactory /*?} else >>*/ /*LogManager*/
			.getLogger("skin overrides");
	public static final String MOD_ID = "skin_overrides";
	public static final String MOD_VERSION = /*$ modVersion*/ "2.2.0-beta";

	public static final String SKIN_OVERRIDES_PATH = "skin_overrides";
	public static final String CAPE_OVERRIDES_PATH = "cape_overrides";

	public static final OverrideManager SKINS = new OverrideManager(true, SKIN_OVERRIDES_PATH, SkinLibrary.INSTANCE,
			new LocalSkinOverrider());
	public static final OverrideManager CAPES = new OverrideManager(false, CAPE_OVERRIDES_PATH, CapeLibrary.INSTANCE,
			new LocalCapeOverrider());

	public static Identifier id(String namespace, String path) {
		//? if >=1.21 {
		 return Identifier.of(namespace, path.toLowerCase(Locale.ROOT));
		//?} else
		/*return new Identifier(namespace, path.toLowerCase(Locale.ROOT));*/
	}

	public static Identifier id(String path) {
		return id(MOD_ID, path);
	}

	public static Identifier defaultId(String path) {
		return id("minecraft", path);
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
		return SKINS.get(profile).map(ov -> new Pair<>(ov.texture(), ov.model()));
	}

	public static Optional<Identifier> overrideCape(GameProfile profile) {
		return CAPES.get(profile).map(OverrideManager.Override::texture);
	}

	public static Optional<Skin.Signature> overrideSignature(OverrideManager.Override override) {
		if (override instanceof LibraryOverrider.LibraryOverride library
				&& library.entry() instanceof SkinLibrary.SkinEntry skinEntry
				&& skinEntry.signature != null) {
			return Optional.of(skinEntry.signature);
		}
		return Optional.empty();
	}

	public static void onUserOverrideUpdate(
			@Nullable OverrideManager.Override oldOverride,
			@Nullable OverrideManager.Override newOverride) {
		var newSignature = overrideSignature(newOverride);
		if (newSignature.isPresent()) {
			// switched to a signed library override
			//? if hasNetworking
			ModNetworking.updateSkinOnServer(newSignature.get().value(), newSignature.get().signature());
		} else if (overrideSignature(oldOverride).isPresent()) {
			// remove signed library override
			//? if hasNetworking
			ModNetworking.clearSkinOverrideOnServer();
		}
	}
}
