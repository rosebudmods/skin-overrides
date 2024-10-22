package net.orifu.skin_overrides;

import net.fabricmc.api.ClientModInitializer;
//? if hasUi {
import com.mojang.blaze3d.platform.InputUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBind;
import net.orifu.skin_overrides.screen.OverridesScreen;
import org.lwjgl.glfw.GLFW;
//?}
//? if hasNetworking
import net.orifu.skin_overrides.networking.ModNetworking;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.orifu.skin_overrides.Mod.CAPES;
import static net.orifu.skin_overrides.Mod.SKINS;

@SuppressWarnings("deprecation")
public class ModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        var scheduler = Executors.newScheduledThreadPool(1);
        // reload override files every 500 ms
        scheduler.scheduleAtFixedRate(SKINS::update, 1_000, 500, TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(CAPES::update, 1_000, 500, TimeUnit.MILLISECONDS);
        // reload library files every 2 seconds
        scheduler.scheduleAtFixedRate(SKINS.library()::reload, 0, 2, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(CAPES.library()::reload, 0, 2, TimeUnit.SECONDS);

        //? if hasUi {
        KeyBind binding = KeyBindingHelper.registerKeyBinding(new KeyBind(
                "key.skin_overrides.open_screen",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O,
                "key.categories.misc"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (binding.wasPressed()) {
                client.setScreen(new OverridesScreen(client.currentScreen));
            }
        });
        //?}

        //? if hasNetworking
        ModNetworking.initClient();
    }
}
