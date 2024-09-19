package net.orifu.skin_overrides.library;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.Skin;
import net.orifu.skin_overrides.texture.LocalSkinTexture;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import static net.orifu.skin_overrides.Mod.SKIN_OVERRIDES_PATH;

public class SkinLibrary extends AbstractLibrary {
    public static final SkinLibrary INSTANCE = new SkinLibrary();

    private SkinLibrary() {
        super(SKIN_OVERRIDES_PATH);
    }

    @Override
    protected boolean tryLoadFromJson(JsonObject object, String name, String id, @Nullable File file, @Nullable Identifier textureId) {
        var model = Util.readString(object, "model");
        if (model.isPresent()) {
            Skin.Model skinModel = Skin.Model.parse(model.get());

            String skinValue = null;
            String skinSignature = null;
            if (object.has("signed") && object.get("signed").isJsonObject()) {
                JsonObject signed = (JsonObject) object.get("signed");
                var maybeSkinValue = Util.readString(signed, "value");
                var maybeSkinSignature = Util.readString(signed, "signature");

                if (maybeSkinValue.isPresent() && maybeSkinSignature.isPresent()) {
                    skinValue = maybeSkinValue.get();
                    skinSignature = maybeSkinSignature.get();
                }
            }

            this.entries.add(new SkinEntry(name, id, skinModel, file, textureId, skinValue, skinSignature));
            return true;
        }

        return false;
    }

    @Override
    protected void addDefaultEntries() {
        // add default player skins
        for (var playerSkin : ProfileHelper.getDefaultSkins()) {
            String name = playerSkin.texture().getPath();
            name = name.substring(name.lastIndexOf('/') + 1);
            name = name.substring(0, 1).toUpperCase() + name.substring(1).replace(".png", "");
            name += playerSkin.model().equals(Skin.Model.WIDE) ? " (wide)" : " (slim)";
            this.entries.add(new SkinEntry(name, Util.randomId(), playerSkin.model(), playerSkin.texture()));
        }
    }

    public Optional<SkinEntry> create(String name, Path path, Skin.Model model) {
        return this.createInternal(name, model, null, path, null, null);
    }

    public Optional<SkinEntry> create(String name, Identifier texture, Skin.Model model) {
        return this.createInternal(name, model, texture, null, null, null);
    }

    public Optional<SkinEntry> createSigned(
            String name, Identifier texture, Skin.Model model,
            GameProfile profile) {
        var property = profile.getProperties().get("textures").stream().findAny().get();
        return this.createInternal(name, model, texture, null, property.value(), property.signature());
    }

    private Optional<SkinEntry> createInternal(
            String name, Skin.Model model,
            Identifier texture, Path path,
            String skinValue, String skinSignature) {
        try {
            String id = Util.randomId();
            File file = new File(this.libraryFolder, id + ".png");
            var entry = new SkinEntry(name, id, model, file, skinValue, skinSignature);

            if (path != null) {
                Files.copy(path, file.toPath());
            } else {
                Util.saveTexture(texture, 64, 64, file.toPath());
            }

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
        public final String skinValue;
        @Nullable
        public final String skinSignature;

        protected SkinEntry(
                String name, String id, Skin.Model model,
                @Nullable File file, @Nullable Identifier textureId,
                @Nullable String skinValue, @Nullable String skinSignature) {
            super(name, id, file, textureId);

            this.model = model;

            this.skinValue = skinValue;
            this.skinSignature = skinSignature;
        }

        protected SkinEntry(
                String name, String id, Skin.Model model,
                @NotNull File file,
                String skinValue, String skinSignature) {
            this(name, id, model, file, null, skinValue, skinSignature);
        }

        protected SkinEntry(String name, String id, Skin.Model model, @NotNull Identifier textureId) {
            this(name, id, model, null, textureId, null, null);
        }

        @Override
        protected Identifier getTextureFromFile() {
            return Util.texture("skin/library/" + this.id, new LocalSkinTexture(this.file, this.model));
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

            if (this.skinValue != null && this.skinSignature != null) {
                JsonObject signedSkin = new JsonObject();
                signedSkin.addProperty("value", this.skinValue);
                signedSkin.addProperty("signature", this.skinSignature);
                obj.add("signed", signedSkin);
            }

            return obj;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SkinEntry skinEntry)) return false;
            if (!super.equals(o)) return false;
            return model == skinEntry.model && Objects.equals(skinValue, skinEntry.skinValue) && Objects.equals(skinSignature, skinEntry.skinSignature);
        }
    }
}
