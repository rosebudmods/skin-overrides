package net.orifu.skin_overrides;

import net.fabricmc.api.ModInitializer;
//? if hasNetworking
/*import net.orifu.skin_overrides.networking.ModNetworking;*/

@SuppressWarnings("deprecation")
public class ModInit implements ModInitializer {
    @Override
    public void onInitialize() {
        //? if hasNetworking
        /*ModNetworking.init();*/
    }
}
