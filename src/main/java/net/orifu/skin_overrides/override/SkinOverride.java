package net.orifu.skin_overrides.override;

import net.minecraft.client.texture.PlayerSkin;
import net.orifu.skin_overrides.Library.SkinEntry;
import net.orifu.skin_overrides.texture.CopiedSkinTexture;
import net.orifu.skin_overrides.texture.LocalSkinTexture;

public class SkinOverride extends
        AbstractWrapper<PlayerSkin.Model, LocalSkinTexture, SkinEntry, CopiedSkinTexture, LocalSkinOverride, LibrarySkinOverride> {
    public SkinOverride() {
        super(LocalSkinOverride.INSTANCE, LibrarySkinOverride.INSTANCE);
    }
}
