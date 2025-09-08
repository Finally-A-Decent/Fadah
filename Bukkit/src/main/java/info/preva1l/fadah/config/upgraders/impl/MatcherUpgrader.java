package info.preva1l.fadah.config.upgraders.impl;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.upgraders.ConfigUpgrader;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created on 16/06/2025
 *
 * @author Preva1l
 */
@AllArgsConstructor
public class MatcherUpgrader implements ConfigUpgrader {
    private final Logger logger;

    @Override
    public void migrate() {
        migrate(new File(Fadah.instance.getDataFolder(), "categories.yml"));
        migrate(new File(Fadah.instance.getDataFolder(), "config.yml"));
    }

    private void migrate(File file) {
        if (!file.exists()) return;

        try {
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String originalContent = content;

            content = content.replaceAll("([^.]+)\\.includes\\(([^)]+)\\)", "$1 contains $2");
            content = content.replaceAll("([^.]+)\\.startsWith\\(([^)]+)\\)", "$1 startsWith $2");
            content = content.replaceAll("([^.]+)\\.endsWith\\(([^)]+)\\)", "$1 endsWith $2");

            if (!content.equals(originalContent)) {
                Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to upgrade " + file.getName() + " matcher syntax", e);
        }
    }
}