package net.orifu.skin_overrides.screen;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.library.CapeLibrary;
import net.orifu.skin_overrides.library.SkinLibrary;
import net.orifu.skin_overrides.library.SkinLibrary.SkinEntry;
import net.orifu.skin_overrides.screen.widget.ModelPreviewWidget;
import net.orifu.skin_overrides.texture.LocalPlayerTexture;
import net.orifu.skin_overrides.texture.LocalSkinTexture;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.util.Util;
import net.orifu.xplat.CommonTexts;
import net.orifu.xplat.gui.GuiGraphics;
import net.orifu.xplat.gui.LayoutSettings;
import net.orifu.xplat.gui.Screen;
import net.orifu.xplat.gui.widget.ButtonWidget;
import net.orifu.xplat.gui.widget.CyclingButtonWidget;
import net.orifu.xplat.gui.widget.FrameWidget;
import net.orifu.xplat.gui.widget.LinearLayoutWidget;
import net.orifu.xplat.gui.widget.TextFieldWidget;
import net.orifu.xplat.gui.widget.TextWidget;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LibraryScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.library.title");

    private static final int OPTIONS_PAD = 24;
    private static final int OPTIONS_WIDTH = 150;

    public final OverrideManager ov;
    @Nullable
    private final Screen parent;
    @Nullable
    private final Consumer<LibraryEntry> callback;

    public final Skin userSkin = Skin.fromProfile(ProfileHelper.user());

    private LibraryListWidget libraryList;

    private TextFieldWidget searchBox;
    @Nullable
    private ModelPreviewWidget entryPreview;
    @Nullable
    private TextFieldWidget nameField;

    @Nullable
    protected LibraryListEntry selectedEntry;

    protected boolean showAttachment = true;
    protected boolean showElytra = false;

    @Nullable
    private CompletableFuture<Skin> adding;
    @Nullable
    private GameProfile addingProfile;

    public LibraryScreen(OverrideManager ov, @Nullable Screen parent, @Nullable Consumer<LibraryEntry> callback) {
        super(TITLE);

        this.ov = ov;
        this.parent = parent;
        this.callback = callback;
    }

    public LibraryScreen(OverrideManager ov, @Nullable Screen parent) {
        this(ov, parent, null);
    }

    @Override
    protected void init() {
        if (this.libraryList == null) {
            this.libraryList = new LibraryListWidget(this, this.ov);
        }

        if (this.searchBox == null) {
            this.searchBox = new TextFieldWidget(this.textRenderer, 200, 20,
                    Text.translatable("skin_overrides.input.search"));
            this.searchBox.setHint(Text.translatable("skin_overrides.input.search.hint"));
            this.searchBox.setChangedListener(query -> {
                this.libraryList.filter(query);
                this.clearAndInit();
            });
            this.searchBox.setMaxLength(100);
        }

        int libraryListWidth = this.selectedEntry == null ? this.width : this.width - OPTIONS_WIDTH - OPTIONS_PAD;

        var root = LinearLayoutWidget.createVertical();

        root.add(new TextWidget(TITLE, this.textRenderer),
                LayoutSettings.create().alignHorizontallyCenter().setTopPadding(8).setBottomPadding(5));

        var search = root.add(LinearLayoutWidget.createHorizontal(),
                LayoutSettings.create().alignHorizontallyCenter().setBottomPadding(6));
        search.add(this.searchBox);
        search.add(ButtonWidget
                .builder(Text.translatable("skin_overrides.library.search_add"), btn -> this.addFromSearch())
                .width(60).build());

        var body = root.add(LinearLayoutWidget.createHorizontal());
        this.libraryList.add(body::add, this::addDrawableSelectableElement);
        this.libraryList.setDimensions(libraryListWidth, this.height - 8 - 9 - 5 - 20 - 6 - 33);

        if (this.selectedEntry != null) {
            var controlsFrame = body.add(new FrameWidget(OPTIONS_WIDTH + OPTIONS_PAD, 0));
            var controls = controlsFrame.add(LinearLayoutWidget.createVertical().setSpacing(2));
            int previewScale = PlayerSkinRenderer.HEIGHT * 5 + 180 < this.height ? 5
                    : PlayerSkinRenderer.HEIGHT * 4 + 180 < this.height ? 4
                    : PlayerSkinRenderer.HEIGHT * 3 + 180 < this.height ? 3 : 2;

            // library entry preview
            this.entryPreview = controls.add(this.ov.skin
                            ? ModelPreviewWidget.skin(null, previewScale, this.client)
                            : ModelPreviewWidget.cape(null, previewScale, this.client),
                    LayoutSettings.create().alignHorizontallyCenter());

            // padding
            controls.add(new FrameWidget(0, 4));

            // name input
            if (this.nameField == null) {
                this.nameField = new TextFieldWidget(this.textRenderer, OPTIONS_WIDTH, 20,
                        Text.translatable("skin_overrides.library.input.name"));
                this.nameField.setMaxLength(32);
                this.nameField.setChangedListener(this::renameEntry);
                this.nameField.setText(this.selectedEntry.entry.getName());
            }
            controls.add(this.nameField, LayoutSettings.create().alignHorizontallyCenter());

            var smallControls = controls.add(LinearLayoutWidget.createHorizontal());

            // previous entry
            int index = this.libraryList.indexOf(this.selectedEntry);
            boolean isFirst = index == 0;
            smallControls.add(ButtonWidget.builder(Text.literal("<"),
                    btn -> this.libraryList.moveSelection(-1)).width(20)
                    .tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.back")))
                    .build()).active = !isFirst;
            // swap this and previous entry
            smallControls.add(ButtonWidget.builder(Text.literal("<<"), btn -> {
                this.libraryList.move(index, index - 1);
                this.libraryList.ensureVisible(this.selectedEntry);
                this.clearAndInit();
            }).width(25).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.move_back")))
                    .build()).active = !isFirst;

            // use this entry
            int mainControlWidth = (OPTIONS_WIDTH - 40 - 50) / 2;
            smallControls.add(ButtonWidget.builder(Text.literal("+"), btn -> {
                if (this.callback != null) {
                    this.callback.accept(this.selectedEntry.entry);
                }
                this.client.setScreen(this.parent);
            }).width(mainControlWidth).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.use")))
                    .build()).active = this.callback != null;

            // remove this entry
            smallControls.add(ButtonWidget.builder(Text.literal("-"), btn ->
                    this.libraryList.removeFromLibrary()
            ).width(mainControlWidth).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.remove")))
                    .build()).active = this.callback != null;

            // swap this and next entry
            boolean isLast = index == this.libraryList.children().size() - 1;
            smallControls.add(ButtonWidget.builder(Text.literal(">>"), btn -> {
                this.libraryList.move(index, index + 1);
                this.libraryList.ensureVisible(this.selectedEntry);
                this.clearAndInit();
            }).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.move_next"))).width(25)
                    .build()).active = !isLast;
            // next entry
            smallControls.add(ButtonWidget.builder(Text.literal(">"),
                    btn -> this.libraryList.moveSelection(1))
                    .tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.next"))).width(20)
                    .build()).active = !isLast;

            // preview options
            controls.add(new FrameWidget(0, 4));
            if (this.ov.skin) {
                controls.add(CyclingButtonWidget
                        .builder(option -> option.equals(0) ? CommonTexts.OFF : option.equals(1)
                                ? Text.translatable("skin_overrides.model.cape")
                                : Text.translatable("skin_overrides.model.elytra"))
                        .values(0, 1, 2).initially(this.showAttachment ? this.showElytra ? 2 : 1 : 0)
                        .build(
                                0, 0, OPTIONS_WIDTH, 20,
                                Text.translatable("skin_overrides.library.input.accessory"),
                                (btn, opt) -> {
                                    this.showAttachment = !opt.equals(0);
                                    this.showElytra = opt.equals(2);
                                }));
            } else {
                controls.add(CyclingButtonWidget
                        .builder(option -> (Boolean) option ? CommonTexts.ON : CommonTexts.OFF)
                        .values(true, false).initially(this.showAttachment)
                        .build(
                                0, 0, OPTIONS_WIDTH, 20,
                                Text.translatable("skin_overrides.library.input.show_skin"),
                                (btn, opt) -> this.showAttachment = (boolean) opt));
                controls.add(CyclingButtonWidget
                        .builder(option -> (Boolean) option
                                ? Text.translatable("skin_overrides.model.elytra")
                                : Text.translatable("skin_overrides.model.cape"))
                        .values(true, false).initially(this.showElytra)
                        .build(
                                0, 0, OPTIONS_WIDTH, 20,
                                Text.translatable("skin_overrides.library.input.model"),
                                (btn, opt) -> this.showElytra = (boolean) opt));
            }
        }

        var footer = root.add(new FrameWidget(this.width, 33));
        footer.add(ButtonWidget.builder(CommonTexts.BACK, btn -> this.closeScreen()).build());

        root.visitWidgets(this::addDrawableSelectableElement);
        root.arrangeElements();
        this.setFocusedChild(this.searchBox);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        //? if <1.20.2
        /*this.renderBackground(graphics.portable());*/
        this.renderSuper(graphics, mouseX, mouseY, delta);

        if (this.selectedEntry != null) {
            if (this.selectedEntry.entry instanceof SkinEntry entry) {
                this.entryPreview.renderer.setSkin(entry.toSkin());
                this.entryPreview.renderer.setCape(this.userSkin.capeTexture());
                this.entryPreview.renderer.showCape(this.showAttachment);
            } else {
                this.entryPreview.renderer.setSkin(this.userSkin);
                this.entryPreview.renderer.setCape(this.selectedEntry.entry.getTexture());
                this.entryPreview.renderer.showSkin(this.showAttachment);
            }

            this.entryPreview.renderer.showElytra(this.showElytra);
        }

        // empty list text
        if (this.libraryList.children().isEmpty()) {
            graphics.drawCenteredShadowedText(this.textRenderer, Text.translatable("skin_overrides.library.empty"),
                    this.libraryList.getX() + this.libraryList.getWidth() / 2,
                    this.libraryList.getY() + this.libraryList.getHeight() / 2 - 4, 0xaaaaaa);
        }

        // the skin won't be properly loaded for a few frames
        if (this.adding != null && this.adding.isDone()) {
            var skin = this.adding.getNow(null);
            String name = this.addingProfile.getName();
            if (this.ov.skin) {
                ((SkinLibrary) this.ov.library()).createSigned(name, skin.texture(), skin.model(), this.addingProfile);
            } else {
                Identifier cape = skin.capeTexture();
                if (cape != null) {
                    ((CapeLibrary) this.ov.library()).create(name, cape);
                } else {
                    this.toast(
                            Text.translatable("skin_overrides.no_cape.title"),
                            Text.translatable("skin_overrides.no_cape.description", name)
                    );
                }
            }

            this.adding = null;
            this.libraryList.reload();
            LibraryListEntry entry = this.libraryList.getFirstChild();
            this.libraryList.setSelected(entry);
            this.selectEntry(entry);
        }
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox.isActive() && keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.addFromSearch();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public void selectEntry(@Nullable LibraryListEntry entry) {
        this.selectedEntry = entry;
        this.nameField = null;
        this.clearAndInit();

        if (entry != null) {
            this.libraryList.ensureVisible(entry);
        }
    }

    public void renameEntry(String newName) {
        if (!newName.equals(this.selectedEntry.entry.getName())) {
            this.ov.library().rename(this.selectedEntry.entry, newName);
            this.clearAndInit();
            this.setFocusedChild(this.nameField);
        }
    }

    @Override
    public void filesDragged(List<Path> paths) {
        if (paths.isEmpty()) {
            return;
        }
        Path path = paths.get(0);
        if (!path.toFile().isFile() || !FilenameUtils.isExtension(path.toFile().getName(), "png")) {
            return;
        }
        String guessedName = path.toFile().getName().replace(".png", "").replace("_", " ");

        if (this.ov.skin) {
            // open name and model input screen
            this.client.setScreen(OverrideInfoEntryScreen.getNameAndModel(this,
                    Util.texture(new LocalSkinTexture(path.toFile())), guessedName,
                    (name, model) -> {
                        // add skin
                        ((SkinLibrary) this.ov.library()).create(name, path, model);
                        this.libraryList.reload();
                        this.clearAndInit();
                    }));
        } else {
            // open name input screen
            this.client.setScreen(OverrideInfoEntryScreen.getName(this,
                    Util.texture(new LocalPlayerTexture(path.toFile())), guessedName,
                    name -> {
                        // add cape
                        ((CapeLibrary) this.ov.library()).create(name, path);
                        this.libraryList.reload();
                        this.clearAndInit();
                    }));
        }
    }

    private void addFromSearch() {
        String name = this.searchBox.getText();
        this.searchBox.setText("");

        ProfileHelper.idToSecureProfile(name).thenAccept(profile -> {
            if (profile.isEmpty()) {
                this.toast(
                        Text.translatable("skin_overrides.no_profile.title", this.searchBox.getText()),
                        Text.translatable("skin_overrides.no_profile.description"));
            } else {
                // i tried getting the skin asynchronously here... don't do that.
                // i guess it needs to be on the render thread to be added?
                this.adding = this.ov.skin
                        ? Skin.fetchSkin(profile.get())
                        : Skin.fetchCape(profile.get());
                this.addingProfile = profile.get();
            }
        });
    }

    private void toast(Text title, Text description) {
        //? if >=1.21.2 {
        /*SystemToast.show(this.client.method_1566(), SystemToast.Id.PACK_LOAD_FAILURE, title, description);
        *///?} else {
        this.client.getToastManager().add(new SystemToast(
                SystemToast.
                        /*? if >=1.20.6 {*/ Id.PACK_LOAD_FAILURE
                        /*?} else if =1.20.4 {*/ /*C_ozahoshp.field_47585
                        *//*?} else >>*/ /*Type.PACK_LOAD_FAILURE*/ ,
                title, description
        ));
        //?}
    }
}
