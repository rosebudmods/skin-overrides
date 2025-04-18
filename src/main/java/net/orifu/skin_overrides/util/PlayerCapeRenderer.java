package net.orifu.skin_overrides.util;

import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Skin;
import net.orifu.xplat.gui.GuiGraphics;

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
        graphics.blit(texture,
                x, y, WIDTH * scale, HEIGHT * scale,
                U, V, WIDTH, HEIGHT, 64, 32);
    }
}
