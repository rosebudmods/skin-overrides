package net.orifu.skin_overrides.screen;

import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.texture.LocalPlayerTexture;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;

public class OverrideInfoEntryScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.pick_model");
    private static final Text MODEL_WIDE = Text.translatable("skin_overrides.model.wide");
    private static final Text MODEL_SLIM = Text.translatable("skin_overrides.model.slim");

    private static final int SKIN_SCALE = 4;
    private static final int SKIN_WIDTH = PlayerSkinRenderer.WIDTH * SKIN_SCALE;
    private static final int MODEL_BUTTON_WIDTH = SKIN_WIDTH + 20;

    @Nullable
    private final Screen parent;
    private final boolean wantsModel;
    private final boolean wantsName;

    private final Identifier texture;
    private final OverrideInfoCallback callback;

    private MultilineText message;

    @Nullable
    private TextFieldWidget nameInput;

    private OverrideInfoEntryScreen(@Nullable Screen parent, boolean wantsModel, boolean wantsName, Identifier texture,
            OverrideInfoCallback callback) {
        super(TITLE);

        this.parent = parent;
        this.wantsModel = wantsModel;
        this.wantsName = wantsName;
        this.texture = texture;
        this.callback = callback;
    }

    public static OverrideInfoEntryScreen getModel(@Nullable Screen parent, Path texturePath,
            Consumer<PlayerSkin.Model> callback) {
        var texture = new LocalPlayerTexture(texturePath.toFile());
        Identifier textureId = new Identifier("skin_overrides", UUID.randomUUID().toString());
        MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);

        return new OverrideInfoEntryScreen(parent, true, false, textureId, (name, model) -> callback.accept(model));
    }

    public static OverrideInfoEntryScreen getName(@Nullable Screen parent, Path texturePath, String defaultName,
            Consumer<String> callback) {
        var texture = new LocalPlayerTexture(texturePath.toFile());
        Identifier textureId = new Identifier("skin_overrides", UUID.randomUUID().toString());
        MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, texture);

        var screen = new OverrideInfoEntryScreen(parent, false, true, textureId,
                (name, model) -> callback.accept(name));
        // TODO screen.nameInput.setText(defaultName);
        return screen;
    }

    @Override
    protected void init() {
        this.message = MultilineText.create(this.textRenderer, TITLE, this.width - 50);

        // add skin model selector buttons
        int buttonY = this.getModelButtonY();
        if (this.wantsModel) {
            this.addDrawableSelectableElement(
                    ButtonWidget.builder(MODEL_WIDE, btn -> this.select(PlayerSkin.Model.WIDE))
                            .positionAndSize(this.width / 2 - 5 - MODEL_BUTTON_WIDTH, buttonY, MODEL_BUTTON_WIDTH, 20)
                            .build());
            this.addDrawableSelectableElement(
                    ButtonWidget.builder(MODEL_SLIM, btn -> this.select(PlayerSkin.Model.SLIM))
                            .positionAndSize(this.width / 2 + 5, buttonY, MODEL_BUTTON_WIDTH, 20).build());
        }

        // add name input
        if (this.wantsName) {
            int wrapperWidth = this.wantsModel ? 120 : 120 + 50;
            var nameInputWrapper = LinearLayoutWidget.createHorizontal();
            nameInputWrapper.setPosition((this.width - wrapperWidth) / 2, this.getNameInputY());

            // TODO
            this.nameInput = nameInputWrapper.add(
                    new TextFieldWidget(this.textRenderer, 120, 20, Text.literal("WIP")));

            if (!this.wantsModel) {
                nameInputWrapper.add(ButtonWidget.builder(CommonTexts.DONE, btn -> {
                    this.callback.receive(this.nameInput.getText(), null);
                    this.client.setScreen(this.parent);
                }).width(50).build());
            }

            nameInputWrapper.visitWidgets(this::addDrawableSelectableElement);
            nameInputWrapper.arrangeElements();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        this.message.render(graphics, this.width / 2, this.getMessageY());

        if (this.wantsModel) {
            PlayerSkinRenderer.draw(graphics, this.texture, PlayerSkin.Model.WIDE,
                    this.width / 2 - 5 - 10 - SKIN_WIDTH, this.getSkinY(), SKIN_SCALE);
            PlayerSkinRenderer.draw(graphics, this.texture, PlayerSkin.Model.SLIM,
                    this.width / 2 + 5 + 10, this.getSkinY(), SKIN_SCALE);
        }
    }

    private void select(PlayerSkin.Model model) {
        String name = this.wantsName ? null : this.nameInput.getText();
        this.callback.receive(name, model);
        this.client.setScreen(this.parent);
    }

    private int getContentHeight() {
        return this.getMessagesHeight()
                + (this.wantsModel ? 20 + PlayerSkinRenderer.HEIGHT * SKIN_SCALE + 20 + 20 : 0)
                + (this.wantsName ? 20 + 20 : 0);
    }

    private int getMessagesHeight() {
        return this.message.count() * 9;
    }

    private int getMessageY() {
        return (this.height - this.getContentHeight()) / 2;
    }

    private int getNameInputY() {
        return this.getMessageY() + this.getMessagesHeight() + 20;
    }

    private int getSkinY() {
        return this.getNameInputY() + (this.wantsName ? 40 : 0);
    }

    private int getModelButtonY() {
        return this.getSkinY() + PlayerSkinRenderer.HEIGHT * SKIN_SCALE + 20;
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    public interface OverrideInfoCallback {
        void receive(String name, PlayerSkin.Model model);
    }
}
