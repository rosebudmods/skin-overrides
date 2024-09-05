package net.orifu.skin_overrides.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.ModelPreview;
import org.jetbrains.annotations.Nullable;

public class ModelPreviewWidget extends ClickableWidget {
    public final ModelPreview renderer;

    protected ModelPreviewWidget(ModelPreview renderer) {
        super(0, 0, renderer.width(), renderer.height(), Text.empty());

        this.renderer = renderer;
    }

    public static ModelPreviewWidget skin(@Nullable Skin skin, int scale, MinecraftClient client) {
        return new ModelPreviewWidget(new ModelPreview(skin, scale, client));
    }

    public static ModelPreviewWidget cape(@Nullable Identifier cape, int scale, MinecraftClient client) {
        var preview = new ModelPreview(null, scale, client);
        if (cape != null) preview.setCape(cape);
        preview.turn(180);
        return new ModelPreviewWidget(preview);
    }

    public static ModelPreviewWidget capeWithSkin(@Nullable Skin skin, int scale, MinecraftClient client) {
        var preview = new ModelPreview(skin, scale, client);
        preview.turn(180);
        return new ModelPreviewWidget(preview);
    }

    @Override
    protected void drawWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderer.draw(graphics, this.getX(), this.getY());
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
        this.renderer.drag((float) deltaX, (float) deltaY);
    }

    @Override
    protected void updateNarration(NarrationMessageBuilder builder) {}

    @Override
    public boolean isNarratable() {
        return false;
    }

    @Override
    public void playDownSound(SoundManager soundManager) {}
}
