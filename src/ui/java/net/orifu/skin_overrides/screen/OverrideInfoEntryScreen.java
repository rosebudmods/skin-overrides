package net.orifu.skin_overrides.screen;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputUtil;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.widget.layout.LayoutSettings;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.screen.widget.ModelPreviewWidget;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.xplat.CommonTexts;
import net.orifu.xplat.gui.GuiGraphics;
import net.orifu.xplat.gui.Screen;
import net.orifu.xplat.gui.widget.ButtonWidget;
import net.orifu.xplat.gui.widget.GridWidget;
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
    private LinearLayoutWidget root;
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
         this.message = MultilineText.create(this.textRenderer, this.getMessage(), this.width - 50);

         this.root = LinearLayoutWidget.createVertical();
         this.root.setSpacing(PAD);

        // add name input
        if (this.wantsName) {
            var nameInputWrapper = this.root.add(LinearLayoutWidget.createHorizontal(),
                    LayoutSettings.create().alignHorizontallyCenter());

            this.nameInput = nameInputWrapper.add(new TextFieldWidget(this.textRenderer, 120, 20,
                    Text.translatable("skin_overrides.library.input.name")));
            this.nameInput.setText(this.defaultName);
            this.setFocusedChild(this.nameInput);

            if (!this.wantsModel) {
                nameInputWrapper.add(ButtonWidget.builder(CommonTexts.DONE, btn -> this.nameEntered())
                        .width(50).build());
            }

            // add player model widget
            if (this.model != null) {
                this.root.add(ModelPreviewWidget.skin(
                                new Skin(this.texture, null, null, this.model),
                                SKIN_SCALE, this.client),
                        LayoutSettings.create().alignHorizontallyCenter());
            }
        }

        if (this.wantsModel) {
            var grid = this.root.add(new GridWidget(), LayoutSettings.create().alignHorizontallyCenter());
            grid.setSpacing(PAD);

            // add player model widgets
            grid.add(ModelPreviewWidget.skin(
                            new Skin(this.texture, null, null, Skin.Model.WIDE),
                            SKIN_SCALE, this.client),
                    0, 0, LayoutSettings.create().alignHorizontallyCenter());
            grid.add(ModelPreviewWidget.skin(
                            new Skin(this.texture, null, null, Skin.Model.SLIM),
                            SKIN_SCALE, this.client),
                    0, 1, LayoutSettings.create().alignHorizontallyCenter());

            // add selection buttons
            grid.add(ButtonWidget.builder(MODEL_WIDE, btn -> this.select(Skin.Model.WIDE))
                            .size(MODEL_BUTTON_WIDTH, 20).build(),
                    1, 0);
            grid.add(ButtonWidget.builder(MODEL_SLIM, btn -> this.select(Skin.Model.SLIM))
                            .size(MODEL_BUTTON_WIDTH, 20).build(),
                    1, 1);
        }

        if (!this.wantsModel && this.model == null) {
            // add cape model widget
            this.root.add(ModelPreviewWidget.capeWithSkin(
                        Skin.fromProfile(ProfileHelper.user()).withCape(this.texture),
                        SKIN_SCALE, this.client),
                    LayoutSettings.create().alignHorizontallyCenter());
        }

        this.root.visitWidgets(this::addDrawableSelectableElement);
        this.root.arrangeElements();

        this.root.setX((this.width - root.getWidth()) / 2);
        this.root.setY((this.height - this.getContentHeight()) / 2 + this.getContentOffset());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        //? if <1.20.2
        /*this.renderBackground(graphics.portable());*/
        this.renderSuper(graphics, mouseX, mouseY, delta);

        //? if >=1.21 {
        this.message.drawCenteredWithShadow(graphics.portable(), this.width / 2, this.getMessageY());
        //?} else if =1.20.6 {
        /*this.message.render(graphics.portable(), this.width / 2, this.getMessageY());
        *///?} else if >=1.20.1 {
        /*this.message.render(graphics.portable(), this.width / 2, this.getMessageY());
        *///?} else
        /*this.message.drawCenterWithShadow(graphics.portable(), this.width / 2, this.getMessageY());*/
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

    private int getMessagesHeight() {
        return this.message.count() * 9;
    }

    private int getMessageY() {
        return (this.height - this.getContentHeight()) / 2;
    }

    private int getContentOffset() {
        return this.getMessagesHeight() + PAD;
    }

    private int getContentHeight() {
        return this.getContentOffset() + root.getHeight();
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    public interface OverrideInfoCallback {
        void receive(String name, Skin.Model model);
    }
}
