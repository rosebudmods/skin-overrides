package net.orifu.skin_overrides;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.texture.PlayerSkin;

public class SkinOverrides {
	public static final Logger LOGGER = LoggerFactory.getLogger("skin overrides");

	public static final String SKIN_OVERRIDES = "skin_overrides";
	public static final String CAPE_OVERRIDES = "cape_overrides";

	public static Optional<OverriddenSkinTexture> skinTextureFor(GameProfile profile) {
		return getTextureFor(SKIN_OVERRIDES, profile)
				.map(file -> new OverriddenSkinTexture(file, PlayerSkin.Model.WIDE))
				.or(() -> getTextureFor(SKIN_OVERRIDES, profile, "wide")
						.map(file -> new OverriddenSkinTexture(file, PlayerSkin.Model.WIDE)))
				.or(() -> getTextureFor(SKIN_OVERRIDES, profile, "slim")
						.map(file -> new OverriddenSkinTexture(file, PlayerSkin.Model.SLIM)));
	}

	public static Optional<OverriddenHttpTexture> capeTextureFor(GameProfile profile) {
		return getTextureFor(CAPE_OVERRIDES, profile).map(file -> new OverriddenHttpTexture(file));
	}

	public static Optional<File> getTextureFor(String path, GameProfile profile, @Nullable String suffix) {
		String suff = (suffix == null ? "" : "." + suffix) + ".png";

		// username
		File file = Paths.get(path, profile.getName() + suff).toFile();
		if (file.exists())
			return Optional.of(file);

		// uuid with hyphens
		String uuid = profile.getId().toString();
		file = Paths.get(path, uuid + suff).toFile();
		if (file.exists())
			return Optional.of(file);

		// uuid without hyphens
		file = Paths.get(path, uuid.replace("-", "") + suff).toFile();
		if (file.exists())
			return Optional.of(file);

		return Optional.empty();
	}

	public static Optional<File> getTextureFor(String path, GameProfile profile) {
		return getTextureFor(path, profile, null);
	}
}
