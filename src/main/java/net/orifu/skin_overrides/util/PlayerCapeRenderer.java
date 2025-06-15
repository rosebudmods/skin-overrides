package net.orifu.skin_overrides.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Skin;

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
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
                x, y, U, V, WIDTH * scale, HEIGHT * scale,
                WIDTH, HEIGHT, 64, 32);
    }
}
