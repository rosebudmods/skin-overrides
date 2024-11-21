package net.orifu.xplat.gui;

//? if >=1.20.1 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class GuiGraphics extends net.minecraft.client.gui.GuiGraphics {
    public GuiGraphics(net.minecraft.client.gui.GuiGraphics graphics) {
        super(Minecraft.getInstance(), graphics.bufferSource);
    }

    public GuiGraphics portable() {
        return this;
    }

    //? if >=1.21.3 {
    public void blit(
            ResourceLocation id,
            int x, int y, int w, int h,
            float u, float v, int uvW, int uvH,
            int txW, int txH
    ) {
        this.blit(RenderType::guiTextured, id, x, y, u, v, w, h, uvW, uvH, txW, txH);
    }
    //?}
}
//?} else {

/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class GuiGraphics {
    private final PoseStack stack;

    public GuiGraphics(PoseStack poseStack) {
        this.stack = poseStack;
    }

    public PoseStack portable() {
        return this.stack;
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

    public void blit(ResourceLocation texture, int x, int y, int w, int h, int u, int v, int uvW, int uvH, int txW, int txH) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(this.stack, x, y, w, h, u, v, uvW, uvH, txW, txH);
    }

    public void drawString(Font font, Component component, int x, int y, int color) {
        GuiComponent.drawString(this.stack, font, component, x, y, color);
    }

    public void drawCenteredString(Font font, Component component, int x, int y, int color) {
        GuiComponent.drawCenteredString(this.stack, font, component.getVisualOrderText(), x, y, color);
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        GuiComponent.fill(this.stack, x1, y1, x2, y2, color);
    }
}

*///?}
