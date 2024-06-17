package net.orifu.skin_overrides.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

import com.mojang.authlib.GameProfile;

public class OverrideFiles {
    protected static boolean matches(String id, GameProfile profile) {
        // username
        if (id.equalsIgnoreCase(profile.getName())) {
            return true;
        }
        // uuid with hyphens
        String uuid = profile.getId().toString();
        if (id.equalsIgnoreCase(uuid)) {
            return true;
        }
        // uuid without hyphens
        if (id.equalsIgnoreCase(uuid.replace("-", ""))) {
            return true;
        }

        return false;
    }

    public static <T> Optional<Validated<T>> findProfileFile(String path, GameProfile profile,
            Validator<T> validator) {
        for (File file : new File(path).listFiles()) {
            String fileName = file.getName();
            String name = FilenameUtils.getBaseName(fileName);
            String ext = FilenameUtils.getExtension(fileName);

            // ensure file name is valid
            Optional<Validated<T>> maybeValidated = validator.validate(name, ext);
            if (maybeValidated.isEmpty()) {
                continue;
            }
            var validated = maybeValidated.get().withFile(file);

            if (matches(validated.id, profile)) {
                return Optional.of(validated);
            }
        }

        return Optional.empty();
    }

    public static <T> List<Validated<T>> listValidated(String path, Validator<T> validator) {
        File dir = new File(path);
        dir.mkdir();

        ArrayList<Validated<T>> files = new ArrayList<>();
        for (File file : dir.listFiles()) {
            String name = FilenameUtils.getBaseName(file.getName());
            String ext = FilenameUtils.getExtension(file.getName());

            Optional<Validated<T>> validated = validator.validate(name, ext);
            if (validated.isPresent()) {
                files.add(validated.get().withFile(file));
            }
        }

        return files;
    }

    public static <T> List<GameProfile> listProfiles(String path, Validator<T> validator) {
        return listValidated(path, validator).stream().map(v -> ProfileHelper.idToBasicProfile(v.id())).toList();
    }

    public static <T> void deleteProfileFiles(String path, Validator<T> validator, GameProfile profile) {
        for (var v : listValidated(path, validator)) {
            if (matches(v.id, profile)) {
                v.file().delete();
            }
        }
    }

    public interface Validator<T> {
        Optional<Validated<T>> validate(String name, String ext);
    }

    public static class Validated<T> {
        private String id;
        private File file;
        private T data;

        protected Validated(String id, File file, T data) {
            this.id = id;
            this.file = file;
            this.data = data;
        }

        protected Validated<T> withFile(File file) {
            if (this.file == null) {
                this.file = file;
            }
            return this;
        }

        public static <T> Validated<T> of(String id, T data) {
            return new Validated<>(id, null, data);
        }

        public static <T> Validated<T> of(String id) {
            return new Validated<>(id, null, null);
        }

        public String id() {
            return this.id;
        }

        public File file() {
            return this.file;
        }

        public T data() {
            return this.data;
        }
    }
}
