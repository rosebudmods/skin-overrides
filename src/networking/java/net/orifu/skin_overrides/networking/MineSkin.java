package net.orifu.skin_overrides.networking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.util.Util;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MineSkin {
    private static final HttpClient HTTP_CLIENT = HttpClients.createDefault();

    public static CompletableFuture<Optional<Skin.Signature>> sign(ResourceLocation texture, Skin.Model model) {
        var image = Util.saveTexture(texture, 64, 64);
        return image.thenApplyAsync(img -> sign(img, model));
    }

    public static Optional<Skin.Signature> sign(NativeImage image, Skin.Model model) {
        // write skin to disk
        File skinFile;
        try {
            skinFile = File.createTempFile("skin-overrides_", "_temp-skin");
            image.writeToFile(skinFile);
            image.close();
        } catch (IOException e) {
            return Optional.empty();
        }

        // create post request
        var post = new HttpPost("https://api.mineskin.org/v2/generate");
        post.setHeader("User-Agent", "skin-overrides/" + Mod.MOD_VERSION + " (github/rosebudmods)");
        post.setEntity(MultipartEntityBuilder.create()
                .addTextBody("variant", model.apiName)
                .addTextBody("visibility", "unlisted")
                .addBinaryBody("file", skinFile, ContentType.IMAGE_PNG, "skin.png")
                .build());

        // send request
        HttpResponse response;
        try {
            response = HTTP_CLIENT.execute(post);
        } catch (IOException e) {
            return Optional.empty();
        }

        // read request body
        String body;
        try {
            body = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (IOException e) {
            return Optional.empty();
        }

        // validate request success
        if (response.getStatusLine().getStatusCode() / 100 != 2) {
            Mod.LOGGER.error("failed to sign skin, got API response:\n{}", body);
            return Optional.empty();
        }

        // read response as JSON
        var jsonBody = new Gson().fromJson(body, JsonObject.class);
        Mod.LOGGER.debug("mineskin response: {}", body);

        // validate body success
        if (!jsonBody.get("success").getAsBoolean()) {
            Mod.LOGGER.error("failed to sign skin, got API response:\n{}", body);
            return Optional.empty();
        }

        // read skin value and signature from JSON
        var skinInfo = jsonBody.getAsJsonObject("skin").getAsJsonObject("texture").getAsJsonObject("data");
        String skinValue = skinInfo.get("value").getAsString();
        String skinSignature = skinInfo.get("signature").getAsString();

        return Optional.of(new Skin.Signature(skinValue, skinSignature));
    }
}
