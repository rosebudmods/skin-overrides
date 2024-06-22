package net.orifu.skin_overrides.screen;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenArea;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.HeaderBar;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.FrameWidget;
import net.minecraft.client.gui.widget.layout.GridWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LayoutSettings;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.override.Overridden;
import net.orifu.skin_overrides.util.PlayerCapeRenderer;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;

public class SkinOverridesScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.title");
    private static final Text SKIN_TITLE = Text.translatable("skin_overrides.title.skin");
    private static final Text CAPE_TITLE = Text.translatable("skin_overrides.title.cape");

    private static final int PREVIEW_SCALE = 3;

    // TODO: move this into an xplat helper?
    private static final Identifier FOOTER_SEPARATOR_TEXTURE = new Identifier("minecraft",
            "textures/gui/footer_separator.png");

    private final TabManager tabManager = new TabManager(this::addDrawableSelectableElement, (wg) -> this.remove(wg));

    @Nullable
    private final Screen parent;

    private HeaderFooterLayoutWidget layout;
    private HeaderBar header;
    private GridWidget grid;
    private PlayerListWidget playerList;
    private FrameWidget configFrame;

    @Nullable
    private FrameWidget previewFrame;

    private Overridden ov = Mod.SKINS;
    @Nullable
    private GameProfile selectedProfile;

    public SkinOverridesScreen(@Nullable Screen parent) {
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
        LinearLayoutWidget footer = this.layout.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(8));

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

        if (this.playerList != null && this.playerList.ov == this.ov) {
            // player list already exists
        } else {
            this.playerList = new PlayerListWidget(this, this.ov);
        }

        // add player list
        helper.add(this.playerList);

        // add configuration
        this.configFrame = helper.add(new FrameWidget());
        LinearLayoutWidget configCols = configFrame.add(LinearLayoutWidget.createHorizontal()).setSpacing(8);
        GridWidget config = configCols.add(new GridWidget()).setSpacing(4);
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
        config.add(ButtonWidget.builder(Text.translatable("skin_overrides.library.add"), (btn) -> {
        }).width(120).build(), 2, 0).active = !this.ov.library().hasOverride(this.selectedProfile);

        // remove override button
        config.add(ButtonWidget
                .builder(Text.translatable("skin_overrides.remove"), (btn) -> this.removeOverride())
                .width(120)
                .build(), 3, 0).active = this.ov.hasOverride(this.selectedProfile);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        RenderSystem.enableBlend();
        graphics.drawTexture(FOOTER_SEPARATOR_TEXTURE, 0,
                this.height - this.layout.getFooterHeight() - 2, 0, 0, this.width, 2, 32, 2);
        RenderSystem.disableBlend();

        if (this.selectedProfile != null) {
            // draw skin/cape preview
            PlayerSkin skin = Mod.getSkin(this.selectedProfile);
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

        // reposition main content area
        ScreenArea area = new ScreenArea(0, hh, this.width, this.height - fh - hh);
        FrameWidget.align(this.grid, area);
        this.playerList.setPosition(area.x(), area.y());
        this.playerList.setDimensions(area.width() / 2, area.height());
        this.configFrame.setMinDimensions(area.width() / 2, area.height());

        // reposition layout
        this.layout.setHeaderHeight(hh);
        this.layout.arrangeElements();
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    public Overridden overriden() {
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

    @SuppressWarnings("unchecked")
    public void pickedFromLibrary(LibraryEntry entry) {
        var profile = this.selectedProfile != null ? this.selectedProfile : this.client.method_53462();
        this.ov.library().addOverride(profile, entry);
        this.clearAndInit();
    }

    public void removeOverride() {
        this.ov.removeOverride(this.selectedProfile);
        this.upgradeProfile(); // get player's actual skin/cape
        this.clearAndInit(); // update remove buttons
    }

    @SuppressWarnings("unchecked")
    @Override
    public void filesDragged(List<Path> paths) {
        if (paths.size() == 0) {
            return;
        }
        Path path = paths.get(0);
        if (!path.toFile().isFile() || !FilenameUtils.isExtension(path.toFile().getName(), "png")) {
            return;
        }

        GameProfile profile = this.selectedProfile != null ? this.selectedProfile : this.client.method_53462();

        if (this.ov.skin()) {
            // open model selection screen
            this.client.setScreen(OverrideInfoEntryScreen.getModel(this, path, model -> {
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
