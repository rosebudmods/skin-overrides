package net.orifu.skin_overrides.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
//? if >=1.20.2
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {
    @Shadow
    @Final
    private GameProfile profile;

    //? if >=1.20.2 {

    @ModifyReturnValue(method = "getSkin", at = @At("RETURN"))
    private PlayerSkin getSkin(PlayerSkin skin) {
        // the return value of this method is otherwise memoised
        return Mod.override(this.profile, Skin.fromPlayerSkin(skin).withDefaultCape(this.profile)).toPlayerSkin();
    }
    
    //?} else {
    
    /*@ModifyReturnValue(method = "getSkinTexture", at = @At("RETURN"))
    private Identifier getSkinTexture(Identifier texture) {
        return Mod.overrideSkin(profile).map(Pair::getLeft).orElse(texture);
    }

    @ModifyReturnValue(method = "getModel", at = @At("RETURN"))
    private String getModel(String model) {
        return Mod.overrideSkin(profile).map(p -> p.getRight().id()).orElse(model);
    }

    @ModifyReturnValue(method = "getCapeTexture", at = @At("RETURN"))
    private Identifier getCapeTexture(Identifier texture) {
        return Mod.overrideCape(profile).orElse(texture);
    }

    *///?}
}
