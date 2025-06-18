package net.orifu.skin_overrides.library;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.networking.MineSkin;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.util.TextureHelper;
import net.orifu.skin_overrides.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static net.orifu.skin_overrides.Mod.SKIN_OVERRIDES_PATH;

public class SkinLibrary extends AbstractLibrary {
    public static final SkinLibrary INSTANCE = new SkinLibrary();

    private SkinLibrary() {
        super(SKIN_OVERRIDES_PATH);
    }

    @Override
    protected Optional<LibraryEntry> tryLoadFromJson(JsonObject object, String name, String id, @Nullable File file, @Nullable ResourceLocation textureLoc) {
        var model = Util.readString(object, "model");
        if (model.isPresent()) {
            Skin.Model skinModel = Skin.Model.parse(model.get());

            Skin.Signature signature = null;
            if (object.has("signed") && object.get("signed").isJsonObject()) {
                JsonObject signed = (JsonObject) object.get("signed");
                var maybeSkinValue = Util.readString(signed, "value");
                var maybeSkinSignature = Util.readString(signed, "signature");

                if (maybeSkinValue.isPresent() && maybeSkinSignature.isPresent()) {
                    signature = new Skin.Signature(maybeSkinValue.get(), maybeSkinSignature.get());
                }
            }

            return Optional.of(new SkinEntry(name, id, skinModel, file, textureLoc, signature));
        }

        return Optional.empty();
    }

    @Override
    protected void addDefaultEntries() {
        // add default player skins
        for (var playerSkin : ProfileHelper.getDefaultSkins()) {
            String name = playerSkin.texture().getPath();
            name = name.substring(name.lastIndexOf('/') + 1);
            name = name.substring(0, 1).toUpperCase() + name.substring(1).replace(".png", "");
            name += playerSkin.model().equals(Skin.Model.WIDE) ? " (wide)" : " (slim)";
            this.entries.add(new SkinEntry(name, Util.seededId(name), playerSkin.model(), playerSkin.texture()));
        }
    }

    public Optional<SkinEntry> create(String name, Path path, Skin.Model model) {
        return this.createInternal(name, model, null, path, null);
    }

    public Optional<SkinEntry> create(String name, ResourceLocation texture, Skin.Model model) {
        return this.createInternal(name, model, texture, null, null);
    }

    public Optional<SkinEntry> createSigned(
            String name, ResourceLocation texture, Skin.Model model,
            GameProfile profile) {
        var property = profile.getProperties().get("textures").stream().findAny();
        return property.flatMap(Skin.Signature::fromProperty).flatMap(sig ->
                this.createInternal(name, model, texture, null, sig));
    }

    private Optional<SkinEntry> createInternal(
            String name, Skin.Model model,
            ResourceLocation texture, Path path,
            Skin.Signature signature) {
        try {
            String id = Util.randomId();
            File file = new File(this.libraryFolder, id + ".png");

            if (path != null) {
                Files.copy(path, file.toPath());
            } else {
                Util.saveTexture(texture, 64, 64, file.toPath());
            }

            var entry = new SkinEntry(name, id, model, file, texture, signature);

            this.add(entry);
            return Optional.of(entry);
        } catch (IOException e) {
            Mod.LOGGER.error("failed to copy {}", path, e);
            return Optional.empty();
        }
    }

    public static class SkinEntry extends AbstractLibraryEntry {
        protected final Skin.Model model;

        @Nullable
        public final Skin.Signature signature;

        private final Supplier<ResourceLocation> texture = Suppliers.memoize(() ->
                TextureHelper.skin().location("skin/library/" + this.fileHash).path(this.file).register().orElseThrow());

        @Nullable
        private CompletableFuture<Optional<SkinEntry>> signedCache = null;

        protected SkinEntry(
                String name, String id, Skin.Model model,
                @Nullable File file, @Nullable ResourceLocation textureLoc,
                @Nullable Skin.Signature signature) {
            super(name, id, file, textureLoc);

            this.model = model;
            this.signature = signature;
        }

        protected SkinEntry(
                String name, String id, Skin.Model model,
                @NotNull File file,
                Skin.Signature signature) {
            this(name, id, model, file, null, signature);
        }

        protected SkinEntry(String name, String id, Skin.Model model, @NotNull ResourceLocation textureLoc) {
            this(name, id, model, null, textureLoc, null);
        }

        @Override
        protected ResourceLocation getTextureFromFile() {
            return this.texture.get();
        }

        public Skin toSkin() {
            return new Skin(this.getTexture(), null, null, this.getModel());
        }

        public Skin.Model getModel() {
            return this.model;
        }

        @Override
        public JsonElement toJson() {
            JsonObject obj = super.toJson().getAsJsonObject();
            obj.addProperty("model", this.model.id());

            if (this.signature != null) {
                JsonObject signedSkin = new JsonObject();
                signedSkin.addProperty("value", this.signature.value());
                signedSkin.addProperty("signature", this.signature.signature());
                obj.add("signed", signedSkin);
            }

            return obj;
        }

        public CompletableFuture<Optional<SkinEntry>> signed() {
            if (this.signature != null) {
                return CompletableFuture.completedFuture(Optional.of(this));
            } else if (this.signedCache != null) {
                return this.signedCache;
            } else {
                var signature = MineSkin.sign(this.getTexture(), this.model);
                this.signedCache = signature.thenApply(maybeSig -> maybeSig.map(sig ->
                        new SkinEntry(this.name, this.id, this.model, this.file, this.textureLoc, sig)));
                return this.signedCache;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SkinEntry skinEntry)) return false;
            if (!super.equals(o)) return false;

            // intentionally ignore the signature when comparing, as the skin doesn't change
            // just because it's signed now.
            return model == skinEntry.model;
        }
    }
}
