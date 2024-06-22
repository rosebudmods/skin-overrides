package net.orifu.skin_overrides.override;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.mojang.authlib.GameProfile;

import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.texture.AbstractLibraryTexture;
import net.orifu.skin_overrides.Mod;

public abstract class AbstractLibraryOverride<E extends LibraryEntry, T extends AbstractLibraryTexture>
        extends AbstractOverride<E, T> {
    @Override
    protected String getFileName(GameProfile profile, E entry) {
        return profile.getName() + ".txt";
    }

    public void addOverride(GameProfile profile, E entry) {
        this.removeOverride(profile);
        var outputPath = Paths.get(this.rootFolder(), this.getFileName(profile, entry));

        try {
            var writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8);
            writer.write(new Identifier("skin_overrides", entry.getId()).toString());
            writer.close();
        } catch (IOException e) {
            Mod.LOGGER.error("failed to save library entry with id {} to file", entry.getId(), e);
        }
    }
}
