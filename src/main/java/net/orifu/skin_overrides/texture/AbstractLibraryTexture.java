package net.orifu.skin_overrides.texture;

import net.minecraft.util.Identifier;

public abstract class AbstractLibraryTexture {
    public final Identifier source;
    public final String name;

    protected AbstractLibraryTexture(Identifier source, String name) {
        this.source = source;
        this.name = name;
    }

    public Identifier texture() {
        return this.source;
    }
}
