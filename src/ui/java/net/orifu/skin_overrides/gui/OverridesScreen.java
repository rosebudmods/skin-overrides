package net.orifu.skin_overrides.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.OverrideManager;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.library.CapeLibrary;
import net.orifu.skin_overrides.library.SkinLibrary;
import net.orifu.skin_overrides.override.LibraryOverrider.LibraryOverride;
import net.orifu.skin_overrides.gui.components.ModelPreview;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.util.Util;
import net.orifu.xplat.GuiHelper;
import net.orifu.xplat.gui.GuiGraphics;
import net.orifu.xplat.gui.Screen;
import net.orifu.xplat.gui.components.LinearLayout;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class OverridesScreen extends Screen {
    private static final Component TITLE = Component.translatable("skin_overrides.title");
    private static final Component SKIN_TITLE = Component.translatable("skin_overrides.title.skin");
    private static final Component CAPE_TITLE = Component.translatable("skin_overrides.title.cape");

    private static final int PREVIEW_SCALE = 3;

    private final TabManager tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);

    @Nullable
    private final net.minecraft.client.gui.screens.Screen parent;

    private HeaderAndFooterLayout layout;
    private TabNavigationBar header;
    private GridLayout grid;

    private OverridesSelectionList overrideList;
    private EditBox searchBox;

    private ModelPreview modelPreview;
    private FrameLayout configFrame;

    private OverrideManager ov = Mod.SKINS;
    @Nullable
    private GameProfile selectedProfile;

    public OverridesScreen(@Nullable net.minecraft.client.gui.screens.Screen parent) {
        super(TITLE);

        this.parent = parent;
    }

    @Override
    protected void init() {
        // add tabs header
        this.header = this.addRenderableWidget(
                TabNavigationBar.builder(this.tabManager, this.width)
                        .addTabs(new DummyTab(Mod.SKINS), new DummyTab(Mod.CAPES))
                        .build());
        this.header.selectTab(this.ov.skin ? 0 : 1, false);

        // add main content
        this.layout = new HeaderAndFooterLayout(this);
        this.grid = this.layout.addToContents(new GridLayout());
        this.initContent();

        // add footer
        var footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));

        // library button
        footer.addChild(Button.builder(Component.translatable("skin_overrides.library.open"),
                        (btn) -> this.minecraft.setScreen(new LibraryScreen(this.ov, this, this::pickedFromLibrary))).build());

        // done button
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, (btn) -> this.onClose()).build());

        // finish
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected void initContent() {
        var helper = this.grid.createRowHelper(2);

        if (this.overrideList == null || this.overrideList.ov != this.ov) {
            this.overrideList = new OverridesSelectionList(this, this.ov);
            this.searchBox = GuiHelper.editBox(this.font, 200, 20,
                    Component.translatable("skin_overrides.input.search"));
            this.searchBox.setResponder(this.overrideList::filter);
        }

        // add player list
        var listWrapper = helper.addChild(LinearLayout.vertical().spacing(6));
        var searchWrapper = listWrapper.addChild(LinearLayout.horizontal(), LayoutSettings.defaults().alignHorizontallyCenter().paddingTop(5));
        this.setFocused(this.searchBox);
        this.overrideList.addEntry(listWrapper::addChild, this::addRenderableWidget);

        searchWrapper.addChild(this.searchBox);
        searchWrapper.addChild(Button.builder(Component.literal("+"), btn -> this.addOverrideFromSearch())
                .tooltip(Tooltip.create(Component.translatable("skin_overrides.add_override")))
                .width(20).build());

        // add configuration
        this.configFrame = helper.addChild(new FrameLayout());
        var configCols = configFrame.addChild(LinearLayout.horizontal().spacing(12));
        GridLayout config = configCols.addChild(new GridLayout().spacing(4));
        if (this.selectedProfile == null) {
            this.configFrame.addChild(new StringWidget(Component.translatable("skin_overrides.no_selection"), this.font));
        } else {
            this.initConfig(config);

            Skin overriddenSkin = Mod.override(this.selectedProfile);
            if (this.selectedProfile != null) {
                this.modelPreview = configCols.addChild(this.ov.skin
                                ? ModelPreview.skin(overriddenSkin, PREVIEW_SCALE, this.minecraft)
                                : ModelPreview.capeWithSkin(overriddenSkin, PREVIEW_SCALE, this.minecraft),
                        LayoutSettings.defaults().alignHorizontallyRight().alignVerticallyMiddle());
            }
        }
    }

    protected void initConfig(GridLayout config) {
        // make sure the latest overrides are loaded before getting them
        Mod.SKINS.update();
        Mod.CAPES.update();

        var override = this.ov.get(this.selectedProfile);

        config.addChild(new StringWidget(Component.translatable(this.ov.skin
                ? "skin_overrides.add_skin"
                : "skin_overrides.add_cape"),
                this.font), 0, 0);

        // override from library button
        config.addChild(Button.builder(Component.translatable("skin_overrides.library.pick"),
                        btn -> this.minecraft.setScreen(new LibraryScreen(this.ov, this, this::pickedFromLibrary)))
                .width(120).build(), 1, 0);

        // remove override button
        config.addChild(Button.builder(Component.translatable("skin_overrides.remove"), btn -> this.removeOverride())
                .width(120).build(), 2, 0).active = override.isPresent();

        // add to library button
        config.addChild(Button.builder(Component.translatable("skin_overrides.library.add"), btn -> this.addToLibrary())
                .width(120).build(), 4, 0).active = !override.map(ov -> ov instanceof LibraryOverride).orElse(false);

        // change skin button
        if (this.selectedProfile.equals(ProfileHelper.user())) {
            config.addChild(Button.builder(Component.translatable("skin_overrides.change_skin"),
                            btn -> this.minecraft.setScreen(new SkinChangeInfoScreen(this)))
                    .width(120).build(), 5, 0).active = override.isPresent();
        }
    }

    @Override
    public void tick() {
        super.tick();

        this.overrideList.tick();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        //? if <1.20.2
        /*this.renderBackground(graphics.portable());*/
        this.renderSuper(graphics, mouseX, mouseY, delta);

        // ensure displayed skin is up to date (e.g. if just loaded)
        if (this.selectedProfile != null) {
            this.modelPreview.renderer.setSkin(Mod.override(this.selectedProfile));
        }
    }

    @Override
    public void repositionElements() {
        // reposition tab header
        this.header.setWidth(this.width);
        this.header.arrangeElements();

        int hh = this.header.getRectangle().bottom();
        int fh = this.layout.getFooterHeight();
        int height = this.height - hh - fh;

        // set main content size
        this.searchBox.setWidth(Math.min(200, this.width / 2 - 36));
        this.overrideList.setSize(this.width / 2, height - 5 - 20 - 6);
        this.configFrame.setMinDimensions(this.width / 2, height);

        // reposition layout
        this.layout.setHeaderHeight(0);
        this.layout.arrangeElements();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox.isActive() && keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.addOverrideFromSearch();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    public OverrideManager overrideManager() {
        return this.ov;
    }

    public void setOverrideManager(OverrideManager ov) {
        if (this.ov != ov) {
            this.ov = ov;
            this.selectedProfile = null;
            this.rebuildWidgets();
        }
    }

    public void reload() {
        this.rebuildWidgets();
    }

    public void selectPlayer(OverrideListEntry entry) {
        this.overrideList.setSelected(entry);
        this.overrideList.ensureVisible(entry);
        this.selectedProfile = entry.profile;

        // if we aren't overriding this player, or we're looking
        // at capes, fetch this player's skin
        if (!this.ov.has(this.selectedProfile) || !this.ov.skin) {
            this.upgradeProfile();
            entry.profile = this.selectedProfile;
        }

        this.rebuildWidgets();
    }

    protected void upgradeProfile() {
        // get the full profile so we have the player's skin/cape (if any)
        this.selectedProfile = this.overrideList.getSelected().upgrade();
    }

    public void pickedFromLibrary(LibraryEntry entry) {
        var profile = this.selectedProfile != null ? this.selectedProfile : ProfileHelper.user();
        this.ov.addOverride(profile, entry);
        this.rebuildWidgets();
    }

    protected void addOverrideFromSearch() {
        if (this.searchBox.getValue().isBlank()) {
            return;
        }

        // i tried to make this asynchronous and got ConcurrentModificationException
        GameProfile profile = ProfileHelper.idToBasicProfileSync(this.searchBox.getValue());
        this.searchBox.setValue("");
        this.selectPlayer(this.overrideList.addEntry(profile));
    }

    protected void addToLibrary() {
        String guessedName = this.selectedProfile.getName();

        Skin playerSkin = Mod.override(this.selectedProfile);
        var texture = this.ov.skin ? playerSkin.texture() : playerSkin.capeTexture();
        boolean hasOverride = Mod.overrideSkin(this.selectedProfile).isPresent();
        Consumer<String> callback = name -> {
            // get secure profile if we're adding a skin with no override
            Optional<GameProfile> maybeSecureProfile = this.ov.skin && !hasOverride
                    ? ProfileHelper.uuidToSecureProfile(this.selectedProfile.getId())
                    : Optional.empty();

            // create the library entry
            Optional<LibraryEntry> entry = maybeSecureProfile
                    .map(profile -> ((SkinLibrary) this.ov.library()).createSigned(name, texture, playerSkin.model(), profile).map(e -> (LibraryEntry) e))
                    .orElseGet(() -> this.ov.skin
                            ? ((SkinLibrary) this.ov.library()).create(name, texture, playerSkin.model()).map(e -> e)
                            : ((CapeLibrary) this.ov.library()).create(name, texture).map(e -> e));

            // if this is an override, replace it with the library version
            if (this.ov.has(this.selectedProfile) && entry.isPresent()) {
                this.ov.removeOverride(this.selectedProfile);
                this.ov.addOverride(this.selectedProfile, entry.get());
                this.rebuildWidgets();
            }
        };

        this.minecraft.setScreen(this.ov.skin
                ? OverrideInfoEntryScreen.getName(this, texture, playerSkin.model(), guessedName, callback)
                : OverrideInfoEntryScreen.getName(this, texture, guessedName, callback));
    }

    public void removeOverride() {
        this.ov.removeOverride(this.selectedProfile);
        this.upgradeProfile(); // get player's actual skin/cape
        this.rebuildWidgets(); // update remove buttons
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        if (paths.isEmpty()) {
            return;
        }
        Path path = paths.get(0);
        if (!path.toFile().isFile() || !FilenameUtils.isExtension(path.toFile().getName(), "png")) {
            return;
        }

        GameProfile profile = this.selectedProfile != null ? this.selectedProfile : ProfileHelper.user();

        if (this.ov.skin) {
            // open model selection screen
            this.minecraft.setScreen(OverrideInfoEntryScreen.getModel(this,
                    Util.texture(Util.skinTextureFromFile(path.toFile())),
                    model -> {
                        this.ov.copyOverride(profile, path, model);
                        this.rebuildWidgets();
                    }));
        } else {
            this.ov.copyOverride(profile, path, null);
            this.rebuildWidgets();
        }
    }

    class DummyTab implements Tab {
        public final Component title;
        public final OverrideManager ov;

        public DummyTab(OverrideManager ov) {
            this.title = ov.skin ? SKIN_TITLE : CAPE_TITLE;
            this.ov = ov;
        }

        @Override
        public Component getTabTitle() {
            return this.title;
        }

        @Override
        public void visitChildren(Consumer<AbstractWidget> consumer) {
            // when selected
            OverridesScreen.this.setOverrideManager(this.ov);
        }

        @Override
        public void doLayout(ScreenRectangle rect) {}
    }
}
