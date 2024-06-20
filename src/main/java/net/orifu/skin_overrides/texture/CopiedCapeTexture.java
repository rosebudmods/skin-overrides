package net.orifu.skin_overrides.texture;

import java.util.Optional;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Library;
import net.orifu.skin_overrides.util.ProfileHelper;

public class CopiedCapeTexture extends AbstractCopiedTexture {
    protected CopiedCapeTexture(Identifier source, String name, boolean isCopying) {
        super(source, name, isCopying);
    }

    public static Optional<CopiedCapeTexture> fromPlayer(String playerId) {
        var profile = ProfileHelper.idToProfile(playerId);
        if (profile.isPresent()) {
            PlayerSkin skin = MinecraftClient.getInstance().getSkinProvider().getSkin(profile.get());
            return Optional.of(new CopiedCapeTexture(skin.capeTexture(), profile.get().getName(), true));
        }

        return Optional.empty();
    }

    public static Optional<CopiedCapeTexture> fromEntry(String id) {
        var entry = Library.getCape(id);
        if (entry != null) {
            return Optional.of(new CopiedCapeTexture(entry.getTexture(), entry.getName(), false));
        }

        return Optional.empty();
    }

    public static Optional<CopiedCapeTexture> fromIdentifier(Identifier id) {
        if (id.getNamespace().equals("minecraft")) {
            return CopiedCapeTexture.fromPlayer(id.getPath());
        } else if (id.getNamespace().equals("skin_overrides")) {
            return CopiedCapeTexture.fromEntry(id.getPath());
        }
        return Optional.empty();
    }
}
