package net.orifu.skin_overrides.texture;

import net.minecraft.util.Identifier;

public abstract class AbstractLibraryTexture {
    public final Identifier texture;
    public final String name;

    protected AbstractLibraryTexture(Identifier texture, String name) {
        this.texture = texture;
        this.name = name;
    }
}
