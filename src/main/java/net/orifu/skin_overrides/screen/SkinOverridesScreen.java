package net.orifu.skin_overrides.screen;

import java.util.function.Consumer;

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
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.SkinOverrides;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;

public class SkinOverridesScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.title");
    private static final Text SKIN_TITLE = Text.translatable("skin_overrides.title.skin");
    private static final Text CAPE_TITLE = Text.translatable("skin_overrides.title.cape");

    private static final int PAD = 32;

    // TODO: move this into an xplat helper?
    private static final Identifier FOOTER_SEPARATOR_TEXTURE = new Identifier("minecraft",
            "textures/gui/footer_separator.png");

    private final TabManager tabManager = new TabManager(this::addDrawableSelectableElement, (wg) -> this.remove(wg));

    @Nullable
    private Screen parent;

    private HeaderFooterLayoutWidget layout;
    private HeaderBar header;
    private GridWidget grid;
    private PlayerListWidget playerList;
    @Nullable
    private FrameWidget playerSkinFrame;

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
        GridWidget config = helper.add(new GridWidget());
        if (this.selectedProfile == null) {
            config.add(new TextWidget(Text.translatable("skin_overrides.no_selection"), this.textRenderer), 0, 0);
        } else {
            config.add(new TextWidget(Text.translatable("skin_overrides.add_image"), this.textRenderer), 0, 0);

            this.playerSkinFrame = config
                    .add(new FrameWidget(PlayerSkinRenderer.WIDTH * 2, PlayerSkinRenderer.HEIGHT * 2), 0, 1);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        RenderSystem.enableBlend();
        graphics.drawTexture(FOOTER_SEPARATOR_TEXTURE, 0,
                this.height - this.layout.getFooterHeight() - 2, 0, 0, this.width, 2, 32, 2);
        RenderSystem.disableBlend();

        if (this.selectedProfile != null) {
            PlayerSkin skin = SkinOverrides.getSkin(this.selectedProfile);
            PlayerSkinRenderer.draw(graphics, skin, this.playerSkinFrame.getX(), this.playerSkinFrame.getY(), 2);
        }
    }

    @Override
    public void repositionElements() {
        // reposition tab header
        this.header.setWidth(this.width);
        this.header.arrangeElements();

        int hh = this.header.getArea().bottom();
        int ff = this.layout.getFooterHeight();

        // reposition main content area
        ScreenArea area = new ScreenArea(PAD, hh + PAD, this.width - PAD * 2, this.height - ff - hh - PAD * 2);
        FrameWidget.align(this.grid, area);
        this.playerList.setPosition(area.x(), area.y());
        this.playerList.setDimensions(Math.min(200, area.width() / 2), area.height());

        // reposition layout
        this.layout.setHeaderHeight(hh);
        this.layout.arrangeElements();
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
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
