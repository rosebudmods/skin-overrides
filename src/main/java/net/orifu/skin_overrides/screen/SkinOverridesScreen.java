package net.orifu.skin_overrides.screen;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.tab.GridWidgetTab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.HeaderBar;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.FrameWidget;
import net.minecraft.client.gui.widget.layout.GridWidget;
import net.minecraft.text.CommonTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class SkinOverridesScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.title");
    private static final Text SKIN_TITLE = Text.translatable("skin_overrides.title.skin");
    private static final Text CAPE_TITLE = Text.translatable("skin_overrides.title.cape");

    private final TabManager tabManager = new TabManager(this::addDrawableSelectableElement, (wg) -> this.remove(wg));

    @Nullable
    private Screen parent;

    private HeaderBar header;
    private GridWidget grid;

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

        // add footer grid
        this.grid = new GridWidget().setColumnSpacing(10);
        var helper = this.grid.createAdditionHelper(2);

        // done button
        helper.add(ButtonWidget.builder(CommonTexts.DONE, (btn) -> this.closeScreen()).build());

        this.grid.visitWidgets(this::addDrawableSelectableElement);

        this.repositionElements();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        graphics.drawTexture(CreateWorldScreen.FOOTER_SEPARATOR_TEXTURE, 0,
                MathHelper.roundUpToMultiple(this.height - 36 - 2, 2), 0, 0, this.width, 2, 32, 2);
    }

    @Override
    public void repositionElements() {
        // reposition tab header
        this.header.setWidth(this.width);
        this.header.arrangeElements();

        // reposition footer grid
        this.grid.arrangeElements();
        FrameWidget.align(this.grid, 0, this.height - 36, this.width, 36);
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
