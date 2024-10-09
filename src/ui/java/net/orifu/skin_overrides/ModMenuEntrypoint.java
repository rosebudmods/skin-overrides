package net.orifu.skin_overrides;

//? if hasModMenu {
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.orifu.skin_overrides.screen.SkinOverridesScreen;

public class ModMenuEntrypoint implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SkinOverridesScreen::new;
    }
}
//?} else
/*public class ModMenuEntrypoint {}*/
