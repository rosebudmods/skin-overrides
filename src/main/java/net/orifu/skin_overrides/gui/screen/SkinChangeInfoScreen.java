package net.orifu.skin_overrides.gui.screen;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.networking.ModNetworking;
import net.orifu.skin_overrides.override.SkinChangeOverride;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.util.Toast;
import net.orifu.skin_overrides.util.Util;
import net.orifu.xplat.gui.components.LinearLayout;

import java.util.Optional;

public class SkinChangeInfoScreen extends WarningScreen {
    private static final Component HEADER = Component.translatable("skin_overrides.change_skin.title");
    private static final Component MESSAGE = Component.translatable("skin_overrides.change_skin.message");
    private static final Component MESSAGE_VANILLA = Component.translatable("skin_overrides.change_skin.message.vanilla");
    private static final Component MESSAGE_MODDED = Component.translatable("skin_overrides.change_skin.message.modded");

    private static final String LEARN_MORE_URL = "https://rosebud.dev/skin-overrides/networking/";

    private final OverridesScreen parent;

    protected SkinChangeInfoScreen(OverridesScreen parent) {
        super(HEADER, getMessage(), getMessage());

        this.parent = parent;
    }

    protected static Component getMessage() {
        return Minecraft.getInstance().player == null ? MESSAGE
                : ModNetworking.isOnSkinOverridesServer() ? MESSAGE_MODDED : MESSAGE_VANILLA;
    }

    @Override
    //? if >=1.20.6 {
    protected Layout addFooterButtons()
    //?} else
    /*public void initButtons(int textHeight)*/
    {
        var rows = LinearLayout.vertical().spacing(8);

        var buttons = rows.addChild(LinearLayout.horizontal().spacing(8));
        buttons.addChild(Button.builder(CommonComponents.GUI_PROCEED, btn -> this.changeSkin()).build());
        buttons.addChild(Button.builder(CommonComponents.GUI_CANCEL, btn -> this.onClose()).build());

        rows.addChild(Button.builder(Component.translatable("skin_overrides.change_skin.learn_more"),
                        /*? if >=1.20.4 {*/ ConfirmLinkScreen.confirmLink(this, LEARN_MORE_URL)
                        /*?} else*/ /*ConfirmLinkScreen.confirmLink(LEARN_MORE_URL, this, true)*/
                ).build(),
                LayoutSettings.defaults().alignHorizontallyCenter());

        //? if >=1.20.6 {
        return rows;
        //?} else {
        /*rows.setPosition((this.width - (150 * 2 + 8)) / 2, textHeight + 100);
        rows.visitWidgets(this::addRenderableWidget);
        rows.arrangeElements();
        *///?}
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    protected void changeSkin() {
        this.minecraft.setScreen(this.parent);

        var userProfile = ProfileHelper.user();
        var skin = Mod.override(userProfile);
        skin.setUserSkin().thenAccept(newSkin -> this.onSkinChanged(newSkin, userProfile, skin));
    }

    protected void onSkinChanged(Optional<String> newSkin, GameProfile userProfile, Skin skin) {
        if (newSkin.isPresent()) {
            Mod.LOGGER.debug("player has changed skin. new url: {}", newSkin.get());

            // remove existing override
            Mod.SKINS.removeOverride(userProfile);
            Util.runOnRenderThread(this.parent::reload);

            // add new "override" for showing updated skin until restarting
            SkinChangeOverride.set(newSkin.get(), skin.model());

            var updatedProfile = ProfileHelper.uuidToProfileExpectingSkinUrl(userProfile.getId(), newSkin.get());
            updatedProfile.thenAccept(profile -> {
                if (profile.isPresent()) {
                    var signedSkin = ProfileHelper.getProfileSkinSignature(profile.get());

                    Mod.LOGGER.debug("received updated profile from services:\nval: {}\nsig: {}",
                            signedSkin.value(), signedSkin.signature());

                    ModNetworking.updateSkinOnServer(signedSkin.value(), signedSkin.signature());
                } else {
                    Toast.show(Component.translatable("skin_overrides.change_skin.reload_fail.title"),
                            Component.translatable("skin_overrides.change_skin.reload_fail.description"));
                }
            });
        } else {
            Toast.show(Component.translatable("skin_overrides.change_skin.fail.title"),
                    Component.translatable("skin_overrides.change_skin.fail.description"));
        }
    }
}
