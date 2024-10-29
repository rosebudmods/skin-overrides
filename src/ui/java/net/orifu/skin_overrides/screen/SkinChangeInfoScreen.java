package net.orifu.skin_overrides.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.WarningScreen;
import net.minecraft.client.gui.widget.layout.LayoutSettings;
import net.minecraft.client.gui.widget.layout.LayoutWidget;
import net.minecraft.text.Text;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.override.SkinChangeOverride;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.xplat.CommonTexts;
import net.orifu.xplat.gui.Screen;
import net.orifu.xplat.gui.widget.ButtonWidget;
import net.orifu.xplat.gui.widget.LinearLayoutWidget;

public class SkinChangeInfoScreen extends WarningScreen {
    private static final Text HEADER = Text.translatable("skin_overrides.change_skin.title");
    private static final Text MESSAGE = Text.translatable("skin_overrides.change_skin.message");
    private static final Text MESSAGE_VANILLA = Text.translatable("skin_overrides.change_skin.message.vanilla");
    private static final Text MESSAGE_MODDED = Text.translatable("skin_overrides.change_skin.message.modded");

    private static final String LEARN_MORE_URL = "https://rosebud.dev/skin-overrides/networking/";

    private final OverridesScreen parent;

    protected SkinChangeInfoScreen(OverridesScreen parent) {
        super(HEADER, getMessage(), getMessage());

        this.parent = parent;
    }

    protected static Text getMessage() {
        return MinecraftClient.getInstance().player == null ? MESSAGE
                : Mod.isOnSkinOverridesServer() ? MESSAGE_MODDED : MESSAGE_VANILLA;
    }

    @Override
    protected LayoutWidget initContent() {
        var rows = LinearLayoutWidget.createVertical().setSpacing(8);

        var buttons = rows.add(LinearLayoutWidget.createHorizontal().setSpacing(8));
        buttons.add(ButtonWidget.builder(CommonTexts.PROCEED, btn -> this.changeSkin()).build());
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

    protected void changeSkin() {
        var userProfile = ProfileHelper.user();
        var skin = Mod.override(userProfile);
        var newSkin = skin.setUserSkin();

        if (newSkin.isPresent()) {
            System.out.println("url: " + newSkin.get().getLeft());
            System.out.println("model: " + newSkin.get().getRight());

            // remove existing override
            Mod.SKINS.removeOverride(userProfile);
            this.parent.reload();
            // add new "override" for showing updated skin until restarting
            SkinChangeOverride.set(newSkin.get().getLeft(), newSkin.get().getRight());

            // TODO: update on server
        } else {
            // TODO: toast
        }

        this.closeScreen();
    }
}
