package net.orifu.skin_overrides.screen;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenArea;
import net.minecraft.client.gui.tab.GridWidgetTab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.HeaderBar;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.HeaderFooterLayoutWidget;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SkinOverridesScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.title");
    private static final Text SKIN_TITLE = Text.translatable("skin_overrides.title.skin");
    private static final Text CAPE_TITLE = Text.translatable("skin_overrides.title.cape");

    // TODO: move this into an xplat helper?
    private static final Identifier FOOTER_SEPARATOR_TEXTURE = Identifier.tryParse("textures/gui/footer_separator.png");

    private final TabManager tabManager = new TabManager(this::addDrawableSelectableElement, (wg) -> this.remove(wg));

    @Nullable
    private Screen parent;

    private final HeaderFooterLayoutWidget layout = new HeaderFooterLayoutWidget(this);
    private HeaderBar header;

    public SkinOverridesScreen(@Nullable Screen parent) {
        super(TITLE);

        this.parent = parent;
    }

    @Override
    protected void init() {
        // add tabs header
        this.header = this.addDrawableSelectableElement(
                HeaderBar.builder(this.tabManager, this.width)
                        .tabs(new OverridesTab(true), new OverridesTab(false))
                        .build());
        this.header.setFocusedTab(0, false);

        // add footer
        LinearLayoutWidget footer = this.layout.addToFooter(LinearLayoutWidget.createHorizontal().setSpacing(5));

        // done button
        footer.add(ButtonWidget.builder(CommonTexts.DONE, (btn) -> this.closeScreen()).build());

        // finish
        this.layout.visitWidgets(this::addDrawableSelectableElement);
        this.repositionElements();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        RenderSystem.enableBlend();
        graphics.drawTexture(FOOTER_SEPARATOR_TEXTURE, 0,
                this.height - this.layout.getFooterHeight() - 2, 0, 0, this.width, 2, 32, 2);
        RenderSystem.disableBlend();
    }

    @Override
    public void repositionElements() {
        // reposition tab header
        this.header.setWidth(this.width);
        this.header.arrangeElements();

        // reposition tab area
        int headerHeight = this.header.getArea().bottom();
        int footerHeight = this.layout.getFooterHeight();
        ScreenArea area = new ScreenArea(0, headerHeight, this.width, this.height - footerHeight - headerHeight);
        this.tabManager.setTabArea(area);

        // reposition layout
        this.layout.setHeaderHeight(headerHeight);
        this.layout.arrangeElements();
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    class OverridesTab extends GridWidgetTab {
        public final boolean isSkin;

        public OverridesTab(boolean isSkin) {
            super(isSkin ? SKIN_TITLE : CAPE_TITLE);

            this.isSkin = isSkin;
        }
    }
}
