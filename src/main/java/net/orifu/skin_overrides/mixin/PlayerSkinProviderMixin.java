package net.orifu.skin_overrides.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.orifu.skin_overrides.SkinOverrides;

@Mixin(PlayerSkinProvider.class)
public class PlayerSkinProviderMixin {
    @ModifyReturnValue(method = "getSkin", at = @At("RETURN"))
    private PlayerSkin getSkin(PlayerSkin skin, GameProfile profile) {
        return SkinOverrides.overrideSkin(profile, skin);
    }
}
