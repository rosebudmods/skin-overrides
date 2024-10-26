package net.orifu.skin_overrides.screen;

import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.widget.layout.LayoutSettings;
import net.minecraft.client.gui.widget.layout.LayoutWidget;
import net.minecraft.text.Text;
import net.orifu.xplat.CommonTexts;
import net.orifu.xplat.gui.Screen;
import net.orifu.xplat.gui.widget.ButtonWidget;
import net.orifu.xplat.gui.widget.LinearLayoutWidget;

public class SkinChangeInfoScreen extends WarningScreen {
    private static final Text HEADER = Text.translatable("skin_overrides.change_skin.title");
    private static final Text MESSAGE = Text.translatable("skin_overrides.change_skin.message");

    private static final String LEARN_MORE_URL = "https://rosebud.dev/skin-overrides/networking/";

    private final Screen parent;

    protected SkinChangeInfoScreen(Screen parent) {
        super(HEADER, MESSAGE, MESSAGE);

        this.parent = parent;
    }

    @Override
    protected LayoutWidget initContent() {
        var rows = LinearLayoutWidget.createVertical().setSpacing(8);

        var buttons = rows.add(LinearLayoutWidget.createHorizontal().setSpacing(8));
        buttons.add(ButtonWidget.builder(CommonTexts.PROCEED, btn -> {}).build());
        buttons.add(ButtonWidget.builder(CommonTexts.CANCEL, btn -> this.closeScreen()).build());

        rows.add(ButtonWidget.builder(Text.translatable("mco.snapshotRealmsPopup.urlText"),
                    ConfirmLinkScreen.createOpenAction(this, LEARN_MORE_URL)).build(),
                LayoutSettings.create().alignHorizontallyCenter());

        return rows;
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }
}
