package net.orifu.skin_overrides.util;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Skin;

public class PlayerSkinRenderer {
    public static final int HEAD_SIZE = 8;
    public static final int HEAD_U = 8;
    public static final int HEAD_V = 8;
    public static final int HEAD_LAYER_U = 40;
    public static final int HEAD_LAYER_V = 8;
    public static final int HEAD_X = 4;
    public static final int HEAD_Y = 0;

    public static final int TORSO_WIDTH = 8;
    public static final int TORSO_HEIGHT = 12;
    public static final int TORSO_U = 20;
    public static final int TORSO_V = 20;
    public static final int TORSO_LAYER_U = 20;
    public static final int TORSO_LAYER_V = 36;
    public static final int TORSO_X = 4;
    public static final int TORSO_Y = HEAD_SIZE;

    public static final int ARM_WIDE_WIDTH = 4;
    public static final int ARM_SLIM_WIDTH = 3;
    public static final int ARM_HEIGHT = 12;

    public static final int ARM_RIGHT_U = 44;
    public static final int ARM_RIGHT_V = 20;
    public static final int ARM_RIGHT_LAYER_U = 44;
    public static final int ARM_RIGHT_LAYER_V = 36;
    public static final int ARM_RIGHT_X = 0;
    public static final int ARM_RIGHT_Y = HEAD_SIZE;

    public static final int ARM_LEFT_U = 36;
    public static final int ARM_LEFT_V = 52;
    public static final int ARM_LEFT_LAYER_U = 52;
    public static final int ARM_LEFT_LAYER_V = 52;
    public static final int ARM_LEFT_X = 4 + TORSO_WIDTH;
    public static final int ARM_LEFT_Y = HEAD_SIZE;

    public static final int LEG_WIDTH = 4;
    public static final int LEG_HEIGHT = 12;

    public static final int LEG_RIGHT_U = 4;
    public static final int LEG_RIGHT_V = 20;
    public static final int LEG_RIGHT_LAYER_U = 4;
    public static final int LEG_RIGHT_LAYER_V = 36;
    public static final int LEG_RIGHT_X = 4;
    public static final int LEG_RIGHT_Y = HEAD_SIZE + TORSO_HEIGHT;

    public static final int LEG_LEFT_U = 20;
    public static final int LEG_LEFT_V = 52;
    public static final int LEG_LEFT_LAYER_U = 4;
    public static final int LEG_LEFT_LAYER_V = 52;
    public static final int LEG_LEFT_X = 4 + LEG_WIDTH;
    public static final int LEG_LEFT_Y = HEAD_SIZE + TORSO_HEIGHT;

    public static final int WIDTH = TORSO_WIDTH + ARM_WIDE_WIDTH * 2;
    public static final int HEIGHT = HEAD_SIZE + TORSO_HEIGHT + LEG_HEIGHT;

    public static final int LAYER_DOWNSCALE = 2;

    public static void draw(GuiGraphics graphics, Skin skin, int x, int y, int scale) {
        draw(graphics, skin.texture(), skin.model(), x, y, scale);
    }

    public static void draw(GuiGraphics graphics, Identifier texture, Skin.Model model, int x, int y, int scale) {
        int armWidth = model.equals(Skin.Model.WIDE) ? ARM_WIDE_WIDTH : ARM_SLIM_WIDTH;
        int rightArmOffset = model.equals(Skin.Model.WIDE) ? 0 : 1;

        drawLeftArm(graphics, texture, x, y, scale, armWidth, false);
        drawRightArm(graphics, texture, x, y, scale, armWidth, rightArmOffset, false);
        drawLeftLeg(graphics, texture, x, y, scale, false);
        drawRightLeg(graphics, texture, x, y, scale, false);
        drawTorso(graphics, texture, x, y, scale, false);

        RenderSystem.enableBlend();
        drawLeftArm(graphics, texture, x, y, scale, armWidth, true);
        drawRightArm(graphics, texture, x, y, scale, armWidth, rightArmOffset, true);
        drawLeftLeg(graphics, texture, x, y, scale, true);
        drawRightLeg(graphics, texture, x, y, scale, true);
        drawTorso(graphics, texture, x, y, scale, true);

        drawHead(graphics, texture, x, y, scale, false);
        drawHead(graphics, texture, x, y, scale, true);
        RenderSystem.disableBlend();
    }

    public static void drawFace(GuiGraphics graphics, Skin skin, int x, int y, int scale) {
        drawFace(graphics, skin.texture(), x, y, scale);
    }

    public static void drawFace(GuiGraphics graphics, Identifier texture, int x, int y, int scale) {
        drawHead(graphics, texture, x - HEAD_X * scale, y - HEAD_Y * scale, scale, false);
        RenderSystem.enableBlend();
        drawHead(graphics, texture, x - HEAD_X * scale, y - HEAD_Y * scale, scale, true);
        RenderSystem.disableBlend();
    }

    private static int offset(boolean isLayer, int scale) {
        return isLayer ? Math.max(scale / LAYER_DOWNSCALE, 1) : 0;
    }

    private static void drawHead(GuiGraphics graphics, Identifier texture, int x, int y, int scale, boolean isLayer) {
        int o = offset(isLayer, scale);
        graphics.drawTexture(texture,
                x + HEAD_X * scale - o, y + HEAD_Y * scale - o,
                HEAD_SIZE * scale + o * 2, HEAD_SIZE * scale + o * 2,
                isLayer ? HEAD_LAYER_U : HEAD_U,
                isLayer ? HEAD_LAYER_V : HEAD_V,
                HEAD_SIZE, HEAD_SIZE, 64, 64);
    }

    private static void drawTorso(GuiGraphics graphics, Identifier texture, int x, int y, int scale, boolean isLayer) {
        int o = offset(isLayer, scale);
        graphics.drawTexture(texture,
                x + TORSO_X * scale - o, y + TORSO_Y * scale - o,
                TORSO_WIDTH * scale + o * 2, TORSO_HEIGHT * scale + o * 2,
                isLayer ? TORSO_LAYER_U : TORSO_U,
                isLayer ? TORSO_LAYER_V : TORSO_V,
                TORSO_WIDTH, TORSO_HEIGHT, 64, 64);
    }

    private static void drawLeftArm(GuiGraphics graphics, Identifier texture, int x, int y, int scale, int armWidth,
            boolean isLayer) {
        int o = offset(isLayer, scale);
        graphics.drawTexture(texture,
                x + ARM_LEFT_X * scale - o, y + ARM_LEFT_Y * scale - o,
                armWidth * scale + o * 2, ARM_HEIGHT * scale + o * 2,
                isLayer ? ARM_LEFT_LAYER_U : ARM_LEFT_U,
                isLayer ? ARM_LEFT_LAYER_V : ARM_LEFT_V,
                armWidth, ARM_HEIGHT, 64, 64);
    }

    private static void drawRightArm(GuiGraphics graphics, Identifier texture, int x, int y, int scale, int armWidth,
            int rightArmOffset, boolean isLayer) {
        int o = offset(isLayer, scale);
        graphics.drawTexture(texture,
                x + (ARM_RIGHT_X + rightArmOffset) * scale - o, y + ARM_RIGHT_Y * scale - o,
                armWidth * scale + o * 2, ARM_HEIGHT * scale + o * 2,
                isLayer ? ARM_RIGHT_LAYER_U : ARM_RIGHT_U,
                isLayer ? ARM_RIGHT_LAYER_V : ARM_RIGHT_V,
                armWidth, ARM_HEIGHT, 64, 64);
    }

    private static void drawLeftLeg(GuiGraphics graphics, Identifier texture, int x, int y, int scale,
            boolean isLayer) {
        int o = offset(isLayer, scale);
        graphics.drawTexture(texture,
                x + LEG_LEFT_X * scale - o, y + LEG_LEFT_Y * scale - o,
                LEG_WIDTH * scale + o * 2, LEG_HEIGHT * scale + o * 2,
                isLayer ? LEG_LEFT_LAYER_U : LEG_LEFT_U,
                isLayer ? LEG_LEFT_LAYER_V : LEG_LEFT_V,
                LEG_WIDTH, LEG_HEIGHT, 64, 64);
    }

    private static void drawRightLeg(GuiGraphics graphics, Identifier texture, int x, int y, int scale,
            boolean isLayer) {
        int o = offset(isLayer, scale);
        graphics.drawTexture(texture,
                x + LEG_RIGHT_X * scale - o, y + LEG_RIGHT_Y * scale - o,
                LEG_WIDTH * scale + o * 2, LEG_HEIGHT * scale + o * 2,
                isLayer ? LEG_RIGHT_LAYER_U : LEG_RIGHT_U,
                isLayer ? LEG_RIGHT_LAYER_V : LEG_RIGHT_V,
                LEG_WIDTH, LEG_HEIGHT, 64, 64);
    }
}
