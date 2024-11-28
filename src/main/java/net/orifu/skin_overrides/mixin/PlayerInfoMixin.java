package net.orifu.skin_overrides.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
//? if >=1.20.2
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

//? if <1.20.2
/*import net.minecraft.util.Tuple;*/

@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {
    @Shadow
    @Final
    private GameProfile profile;

    //? if >=1.20.2 {

    @ModifyReturnValue(method = "getSkin", at = @At("RETURN"))
    private PlayerSkin getSkin(PlayerSkin skin) {
        // the return value of this method is otherwise memoised
        return Mod.override(this.profile, Skin.fromPlayerSkin(skin)).toPlayerSkin();
    }

    //?} else {

    /*@ModifyReturnValue(method = "getSkinLocation", at = @At("RETURN"))
    private ResourceLocation getSkinTexture(ResourceLocation texture) {
        return Mod.overrideSkin(profile).map(Tuple::getA).orElse(texture);
    }

    @ModifyReturnValue(method = "getModelName", at = @At("RETURN"))
    private String getModel(String model) {
        return Mod.overrideSkin(profile).map(p -> p.getB().id()).orElse(model);
    }

    @ModifyReturnValue(method = "getCapeLocation", at = @At("RETURN"))
    private ResourceLocation getCapeTexture(ResourceLocation texture) {
        return Mod.overrideCapeOrDefault(profile);
    }

    *///?}
}
