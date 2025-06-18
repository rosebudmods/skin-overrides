package net.orifu.skin_overrides.gui.util;

import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Skin;
import net.orifu.xplat.gui.RenderPipelines;

//? if >=1.20.1 {
import net.minecraft.client.gui.GuiGraphics;
 //?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.orifu.xplat.gui.GuiGraphics;
*///?}

public class SimpleCapeRenderer {
    public static final int WIDTH = 10;
    public static final int HEIGHT = 16;
    public static final int U = 1;
    public static final int V = 1;

    public static void draw(GuiGraphics graphics, Skin skin, int x, int y, int scale) {
        ResourceLocation capeTexture = skin.capeTexture();

        if (capeTexture != null) {
            blit(graphics, capeTexture, x, y, scale);
        }
    }

    //? if <1.20.1 {
    /*public static void draw(PoseStack stack, Skin skin, int x, int y, int scale) {
        draw(new GuiGraphics(stack), skin, x, y, scale);
    }
    *///?}

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int scale) {
        graphics.blit(
                /*? if >=1.21.3*/ RenderPipelines.GUI_TEXTURED,
                texture,
                x, y,
                /*? if <1.21.3*/ /*WIDTH * scale, HEIGHT * scale,*/
                U, V,
                /*? if >=1.21.3*/ WIDTH * scale, HEIGHT * scale,
                WIDTH, HEIGHT, 64, 32);
    }
}
