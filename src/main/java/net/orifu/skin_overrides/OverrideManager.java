package net.orifu.skin_overrides;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.util.Util;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class OverrideManager {
    public final boolean skin;
    private final File dir;
    private final Library library;
    private final List<Overrider> overriders = new ArrayList<>();
    private final Map<String, Overridden> overrides = new HashMap<>();

    public OverrideManager(boolean skin, String directory, Library library, Overrider... overriders) {
        this.skin = skin;
        this.dir = new File(directory);
        this.library = library;

        this.overriders.add(library.overrider());
        this.overriders.addAll(List.of(overriders));
    }

    public void update() {
        Util.runOnRenderThread(this::updateOnRenderThread);
    }

    protected void updateOnRenderThread() {
        Map<String, Overridden> newOverrides = new HashMap<>();
        this.findOverrides(ov -> {
            String key = ov.override.playerIdent().toLowerCase(Locale.ROOT);
            var maybeExisting = this.overrides.get(key);

            newOverrides.put(key, ov.equals(maybeExisting) ? maybeExisting : ov);
        });

        synchronized (this.overrides) {
            Optional<Override> oldUserOverride = this.get(ProfileHelper.user());

            this.overrides.clear();
            this.overrides.putAll(newOverrides);

            Optional<Override> newUserOverride = this.get(ProfileHelper.user());

            if (!oldUserOverride.equals(newUserOverride)) {
                CompletableFuture.runAsync(() ->
                        Mod.onUserOverrideUpdate(oldUserOverride.orElse(null), newUserOverride.orElse(null)));
            }
        }
    }

    public void removeOverride(GameProfile profile) {
        this.getData(profile).ifPresent(data -> {
            data.file.delete();
            this.update();
        });
    }

    private void findOverrides(Consumer<Overridden> handler) {
        this.dir.mkdir();

        for (File file : this.dir.listFiles()) {
            String name = FilenameUtils.getBaseName(file.getName());
            String ext = FilenameUtils.getExtension(file.getName());

            for (Overrider overrider : this.overriders) {
                Optional<Override> override = overrider.get(file, name, ext);
                if (override.isPresent()) {
                    handler.accept(new Overridden(file, override.get()));
                    break;
                }
            }
        }
    }

    public Library library() {
        return this.library;
    }

    public synchronized Optional<Override> get(GameProfile profile) {
        return this.getData(profile).map(Overridden::override);
    }

    public synchronized Optional<Override> get(String ident) {
        return this.getData(ident).map(Overridden::override);
    }

    private synchronized Optional<Overridden> getData(GameProfile profile) {
        return Optional.ofNullable(profile.getName()).flatMap(this::getData)
                .or(() -> Optional.ofNullable(profile.getId()).flatMap(id -> this.getData(id.toString())));
    }

    private synchronized Optional<Overridden> getData(String ident) {
        return Optional.ofNullable(this.overrides.get(ident.toLowerCase(Locale.ROOT)));
    }

    public synchronized boolean has(GameProfile profile) {
        return this.get(profile).isPresent();
    }

    public void addOverride(GameProfile profile, Library.LibraryEntry entry) {
        this.removeOverride(profile);
        this.library.overrider().addOverride(profile, entry);
    }

    public void copyOverride(GameProfile profile, Path path, @Nullable Skin.Model model) {
        this.removeOverride(profile);

        String ext = FilenameUtils.getExtension(path.toString());
        for (var overrider : this.overriders) {
            String fileName = overrider.fileName(profile, model);
            if (FilenameUtils.getExtension(fileName).equals(ext)) {
                try {
                    Path outputPath = new File(this.dir, fileName).toPath();
                    Files.copy(path, outputPath);
                } catch (IOException e) {
                    Mod.LOGGER.error("failed to copy {}", path, e);
                }

                break;
            }
        }
    }

    public List<CompletableFuture<GameProfile>> profilesWithOverride() {
        synchronized (this.overrides) {
            return this.overrides.keySet().stream().map(ProfileHelper::idToBasicProfile).toList();
        }
    }

    public List<GameProfile> profilesWithOverrideSync() {
        synchronized (this.overrides) {
            return this.overrides.keySet().stream().map(ProfileHelper::idToBasicProfileSync).toList();
        }
    }

    public interface Overrider {
        String fileName(GameProfile profile, Skin.Model model);

        Optional<Override> get(File file, String name, String ext);
    }

    public record Overridden(File file, Override override) {}

    public interface Override extends Skin.Signature.Provider {
        String playerIdent();

        @Nullable
        ResourceLocation texture();

        Component info();

        @Nullable
        default Skin.Model model() {
            return null;
        }

        @java.lang.Override
        default CompletableFuture<Optional<Skin.Signature>> signature() {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }
}
