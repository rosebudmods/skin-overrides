package net.orifu.skin_overrides;

import net.fabricmc.api.ModInitializer;
import net.orifu.skin_overrides.networking.ModNetworking;

public class ModInit implements ModInitializer {
    @Override
    public void onInitialize() {
        ModNetworking.init();
    }
}
