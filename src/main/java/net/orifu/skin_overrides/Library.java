package net.orifu.skin_overrides;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;

import net.minecraft.util.Identifier;

public interface Library {
    void save();

    void reload();

    List<LibraryEntry> entries();

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

    default void remove(int index) {
        this.entries().remove(index).remove();
        this.save();
    }

    default void move(int i, int j) {
        this.entries().add(j, this.entries().remove(i));
        this.save();
    }

    default void rename(LibraryEntry entry, String newName) {
        entry.name = newName;
        this.save();
    }

    abstract class LibraryEntry {
        protected String name;
        protected String id;

        public LibraryEntry(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public abstract Identifier getTexture();

        public abstract JsonElement toJson();

        public abstract void remove();
    }
}
