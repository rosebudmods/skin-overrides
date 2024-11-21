package net.orifu.xplat.gui;

import net.minecraft.network.chat.Component;

public abstract class Screen extends net.minecraft.client.gui.screens.Screen {
    protected Screen(Component title) {
        super(title);
    }

    //? if <1.20.2 {
    /*protected <T extends Element & Drawable & Selectable> T addDrawableSelectableElement(T drawableElement) {
        return this.addDrawableChild(drawableElement);
    }

    protected <T extends Element & Selectable> T addSelectableElement(T selectableElement) {
        return this.addSelectableChild(selectableElement);
    }
    *///?}

    public abstract void render(GuiGraphics graphics, int mouseX, int mouseY, float delta);

    public void renderSuper(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics.portable(), mouseX, mouseY, delta);
    }

    //? if <1.20.1 {
    /*@Override
    public final void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.render(new GuiGraphics(matrices), mouseX, mouseY, delta);
    }
    *///?} else {
    @Override
    public void render(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.render(new GuiGraphics(graphics), mouseX, mouseY, delta);
    }
    //?}
}
