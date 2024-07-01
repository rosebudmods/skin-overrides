package net.orifu.skin_overrides.xplat.gui;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

public class Screen extends net.minecraft.client.gui.screen.Screen {
    protected Screen(Text title) {
        super(title);
    }

    //? if <=1.20.1 {
    /*protected <T extends Element & Drawable & Selectable> T addDrawableSelectableElement(T drawableElement) {
        return this.addDrawableChild(drawableElement);
    }

    protected <T extends Element & Selectable> T addSelectableElement(T selectableElement) {
        return this.addSelectableChild(selectableElement);
    }
    *///?}
}
