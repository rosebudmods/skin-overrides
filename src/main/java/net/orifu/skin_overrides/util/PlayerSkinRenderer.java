package net.orifu.skin_overrides.util;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.util.Identifier;

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

    public static void draw(GuiGraphics graphics, PlayerSkin skin, int x, int y, int scale) {
        draw(graphics, skin.texture(), skin.model(), x, y, scale);
    }

    public static void draw(GuiGraphics graphics, Identifier texture, PlayerSkin.Model model, int x, int y, int scale) {
        int armWidth = model.equals(PlayerSkin.Model.WIDE) ? ARM_WIDE_WIDTH : ARM_SLIM_WIDTH;
        int rightArmOffset = model.equals(PlayerSkin.Model.WIDE) ? 0 : 1;

        drawHead(graphics, texture, x, y, scale, HEAD_U, HEAD_V);
        drawTorso(graphics, texture, x, y, scale, TORSO_U, TORSO_V);
        drawLeftArm(graphics, texture, x, y, scale, armWidth, ARM_LEFT_U, ARM_LEFT_V);
        drawRightArm(graphics, texture, x, y, scale, armWidth, rightArmOffset, ARM_RIGHT_U, ARM_RIGHT_V);
        drawLeftLeg(graphics, texture, x, y, scale, LEG_LEFT_U, LEG_LEFT_V);
        drawRightLeg(graphics, texture, x, y, scale, LEG_RIGHT_U, LEG_RIGHT_V);

        RenderSystem.enableBlend();
        drawHead(graphics, texture, x, y, scale, HEAD_LAYER_U, HEAD_LAYER_V);
        drawTorso(graphics, texture, x, y, scale, TORSO_LAYER_U, TORSO_LAYER_V);
        drawLeftArm(graphics, texture, x, y, scale, armWidth, ARM_LEFT_LAYER_U, ARM_LEFT_LAYER_V);
        drawRightArm(graphics, texture, x, y, scale, armWidth, rightArmOffset, ARM_RIGHT_LAYER_U, ARM_RIGHT_LAYER_V);
        drawLeftLeg(graphics, texture, x, y, scale, LEG_LEFT_LAYER_U, LEG_LEFT_LAYER_V);
        drawRightLeg(graphics, texture, x, y, scale, LEG_RIGHT_LAYER_U, LEG_RIGHT_LAYER_V);
        RenderSystem.disableBlend();
    }

    private static void drawHead(GuiGraphics graphics, Identifier texture, int x, int y, int scale, int u, int v) {
        graphics.drawTexture(texture,
                x + HEAD_X * scale, y + HEAD_Y * scale,
                HEAD_SIZE * scale, HEAD_SIZE * scale,
                u, v, HEAD_SIZE, HEAD_SIZE, 64, 64);
    }

    private static void drawTorso(GuiGraphics graphics, Identifier texture, int x, int y, int scale, int u, int v) {
        graphics.drawTexture(texture,
                x + TORSO_X * scale, y + TORSO_Y * scale,
                TORSO_WIDTH * scale, TORSO_HEIGHT * scale,
                u, v, TORSO_WIDTH, TORSO_HEIGHT, 64, 64);
    }

    private static void drawLeftArm(GuiGraphics graphics, Identifier texture, int x, int y, int scale, int armWidth,
            int u, int v) {
        graphics.drawTexture(texture,
                x + ARM_LEFT_X * scale, y + ARM_LEFT_Y * scale,
                armWidth * scale, ARM_HEIGHT * scale,
                u, v, armWidth, ARM_HEIGHT, 64, 64);
    }

    private static void drawRightArm(GuiGraphics graphics, Identifier texture, int x, int y, int scale, int armWidth,
            int rightArmOffset, int u, int v) {
        graphics.drawTexture(texture,
                x + (ARM_RIGHT_X + rightArmOffset) * scale, y + ARM_RIGHT_Y * scale,
                armWidth * scale, ARM_HEIGHT * scale,
                u, v, armWidth, ARM_HEIGHT, 64, 64);
    }

    private static void drawLeftLeg(GuiGraphics graphics, Identifier texture, int x, int y, int scale, int u, int v) {
        graphics.drawTexture(texture,
                x + LEG_LEFT_X * scale, y + LEG_LEFT_Y * scale,
                LEG_WIDTH * scale, LEG_HEIGHT * scale,
                u, v, LEG_WIDTH, LEG_HEIGHT, 64, 64);
    }

    private static void drawRightLeg(GuiGraphics graphics, Identifier texture, int x, int y, int scale, int u, int v) {
        graphics.drawTexture(texture,
                x + LEG_RIGHT_X * scale, y + LEG_RIGHT_Y * scale,
                LEG_WIDTH * scale, LEG_HEIGHT * scale,
                u, v, LEG_WIDTH, LEG_HEIGHT, 64, 64);
    }
}
