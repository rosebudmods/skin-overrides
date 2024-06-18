package net.orifu.skin_overrides.screen;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
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
import net.orifu.skin_overrides.Overrides;
import net.orifu.skin_overrides.SkinOverrides;
import net.orifu.skin_overrides.texture.LocalSkinTexture;
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

    private boolean isSkin = true;
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
                        .tabs(new DummyTab(true), new DummyTab(false))
                        .build());
        this.header.setFocusedTab(this.isSkin ? 0 : 1, false);

        // add main content
        this.layout = new HeaderFooterLayoutWidget(this);
        this.grid = this.layout.addToContents(new GridWidget().setColumnSpacing(16));
        this.initContent();

        // add footer
        LinearLayoutWidget footer = this.layout.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(5));

        // library button
        footer.add(ButtonWidget
                .builder(Text.translatable("skin_overrides.library.open"),
                        (btn) -> this.client.setScreen(new LibraryScreen(this)))
                .build());

        // done button
        footer.add(ButtonWidget.builder(CommonTexts.DONE, (btn) -> this.closeScreen()).build());

        // finish
        this.layout.visitWidgets(this::addDrawableSelectableElement);
        this.repositionElements();
    }

    protected void initContent() {
        var helper = this.grid.createAdditionHelper(2);

        if (this.playerList != null && this.playerList.isSkin == this.isSkin) {
            // player list already exists
        } else {
            this.playerList = new PlayerListWidget(this, this.isSkin);
        }

        // add player list
        helper.add(this.playerList);

        // add configuration
        this.configFrame = helper.add(new FrameWidget());
        LinearLayoutWidget configCols = configFrame.add(LinearLayoutWidget.createHorizontal()).setSpacing(8);
        GridWidget config = configCols.add(new GridWidget()).setSpacing(4);
        if (this.selectedProfile == null) {
            config.add(new TextWidget(Text.translatable("skin_overrides.no_selection"), this.textRenderer), 0, 0);
        } else {
            if (this.isSkin) {
                this.initSkinConfig(config);
            } else {
                this.initCapeConfig(config);
            }

            this.previewFrame = configCols.add(
                    new FrameWidget(PlayerSkinRenderer.WIDTH * PREVIEW_SCALE,
                            PlayerSkinRenderer.HEIGHT * PREVIEW_SCALE),
                    LayoutSettings.create().alignHorizontallyRight().alignVerticallyCenter());
        }
    }

    protected void initSkinConfig(GridWidget config) {
        config.add(new TextWidget(Text.translatable("skin_overrides.add_skin"), this.textRenderer), 0, 0);

        // add remove local image button
        config.add(ButtonWidget
                .builder(Text.translatable("skin_overrides.remove.local_image"), (btn) -> this.removeLocalImage())
                .width(120)
                .build(), 1, 0).active = Overrides.hasLocalSkinOverride(this.selectedProfile);
        // add remove copy button
        config.add(ButtonWidget
                .builder(Text.translatable("skin_overrides.remove.copy"), (btn) -> this.removeCopy())
                .width(120)
                .build(), 2, 0).active = Overrides.hasSkinCopyOverride(this.selectedProfile);
    }

    protected void initCapeConfig(GridWidget config) {
        config.add(new TextWidget(Text.translatable("skin_overrides.add_cape"), this.textRenderer), 0, 0);

        // add remove local image button
        config.add(ButtonWidget
                .builder(Text.translatable("skin_overrides.remove.local_image"), (btn) -> this.removeLocalImage())
                .width(120)
                .build(), 1, 0).active = Overrides.hasLocalCapeOverride(this.selectedProfile);
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
            PlayerSkin skin = SkinOverrides.getSkin(this.selectedProfile);
            if (this.isSkin) {
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
        this.playerList.setDimensions(Math.min(200, area.width() / 2), area.height());
        this.configFrame.setMinDimensions(Math.min(200, area.width() / 2), area.height());

        // reposition layout
        this.layout.setHeaderHeight(hh);
        this.layout.arrangeElements();
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    public boolean isSkin() {
        return this.isSkin;
    }

    public void setIsSkin(boolean isSkin) {
        if (this.isSkin != isSkin) {
            this.isSkin = isSkin;
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

    public void removeLocalImage() {
        if (this.isSkin) {
            Overrides.removeLocalSkinOverride(this.selectedProfile);
        } else {
            Overrides.removeLocalCapeOverride(this.selectedProfile);
        }
        this.upgradeProfile(); // get player's actual skin/cape
        this.clearAndInit(); // update remove buttons
    }

    public void removeCopy() {
        Overrides.removeSkinCopyOverride(this.selectedProfile);
        this.upgradeProfile(); // get player's actual skin/cape
        this.clearAndInit(); // update remove buttons
    }

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

        if (this.isSkin) {
            // register skin texture for preview
            var texture = new LocalSkinTexture(path.toFile(), null);
            Identifier textureId = new Identifier("skin_overrides", UUID.randomUUID().toString());
            this.client.getTextureManager().registerTexture(textureId, texture);

            // open model selection screen
            this.client.setScreen(new PlayerModelSelectScreen(this, textureId, model -> {
                Overrides.copyLocalSkinOverride(profile, path, model);
                this.clearAndInit();
            }));
        } else {
            Overrides.copyLocalCapeOverride(profile, path);
            this.clearAndInit();
        }
    }

    class DummyTab implements Tab {
        public final Text title;
        public final boolean isSkin;

        public DummyTab(boolean isSkin) {
            this.title = isSkin ? SKIN_TITLE : CAPE_TITLE;
            this.isSkin = isSkin;
        }

        @Override
        public Text getTitle() {
            return this.title;
        }

        @Override
        public void visitChildren(Consumer<ClickableWidget> consumer) {
            // when selected
            SkinOverridesScreen.this.setIsSkin(this.isSkin);
        }

        @Override
        public void refreshLayout(ScreenArea area) {
        }
    }
}
