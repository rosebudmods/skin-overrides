package net.orifu.skin_overrides;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

@SuppressWarnings("deprecation")
public class SkinOverrides implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("skin overrides");

	@Override
	public void onInitialize() {
		LOGGER.info("hello, world!");
	}
}
