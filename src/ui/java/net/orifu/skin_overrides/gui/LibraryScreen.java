package net.orifu.skin_overrides.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.gui.components.ModelPreview;
import net.orifu.skin_overrides.library.CapeLibrary;
import net.orifu.skin_overrides.library.SkinLibrary;
import net.orifu.skin_overrides.library.SkinLibrary.SkinEntry;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.util.Toast;
import net.orifu.skin_overrides.util.Util;
import net.orifu.xplat.GuiHelper;
import net.orifu.xplat.gui.GuiGraphics;
import net.orifu.xplat.gui.Screen;
import net.orifu.xplat.gui.components.LinearLayout;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LibraryScreen extends Screen {
    private static final Component TITLE = Component.translatable("skin_overrides.library.title");

    private static final int OPTIONS_PAD = 24;
    private static final int OPTIONS_WIDTH = 150;

    public final OverrideManager ov;
    @Nullable
    private final Screen parent;
    @Nullable
    private final Consumer<LibraryEntry> callback;

    public final Skin userSkin = Mod.override(ProfileHelper.user());

    private LibrarySelectionGrid libraryList;

    private EditBox searchBox;
    @Nullable
    private ModelPreview entryPreview;
    @Nullable
    private EditBox nameField;

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

    @Override
    protected void init() {
        if (this.libraryList == null) {
            this.libraryList = new LibrarySelectionGrid(this, this.ov);
        }

        if (this.searchBox == null) {
            this.searchBox = GuiHelper.editBox(this.font, 200, 20,
                    Component.translatable("skin_overrides.input.search"));
            this.searchBox.setHint(Component.translatable("skin_overrides.input.search.hint"));
            this.searchBox.setResponder(query -> {
                this.libraryList.filter(query);
                this.rebuildWidgets();
            });
            this.searchBox.setMaxLength(100);
        }

        int libraryListWidth = this.selectedEntry == null ? this.width : this.width - OPTIONS_WIDTH - OPTIONS_PAD;

        var root = LinearLayout.vertical();

        root.addChild(new StringWidget(TITLE, this.font),
                LayoutSettings.defaults().alignHorizontallyCenter().paddingTop(8).paddingBottom(5));

        var search = root.addChild(LinearLayout.horizontal(),
                LayoutSettings.defaults().alignHorizontallyCenter().paddingBottom(6));
        search.addChild(this.searchBox);
        search.addChild(Button
                .builder(Component.translatable("skin_overrides.library.search_add"), btn -> this.addFromSearch())
                .width(60).build());

        var body = root.addChild(LinearLayout.horizontal());
        this.libraryList.addEntry(body::addChild, this::addRenderableWidget);
        this.libraryList.setSize(libraryListWidth, this.height - 8 - 9 - 5 - 20 - 6 - 33);

        if (this.selectedEntry != null) {
            var controlsFrame = body.addChild(new FrameLayout(OPTIONS_WIDTH + OPTIONS_PAD, 0));
            var controls = controlsFrame.addChild(LinearLayout.vertical().spacing(2));

            // believe me, "figure out a way to stop tiny player" is on my to-do
            int previewScale = PlayerSkinRenderer.HEIGHT * 5 + 210 < this.height ? 5
                    : PlayerSkinRenderer.HEIGHT * 4 + 210 < this.height ? 4
                    : PlayerSkinRenderer.HEIGHT * 3 + 210 < this.height ? 3
                    : PlayerSkinRenderer.HEIGHT * 2 + 210 < this.height ? 2 : 1;

            // library entry preview
            this.entryPreview = controls.addChild(this.ov.skin
                            ? ModelPreview.skin(null, previewScale, this.minecraft)
                            : ModelPreview.cape(null, previewScale, this.minecraft),
                    LayoutSettings.defaults().alignHorizontallyCenter());

            // padding
            controls.addChild(new FrameLayout(0, 4));

            // control layer 1
            var smallControls = controls.addChild(LinearLayout.horizontal());

            // previous entry
            int index = this.libraryList.indexOf(this.selectedEntry);
            boolean isFirst = index == 0;
            smallControls.addChild(Button.builder(Component.literal("<"),
                            btn -> this.libraryList.moveSelection(-1)).width(20)
                    .tooltip(Tooltip.create(Component.translatable("skin_overrides.library.input.back")))
                    .build()).active = !isFirst;

            // name input
            if (this.nameField == null) {
                this.nameField = GuiHelper.editBox(this.font, OPTIONS_WIDTH - 20 * 2, 20,
                        Component.translatable("skin_overrides.library.input.name"));
                this.nameField.setMaxLength(32);
                this.nameField.setResponder(this::renameEntry);
                this.nameField.setValue(this.selectedEntry.entry.getName());
            }
            smallControls.addChild(this.nameField, LayoutSettings.defaults().alignHorizontallyCenter());

            // next entry
            boolean isLast = index == this.libraryList.children().size() - 1;
            smallControls.addChild(Button.builder(Component.literal(">"),
                            btn -> this.libraryList.moveSelection(1))
                    .tooltip(Tooltip.create(Component.translatable("skin_overrides.library.input.next"))).width(20)
                    .build()).active = !isLast;

            // control layer 2
            var otherControls = controls.addChild(LinearLayout.horizontal());

            // swap this and previous entry
            otherControls.addChild(Button.builder(Component.literal("<<"), btn -> {
                this.libraryList.move(index, index - 1);
                this.libraryList.ensureVisible(this.selectedEntry);
                this.rebuildWidgets();
            }).width(20).tooltip(Tooltip.create(Component.translatable("skin_overrides.library.input.move_back")))
                    .build()).active = !isFirst;

            // use this entry
            otherControls.addChild(Button.builder(Component.translatable("skin_overrides.library.input.use"), btn -> {
                        if (this.callback != null) {
                            this.callback.accept(this.selectedEntry.entry);
                        }
                        this.minecraft.setScreen(this.parent);
                    })
                    .width(OPTIONS_WIDTH - 20 * 2)
                    .build()).active = this.callback != null;

            // swap this and next entry
            otherControls.addChild(Button.builder(Component.literal(">>"), btn -> {
                this.libraryList.move(index, index + 1);
                this.libraryList.ensureVisible(this.selectedEntry);
                this.rebuildWidgets();
            }).width(20).tooltip(Tooltip.create(Component.translatable("skin_overrides.library.input.move_next")))
                    .build()).active = !isLast;

            // preview options
            controls.addChild(new FrameLayout(0, 4));
            if (this.ov.skin) {
                controls.addChild(CycleButton
                        .builder(option -> option.equals(0) ? CommonComponents.OPTION_OFF : option.equals(1)
                                ? Component.translatable("skin_overrides.model.cape")
                                : Component.translatable("skin_overrides.model.elytra"))
                        .withValues(0, 1, 2).withInitialValue(this.showAttachment ? this.showElytra ? 2 : 1 : 0)
                        .create(
                                0, 0, OPTIONS_WIDTH, 20,
                                Component.translatable("skin_overrides.library.input.accessory"),
                                (btn, opt) -> {
                                    this.showAttachment = !opt.equals(0);
                                    this.showElytra = opt.equals(2);
                                }));
            } else {
                controls.addChild(CycleButton
                        .builder(option -> (Boolean) option ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF)
                        .withValues(true, false).withInitialValue(this.showAttachment)
                        .create(
                                0, 0, OPTIONS_WIDTH, 20,
                                Component.translatable("skin_overrides.library.input.show_skin"),
                                (btn, opt) -> this.showAttachment = (boolean) opt));
                controls.addChild(CycleButton
                        .builder(option -> (Boolean) option
                                ? Component.translatable("skin_overrides.model.elytra")
                                : Component.translatable("skin_overrides.model.cape"))
                        .withValues(true, false).withInitialValue(this.showElytra)
                        .create(
                                0, 0, OPTIONS_WIDTH, 20,
                                Component.translatable("skin_overrides.library.input.model"),
                                (btn, opt) -> this.showElytra = (boolean) opt));
            }

            // remove this entry
            controls.addChild(new FrameLayout(0, 4));
            controls.addChild(Button.builder(Component.translatable("skin_overrides.library.input.delete"), btn ->
                            this.libraryList.removeFromLibrary()
                    ).width(OPTIONS_WIDTH).build()).active = this.callback != null;
        }

        var footer = root.addChild(new FrameLayout(this.width, 33));
        footer.addChild(Button.builder(CommonComponents.GUI_BACK, btn -> this.onClose()).build());

        root.visitWidgets(this::addRenderableWidget);
        root.arrangeElements();
        this.setFocused(this.searchBox);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
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
            graphics.drawCenteredString(this.font, Component.translatable("skin_overrides.library.empty"),
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
                ResourceLocation cape = skin.capeTexture();
                if (cape != null) {
                    ((CapeLibrary) this.ov.library()).create(name, cape);
                } else {
                    Toast.show(Component.translatable("skin_overrides.no_cape.title"),
                            Component.translatable("skin_overrides.no_cape.description", name));
                }
            }

            this.adding = null;
            this.libraryList.reload();
            LibraryListEntry entry = this.libraryList.getFirstElement();
            this.libraryList.setSelected(entry);
            this.selectEntry(entry);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
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
        this.rebuildWidgets();

        if (entry != null) {
            this.libraryList.ensureVisible(entry);
        }
    }

    public void renameEntry(String newName) {
        if (!newName.equals(this.selectedEntry.entry.getName())) {
            this.ov.library().rename(this.selectedEntry.entry, newName);
            this.rebuildWidgets();
            this.setFocused(this.nameField);
        }
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        if (paths.isEmpty()) {
            return;
        }
        Path path = paths.get(0);
        if (!path.toFile().isFile()) {
            return;
        }
        String guessedName = path.toFile().getName().replace(".png", "").replace("_", " ");

        if (this.ov.skin) {
            // open name and model input screen
            Util.skinTextureFromFile(path.toFile()).ifPresentOrElse(
                    tex -> this.minecraft.setScreen(OverrideInfoEntryScreen.getNameAndModel(this,
                            Util.texture(tex), guessedName,
                            (name, model) -> {
                                // add skin
                                ((SkinLibrary) this.ov.library()).create(name, path, model);
                                this.libraryList.reload();
                                this.rebuildWidgets();
                            })),
                    Toast::showInvalidImage
            );
        } else {
            // open name input screen
            Util.textureFromFile(path.toFile()).ifPresentOrElse(
                    tex -> this.minecraft.setScreen(OverrideInfoEntryScreen.getName(this,
                            Util.texture(tex), guessedName,
                            name -> {
                                // add cape
                                ((CapeLibrary) this.ov.library()).create(name, path);
                                this.libraryList.reload();
                                this.rebuildWidgets();
                            })),
                    Toast::showInvalidImage
            );
        }
    }

    private void addFromSearch() {
        String name = this.searchBox.getValue();
        this.searchBox.setValue("");

        ProfileHelper.idToSecureProfile(name).thenAccept(profile -> {
            if (profile.isEmpty()) {
                Toast.show(Component.translatable("skin_overrides.no_profile.title", name),
                        Component.translatable("skin_overrides.no_profile.description"));
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
}
