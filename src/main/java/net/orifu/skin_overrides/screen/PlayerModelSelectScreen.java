package net.orifu.skin_overrides.screen;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;

public class PlayerModelSelectScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.pick_model");
    private static final Text MODEL_WIDE = Text.translatable("skin_overrides.model.wide");
    private static final Text MODEL_SLIM = Text.translatable("skin_overrides.model.slim");

    private static final int PREVIEW_SCALE = 4;
    private static final int PREVIEW_WIDTH = PlayerSkinRenderer.WIDTH * PREVIEW_SCALE;
    private static final int BUTTON_WIDTH = PREVIEW_WIDTH + 20;

    @Nullable
    private final Screen parent;
    private final Identifier texture;
    private final Consumer<PlayerSkin.Model> callback;

    private MultilineText message;

    public PlayerModelSelectScreen(@Nullable Screen parent, Identifier texture, Consumer<PlayerSkin.Model> callback) {
        super(TITLE);

        this.parent = parent;
        this.texture = texture;
        this.callback = callback;
    }

    @Override
    protected void init() {
        this.message = MultilineText.create(this.textRenderer, TITLE, this.width - 50);

        int buttonY = this.getButtonY();
        this.addDrawableSelectableElement(
                ButtonWidget.builder(MODEL_WIDE, btn -> this.select(PlayerSkin.Model.WIDE))
                        .positionAndSize(this.width / 2 - 5 - BUTTON_WIDTH, buttonY, BUTTON_WIDTH, 20).build());
        this.addDrawableSelectableElement(
                ButtonWidget.builder(MODEL_SLIM, btn -> this.select(PlayerSkin.Model.SLIM))
                        .positionAndSize(this.width / 2 + 5, buttonY, BUTTON_WIDTH, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        this.message.render(graphics, this.width / 2, this.getMessageY());

        PlayerSkinRenderer.draw(graphics, this.texture, PlayerSkin.Model.WIDE,
                this.width / 2 - 5 - 10 - PREVIEW_WIDTH, this.getPreviewY(), PREVIEW_SCALE);
        PlayerSkinRenderer.draw(graphics, this.texture, PlayerSkin.Model.SLIM,
                this.width / 2 + 5 + 10, this.getPreviewY(), PREVIEW_SCALE);
    }

    private void select(PlayerSkin.Model model) {
        this.callback.accept(model);
        this.client.setScreen(this.parent);
    }

    private int getContentHeight() {
        return this.getMessagesHeight() + 20 + PlayerSkinRenderer.HEIGHT * PREVIEW_SCALE + 20 + 20;
    }

    private int getMessagesHeight() {
        return this.message.count() * 9;
    }

    private int getMessageY() {
        return (this.height - this.getContentHeight()) / 2;
    }

    private int getPreviewY() {
        return this.getMessageY() + this.getMessagesHeight() + 20;
    }

    private int getButtonY() {
        return this.getPreviewY() + PlayerSkinRenderer.HEIGHT * PREVIEW_SCALE + 20;
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }
}
