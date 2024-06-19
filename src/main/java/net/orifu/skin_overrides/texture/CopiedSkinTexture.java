package net.orifu.skin_overrides.texture;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Library.LibraryEntry;

public class CopiedSkinTexture {
    public final Identifier source;
    public final PlayerSkin.Model model;
    public final String name;
    public final boolean isCopying;

    public CopiedSkinTexture(GameProfile profile) {
        // fetch the remote skin (cached)
        PlayerSkin remoteSkin = MinecraftClient.getInstance().getSkinProvider().getSkin(profile);
        this.source = remoteSkin.texture();
        this.model = remoteSkin.model();
        this.name = profile.getName();
        this.isCopying = true;
    }

    public CopiedSkinTexture(LibraryEntry libraryEntry) {
        this.source = libraryEntry.getTexture();
        this.model = libraryEntry.getModel();
        this.name = libraryEntry.getName();
        this.isCopying = false;
    }

    public Identifier texture() {
        return this.source;
    }

    public PlayerSkin.Model model() {
        return this.model;
    }

    public MutableText description() {
        if (this.isCopying) {
            return Text.translatable("skin_overrides.override.copy", this.name);
        } else {
            return Text.translatable("skin_overrides.override.library", this.name);
        }
    }
}
