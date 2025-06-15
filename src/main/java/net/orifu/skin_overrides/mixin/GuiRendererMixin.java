package net.orifu.skin_overrides.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.orifu.skin_overrides.gui.GuiRendererExt;
import net.orifu.skin_overrides.gui.pip.MultiplePictureInPictureRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Function;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin implements GuiRendererExt {
    @Shadow
    @Final
    private MultiBufferSource.BufferSource bufferSource;

    @Shadow
    @Final
    private GuiRenderState renderState;

    @Unique
    private final Map<Object, MultiplePictureInPictureRenderer<?>> multiplePipRenderers = new Object2ObjectOpenHashMap<>();

    @Unique
    private void clearUnusedMultiplePipRenderers() {
        var iter = this.multiplePipRenderers.entrySet().iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            var renderer = entry.getValue();

            if (!renderer.usedOnThisFrame()) {
                renderer.close();
                iter.remove();
            } else {
                renderer.resetUsedOnThisFrame();
            }
        }
    }

    @Unique
    @Override
    @SuppressWarnings("unchecked")
    public <T extends PictureInPictureRenderState> void addMultiplePipRenderer(
            Object key,
            Function<MultiBufferSource.BufferSource, MultiplePictureInPictureRenderer<T>> function,
            T state) {
        var renderer = this.multiplePipRenderers.computeIfAbsent(key, obj -> function.apply(this.bufferSource));

        // just don't do the code wrong???
        ((PictureInPictureRenderer<T>) renderer)
                .prepare(state, this.renderState, Minecraft.getInstance().getWindow().getGuiScale());
    }

    @Inject(method = "clearUnusedOversizedItemRenderers", at = @At("RETURN"))
    private void clearUnusedRenderers(CallbackInfo ci) {
        this.clearUnusedMultiplePipRenderers();;
    }

    @Inject(method = "close", at = @At("RETURN"))
    private void close(CallbackInfo ci) {
        this.multiplePipRenderers.values().forEach(PictureInPictureRenderer::close);
    }
}
