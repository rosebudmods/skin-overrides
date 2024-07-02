package net.orifu.skin_overrides.screen;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.authlib.GameProfile;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.xplat.gui.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.ScreenArea;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.HeaderBar;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.override.Overridden;
import net.orifu.skin_overrides.override.LibraryCapeOverride.CapeEntry;
import net.orifu.skin_overrides.override.LibrarySkinOverride.SkinEntry;
import net.orifu.skin_overrides.texture.LocalSkinTexture;
import net.orifu.skin_overrides.util.PlayerCapeRenderer;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.skin_overrides.util.Util;
import net.orifu.skin_overrides.xplat.gui.widget.ButtonWidget;
import net.orifu.skin_overrides.xplat.gui.widget.FrameWidget;
import net.orifu.skin_overrides.xplat.gui.widget.GridWidget;
import net.orifu.skin_overrides.xplat.gui.LayoutSettings;
import net.orifu.skin_overrides.xplat.gui.widget.HeaderFooterLayoutWidget;
import net.orifu.skin_overrides.xplat.gui.widget.LinearLayoutWidget;
import net.orifu.skin_overrides.xplat.gui.widget.TextFieldWidget;
import net.orifu.skin_overrides.xplat.gui.widget.TextWidget;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

public class SkinOverridesScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.title");
    private static final Text SKIN_TITLE = Text.translatable("skin_overrides.title.skin");
    private static final Text CAPE_TITLE = Text.translatable("skin_overrides.title.cape");

    private static final int PREVIEW_SCALE = 3;

    private final TabManager tabManager = new TabManager(this::addDrawableSelectableElement, this::remove);

    @Nullable
    private final net.minecraft.client.gui.screen.Screen parent;

    private HeaderFooterLayoutWidget layout;
    private HeaderBar header;
    private GridWidget grid;

    private PlayerListWidget playerList;
    private TextFieldWidget searchBox;

    private FrameWidget configFrame;
    @Nullable
    private FrameWidget previewFrame;

    private Overridden ov = Mod.SKINS;
    @Nullable
    private GameProfile selectedProfile;

    public SkinOverridesScreen(@Nullable net.minecraft.client.gui.screen.Screen parent) {
        super(TITLE);

        this.parent = parent;
    }

    @Override
    protected void init() {
        // add tabs header
        this.header = this.addDrawableSelectableElement(
                HeaderBar.builder(this.tabManager, this.width)
                        .tabs(new DummyTab(Mod.SKINS), new DummyTab(Mod.CAPES))
                        .build());
        this.header.setFocusedTab(this.ov.skin() ? 0 : 1, false);

        // add main content
        this.layout = new HeaderFooterLayoutWidget(this);
        this.grid = this.layout.addToContents(new GridWidget());
        this.initContent();

        // add footer
        var footer = this.layout.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(8));

        // library button
        footer.add(ButtonWidget
                .builder(Text.translatable("skin_overrides.library.open"),
                        (btn) -> this.client.setScreen(new LibraryScreen(this.ov, this, this::pickedFromLibrary)))
                .build());

        // done button
        footer.add(ButtonWidget.builder(CommonTexts.DONE, (btn) -> this.closeScreen()).build());

        // finish
        this.layout.visitWidgets(this::addDrawableSelectableElement);
        this.repositionElements();
    }

    protected void initContent() {
        var helper = this.grid.createAdditionHelper(2);

        if (this.playerList == null || this.playerList.ov != this.ov) {
            this.playerList = new PlayerListWidget(this, this.ov);
            this.searchBox = new TextFieldWidget(this.textRenderer, 200, 20,
                    Text.translatable("skin_overrides.input.search"));
            this.searchBox.setChangedListener(this.playerList::filter);
        }

        // add player list
        var listWrapper = helper.add(LinearLayoutWidget.createVertical().setSpacing(6));
        listWrapper.add(this.searchBox, LayoutSettings.create().alignHorizontallyCenter().setTopPadding(5));
        this.setFocusedChild(this.searchBox);
        this.playerList.add(listWrapper::add, this::addDrawableSelectableElement);

        // add configuration
        this.configFrame = helper.add(new FrameWidget());
        var configCols = configFrame.add(LinearLayoutWidget.createHorizontal()).setSpacing(8);
        GridWidget config = (GridWidget) configCols.add(new GridWidget()).setSpacing(4);
        if (this.selectedProfile == null) {
            this.configFrame.add(new TextWidget(Text.translatable("skin_overrides.no_selection"), this.textRenderer));
        } else {
            this.initConfig(config);

            this.previewFrame = configCols.add(
                    new FrameWidget(PlayerSkinRenderer.WIDTH * PREVIEW_SCALE,
                            PlayerSkinRenderer.HEIGHT * PREVIEW_SCALE),
                    LayoutSettings.create().alignHorizontallyRight().alignVerticallyCenter());
        }
    }

    protected void initConfig(GridWidget config) {
        config.add(new TextWidget(Text.translatable(this.ov.skin()
                ? "skin_overrides.add_skin"
                : "skin_overrides.add_cape"),
                this.textRenderer), 0, 0);

        // pick from library button
        config.add(ButtonWidget
                .builder(Text.translatable("skin_overrides.library.pick"),
                        (btn) -> this.client.setScreen(new LibraryScreen(this.ov, this, this::pickedFromLibrary)))
                .width(120).build(), 1, 0);

        // add to library button
        config.add(ButtonWidget.builder(Text.translatable("skin_overrides.library.add"), (btn) -> this.addToLibrary())
                .width(120).build(), 2, 0).active = !this.ov.library().hasOverride(this.selectedProfile);

        // remove override button
        config.add(ButtonWidget
                .builder(Text.translatable("skin_overrides.remove"), (btn) -> this.removeOverride())
                .width(120)
                .build(), 3, 0).active = this.ov.hasOverride(this.selectedProfile);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        //? if <1.20.2
        /*this.renderBackground(graphics);*/
        super.render(graphics, mouseX, mouseY, delta);

        if (this.selectedProfile != null) {
            // draw skin/cape preview
            Skin skin = Mod.override(this.selectedProfile);
            if (this.ov.skin()) {
                PlayerSkinRenderer.draw(graphics, skin, this.previewFrame.getX(), this.previewFrame.getY(),
                        PREVIEW_SCALE);
            } else {
                PlayerCapeRenderer.draw(graphics, skin, this.previewFrame.getX(), this.previewFrame.getY(),
                        PREVIEW_SCALE);
            }
        }
    }

    @Override
    public void repositionElements() {
        // reposition tab header
        this.header.setWidth(this.width);
        this.header.arrangeElements();

        int hh = this.header.getArea().bottom();
        int fh = this.layout.getFooterHeight();
        int height = this.height - hh - fh;

        // set main content size
        this.searchBox.setWidth(Math.min(200, this.width / 2 - 16));
        this.playerList.setDimensions(this.width / 2, height - 5 - 20 - 6);
        this.configFrame.setMinDimensions(this.width / 2, height);

        // reposition layout
        this.layout.setHeaderHeight(0);
        this.layout.arrangeElements();
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    public Overridden overridden() {
        return this.ov;
    }

    public void setOverridden(Overridden ov) {
        if (this.ov != ov) {
            this.ov = ov;
            this.selectedProfile = null;
            this.clearAndInit();
        }
    }

    public void selectPlayer(PlayerListEntry entry) {
        this.playerList.setSelected(entry);
        this.selectedProfile = entry.profile;
        this.clearAndInit();
    }

    protected void upgradeProfile() {
        // get the full profile so we have the player's skin/cape (if any)
        this.selectedProfile = this.playerList.getSelectedOrNull().upgrade();
    }

    public void pickedFromLibrary(LibraryEntry entry) {
        var profile = this.selectedProfile != null ? this.selectedProfile : ProfileHelper.user();
        this.ov.library().addOverride(profile, entry);
        this.clearAndInit();
    }

    protected void addToLibrary() {
        String guessedName = this.selectedProfile.getName();

        Skin playerSkin = Mod.override(this.selectedProfile);
        var texture = this.ov.skin() ? playerSkin.texture() : playerSkin.capeTexture();
        Consumer<String> callback = name -> {
            // create the library entry
            Optional<LibraryEntry> entry = this.ov.skin()
                    ? SkinEntry.create(name, texture, playerSkin.model()).map(e -> (LibraryEntry) e)
                    : CapeEntry.create(name, texture).map(e -> (LibraryEntry) e);

            // if this is an override, replace it with the library version
            if (this.ov.hasOverride(this.selectedProfile) && entry.isPresent()) {
                this.ov.removeOverride(this.selectedProfile);
                this.ov.library().addOverride(this.selectedProfile, entry.get());
                this.clearAndInit();
            }
        };

        this.client.setScreen(this.ov.skin()
                ? OverrideInfoEntryScreen.getName(this, texture, playerSkin.model(), guessedName, callback)
                : OverrideInfoEntryScreen.getName(this, texture, guessedName, callback));
    }

    public void removeOverride() {
        this.ov.removeOverride(this.selectedProfile);
        this.upgradeProfile(); // get player's actual skin/cape
        this.clearAndInit(); // update remove buttons
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

        GameProfile profile = this.selectedProfile != null ? this.selectedProfile : ProfileHelper.user();

        if (this.ov.skin()) {
            // open model selection screen
            this.client.setScreen(OverrideInfoEntryScreen.getModel(this,
                    Util.texture(new LocalSkinTexture(path.toFile())),
                    model -> {
                        this.ov.local().copyOverride(profile, path, model);
                        this.clearAndInit();
                    }));
        } else {
            this.ov.local().copyOverride(profile, path, null);
            this.clearAndInit();
        }
    }

    class DummyTab implements Tab {
        public final Text title;
        public final Overridden ov;

        public DummyTab(Overridden ov) {
            this.title = ov.skin() ? SKIN_TITLE : CAPE_TITLE;
            this.ov = ov;
        }

        @Override
        public Text getTitle() {
            return this.title;
        }

        @Override
        public void visitChildren(Consumer<ClickableWidget> consumer) {
            // when selected
            SkinOverridesScreen.this.setOverridden(this.ov);
        }

        @Override
        public void refreshLayout(ScreenArea area) {
        }
    }
}
