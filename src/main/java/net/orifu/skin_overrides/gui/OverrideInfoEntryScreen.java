package net.orifu.skin_overrides.gui;

import java.util.function.Consumer;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.gui.components.ModelPreview;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.xplat.GuiHelper;
import net.orifu.xplat.gui.Screen;
import net.orifu.xplat.gui.components.LinearLayout;
import org.jetbrains.annotations.Nullable;

public class OverrideInfoEntryScreen extends Screen {
    private static final Component INPUT_MODEL = Component.translatable("skin_overrides.input.model");
    private static final Component INPUT_NAME = Component.translatable("skin_overrides.input.name");
    private static final Component INPUT_NAME_AND_MODEL = Component.translatable("skin_overrides.input.name_and_model");

    private static final Component MODEL_WIDE = Component.translatable("skin_overrides.model.wide");
    private static final Component MODEL_SLIM = Component.translatable("skin_overrides.model.slim");

    private static final int PAD = 10;

    private static final int SKIN_SCALE = 4;
    private static final int SKIN_WIDTH = PlayerSkinRenderer.WIDTH * SKIN_SCALE;
    private static final int MODEL_BUTTON_WIDTH = SKIN_WIDTH + 20;

    @Nullable
    private final Screen parent;
    private final boolean wantsModel;
    private final boolean wantsName;
    private final String defaultName;

    private final ResourceLocation texture;
    @Nullable
    private final Skin.Model model;

    private final OverrideInfoCallback callback;

    private MultiLineLabel message;

    @Nullable
    private Layout root;
    @Nullable
    private EditBox nameInput;

    private OverrideInfoEntryScreen(@Nullable Screen parent, boolean wantsModel, boolean wantsName, String defaultName,
            ResourceLocation texture, @Nullable Skin.Model model, OverrideInfoCallback callback) {
        super(getMessageStatic(wantsName, wantsModel));

        this.parent = parent;
        this.wantsModel = wantsModel;
        this.wantsName = wantsName;
        this.defaultName = defaultName;
        this.texture = texture;
        this.model = model;
        this.callback = callback;
    }

    public static OverrideInfoEntryScreen getModel(@Nullable Screen parent, ResourceLocation textureLoc,
            Consumer<Skin.Model> callback) {
        return new OverrideInfoEntryScreen(parent, true, false, "", textureLoc, null,
                (name, model) -> callback.accept(model));
    }

    public static OverrideInfoEntryScreen getName(@Nullable Screen parent, ResourceLocation textureLoc, String defaultName,
            Consumer<String> callback) {
        return new OverrideInfoEntryScreen(parent, false, true, defaultName, textureLoc, null,
                (name, model) -> callback.accept(name));
    }

    public static OverrideInfoEntryScreen getName(@Nullable Screen parent, ResourceLocation textureLoc, Skin.Model model,
            String defaultName, Consumer<String> callback) {
        return new OverrideInfoEntryScreen(parent, false, true, defaultName, textureLoc, model,
                (name, model2) -> callback.accept(name));
    }

    public static OverrideInfoEntryScreen getNameAndModel(@Nullable Screen parent, ResourceLocation textureLoc,
            String defaultName, OverrideInfoCallback callback) {
        return new OverrideInfoEntryScreen(parent, true, true, defaultName, textureLoc, null, callback);
    }

    @Override
    protected void init() {
         this.message = MultiLineLabel.create(this.font, this.getMessage(), this.width - 50);

         var root = LinearLayout.vertical().spacing(PAD);
         this.root = root;

        // add name input
        if (this.wantsName) {
            var nameInputWrapper = root.addChild(LinearLayout.horizontal(),
                    LayoutSettings.defaults().alignHorizontallyCenter());

            this.nameInput = nameInputWrapper.addChild(GuiHelper.editBox(this.font, 120, 20,
                    Component.translatable("skin_overrides.library.input.name")));
            this.nameInput.setValue(this.defaultName);
            this.setFocused(this.nameInput);

            if (!this.wantsModel) {
                nameInputWrapper.addChild(Button.builder(CommonComponents.GUI_DONE, btn -> this.nameEntered())
                        .width(50).build());
            }

            // add player model widget
            if (this.model != null) {
                root.addChild(ModelPreview.skin(
                                new Skin(this.texture, null, null, this.model),
                                SKIN_SCALE, this.minecraft),
                        LayoutSettings.defaults().alignHorizontallyCenter());
            }
        }

        // add model input
        if (this.wantsModel) {
            var grid = root.addChild(new GridLayout().spacing(PAD), LayoutSettings.defaults().alignHorizontallyCenter());

            // add player model widgets
            grid.addChild(ModelPreview.skin(
                            new Skin(this.texture, null, null, Skin.Model.WIDE),
                            SKIN_SCALE, this.minecraft),
                    0, 0, LayoutSettings.defaults().alignHorizontallyCenter());
            grid.addChild(ModelPreview.skin(
                            new Skin(this.texture, null, null, Skin.Model.SLIM),
                            SKIN_SCALE, this.minecraft),
                    0, 1, LayoutSettings.defaults().alignHorizontallyCenter());

            // add selection buttons
            grid.addChild(Button.builder(MODEL_WIDE, btn -> this.select(Skin.Model.WIDE))
                            .size(MODEL_BUTTON_WIDTH, 20).build(),
                    1, 0);
            grid.addChild(Button.builder(MODEL_SLIM, btn -> this.select(Skin.Model.SLIM))
                            .size(MODEL_BUTTON_WIDTH, 20).build(),
                    1, 1);
        }

        if (!this.wantsModel && this.model == null) {
            // add cape model widget
            root.addChild(ModelPreview.capeWithSkin(
                        Mod.override(ProfileHelper.user()).withCape(this.texture),
                        SKIN_SCALE, this.minecraft),
                    LayoutSettings.defaults().alignHorizontallyCenter());
        }

        this.root.visitWidgets(this::addRenderableWidget);
        this.root.arrangeElements();

        this.root.setX((this.width - root.getWidth()) / 2);
        this.root.setY((this.height - this.getContentHeight()) / 2 + this.getContentOffset());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        this.message.renderCentered(graphics, this.width / 2, this.getMessageY());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
            if (this.wantsName && !this.wantsModel) {
                this.nameEntered();
                return true;
            }
        }
        return false;
    }

    private void select(Skin.Model model) {
        String name = this.wantsName ? this.nameInput.getValue() : null;
        this.callback.receive(name, model);
        this.minecraft.setScreen(this.parent);
    }

    private void nameEntered() {
        this.callback.receive(this.nameInput.getValue(), null);
        this.minecraft.setScreen(this.parent);
    }

    protected static Component getMessageStatic(boolean wantsName, boolean wantsModel) {
        return wantsName && wantsModel
                ? INPUT_NAME_AND_MODEL
                : wantsName
                        ? INPUT_NAME
                        : INPUT_MODEL;
    }

    protected Component getMessage() {
        return getMessageStatic(this.wantsName, this.wantsModel);
    }

    private int getMessagesHeight() {
        return this.message.getLineCount() * 9;
    }

    private int getMessageY() {
        return (this.height - this.getContentHeight()) / 2;
    }

    private int getContentOffset() {
        return this.getMessagesHeight() + PAD;
    }

    private int getContentHeight() {
        return this.getContentOffset() + this.root.getHeight();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    public interface OverrideInfoCallback {
        void receive(String name, Skin.Model model);
    }
}
