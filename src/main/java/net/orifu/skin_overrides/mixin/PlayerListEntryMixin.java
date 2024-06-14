package net.orifu.skin_overrides.mixin;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
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

    @Nullable
    String orifu$remoteName = null;
    @Nullable
    Supplier<PlayerSkin> orifu$remoteSkin = null;

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        PlayerSkin skin = cir.getReturnValue();
        MinecraftClient client = MinecraftClient.getInstance();

        var skinFile = SkinOverrides.skinTextureFor(this.profile);
        if (skinFile.isPresent()) {
            // register skin texture
            var texture = skinFile.get();
            Identifier skinId = new Identifier("skin_overrides", "skin/" + this.profile.getId().toString());
            client.getTextureManager().registerTexture(skinId, texture);
            // update skin
            skin = new PlayerSkin(skinId, null, skin.capeTexture(), skin.elytraTexture(), texture.model, false);
        }

        var userOverride = SkinOverrides.userIdFor(this.profile);
        if (userOverride.isPresent()) {
            String name = userOverride.get();

            // new name, fetch the new profile
            if (!name.equals(this.orifu$remoteName)) {
                // get the uuid
                var profile = client.getServer().getUserCache().findByName(name);
                if (profile.isPresent()) {
                    // get the full profile
                    var profileResult = client.getSessionService().fetchProfile(profile.get().getId(), false);

                    this.orifu$remoteName = name;
                    this.orifu$remoteSkin = client.getSkinProvider().getSkinSupplier(profileResult.profile());
                }
            }

            if (this.orifu$remoteSkin != null) {
                // set the skin to the remote one
                PlayerSkin remoteSkin = this.orifu$remoteSkin.get();
                skin = new PlayerSkin(remoteSkin.texture(), remoteSkin.textureUrl(), skin.capeTexture(),
                        skin.elytraTexture(), remoteSkin.model(), false);
            }
        }

        var capeFile = SkinOverrides.capeTextureFor(this.profile);
        if (capeFile.isPresent()) {
            // register cape texture
            Identifier capeId = new Identifier("skin_overrides", "cape/" + this.profile.getId().toString());
            client.getTextureManager().registerTexture(capeId, capeFile.get());
            // update skin
            // note: the elytra texture is a separate part of the record,
            // but updating the cape still updates the elytra.
            skin = new PlayerSkin(skin.texture(), skin.textureUrl(), capeId, skin.elytraTexture(), skin.model(), false);
        }

        cir.setReturnValue(skin);
    }
}
