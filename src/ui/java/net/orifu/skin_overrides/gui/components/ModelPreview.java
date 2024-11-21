package net.orifu.skin_overrides.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Skin;
import net.orifu.xplat.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class ModelPreview extends AbstractWidget {
    public final net.orifu.skin_overrides.util.ModelPreview renderer;

    protected ModelPreview(net.orifu.skin_overrides.util.ModelPreview renderer) {
        super(0, 0, renderer.width(), renderer.height(), Component.empty());

        this.renderer = renderer;
    }

    public static ModelPreview skin(@Nullable Skin skin, int scale, Minecraft client) {
        return new ModelPreview(new net.orifu.skin_overrides.util.ModelPreview(skin, scale, client));
    }

    public static ModelPreview cape(@Nullable ResourceLocation cape, int scale, Minecraft client) {
        var preview = new net.orifu.skin_overrides.util.ModelPreview(null, scale, client);
        if (cape != null) preview.setCape(cape);
        preview.turn(180);
        return new ModelPreview(preview);
    }

    public static ModelPreview capeWithSkin(@Nullable Skin skin, int scale, Minecraft client) {
        var preview = new net.orifu.skin_overrides.util.ModelPreview(skin, scale, client);
        preview.turn(180);
        return new ModelPreview(preview);
    }

    @Override
    //? if >=1.20.1 {
    protected void renderWidget(net.minecraft.client.gui.GuiGraphics graphics, int mouseX, int mouseY, float delta) {
    //?} else
    /*public void renderWidget(net.minecraft.client.util.math.MatrixStack graphics, int mouseX, int mouseY, float delta) {*/
        this.renderer.draw(new GuiGraphics(graphics), this.getX(), this.getY());
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.renderer.drag((float) deltaX, (float) deltaY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}

    // despite the name, this is related to narration
    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {}
}
