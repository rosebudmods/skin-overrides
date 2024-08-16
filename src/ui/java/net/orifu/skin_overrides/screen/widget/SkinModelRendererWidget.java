package net.orifu.skin_overrides.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.skin_overrides.util.SkinModelRenderer;
import org.jetbrains.annotations.Nullable;

public class SkinModelRendererWidget extends ClickableWidget {
    public final SkinModelRenderer renderer;

    public SkinModelRendererWidget(@Nullable Skin skin, int scale, MinecraftClient client) {
        super(0, 0, PlayerSkinRenderer.WIDTH * scale, PlayerSkinRenderer.HEIGHT * scale, Text.empty());

        this.renderer = new SkinModelRenderer(skin, scale, client);
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
