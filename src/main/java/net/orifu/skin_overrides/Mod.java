package net.orifu.skin_overrides;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.orifu.skin_overrides.library.CapeLibrary;
import net.orifu.skin_overrides.library.SkinLibrary;
import net.orifu.skin_overrides.override.LocalCapeOverrider;
import net.orifu.skin_overrides.override.LocalSkinOverrider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//? if hasUi {
import com.mojang.blaze3d.platform.InputUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBind;
import net.orifu.skin_overrides.screen.SkinOverridesScreen;
import org.lwjgl.glfw.GLFW;
//?}

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class Mod implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("skin overrides");
	public static final String MOD_ID = "skin_overrides";

	public static final String SKIN_OVERRIDES_PATH = "skin_overrides";
	public static final String CAPE_OVERRIDES_PATH = "cape_overrides";

	public static final OverrideManager SKINS = new OverrideManager(true, SKIN_OVERRIDES_PATH, SkinLibrary.INSTANCE,
			new LocalSkinOverrider());
	public static final OverrideManager CAPES = new OverrideManager(false, CAPE_OVERRIDES_PATH, CapeLibrary.INSTANCE,
			new LocalCapeOverrider());

	@Override
	public void onInitializeClient() {
		var scheduler = Executors.newScheduledThreadPool(1);
		// reload override files every 500 ms
		scheduler.scheduleAtFixedRate(SKINS::update, 0, 500, TimeUnit.MILLISECONDS);
		scheduler.scheduleAtFixedRate(CAPES::update, 0, 500, TimeUnit.MILLISECONDS);
		// reload library files every 2 seconds
		scheduler.scheduleAtFixedRate(SKINS.library()::reload, 0, 2, TimeUnit.SECONDS);
		scheduler.scheduleAtFixedRate(CAPES.library()::reload, 0, 2, TimeUnit.SECONDS);

		//? if hasUi {
		KeyBind binding = KeyBindingHelper.registerKeyBinding(new KeyBind(
				"key.skin_overrides.open_screen",
				InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O,
				"key.categories.misc"));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (binding.wasPressed()) {
				client.setScreen(new SkinOverridesScreen(client.currentScreen));
			}
		});
		//?}
	}

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
		return SKINS.get(profile).map(ov -> new Pair<>(ov.texture(), ov.model()));
	}

	public static Optional<Identifier> overrideCape(GameProfile profile) {
		return CAPES.get(profile).map(OverrideManager.Override::texture);
	}
}
