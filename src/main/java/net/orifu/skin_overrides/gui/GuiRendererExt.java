package net.orifu.skin_overrides.gui;

import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.orifu.skin_overrides.gui.pip.MultiplePictureInPictureRenderer;

import java.util.function.Function;

public interface GuiRendererExt {
    <T extends PictureInPictureRenderState> void addMultiplePipRenderer(
            Object key,
            Function<MultiBufferSource.BufferSource, MultiplePictureInPictureRenderer<T>> function,
            T state);
}
