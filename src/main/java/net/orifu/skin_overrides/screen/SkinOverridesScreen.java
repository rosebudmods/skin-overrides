package net.orifu.skin_overrides.screen;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;

public class SkinOverridesScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.title");

    @Nullable
    private Screen parent;

    public SkinOverridesScreen(@Nullable Screen parent) {
        super(TITLE);

        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableSelectableElement(
                ButtonWidget.builder(CommonTexts.DONE, (btn) -> this.closeScreen())
                        .positionAndSize(this.width / 2 + 4, this.height - 48, 150, 20)
                        .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawCenteredShadowedText(this.textRenderer, TITLE, this.width / 2, 8, 0xffffff);
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }
}
