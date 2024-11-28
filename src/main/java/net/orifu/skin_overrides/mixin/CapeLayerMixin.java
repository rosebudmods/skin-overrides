package net.orifu.skin_overrides.mixin;

//? if >=1.19.4 <1.20.2 {
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CapeLayer.class)
public class CapeLayerMixin {
    @Unique
    @Nullable
    private ResourceLocation capeTextureLocation;

    @Redirect(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getCloakTextureLocation()Lnet/minecraft/resources/ResourceLocation;", ordinal = 0))
    private ResourceLocation fetchCapeTextureLocation(AbstractClientPlayer player) {
        this.capeTextureLocation = player.getCloakTextureLocation();
        return this.capeTextureLocation;
    }

    @Redirect(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;getCloakTextureLocation()Lnet/minecraft/resources/ResourceLocation;", ordinal = 1))
    private ResourceLocation getCapeTextureLocation(AbstractClientPlayer instance) {
        return this.capeTextureLocation;
    }
}
//?} else
public class CapeLayerMixin {}
