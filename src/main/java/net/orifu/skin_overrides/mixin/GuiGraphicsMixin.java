package net.orifu.skin_overrides.mixin;

//? if >=1.21.6 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.orifu.skin_overrides.gui.GuiGraphicsExt;
import net.orifu.skin_overrides.gui.GuiRendererExt;
import net.orifu.skin_overrides.gui.pip.MultiplePictureInPictureRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Function;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements GuiGraphicsExt {
    @Shadow
    @Final
    private GuiGraphics.ScissorStack scissorStack;

    @Override
    @Unique
    public <T extends PictureInPictureRenderState> void addMultiplePipRenderer(
            Object key,
            Function<MultiBufferSource.BufferSource, MultiplePictureInPictureRenderer<T>> function,
            Function<ScreenRectangle, T> state) {
        ((GuiRendererExt) Minecraft.getInstance().gameRenderer.guiRenderer)
                .addMultiplePipRenderer(key, function, state.apply(this.scissorStack.peek()));
    }
}
//?} else
/*public class GuiGraphicsMixin {}*/
