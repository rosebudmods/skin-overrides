package net.orifu.skin_overrides.override;

import net.orifu.skin_overrides.Library.CapeEntry;
import net.orifu.skin_overrides.texture.CopiedCapeTexture;
import net.orifu.skin_overrides.texture.LocalPlayerTexture;

public class CapeOverride extends
        AbstractWrapper<Boolean, LocalPlayerTexture, CapeEntry, CopiedCapeTexture, LocalCapeOverride, LibraryCapeOverride> {
    public CapeOverride() {
        super(LocalCapeOverride.INSTANCE, LibraryCapeOverride.INSTANCE);
    }
}
