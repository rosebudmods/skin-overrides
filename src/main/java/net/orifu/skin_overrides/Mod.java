package net.orifu.skin_overrides;

import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.orifu.skin_overrides.library.CapeLibrary;
import net.orifu.skin_overrides.library.SkinLibrary;
import net.orifu.skin_overrides.networking.ModNetworking;
import net.orifu.skin_overrides.override.LocalCapeOverrider;
import net.orifu.skin_overrides.override.LocalSkinOverrider;
import net.orifu.skin_overrides.override.SkinChangeOverride;
import net.orifu.skin_overrides.override.SkinCopyOverrider;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

public class Mod {
	public static final Logger LOGGER = LoggerFactory.getLogger("skin overrides");
	public static final String MOD_ID = "skin_overrides";
	public static final String MOD_VERSION = /*$ modVersion*/ "2.4.0-beta.1";

	public static final String SKIN_OVERRIDES_PATH = "skin_overrides";
	public static final String CAPE_OVERRIDES_PATH = "cape_overrides";

	public static final OverrideManager SKINS = new OverrideManager(true, SKIN_OVERRIDES_PATH, SkinLibrary.INSTANCE,
			new LocalSkinOverrider(), new SkinCopyOverrider());
	public static final OverrideManager CAPES = new OverrideManager(false, CAPE_OVERRIDES_PATH, CapeLibrary.INSTANCE,
			new LocalCapeOverrider());

	public static ResourceLocation res(String namespace, String path) {
		//? if >=1.21 {
		 return ResourceLocation.fromNamespaceAndPath(namespace, path);
		//?} else
		/*return new ResourceLocation(namespace, path);*/
	}

	public static ResourceLocation res(String path) {
		return res(MOD_ID, path);
	}

	public static ResourceLocation defaultId(String path) {
		return res("minecraft", path);
	}

	public static Skin override(GameProfile profile) {
		return override(profile, Skin.fromProfile(profile));
	}

	public static Skin override(GameProfile profile, Skin skin) {
		// override skin
		var ovSkin = overrideSkin(profile);
		if (ovSkin.isPresent())
			skin = skin.withSkin(ovSkin.get().getA(), ovSkin.get().getB());

		// override cape
		skin = skin.withCape(overrideCapeOrDefault(profile));

		return skin;
	}

	public static Optional<Tuple<ResourceLocation, Skin.Model>> overrideSkin(GameProfile profile) {
		return SKINS.get(profile)
				.filter(ov -> ov.texture() != null)
				.map(ov -> new Tuple<>(ov.texture(), ov.model()))
				.or(() -> SkinChangeOverride.texture(profile));
	}

	public static Optional<ResourceLocation> overrideCape(GameProfile profile) {
		return CAPES.get(profile).map(OverrideManager.Override::texture);
	}

	public static ResourceLocation overrideCapeOrDefault(GameProfile profile) {
		return overrideCape(profile).orElseGet(() -> Skin.fromProfile(profile).withDefaultCape(profile).capeTexture());
	}

	public static void onUserOverrideUpdate(
			@Nullable OverrideManager.Override oldOverride,
			@Nullable OverrideManager.Override newOverride) {
		if (newOverride != null) {
			// switched to a possibly signed override
			ModNetworking.updateSkinOnServer(newOverride);
		} else if (oldOverride != null) {
			// may have just removed signed override.
			// clear skin just in case!
			ModNetworking.clearSkinOverrideOnServer();
		}
	}
}
