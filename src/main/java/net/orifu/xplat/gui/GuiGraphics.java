package net.orifu.xplat.gui;

//? if <1.20.1 {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public class GuiGraphics {
    public final PoseStack stack;

    public GuiGraphics(PoseStack stack) {
        this.stack = stack;
    }

    public void blit(ResourceLocation texture, int x, int y, int w, int h, int u, int v, int uvW, int uvH, int txW, int txH) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(this.stack, x, y, w, h, u, v, uvW, uvH, txW, txH);
    }

    public PoseStack pose() {
        return this.stack;
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return Minecraft.getInstance().renderBuffers().bufferSource();
    }

    public void flush() {
        this.bufferSource().endBatch();
    }
}
*///?} else
public class GuiGraphics {}
