package net.orifu.xplat.gui;

//? if >=1.20.1 {
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class GuiGraphics extends net.minecraft.client.gui.GuiGraphics {
    public GuiGraphics(net.minecraft.client.gui.GuiGraphics graphics) {
        super(MinecraftClient.getInstance(), graphics.vertexConsumers);
    }

    public GuiGraphics portable() {
        return this;
    }

    //? if >=1.21.3 {
    public void drawTexture(
            Identifier id,
            int x, int y, int w, int h,
            float u, float v, int uvW, int uvH,
            int txW, int txH
    ) {
        this.method_25302(RenderLayer::getGuiTextured, id, x, y, u, v, w, h, uvW, uvH, txW, txH);
    }
    //?}
}
//?} else {

/*import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class GuiGraphics {
    private final MatrixStack stack;

    public GuiGraphics(MatrixStack matrixStack) {
        this.stack = matrixStack;
    }

    public MatrixStack portable() {
        return this.stack;
    }

    public MatrixStack getMatrices() {
        return this.stack;
    }

    public VertexConsumerProvider.Immediate getVertexConsumers() {
        return MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
    }

    public void draw() {
        this.getVertexConsumers().draw();
    }

    public void drawTexture(Identifier texture, int x, int y, int w, int h, int u, int v, int uvW, int uvH, int txW, int txH) {
        RenderSystem.setShaderTexture(0, texture);
        DrawableHelper.drawTexture(this.stack, x, y, w, h, u, v, uvW, uvH, txW, txH);
    }

    public void drawShadowedText(TextRenderer textRenderer, Text text, int x, int y, int color) {
        DrawableHelper.drawTextWithShadow(this.stack, textRenderer, text, x, y, color);
    }

    public void drawCenteredShadowedText(TextRenderer textRenderer, Text text, int x, int y, int color) {
        DrawableHelper.drawCenteredTextWithShadow(this.stack, textRenderer, text.asOrderedText(), x, y, color);
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        DrawableHelper.fill(this.stack, x1, y1, x2, y2, color);
    }
}

*///?}
