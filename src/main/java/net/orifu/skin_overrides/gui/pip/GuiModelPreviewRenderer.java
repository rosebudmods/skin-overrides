package net.orifu.skin_overrides.gui.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class GuiModelPreviewRenderer extends MultiplePictureInPictureRenderer<GuiModelPreviewRenderer.GuiModelPreviewRenderState> {
    public GuiModelPreviewRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<GuiModelPreviewRenderState> getRenderStateClass() {
        return GuiModelPreviewRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiModelPreviewRenderState state, PoseStack poseStack) {
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.PLAYER_SKIN);
        var modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();

        int i = Minecraft.getInstance().getWindow().getGuiScale();
        float f = state.scale() * i;
        modelViewStack.rotateAround(Axis.XP.rotationDegrees(state.xRot()), 0, f * -state.yPivot(), 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot()));
        poseStack.translate(0, -1.6f, 0);

        RenderType renderType = state.playerModel().renderType(state.texture());
        state.playerModel().renderToBuffer(poseStack, this.bufferSource.getBuffer(renderType), 0xf000f0, OverlayTexture.NO_OVERLAY);

        this.bufferSource.endBatch();
        modelViewStack.popMatrix();
    }

    @Override
    protected String getTextureLabel() {
        return "skin overrides model preview renderer";
    }

    public record GuiModelPreviewRenderState(
            int x0,
            int y0,
            int x1,
            int y1,
            float xRot,
            float yRot,
            float scale,
            float yPivot,
            PlayerModel playerModel,
            ResourceLocation texture,
            ScreenRectangle scissorArea,
            @Nullable ScreenRectangle bounds
    ) implements PictureInPictureRenderState {
        public static Function<ScreenRectangle, GuiModelPreviewRenderState> create(
                int x, int y, int w, int h, float xRot, float yRot, float scale, float yPivot,
                PlayerModel playerModel,
                ResourceLocation texture
        ) {
            return scissorArea -> new GuiModelPreviewRenderState(
                    x,
                    y,
                    x + w,
                    y + h,
                    xRot,
                    yRot,
                    scale,
                    yPivot,
                    playerModel,
                    texture,
                    scissorArea,
                    PictureInPictureRenderState.getBounds(x, x + w, y, y + h, scissorArea)
            );
        }
    }
}
