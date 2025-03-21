package net.orifu.skin_overrides.mixin;

//? if >=1.20.2 {
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkinManager.class)
public class SkinManagerMixin {
    @ModifyReturnValue(method = "getInsecureSkin", at = @At("RETURN"))
    private PlayerSkin getSkin(PlayerSkin skin, GameProfile profile) {
        return Mod.override(profile, Skin.fromPlayerSkin(skin).withDefaultCape(profile)).toPlayerSkin();
    }
}
//?} else
/*public class SkinManagerMixin {}*/
