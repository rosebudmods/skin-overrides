package net.orifu.skin_overrides.mixin;

//? if <=1.20.2 {
/*import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.world.level.block.SkullBlock;
import net.orifu.skin_overrides.Mod;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkullBlockRenderer.class)
public class SkullBlockRendererMixin {
    @ModifyReturnValue(method = "getRenderType", at = @At("RETURN"))
    private static RenderType useOverriddenSkinTexture(RenderType original, SkullBlock.Type type, @Nullable GameProfile profile) {
        if (type == SkullBlock.Types.PLAYER && profile != null && profile.getId() != null) {
            var override = Mod.override(profile);
            return RenderType.entityTranslucent(override.texture());
        }

        return original;
    }
}
*///?} else
public class SkullBlockRendererMixin {}
