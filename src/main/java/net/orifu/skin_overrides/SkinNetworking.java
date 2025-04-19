package net.orifu.skin_overrides;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserApiService;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.orifu.skin_overrides.mixin.YggdrasilServiceClientAccessor;
import net.orifu.skin_overrides.mixin.YggdrasilUserApiServiceAccessor;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkinNetworking {
    public static final HttpClient HTTP_CLIENT = HttpClients.createDefault();

    public static CompletableFuture<Optional<String>> setUserSkin(Skin skin) {
        var image = Util.saveTexture(skin.texture(), 64, 64);
        return image.thenApplyAsync(img -> setUserSkin(img, skin.model()));
    }

    public static Optional<String> setUserSkin(NativeImage image, Skin.Model model) {
        // write skin to disk
        File skinFile;
        try {
            skinFile = File.createTempFile("skin-overrides_", "_temp-skin");
            image.writeToFile(skinFile);
            image.close();
        } catch (IOException e) {
            return Optional.empty();
        }

        // get token/server info
        var userApiService = (YggdrasilUserApiService) Minecraft.getInstance().userApiService;
        var userApiServiceAccessor = (YggdrasilUserApiServiceAccessor) userApiService;
        var serviceClient = userApiServiceAccessor.getMinecraftClient();
        var serviceClientAccessor = (YggdrasilServiceClientAccessor) serviceClient;
        var servicesHost = /*? if >=1.20.2 {*/ userApiServiceAccessor.getEnvironment().servicesHost();
        /*?} else*/ /*userApiServiceAccessor.getEnvironment().getServicesHost();*/
        var url = servicesHost + "/minecraft/profile/skins";

        // create post request
        var post = new HttpPost(url);
        post.setHeader("Authorization", "Bearer " + serviceClientAccessor.getAccessToken());
        post.setEntity(MultipartEntityBuilder.create()
                .addTextBody("variant", model.apiName)
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
        Mod.LOGGER.debug("skin server response: {}", body);

        // read skin value and signature from JSON
        var skinInfo = jsonBody.getAsJsonArray("skins").get(0).getAsJsonObject();
        String textureUrl = skinInfo.get("url").getAsString();
//        Skin.Model parsedModel = Skin.Model.parse(skinInfo.get("variant").getAsString());

        return Optional.of(textureUrl);
    }

    public static Optional<PlayerProfileResponse> getPlayerProfile() {
        boolean isSignedIn = Minecraft.getInstance().userApiService instanceof YggdrasilUserApiService;

        if (!isSignedIn) return Optional.empty();

        var userApiService = (YggdrasilUserApiService) Minecraft.getInstance().userApiService;
        var userApiServiceAccessor = (YggdrasilUserApiServiceAccessor) userApiService;
        var serviceClient = userApiServiceAccessor.getMinecraftClient();
        var servicesHost = /*? if >=1.20.2 {*/ userApiServiceAccessor.getEnvironment().servicesHost();
        /*?} else*/ /*userApiServiceAccessor.getEnvironment().getServicesHost();*/

        return Optional.ofNullable(serviceClient.get(HttpAuthenticationService.constantURL(servicesHost + "/minecraft/profile"), PlayerProfileResponse.class));
    }

    public record PlayerProfileResponse(
            @SerializedName("id")
            UUID id,
            @SerializedName("name")
            String name,
            @SerializedName("skins")
            List<PlayerProfileSkin> skins,
            @SerializedName("capes")
            List<PlayerProfileCape> capes
    ) {}

    public record PlayerProfileSkin(
            // @SerializedName("id")
            // UUID id,
            @SerializedName("state")
            String state,
            @SerializedName("url")
            String url,
            // @SerializedName("textureKey")
            // String textureKey,
            @SerializedName("variant")
            String variant
    ) {}

    public record PlayerProfileCape(
            @SerializedName("id")
            String id,
            @SerializedName("state")
            String state,
            @SerializedName("url")
            String url,
            @SerializedName("alias")
            String alias
    ) {}
}
