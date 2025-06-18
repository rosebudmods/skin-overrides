package net.orifu.skin_overrides.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Skin;
import net.orifu.xplat.gui.RenderPipelines;

public class PlayerCapeRenderer {
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
