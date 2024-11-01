package net.orifu.skin_overrides.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.WarningScreen;
//? if >=1.20.6
import net.minecraft.client.gui.widget.layout.LayoutWidget;
import net.minecraft.text.Text;
import net.orifu.skin_overrides.Mod;
//? if hasNetworking
import net.orifu.skin_overrides.networking.ModNetworking;
import net.orifu.skin_overrides.override.SkinChangeOverride;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.xplat.CommonTexts;
import net.orifu.xplat.gui.LayoutSettings;
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
    /*? if >=1.21 {*/ protected LayoutWidget initContent()
    /*?} else if >=1.20.6 {*/ /*protected LayoutWidget method_57750()
    *//*?} else*/ /*protected void initButtons(int textHeight)*/
    {
        var rows = LinearLayoutWidget.createVertical().setSpacing(8);

        var buttons = rows.add(LinearLayoutWidget.createHorizontal().setSpacing(8));
        buttons.add(ButtonWidget.builder(CommonTexts.PROCEED, btn -> this.changeSkin()).build());
        buttons.add(ButtonWidget.builder(CommonTexts.CANCEL, btn -> this.closeScreen()).build());

        rows.add(ButtonWidget.builder(Text.translatable("skin_overrides.change_skin.learn_more"),
                        /*? if >=1.21.1 {*/ ConfirmLinkScreen.createOpenAction(this, LEARN_MORE_URL)
                        /*?} else if >=1.20.4 {*/ /*ConfirmLinkScreen.createPressAction(this, LEARN_MORE_URL)
                        *//*?} else*/ /*ConfirmLinkScreen.createPressAction(LEARN_MORE_URL, this, true)*/
                ).build(),
                LayoutSettings.create().alignHorizontallyCenter());

        //? if >=1.20.6 {
        return rows;
        //?} else {
        /*rows.setPosition((this.width - (150 * 2 + 8)) / 2, textHeight + 100);
        /^? if >=1.20.2 {^//^rows.visitWidgets(this::addDrawableSelectableElement);
        ^//^?} else^/ rows.visitWidgets(this::addDrawableChild);
        rows.arrangeElements();
        *///?}
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
            Mod.LOGGER.debug("player has changed skin. api response:\nurl: {}\nmodel: {}",
                    newSkin.get().getLeft(), newSkin.get().getRight());

            // remove existing override
            Mod.SKINS.removeOverride(userProfile);
            this.parent.reload();
            // add new "override" for showing updated skin until restarting
            SkinChangeOverride.set(newSkin.get().getLeft(), newSkin.get().getRight());

            var updatedProfile = ProfileHelper.uuidToProfileExpectingSkinUrl(userProfile.getId(), newSkin.get().getLeft());
            updatedProfile.thenAccept(profile -> {
                if (profile.isPresent()) {
                    var property = this.client.getSessionService().getPackedTextures(profile.get());

                    Mod.LOGGER.debug("received updated profile from services:\nval: {}\nsig: {}",
                            property.value(), property.signature());

                    //? if hasNetworking
                    ModNetworking.updateSkinOnServer(property.value(), property.signature());
                } else {
                    // TODO: toast
                }
            });
        } else {
            // TODO: toast
        }

        this.closeScreen();
    }
}
