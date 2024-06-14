package net.orifu.skin_overrides;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.texture.PlayerSkin;
import net.orifu.skin_overrides.texture.LocalHttpTexture;
import net.orifu.skin_overrides.texture.LocalSkinTexture;

public class SkinOverrides {
	public static final Logger LOGGER = LoggerFactory.getLogger("skin overrides");

	public static final String SKIN_OVERRIDES = "skin_overrides";
	public static final String CAPE_OVERRIDES = "cape_overrides";

	public static final String UUID_REGEX = "^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{4}-?[0-9a-f]{12}$";

	public static Optional<LocalSkinTexture> skinTextureFor(GameProfile profile) {
		return getTextureFor(SKIN_OVERRIDES, profile)
				.or(() -> getTextureFor(SKIN_OVERRIDES, profile, "wide"))
				.map(file -> new LocalSkinTexture(file, PlayerSkin.Model.WIDE))
				.or(() -> getTextureFor(SKIN_OVERRIDES, profile, "slim")
						.map(file -> new LocalSkinTexture(file, PlayerSkin.Model.SLIM)));
	}

	public static Optional<String> playerIdFor(GameProfile profile) {
		return getTextureFor(SKIN_OVERRIDES, profile, null, "txt").flatMap(file -> {
			try {
				return Optional.of(Files.readString(file.toPath()).trim());
			} catch (IOException e) {
				return Optional.empty();
			}
		}).flatMap(content -> content.length() == 0 ? Optional.empty() : Optional.of(content));
	}

	public static Optional<LocalHttpTexture> capeTextureFor(GameProfile profile) {
		return getTextureFor(CAPE_OVERRIDES, profile).map(file -> new LocalHttpTexture(file));
	}

	public static Optional<File> getTextureFor(String path, GameProfile profile, @Nullable String suffix,
			@Nullable String ext) {
		String suff = (suffix == null ? "" : "." + suffix) + "." + (ext == null ? "png" : ext);

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

	public static Optional<File> getTextureFor(String path, GameProfile profile, @Nullable String suffix) {
		return getTextureFor(path, profile, suffix, null);
	}

	public static Optional<File> getTextureFor(String path, GameProfile profile) {
		return getTextureFor(path, profile, null);
	}
}
