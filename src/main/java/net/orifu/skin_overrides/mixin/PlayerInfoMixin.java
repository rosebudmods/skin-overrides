package net.orifu.skin_overrides.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.orifu.skin_overrides.Mod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

//? if <1.20.6 >=1.20.2 {
/*import com.llamalad7.mixinextras.sugar.Local;
import net.orifu.skin_overrides.networking.ModNetworking;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Supplier;
*///?}

//? if >=1.20.2 {
import net.minecraft.client.resources.PlayerSkin;
import net.orifu.skin_overrides.Skin;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
*///?}

@Mixin(PlayerInfo.class)
public abstract class PlayerInfoMixin {
    @Shadow
    @Final
    private GameProfile profile;

    //? if <1.20.6 >=1.20.2 {
    /*@Inject(method = "<init>", at = @At("RETURN"))
    private void loadProfile(GameProfile profile, boolean b, CallbackInfo ci, @Local Supplier<Supplier<PlayerSkin>> supplier) {
        supplier.get();
        ModNetworking.onProfileLoad();
    }
    *///?}

    //? if >=1.20.2 {

    @ModifyReturnValue(method = "getSkin", at = @At("RETURN"))
    private PlayerSkin getSkin(PlayerSkin skin) {
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
