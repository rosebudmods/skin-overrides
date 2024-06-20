package net.orifu.skin_overrides.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.util.Identifier;

public class PlayerCapeRenderer {
    public static final int WIDTH = 10;
    public static final int HEIGHT = 16;
    public static final int U = 1;
    public static final int V = 1;

    public static void draw(GuiGraphics graphics, PlayerSkin skin, int x, int y, int scale) {
        Identifier capeTexture = skin.capeTexture();

        if (capeTexture != null) {
            draw(graphics, capeTexture, x, y, scale);
        }
    }

    public static void draw(GuiGraphics graphics, Identifier texture, int x, int y, int scale) {
        graphics.drawTexture(texture,
                x, y, WIDTH * scale, HEIGHT * scale,
                U, V, WIDTH, HEIGHT, 64, 32);
    }
}
