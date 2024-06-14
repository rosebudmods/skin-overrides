package net.orifu.skin_overrides.mixin;

import net.minecraft.client.gui.screen.TitleScreen;
import net.orifu.skin_overrides.SkinOverrides;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	@Inject(method = "init", at = @At("TAIL"))
	public void onInit(CallbackInfo ci) {
		SkinOverrides.LOGGER.info("example mixin");
	}
}
