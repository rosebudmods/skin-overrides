package net.orifu.skin_overrides;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

public class SkinOverrides {
	public static final Logger LOGGER = LoggerFactory.getLogger("skin overrides");

	public static Optional<OverridenPlayerSkinTexture> skinTextureFor(GameProfile profile) {
		var skinFile = getTextureFor("skin_overrides", profile);
		return skinFile.map(sf -> new OverridenPlayerSkinTexture(sf));
	}

	public static Optional<File> getTextureFor(String path, GameProfile profile) {
		// username
		File file = Paths.get(path, profile.getName() + ".png").toFile();
		if (file.exists())
			return Optional.of(file);

		// uuid with hyphens
		String uuid = profile.getId().toString();
		file = Paths.get(path, uuid + ".png").toFile();
		if (file.exists())
			return Optional.of(file);

		// uuid without hyphens
		file = Paths.get(path, uuid.replace("-", "") + ".png").toFile();
		if (file.exists())
			return Optional.of(file);

		return Optional.empty();
	}
}
