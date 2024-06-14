package net.orifu.skin_overrides;

import java.io.File;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.fabricmc.api.ModInitializer;

@SuppressWarnings("deprecation")
public class SkinOverrides implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("skin overrides");

	@Override
	public void onInitialize() {
	}

	public static Optional<OverridenPlayerSkinTexture> skinTextureFor(GameProfile profile) {
		File skinFile = new File("skin_overrides/" + profile.getName() + ".png");

		return skinFile.exists()
				? Optional.of(new OverridenPlayerSkinTexture(skinFile))
				: Optional.empty();
	}
}
