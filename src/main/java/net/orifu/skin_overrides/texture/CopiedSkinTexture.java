package net.orifu.skin_overrides.texture;

import java.util.Optional;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Library;
import net.orifu.skin_overrides.util.ProfileHelper;

public class CopiedSkinTexture extends AbstractCopiedTexture {
    public final PlayerSkin.Model model;

    protected CopiedSkinTexture(Identifier source, PlayerSkin.Model model, String name, boolean isCopying) {
        super(source, name, isCopying);
        this.model = model;
    }

    public static Optional<CopiedSkinTexture> fromPlayer(String playerId) {
        var profile = ProfileHelper.idToProfile(playerId);
        if (profile.isPresent()) {
            PlayerSkin skin = MinecraftClient.getInstance().getSkinProvider().getSkin(profile.get());
            return Optional.of(new CopiedSkinTexture(skin.texture(), skin.model(), profile.get().getName(), true));
        }

        return Optional.empty();
    }

    public static Optional<CopiedSkinTexture> fromLibrary(String id) {
        System.out.println("id is " + id);
        var entry = Library.getSkin(id);
        if (entry != null) {
            return Optional.of(new CopiedSkinTexture(entry.getTexture(), entry.getModel(), entry.getName(), false));
        }

        return Optional.empty();
    }

    public static Optional<CopiedSkinTexture> fromIdentifier(Identifier id) {
        if (id.getNamespace().equals("minecraft")) {
            return CopiedSkinTexture.fromPlayer(id.getPath());
        } else if (id.getNamespace().equals("skin_overrides")) {
            return CopiedSkinTexture.fromLibrary(id.getPath());
        }
        return Optional.empty();
    }

    public PlayerSkin.Model model() {
        return this.model;
    }
}
