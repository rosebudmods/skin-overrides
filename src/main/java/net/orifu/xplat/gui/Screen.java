package net.orifu.xplat.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

//? if <1.20.1
/*import com.mojang.blaze3d.vertex.PoseStack;*/

public abstract class Screen extends net.minecraft.client.gui.screens.Screen {
    protected Screen(Component title) {
        super(title);
    }

    //? if <1.20.1 {
    /*@Override
    public final void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        this.render(new GuiGraphics(stack), mouseX, mouseY, delta);
    }
    *///?}

    // render dirt background on versions below 1.20.6
    //? if <1.20.1 {
    /*@Override
    public void renderBackground(PoseStack stack) {
        this.renderDirtBackground(stack);
    }
    *///?} else if <1.20.2 {
    /*@Override
    public void renderBackground(net.minecraft.client.gui.GuiGraphics guiGraphics) {
        this.renderDirtBackground(guiGraphics);
    }
    *///?} else if <1.20.6 {
    /*@Override
    public void renderBackground(net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(guiGraphics);
    }
    *///?}
}
