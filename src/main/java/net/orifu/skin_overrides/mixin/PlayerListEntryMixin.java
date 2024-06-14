package net.orifu.skin_overrides.mixin;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.SkinOverrides;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {
    @Shadow
    private GameProfile profile;

    @Shadow
    private static Supplier<PlayerSkin> getSkinSupplier(GameProfile gameProfile) {
        return null;
    }

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        var skinFile = SkinOverrides.skinTextureFor(this.profile);
        if (skinFile.isPresent()) {
            MinecraftClient client = MinecraftClient.getInstance();

            // register skin texture
            Identifier skinId = new Identifier("skin_overrides", this.profile.getId().toString());
            client.getTextureManager().registerTexture(skinId, skinFile.get());

            // create player skin
            PlayerSkin playerSkin = new PlayerSkin(skinId, null, null, null, PlayerSkin.Model.SLIM, false);
            cir.setReturnValue(playerSkin);
        }
    }
}
