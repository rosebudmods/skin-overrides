package net.orifu.skin_overrides;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.texture.LocalPlayerTexture;
import net.orifu.skin_overrides.texture.LocalSkinTexture;

public class Library {
    public static final File SKIN_FILE = new File(SkinOverrides.SKIN_OVERRIDES, "library.json");
    public static final File CAPE_FILE = new File(SkinOverrides.CAPE_OVERRIDES, "library.json");
    public static final File SKIN_LIBRARY_DIR = new File(SkinOverrides.SKIN_OVERRIDES, "library");
    public static final File CAPE_LIBRARY_DIR = new File(SkinOverrides.CAPE_OVERRIDES, "library");
    public static final Gson GSON = new Gson();

    protected static ArrayList<SkinEntry> skinEntries;
    protected static ArrayList<CapeEntry> capeEntries;

    protected static void ensureLoaded() {
        if (skinEntries == null || capeEntries == null) {
            reload();
        }
    }

    public static List<SkinEntry> skinEntries() {
        ensureLoaded();
        return skinEntries;
    }

    public static List<CapeEntry> capeEntries() {
        ensureLoaded();
        return capeEntries;
    }

    @Nullable
    public static SkinEntry getSkin(String id) {
        ensureLoaded();

        for (var entry : skinEntries) {
            if (entry.getId().equals(id)) {
                return entry;
            }
        }

        return null;
    }

    @Nullable
    public static CapeEntry getCape(String id) {
        ensureLoaded();

        for (var entry : capeEntries) {
            if (entry.getId().equals(id)) {
                return entry;
            }
        }

        return null;
    }

    public static void addCape(CapeEntry entry) {
        capeEntries.add(0, entry);
        save();
    }

    public static void reload() {
        skinEntries = new ArrayList<>();
        capeEntries = new ArrayList<>();

        SKIN_LIBRARY_DIR.mkdir();
        CAPE_LIBRARY_DIR.mkdir();

        reloadInternal(SKIN_FILE, j -> SkinEntry.skinFromJson(j).ifPresent(skinEntries::add), () -> {
            resetSkins();
            save();
        });

        reloadInternal(CAPE_FILE, j -> CapeEntry.capeFromJson(j).ifPresent(capeEntries::add), () -> {
        });
    }

    private static void reloadInternal(File file, Consumer<JsonElement> consumer, Runnable fail) {
        try {
            var reader = Files.newReader(file, StandardCharsets.UTF_8);
            JsonArray arr = GSON.fromJson(reader, JsonArray.class);

            arr.forEach(consumer);
            reader.close();
            return;
        } catch (FileNotFoundException e) {
            fail.run();
        } catch (IOException | JsonParseException | NullPointerException e) {
            SkinOverrides.LOGGER.error("failed to load library file {}", file, e);
        }
    }

    public static void resetSkins() {
        // add default player skins
        for (var playerSkin : DefaultSkinHelper.DEFAULT_SKINS) {
            String name = playerSkin.texture().getPath();
            name = name.substring(name.lastIndexOf('/') + 1);
            name = name.substring(0, 1).toUpperCase() + name.substring(1).replace(".png", "");
            name += playerSkin.model().equals(PlayerSkin.Model.WIDE) ? " (wide)" : " (slim)";
            skinEntries.add(new SkinEntry(name, playerSkin.texture(), playerSkin.model()));
        }
    }

    public static void save() {
        ensureLoaded();

        saveInternal(SKIN_FILE, skinEntries);
        saveInternal(CAPE_FILE, capeEntries);
    }

    private static <E extends LibraryEntry> void saveInternal(File file, List<E> entries) {
        JsonArray arr = new JsonArray();
        entries.forEach(e -> arr.add(e.toJson()));

        try {
            var writer = Files.newWriter(file, StandardCharsets.UTF_8);
            writer.write(GSON.toJson(arr));
            writer.close();
        } catch (IOException e) {
            SkinOverrides.LOGGER.error("failed to save library file {e}", file, e);
        }
    }

    public static abstract class LibraryEntry {
        protected String name;
        protected String id;

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public void rename(String newName) {
            this.name = newName;
            save();
        }

        public abstract Identifier getTexture();

        protected abstract JsonElement toJson();
    }

    public static class SkinEntry extends CapeEntry {
        protected final PlayerSkin.Model model;

        public SkinEntry(String name, String id, File file, PlayerSkin.Model model) {
            super(name, id, file);
            this.model = model;
        }

        public SkinEntry(String name, File file, PlayerSkin.Model model) {
            this(name, UUID.randomUUID().toString(), file, model);
        }

        public SkinEntry(String name, String id, Identifier texture, PlayerSkin.Model model) {
            super(name, id, texture);
            this.model = model;
        }

        public SkinEntry(String name, Identifier texture, PlayerSkin.Model model) {
            this(name, UUID.randomUUID().toString(), texture, model);
        }

        @Override
        public Identifier getTexture() {
            if (this.isFile) {
                Identifier id = new Identifier("skin_overrides", UUID.randomUUID().toString());
                MinecraftClient.getInstance().getTextureManager().registerTexture(id,
                        new LocalSkinTexture(this.file, null));
                return id;
            } else {
                return this.texture;
            }
        }

        public PlayerSkin.Model getModel() {
            return this.model;
        }

        protected static Optional<SkinEntry> skinFromJson(JsonElement el) {
            var maybeCape = CapeEntry.capeFromJson(el);
            if (maybeCape.isEmpty())
                return Optional.empty();
            var cape = maybeCape.get();

            JsonObject obj = el.getAsJsonObject();
            JsonElement model = obj.get("model");

            if (model == null)
                return Optional.empty();
            String modelStr = model.getAsString();
            if (modelStr == null)
                return Optional.empty();
            PlayerSkin.Model playerModel;
            if (modelStr.equals("wide")) {
                playerModel = PlayerSkin.Model.WIDE;
            } else if (modelStr.equals("slim")) {
                playerModel = PlayerSkin.Model.SLIM;
            } else {
                return Optional.empty();
            }

            if (cape.isFile) {
                return Optional.of(new SkinEntry(
                        cape.name, cape.id,
                        new File(SKIN_LIBRARY_DIR, obj.get("file").getAsString()),
                        playerModel));
            } else {
                return Optional.of(new SkinEntry(cape.name, cape.id, cape.texture, playerModel));
            }
        }

        @Override
        protected JsonElement toJson() {
            JsonObject obj = super.toJson().getAsJsonObject();
            obj.addProperty("model", this.model.equals(PlayerSkin.Model.WIDE) ? "wide" : "slim");
            return obj;
        }
    }

    public static class CapeEntry extends LibraryEntry {
        protected final boolean isFile;
        @Nullable
        protected final File file;
        @Nullable
        protected final Identifier texture;

        public CapeEntry(String name, String id, File file) {
            this.isFile = true;
            this.file = file;
            this.texture = null;

            this.name = name;
            this.id = id;
        }

        public CapeEntry(String name, File file) {
            this(name, UUID.randomUUID().toString(), file);
        }

        public CapeEntry(String name, String id) {
            this(name, id, new File(CAPE_LIBRARY_DIR, id + ".png"));
        }

        public CapeEntry(String name) {
            this(name, UUID.randomUUID().toString());
        }

        public CapeEntry(String name, String id, Identifier texture) {
            this.isFile = false;
            this.file = null;
            this.texture = texture;

            this.name = name;
            this.id = id;
        }

        public CapeEntry(String name, Identifier texture) {
            this(name, UUID.randomUUID().toString(), texture);
        }

        @Override
        public Identifier getTexture() {
            if (this.isFile) {
                Identifier id = new Identifier("skin_overrides", UUID.randomUUID().toString());
                MinecraftClient.getInstance().getTextureManager().registerTexture(id,
                        new LocalPlayerTexture(this.file));
                return id;
            } else {
                return this.texture;
            }
        }

        protected static Optional<CapeEntry> capeFromJson(JsonElement el) {
            if (!el.isJsonObject())
                return Optional.empty();

            JsonObject obj = el.getAsJsonObject();
            JsonElement name = obj.get("name");
            JsonElement id = obj.get("id");
            JsonElement file = obj.get("file");
            JsonElement texture = obj.get("texture");

            if (name == null || id == null)
                return Optional.empty();
            String nameStr = name.getAsString();
            String idStr = id.getAsString();
            if (nameStr == null || idStr == null)
                return Optional.empty();

            if (file != null) {
                String fileStr = file.getAsString();
                if (fileStr == null)
                    return Optional.empty();

                return Optional.of(new CapeEntry(nameStr, idStr, new File(CAPE_LIBRARY_DIR, fileStr)));
            } else if (texture != null) {
                String textureStr = texture.getAsString();
                if (textureStr == null)
                    return Optional.empty();
                Identifier textureId = Identifier.tryParse(textureStr);
                if (textureId == null)
                    return Optional.empty();

                return Optional.of(new CapeEntry(nameStr, idStr, textureId));
            } else {
                return Optional.empty();
            }
        }

        @Override
        protected JsonElement toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", this.name);
            obj.addProperty("id", this.id);
            if (this.isFile) {
                obj.addProperty("file", this.file.getName());
            } else {
                obj.addProperty("texture", this.texture.toString());
            }
            return obj;
        }
    }
}
