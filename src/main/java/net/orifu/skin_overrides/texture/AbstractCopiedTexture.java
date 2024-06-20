package net.orifu.skin_overrides.texture;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class AbstractCopiedTexture {
    public final Identifier source;
    public final String name;
    public final boolean isCopying;

    protected AbstractCopiedTexture(Identifier source, String name, boolean isCopying) {
        this.source = source;
        this.name = name;
        this.isCopying = isCopying;
    }

    public Identifier texture() {
        return this.source;
    }

    public MutableText description() {
        if (this.isCopying) {
            return Text.translatable("skin_overrides.override.copy", this.name);
        } else {
            return Text.translatable("skin_overrides.override.library", this.name);
        }
    }
}
