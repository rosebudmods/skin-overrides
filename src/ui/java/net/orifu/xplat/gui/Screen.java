package net.orifu.xplat.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

public abstract class Screen extends net.minecraft.client.gui.screens.Screen {
    protected Screen(Component title) {
        super(title);
    }

    public abstract void render(GuiGraphics graphics, int mouseX, int mouseY, float delta);

    public void renderSuper(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics.portable(), mouseX, mouseY, delta);
    }

    //? if <1.20.1 {
    /*@Override
    public final void render(PoseStack stack, int mouseX, int mouseY, float delta) {
        this.render(new GuiGraphics(stack), mouseX, mouseY, delta);
    }
    *///?} else {
    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.render(new GuiGraphics(graphics), mouseX, mouseY, delta);
    }
    //?}
}
