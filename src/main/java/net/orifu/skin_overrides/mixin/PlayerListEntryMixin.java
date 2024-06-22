package net.orifu.skin_overrides.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.PlayerSkin;
import net.orifu.skin_overrides.Mod;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {
    @Shadow
    private GameProfile profile;

    @ModifyReturnValue(method = "getSkin", at = @At("RETURN"))
    private PlayerSkin getSkin(PlayerSkin skin) {
        // the return value of this method is otherwise memoised
        return Mod.overrideSkin(this.profile, skin);
    }
}
