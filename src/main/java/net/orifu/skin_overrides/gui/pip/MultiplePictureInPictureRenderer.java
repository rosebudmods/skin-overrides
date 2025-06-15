package net.orifu.skin_overrides.gui.pip;

import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;

public abstract class MultiplePictureInPictureRenderer<T extends PictureInPictureRenderState> extends PictureInPictureRenderer<T> {
    protected boolean usedOnThisFrame;

    protected MultiplePictureInPictureRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    public boolean usedOnThisFrame() {
        return this.usedOnThisFrame;
    }

    public void resetUsedOnThisFrame() {
        this.usedOnThisFrame = false;
    }

    @Override
    protected void blitTexture(T pictureInPictureRenderState, GuiRenderState guiRenderState) {
        super.blitTexture(pictureInPictureRenderState, guiRenderState);
        this.usedOnThisFrame = true;
    }
}
