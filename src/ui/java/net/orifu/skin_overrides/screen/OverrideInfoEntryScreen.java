package net.orifu.skin_overrides.screen;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputUtil;
import net.minecraft.client.font.MultilineText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.PlayerCapeRenderer;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.xplat.CommonTexts;
import net.orifu.xplat.gui.GuiGraphics;
import net.orifu.xplat.gui.Screen;
import net.orifu.xplat.gui.widget.ButtonWidget;
import net.orifu.xplat.gui.widget.LinearLayoutWidget;
import net.orifu.xplat.gui.widget.TextFieldWidget;
import org.jetbrains.annotations.Nullable;

public class OverrideInfoEntryScreen extends Screen {
    private static final Text INPUT_MODEL = Text.translatable("skin_overrides.input.model");
    private static final Text INPUT_NAME = Text.translatable("skin_overrides.input.name");
    private static final Text INPUT_NAME_AND_MODEL = Text.translatable("skin_overrides.input.name_and_model");

    private static final Text MODEL_WIDE = Text.translatable("skin_overrides.model.wide");
    private static final Text MODEL_SLIM = Text.translatable("skin_overrides.model.slim");

    private static final int PAD = 10;

    private static final int SKIN_SCALE = 4;
    private static final int SKIN_WIDTH = PlayerSkinRenderer.WIDTH * SKIN_SCALE;
    private static final int MODEL_BUTTON_WIDTH = SKIN_WIDTH + 20;

    private static final int CAPE_SCALE = 6;
    private static final int CAPE_WIDTH = PlayerCapeRenderer.WIDTH * CAPE_SCALE;

    @Nullable
    private final Screen parent;
    private final boolean wantsModel;
    private final boolean wantsName;
    private final String defaultName;

    private final Identifier texture;
    @Nullable
    private final Skin.Model model;

    private final OverrideInfoCallback callback;

    private MultilineText message;

    @Nullable
    private TextFieldWidget nameInput;

    private OverrideInfoEntryScreen(@Nullable Screen parent, boolean wantsModel, boolean wantsName, String defaultName,
            Identifier texture, @Nullable Skin.Model model, OverrideInfoCallback callback) {
        super(getMessageStatic(wantsName, wantsModel));

        this.parent = parent;
        this.wantsModel = wantsModel;
        this.wantsName = wantsName;
        this.defaultName = defaultName;
        this.texture = texture;
        this.model = model;
        this.callback = callback;
    }

    public static OverrideInfoEntryScreen getModel(@Nullable Screen parent, Identifier textureId,
            Consumer<Skin.Model> callback) {
        return new OverrideInfoEntryScreen(parent, true, false, "", textureId, null,
                (name, model) -> callback.accept(model));
    }

    public static OverrideInfoEntryScreen getName(@Nullable Screen parent, Identifier textureId, String defaultName,
            Consumer<String> callback) {
        return new OverrideInfoEntryScreen(parent, false, true, defaultName, textureId, null,
                (name, model) -> callback.accept(name));
    }

    public static OverrideInfoEntryScreen getName(@Nullable Screen parent, Identifier textureId, Skin.Model model,
            String defaultName, Consumer<String> callback) {
        return new OverrideInfoEntryScreen(parent, false, true, defaultName, textureId, model,
                (name, model2) -> callback.accept(name));
    }

    public static OverrideInfoEntryScreen getNameAndModel(@Nullable Screen parent, Identifier textureId,
            String defaultName, OverrideInfoCallback callback) {
        return new OverrideInfoEntryScreen(parent, true, true, defaultName, textureId, null, callback);
    }

    @Override
    protected void init() {
        //? if =1.21 {
         this.message = MultilineText.method_30890(this.textRenderer, this.getMessage(), this.width - 50); 
        //?} else
        /*this.message = MultilineText.create(this.textRenderer, this.getMessage(), this.width - 50);*/

        // add skin model selector buttons
        int buttonY = this.getModelButtonY();
        if (this.wantsModel) {
            this.addDrawableSelectableElement(
                    ButtonWidget.builder(MODEL_WIDE, btn -> this.select(Skin.Model.WIDE))
                            .positionAndSize(this.width / 2 - 5 - MODEL_BUTTON_WIDTH, buttonY, MODEL_BUTTON_WIDTH, 20)
                            .build());
            this.addDrawableSelectableElement(
                    ButtonWidget.builder(MODEL_SLIM, btn -> this.select(Skin.Model.SLIM))
                            .positionAndSize(this.width / 2 + 5, buttonY, MODEL_BUTTON_WIDTH, 20).build());
        }

        // add name input
        if (this.wantsName) {
            int wrapperWidth = this.wantsModel ? 120 : 120 + 50;
            var nameInputWrapper = LinearLayoutWidget.createHorizontal();
            nameInputWrapper.setPosition((this.width - wrapperWidth) / 2, this.getNameInputY());

            this.nameInput = nameInputWrapper.add(new TextFieldWidget(this.textRenderer, 120, 20,
                    Text.translatable("skin_overrides.library.input.name")));
            this.nameInput.setText(this.defaultName);
            this.setFocusedChild(this.nameInput);

            if (!this.wantsModel) {
                nameInputWrapper.add(ButtonWidget.builder(CommonTexts.DONE, btn -> this.nameEntered())
                        .width(50).build());
            }

            nameInputWrapper.visitWidgets(this::addDrawableSelectableElement);
            nameInputWrapper.arrangeElements();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        //? if <1.20.2
        /*this.renderBackground(graphics.portable());*/
        this.renderSuper(graphics, mouseX, mouseY, delta);

        //? if =1.21 {
        this.message.method_30889(graphics.portable(), this.width / 2, this.getMessageY(), 8, 0xffffff);
        //?} else if =1.20.6 {
        /*this.message.render(graphics.portable(), this.width / 2, this.getMessageY());
        *///?} else if >=1.20.1 {
        /*this.message.render(graphics.portable(), this.width / 2, this.getMessageY());
        *///?} else
        /*this.message.drawCenterWithShadow(graphics.portable(), this.width / 2, this.getMessageY());*/

        if (this.wantsModel) {
            PlayerSkinRenderer.draw(graphics, this.texture, Skin.Model.WIDE,
                    this.width / 2 - 5 - 10 - SKIN_WIDTH, this.getPreviewY(), SKIN_SCALE);
            PlayerSkinRenderer.draw(graphics, this.texture, Skin.Model.SLIM,
                    this.width / 2 + 5 + 10, this.getPreviewY(), SKIN_SCALE);
        } else if (this.model != null) {
            PlayerSkinRenderer.draw(graphics, this.texture, this.model,
                    this.width / 2 - SKIN_WIDTH / 2, this.getPreviewY(), SKIN_SCALE);
        } else {
            PlayerCapeRenderer.draw(graphics, this.texture,
                    this.width / 2 - CAPE_WIDTH / 2, this.getPreviewY(), CAPE_SCALE);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == InputUtil.KEY_ENTER_CODE || keyCode == InputUtil.KEY_NUMPAD_ENTER_CODE) {
            if (this.wantsName && !this.wantsModel) {
                this.nameEntered();
                return true;
            }
        }
        return false;
    }

    private void select(Skin.Model model) {
        String name = this.wantsName ? this.nameInput.getText() : null;
        this.callback.receive(name, model);
        this.client.setScreen(this.parent);
    }

    private void nameEntered() {
        this.callback.receive(this.nameInput.getText(), null);
        this.client.setScreen(this.parent);
    }

    private int getContentHeight() {
        return this.getMessagesHeight() + PAD + this.getPreviewHeight()
                + (this.wantsModel ? 20 + PAD : 0)
                + (this.wantsName ? 20 + PAD : 0);
    }

    protected static Text getMessageStatic(boolean wantsName, boolean wantsModel) {
        return wantsName && wantsModel
                ? INPUT_NAME_AND_MODEL
                : wantsName
                        ? INPUT_NAME
                        : INPUT_MODEL;
    }

    protected Text getMessage() {
        return getMessageStatic(this.wantsName, this.wantsModel);
    }

    private int getPreviewHeight() {
        if (this.wantsModel || this.model != null) {
            return PlayerSkinRenderer.HEIGHT * SKIN_SCALE;
        } else {
            return PlayerCapeRenderer.HEIGHT * CAPE_SCALE;
        }
    }

    private int getMessagesHeight() {
        return this.message.count() * 9;
    }

    private int getMessageY() {
        return (this.height - this.getContentHeight()) / 2;
    }

    private int getNameInputY() {
        return this.getMessageY() + this.getMessagesHeight() + PAD;
    }

    private int getPreviewY() {
        return this.getNameInputY() + (this.wantsName ? 20 + PAD : 0);
    }

    private int getModelButtonY() {
        return this.getPreviewY() + this.getPreviewHeight() + PAD;
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    public interface OverrideInfoCallback {
        void receive(String name, Skin.Model model);
    }
}
