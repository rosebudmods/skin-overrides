package net.orifu.skin_overrides;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.orifu.skin_overrides.override.LibraryOverrider;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public interface Library {
    List<LibraryEntry> entries();

    void save();

    void reload();

    LibraryOverrider overrider();

    default Optional<LibraryEntry> get(String id) {
        for (var entry : this.entries()) {
            if (entry.getId().equals(id)) {
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }

    default void add(LibraryEntry entry) {
        this.entries().add(0, entry);
        this.save();
    }

    default boolean replace(LibraryEntry from, LibraryEntry to) {
        for (int i = 0; i < this.entries().size(); i++) {
            var entry = this.entries().get(i);
            if (entry.getId().equals(from.getId())) {
                // remove files if IDs are different
                if (!from.getId().equals(to.getId())) {
                    from.removeFiles();
                }

                this.entries().remove(i);
                this.entries().add(i, to);
                this.save();
                return true;
            }
        }

        return false;
    }

    default void remove(int index) {
        this.entries().remove(index).removeFiles();
        this.save();
    }

    default void move(int i, int j) {
        this.entries().add(j, this.entries().remove(i));
        this.save();
    }

    default void rename(LibraryEntry entry, String newName) {
        this.get(entry.id).ifPresent(internalEntry -> {
            entry.name = internalEntry.name = newName;
            this.save();
        });

        if (!entry.name.equals(newName)) {
            Mod.LOGGER.error("you encountered a rare bug! please report this on our issue tracker.");
        }
    }

    abstract class LibraryEntry {
        protected String name;
        protected String id;

        public LibraryEntry(String name, String id) {
            this.name = name;
            this.id = id.toLowerCase(Locale.ROOT);
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public abstract ResourceLocation getTexture();

        public abstract JsonElement toJson();

        public abstract void removeFiles();
    }
}
