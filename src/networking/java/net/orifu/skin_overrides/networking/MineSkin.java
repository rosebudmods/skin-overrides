package net.orifu.skin_overrides.networking;

import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.Util;
import org.mineskin.ApacheRequestHandler;
import org.mineskin.GenerateOptions;
import org.mineskin.MineSkinClient;
import org.mineskin.data.Visibility;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class MineSkin {
    private static final MineSkinClient CLIENT = MineSkinClient.builder()
            .requestHandler(ApacheRequestHandler::new)
            .userAgent("skin-overrides/" + Mod.MOD_VERSION + " (github/rosebudmods)")
            .apiKey("") // prevents a warning
            .build();

    public static Optional<Skin.Signature> sign(Identifier texture) {
        try {
            File skin = File.createTempFile("skin-overrides_", "_temp-skin");
            Util.saveTexture(texture, 64, 64, skin.toPath());

            // TODO: skin model
            var resp = CLIENT.generateUpload(skin, GenerateOptions.create().visibility(Visibility.UNLISTED)).get();
            var signedTexture = resp.getSkin().data().texture();
            skin.delete();

            return Optional.of(new Skin.Signature(signedTexture.value(), signedTexture.signature()));
        } catch (IOException e) {
            Mod.LOGGER.error("io error while trying to upload skin to mineskin", e);
        } catch (InterruptedException e) {
            Mod.LOGGER.error("thread was interrupted while trying to upload skin to mineskin", e);
        } catch (ExecutionException e) {
            Mod.LOGGER.error("error while trying to upload skin to mineskin", e);
        }

        return Optional.empty();
    }
}
